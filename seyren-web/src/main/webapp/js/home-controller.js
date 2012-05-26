/*global console*/

function HomeController() {
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    
    this.pollAlertsInSeconds = 5;
    this.secondsToUpdateAlerts = this.pollAlertsInSeconds;
    this.$defer(this.countdownToRefresh, 1000);
    
}

HomeController.prototype = {
    
    loadErrorChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks?states=ERROR&enabled=true', this.loadErrorChecksSuccess, this.loadErrorChecksFailure);
    },
        
    loadErrorChecksSuccess : function (code, response) {
        this.errorChecks = response;
    },
        
    loadErrorChecksFailure : function (code, response) {
        console.log('Loading error checks failed');
    },
    
    loadWarnChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks?states=WARN&enabled=true', this.loadWarnChecksSuccess, this.loadWarnChecksFailure);
    },
        
    loadWarnChecksSuccess : function (code, response) {
        this.warnChecks = response;
    },
        
    loadWarnChecksFailure : function (code, response) {
        console.log('Loading warn checks failed');
    },
    
    selectCheck : function (id) {
        this.$location.updateHash('/checks/' + id);
    },
    
    countdownToRefresh : function() {
        this.secondsToUpdateAlerts--;
        if (this.secondsToUpdateAlerts <= 0) {
            this.secondsToUpdateAlerts = this.pollAlertsInSeconds;
            this.loadErrorChecks();
            this.loadWarnChecks();
        } 
        this.$defer(this.countdownToRefresh, 1000);
    },
    
    saveCheck : function (check) {
        this.$xhr('PUT', this.seyrenBaseUrl + '/api/checks/' + check.id, check, this.saveCheckSuccess, this.saveCheckFailure);
    },
    
    saveCheckSuccess : function (code, response) {
        this.loadErrorChecks();
        this.loadWarnChecks();
    },
    
    saveCheckFailure : function (code, response) {
        console.log('Saving check failed');
    },
    
    swapEnabled : function (check) {
        check.enabled = !check.enabled;
        this.saveCheck(check);
    }

};
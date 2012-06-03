/*global console*/

function HomeController() {
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    
    this.pollAlertsInSeconds = 5;
    this.secondsToUpdateAlerts = this.pollAlertsInSeconds;
    this.$defer(this.countdownToRefresh, 1000);
    
}

HomeController.prototype = {
    
    loadUnhealthyChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks?state=ERROR&state=WARN&state=EXCEPTION&state=UNKNOWN&enabled=true', this.loadUnhealthyChecksSuccess, this.loadUnhealthyChecksFailure);
    },
        
    loadUnhealthyChecksSuccess : function (code, response) {
        this.unhealthyChecks = response;
    },
        
    loadUnhealthyChecksFailure : function (code, response) {
        console.log('Loading unhealthy checks failed');
    },
    
    loadAlertStream : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/alerts?items=10', this.loadAlertStreamSuccess, this.loadAlertStreamFailure);
    },
        
    loadAlertStreamSuccess : function (code, response) {
        this.alertStream = response.values;
    },
        
    loadAlertStreamFailure : function (code, response) {
        console.log('Loading alert stream failed');
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
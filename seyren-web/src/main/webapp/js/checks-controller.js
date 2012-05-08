/*global console*/

function ChecksController() {
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    this.location = {};
    
    this.pollChecksInSeconds = 5;
    this.secondsToUpdateChecks = this.pollChecksInSeconds;
    this.$defer(this.countdownToRefresh, 1000);
    
}

ChecksController.prototype = {
    
    loadChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks', this.loadChecksSuccess, this.loadChecksFailure);
    },
    
    loadChecksSuccess : function (code, response) {
        this.checks = response;
    },
    
    loadChecksFailure : function (code, response) {
        console.log('Loading checks failed');
    },
    
    selectCheck : function (id) {
        this.$location.updateHash('/checks/' + id);
    },
    
    createCheck : function () {
        var check = {
                name : this.newcheck.name,
                target : this.newcheck.target,
                warn : this.newcheck.warn,
                error : this.newcheck.error,
                enabled : this.newcheck.enabled
                };
        
        this.$xhr('POST', this.seyrenBaseUrl + '/api/checks', check, this.createCheckSuccess, this.createCheckFailure);
    },
    
    createCheckSuccess : function (code, response) {
        this.loadChecks();
    },
    
    createCheckFailure : function (code, response) {
        console.log('Create check failed');
    },
    
    saveCheck : function (check) {
        this.$xhr('PUT', this.seyrenBaseUrl + '/api/checks/' + check.id, check, this.saveCheckSuccess, this.saveCheckFailure);
    },
    
    saveCheckSuccess : function (code, response) {
        this.loadChecks();
    },
    
    saveCheckFailure : function (code, response) {
        console.log('Saving check failed');
    },
    
    swapEnabled : function (check) {
        check.enabled = !check.enabled;
        this.saveCheck(check);
    },
    
    countdownToRefresh : function() {
        this.secondsToUpdateChecks--;
        if (this.secondsToUpdateChecks <= 0) {
            this.secondsToUpdateChecks = this.pollChecksInSeconds;
            this.loadChecks();
        } 
        this.$defer(this.countdownToRefresh, 1000);
    }
    
};

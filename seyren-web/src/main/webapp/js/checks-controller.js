/*global console,$,angular */

function ChecksController() {
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    this.location = {};
    
    this.pollChecksInSeconds = 30;
    this.secondsToUpdateChecks = this.pollChecksInSeconds;
    this.$defer(this.countdownToRefresh, 1000);
    
    $('#createCheckModal').on('shown', function () {
        $('#newcheck\\.name').focus();
    });
    
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
            description : this.newcheck.description,
            target : this.newcheck.target,
            warn : this.newcheck.warn,
            error : this.newcheck.error,
            enabled : this.newcheck.enabled
        };
        
        $("#createCheckButton").addClass("disabled");
        this.$xhr('POST', this.seyrenBaseUrl + '/api/checks', check, this.createCheckSuccess, this.createCheckFailure);
    },
    
    createCheckSuccess : function (code, response) {
        $("#createCheckModal").modal("hide");
        $("#createCheckButton").removeClass("disabled");
        this.newcheck = { enabled : true };
        this.loadChecks();
    },
    
    createCheckFailure : function (code, response) {
        $("#createCheckButton").removeClass("disabled");
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
    },
    
    getSmallGraphUrl : function() {
        if (this.config && this.newcheck.target) {
            var result = this.config.graphite.baseUrl + '/render/?';
            result += 'target=' + this.newcheck.target;
            result += '&target=alias(dashed(color(constantLine(' + this.newcheck.warn + '),"yellow")),"warn level")';
            result += '&target=alias(dashed(color(constantLine(' + this.newcheck.error + '),"red")),"error level")';
            result += '&width=290&height=70&hideLegend=true&from=-1day';
            return result;
        }
    }
    
};

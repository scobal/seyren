/*global console*/

function ChecksController() {
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.location = {};
}

ChecksController.prototype = {
    
    loadChecks : function () {
        this.$xhr('GET', this.graphiteSirenBaseUrl + '/api/checks', this.loadChecksSuccess, this.loadChecksFailure);
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
        this.$xhr('POST', this.graphiteSirenBaseUrl + '/api/checks', {  }, this.createCheckSuccess, this.createCheckFailure);
    },
    
    createCheckSuccess : function (code, response) {
        this.loadChecks();
    },
    
    createCheckFailure : function (code, response) {
        console.log('Create check failed');
    }
    
};

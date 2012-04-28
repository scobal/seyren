/*global console*/

function CheckController() {
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    this.id = this.$route.current.params.id;
}

CheckController.prototype = {
    
    loadCheck : function () {
        this.$xhr('GET', this.graphiteSirenBaseUrl + '/api/checks/' + this.id, this.loadCheckSuccess, this.loadCheckFailure);
    },
    
    loadCheckSuccess : function (code, response) {
        this.check = response;
    },
    
    loadCheckFailure : function (code, response) {
        console.log('Loading check failed');
    },
    
    deleteCheck : function () {
        this.$xhr('DELETE', this.graphiteSirenBaseUrl + '/api/checks/' + this.id, this.deleteCheckSuccess, this.deleteCheckFailure);
    },
    
    deleteCheckSuccess : function (code, response) {
        this.$location.updateHash('/checks');
    },
    
    deleteCheckFailure : function (code, response) {
        console.log('Deleting check failed');
    },
    
    saveCheck : function () {
        this.$xhr('PUT', this.graphiteSirenBaseUrl + '/api/checks/' + this.id, this.check, this.saveCheckSuccess, this.saveCheckFailure);
    },
    
    saveCheckSuccess : function (code, response) {
        this.loadCheck();
    },
    
    saveCheckFailure : function (code, response) {
        console.log('Saving check failed');
    },
    
    createSubscription : function () {
        var subscription = {
                target : this.newsubscription.target,
                type : this.newsubscription.type
                };
        
        this.$xhr('POST', this.graphiteSirenBaseUrl + '/api/checks/' + this.id + '/subscriptions', subscription, this.createSubscriptionSuccess, this.createSubscriptionFailure);
    },
    
    createSubscriptionSuccess : function (code, response) {
        this.loadCheck();
    },
    
    createSubscriptionFailure : function (code, response) {
        console.log('Creating subscription failed');
    }
    
};

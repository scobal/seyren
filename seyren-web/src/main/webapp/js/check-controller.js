/*global console*/

function CheckController() {
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    this.id = this.$route.current.params.id;
    
    this.alertStartIndex = 0;
    this.alertItemsPerPage = 10;
}

CheckController.prototype = {
    
    loadCheck : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks/' + this.id, this.loadCheckSuccess, this.loadCheckFailure);
    },
    
    loadCheckSuccess : function (code, response) {
        this.check = response;
    },
    
    loadCheckFailure : function (code, response) {
        console.log('Loading check failed');
    },
    
    loadAlerts : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks/' + this.id + '/alerts?start=' + this.alertStartIndex + '&items=' + this.alertItemsPerPage, this.loadAlertsSuccess, this.loadAlertsFailure);
    },
    
    loadAlertsSuccess : function (code, response) {
        this.alerts = response;
    },
    
    loadAlertsFailure : function (code, response) {
        console.log('Loading alerts failed');
    },
    
    deleteCheck : function () {
        this.$xhr('DELETE', this.seyrenBaseUrl + '/api/checks/' + this.id, this.deleteCheckSuccess, this.deleteCheckFailure);
    },
    
    deleteCheckSuccess : function (code, response) {
        this.$location.updateHash('/checks');
    },
    
    deleteCheckFailure : function (code, response) {
        console.log('Deleting check failed');
    },
    
    saveCheck : function () {
        this.$xhr('PUT', this.seyrenBaseUrl + '/api/checks/' + this.id, this.check, this.saveCheckSuccess, this.saveCheckFailure);
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
            type : this.newsubscription.type,
            su : this.newsubscription.su,
            mo : this.newsubscription.mo,
            tu : this.newsubscription.tu,
            we : this.newsubscription.we,
            th : this.newsubscription.th,
            fr : this.newsubscription.fr,
            sa : this.newsubscription.sa,
            fromTime : this.newsubscription.fromTime,
            toTime : this.newsubscription.toTime,
            enabled : this.newsubscription.enabled
        };
        
        this.$xhr('POST', this.seyrenBaseUrl + '/api/checks/' + this.id + '/subscriptions', subscription, this.createSubscriptionSuccess, this.createSubscriptionFailure);
    },
    
    createSubscriptionSuccess : function (code, response) {
        this.newsubscription.target = '';
        this.loadCheck();
    },
    
    createSubscriptionFailure : function (code, response) {
        console.log('Creating subscription failed');
    },
    
    swapEnabled : function (subscription) {
        subscription.enabled = !subscription.enabled;
        this.updateSubscription(subscription);
    },
    
    updateSubscription : function (subscription) {
        this.$xhr('PUT', this.seyrenBaseUrl + '/api/checks/' + this.id + '/subscriptions/' + subscription.id, subscription, this.updateSubscriptionSuccess, this.updateSubscriptionFailure);
    },
    
    updateSubscriptionSuccess : function (code, response) {
        this.loadCheck();
    },
    
    updateSubscriptionFailure : function (code, response) {
        console.log('Updating subscription failed');
    },
    
    deleteSubscription : function (subscriptionId) {
        this.$xhr('DELETE', this.seyrenBaseUrl + '/api/checks/' + this.id + '/subscriptions/' + subscriptionId, this.deleteSubscriptionSuccess, this.deleteSubscriptionFailure);
    },
    
    deleteSubscriptionSuccess : function (code, response) {
        this.loadCheck();
    },
    
    deleteSubscriptionFailure : function (code, response) {
        console.log('Deleting subscription failed');
    },
    
    loadOlderAlerts : function () {
        if (this.alerts.length !== this.alertItemsPerPage) {
            return;
        }
        this.alertStartIndex += this.alertItemsPerPage;
        this.loadAlerts();
    },
    
    loadNewerAlerts : function () {
        if (this.alertStartIndex === 0) {
            return;
        }
        this.alertStartIndex -= this.alertItemsPerPage;
        this.loadAlerts();
    }
    
};

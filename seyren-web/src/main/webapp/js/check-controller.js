/*global console,$ */

function CheckController() {
    var self = this;
    
    this.$xhr.defaults.headers.post['Content-Type'] = 'application/json';
    this.$xhr.defaults.headers.put['Content-Type'] = 'application/json';
    this.id = this.$route.current.params.id;
    
    this.alertStartIndex = 0;
    this.alertItemsPerPage = 10;
    
    this.pollCheckInSeconds = 30;
    this.secondsToUpdateCheck = this.pollCheckInSeconds;
    this.$defer(this.countdownToRefreshCheck, 1000);
    
    this.pollAlertsInSeconds = 5;
    this.secondsToUpdateAlerts = this.pollAlertsInSeconds;
    this.$defer(this.countdownToRefreshAlerts, 1000);
    
    this.graphs = [{
        description : "15 minutes",
        minutes : -15
    }, {
        description : "1 hour",
        minutes : -60
    }, {
        description : "1 day",
        minutes : -1440
    }, {
        description : "1 week",
        minutes : -10080
    }];
    
    $('#editCheckModal').on('hide', function () {
        self.loadCheck();
    });
}

CheckController.prototype = {
    
    loadCheck : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks/' + this.id, this.loadCheckSuccess, this.loadCheckFailure);
    },
    
    loadCheckSuccess : function (code, response) {
        this.check = response;
        this.check.lastLoadTime = new Date().getTime();
        this.copyCheckToEditCheck();
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
        $('#confirmDeleteCheckButton').addClass('disabled');
        this.$xhr('DELETE', this.seyrenBaseUrl + '/api/checks/' + this.id, this.deleteCheckSuccess, this.deleteCheckFailure);
    },
    
    deleteCheckSuccess : function (code, response) {
        $("#confirmCheckDeleteModal").modal("hide"); 
        $('#confirmDeleteCheckButton').removeClass('disabled');
        this.$location.updateHash('/checks');
    },
    
    deleteCheckFailure : function (code, response) {
        $('#confirmDeleteCheckButton').removeClass('disabled');
        console.log('Deleting check failed');
    },
    
    saveCheck : function() {
        this.$xhr('PUT', this.seyrenBaseUrl + '/api/checks/' + this.id, this.check, this.saveCheckSuccess, this.saveCheckFailure);
    },
    
    saveCheckSuccess : function (code, response) {
        $("#editCheckModal").modal("hide"); 
        this.loadCheck();
    },
    
    saveCheckFailure : function (code, response) {
        console.log('Saving check failed');
    },
    
    swapCheckEnabled : function() {
      this.check.enabled = !this.check.enabled;
      this.saveCheck();
    },
    
    closeEditCheckModal : function() {
        this.loadCheck();
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
        $('#createSubscriptionButton').addClass('disabled');
        this.$xhr('POST', this.seyrenBaseUrl + '/api/checks/' + this.id + '/subscriptions', subscription, this.createSubscriptionSuccess, this.createSubscriptionFailure);
    },
    
    createSubscriptionSuccess : function (code, response) {
        $("#addSubscriptionModal").modal("hide"); 
        $('#createSubscriptionButton').removeClass('disabled');
        this.newsubscription.target = '';
        this.loadCheck();
    },
    
    createSubscriptionFailure : function (code, response) {
        $('#createSubscriptionButton').removeClass('disabled');
        console.log('Creating subscription failed');
    },
    
    swapSubscriptionEnabled : function (subscription) {
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
        if (this.alerts.values.length !== this.alertItemsPerPage) {
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
    },
    
    countdownToRefreshCheck : function() {
        this.secondsToUpdateCheck--;
        if (this.secondsToUpdateCheck <= 0) {
            this.secondsToUpdateCheck = this.pollCheckInSeconds;
            this.loadCheck();
        } 
        this.$defer(this.countdownToRefreshCheck, 1000);
    },
    
    countdownToRefreshAlerts : function() {
        this.secondsToUpdateAlerts--;
        if (this.secondsToUpdateAlerts <= 0) {
            this.secondsToUpdateAlerts = this.pollAlertsInSeconds;
            this.loadAlerts();
        } 
        this.$defer(this.countdownToRefreshAlerts, 1000);
    },
    
    getSmallGraphUrl : function(minutes) {
        var baseUrl = this.getBaseGraphUrl(minutes, this.editcheck);
        if (baseUrl) {
            return baseUrl + '&width=365&height=70&hideAxes=true&hideLegend=true&uniq=' + this.check.lastLoadTime;
        }
    },
    
    getBigGraphUrl : function(minutes) {
        var baseUrl = this.getBaseGraphUrl(minutes);
        if (baseUrl) {
            return baseUrl + '&width=1200&height=350';
        }
    },
    
    getBaseGraphUrl : function(minutes, alternativeCheck) {
      if (this.config && this.check) {
            var result = this.config.graphiteUrl + '/render/?',
                currentCheck = alternativeCheck || this.check;
            result += 'target=' + currentCheck.target;
            result += '&from=' + minutes + 'Minutes';
            result += '&target=alias(dashed(color(constantLine(' + currentCheck.warn + '),"yellow")),"warn level")';
            result += '&target=alias(dashed(color(constantLine(' + currentCheck.error + '),"red")),"error level")';
            return result;
        }
    },
    
    copyCheckToEditCheck : function() {
        this.editcheck = {
            name : this.check.name,
            description : this.check.description,
            target : this.check.target,
            warn : this.check.warn,
            error : this.check.error,
            enabled : this.check.enabled
        };
    },
    
    submitEditCheck : function() {
        this.check.name = this.editcheck.name;
        this.check.description = this.editcheck.description;
        this.check.target = this.editcheck.target;
        this.check.warn = this.editcheck.warn;
        this.check.error = this.editcheck.error;
        this.check.enabled = this.editcheck.enabled;
        this.saveCheck();
    }
    
};

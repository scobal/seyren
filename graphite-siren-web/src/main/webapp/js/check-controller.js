/*global console*/

function CheckController() {
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
    }
    
};

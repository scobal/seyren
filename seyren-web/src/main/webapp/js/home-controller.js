/*global console*/

function HomeController() {
}

HomeController.prototype = {
    
    loadErrorChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks?states=ERROR', this.loadErrorChecksSuccess, this.loadErrorChecksFailure);
    },
        
    loadErrorChecksSuccess : function (code, response) {
        this.errorChecks = response;
    },
        
    loadErrorChecksFailure : function (code, response) {
        console.log('Loading error checks failed');
    },
    
    loadWarnChecks : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/checks?states=WARN', this.loadWarnChecksSuccess, this.loadWarnChecksFailure);
    },
        
    loadWarnChecksSuccess : function (code, response) {
        this.warnChecks = response;
    },
        
    loadWarnChecksFailure : function (code, response) {
        console.log('Loading warn checks failed');
    },
    
    selectCheck : function (id) {
        this.$location.updateHash('/checks/' + id);
    }

};
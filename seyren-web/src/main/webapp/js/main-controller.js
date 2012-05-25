/*global ChecksController,CheckController,HomeController,angular,console */

function MainController($xhr, $route, $location, $defer) {
    this.$xhr = $xhr;
    this.$route = $route;
    this.$location = $location;
    this.$defer = $defer;
    this.seyrenBaseUrl = '.';
    
    this.loadConfig();
    
    this.$route.when('/checks', {
        controller: ChecksController,
        template: 'html/checks.html'
    });
    
    this.$route.when('/checks/:id', {
        controller: CheckController,
        template: 'html/check.html'
    });
    
    this.$route.otherwise( {
        controller: HomeController,
        template: 'html/home.html'
    });
    
    this.$route.parent(this);
}

MainController.prototype = {
        
    loadConfig : function () {
        this.$xhr('GET', this.seyrenBaseUrl + '/api/config', this.loadConfigSuccess, this.loadConfigFailure);
    },
    
    loadConfigSuccess : function (code, response) {
        this.config = response;
        this.pingGraphite();
    },
    
    loadConfigFailure : function (code, response) {
        console.log('Loading config failed');
    }
    
};

MainController.$inject = ['$xhr', '$route', '$location', '$defer'];

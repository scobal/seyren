/*global ChecksController,CheckController,HomeController,angular*/

function MainController($xhr, $route, $location, $defer) {
    this.$xhr = $xhr;
    this.$route = $route;
    this.$location = $location;
    this.$defer = $defer;
    this.seyrenBaseUrl = '.';
    
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
};

MainController.$inject = ['$xhr', '$route', '$location', '$defer'];

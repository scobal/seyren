/*global ChecksController,CheckController,angular*/

function MainController($xhr, $route, $location) {
    this.$xhr = $xhr;
    this.$route = $route;
    this.$location = $location;
    this.seyrenBaseUrl = '.';
    
    this.$route.when('/checks', {
        controller: ChecksController,
        template: 'html/checks.html'
    });
    
    this.$route.when('/checks/:id', {
        controller: CheckController,
        template: 'html/check.html'
    });
    
    this.$route.otherwise({
        template: 'html/home.html'
    });
    
    this.$route.parent(this);
}

MainController.prototype = {
};

MainController.$inject = ['$xhr', '$route', '$location'];

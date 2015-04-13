/*global angular,seyrenApp,console,$ */
(function() {
    'use strict';

    seyrenApp.controller('LogoutController', ['$scope', '$rootScope', '$location', 'Authentication', function($scope, $rootScope, $location, Authentication) {

        $scope.logout = function() {
            Authentication.ClearCredentials();
            $location.path('/login');
        };

    }]);

}());
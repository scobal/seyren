/*global angular,seyrenApp,console,$ */
(function() {
    'use strict';

    seyrenApp.controller('LoginController', ['$scope', '$rootScope', '$location', 'Authentication', function($scope, $rootScope, $location, Authentication) {

        $scope.login = function() {
            Authentication.Login($scope.username, $scope.password, function(response) {
                if (response.admin) {
                    Authentication.SetCredentials($scope.username, $scope.password, true);
                    $location.path('/admin');
                } else if (response.authenticated) {
                    Authentication.SetCredentials($scope.username, $scope.password, false);
                    $location.path('/');
                } else {
                    $scope.error = response.message;
                }
            });
        };
    }]);

}());
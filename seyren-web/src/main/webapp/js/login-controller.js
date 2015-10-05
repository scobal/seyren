/*global angular,seyrenApp,console,$ */
(function() {
    'use strict';

    seyrenApp.controller('LoginController', ['$scope', '$rootScope', '$location', 'User', '$cookieStore', function($scope, $rootScope, $location, User, $cookieStore) {

        $scope.login = function() {
            User.authenticate({
                username: $scope.username,
                password: $scope.password
            }, function(response) {
                $rootScope.authToken = response.token;
                if ($scope.rememberMe) {
                    $cookieStore.put('authToken', response.token);
                }
                User.get(function(user) {
                    $rootScope.user = user;
                    $location.path("/");
                });
            }, function(error) {
                $scope.error = error.data;
            });
        };
    }]);

}());
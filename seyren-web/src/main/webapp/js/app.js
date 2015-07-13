/*global angular,console*/
var seyrenApp;

(function () {
    'use strict';

    seyrenApp = angular.module('seyrenApp', ['ngResource', 'ngRoute', 'seyrenApp.services', 'ngCookies', 'linkify', 'angularMoment', 'igTruncate'])
        .config(['$routeProvider', '$httpProvider', function ($routeProvider, $httpProvider) {
            $routeProvider.
                when('/checks', {templateUrl: 'html/checks.html',   controller: 'ChecksController'}).
                when('/checks/:id', {templateUrl: 'html/check.html', controller: 'CheckController',
                    resolve: {
                        configResults: ['Config', function (Config) {
                            return Config.query();
                        }]
                    }
                }).
                when('/login', {templateUrl: 'html/login.html', controller: 'LoginController'}).
                when('/admin', {templateUrl: 'html/admin.html', controller: 'AdminController'}).
                otherwise({templateUrl: 'html/home.html', controller: 'HomeController'});

            $httpProvider.interceptors.push(function($q, $rootScope, $location) {
                return {
                    'responseError': function(rejection) {
                        var status = rejection.status;

                        if (status === 401) {
                            $location.path("/login");
                        }

                        return $q.reject(rejection);
                    }
                };
            });

            $httpProvider.interceptors.push(function($q, $rootScope, $location) {
                return {
                    'request': function(config) {
                        if (angular.isDefined($rootScope.authToken)) {
                            config.headers['X-Auth-Token'] = $rootScope.authToken;
                        }
                        return config || $q.when(config);
                    }
                };
            });

        }])
        .run(['$rootScope', '$location', '$cookieStore', 'User', function ($rootScope, $location, $cookieStore, User) {

            /* Reset error when a new view is loaded */
            $rootScope.$on('$viewContentLoaded', function() {
                delete $rootScope.error;
            });

            $rootScope.hasAuthority = function(authority) {
                var i;
                if ($rootScope.user === undefined) {
                    return false;
                }

                if ($rootScope.user.authorities === undefined) {
                    return false;
                }

                for(i = 0; i < $rootScope.user.authorities.length; i++) {
                    if($rootScope.user.authorities[i].authority === authority) {
                        return true;
                    }
                }

                return false;
            };

            $rootScope.logout = function() {
                delete $rootScope.user;
                delete $rootScope.authToken;
                $cookieStore.remove('authToken');
                $location.path("/login");
            };

            var originalPath = $location.path(),
                authToken = $cookieStore.get('authToken');
            if (authToken !== undefined) {
                $rootScope.authToken = authToken;
                User.get(function(user) {
                    $rootScope.user = user;
                    $location.path(originalPath);
                });
            }

        }]);

}());

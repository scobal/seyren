/*global angular,console*/
var seyrenApp;

(function () {
    'use strict';

    seyrenApp = angular.module('seyrenApp', ['ngResource', 'ngRoute', 'seyrenApp.services', 'ngCookies', 'linkify', 'angularMoment', 'igTruncate'])
    .config(['$routeProvider', function ($routeProvider) {
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

    }])
    .run(['$rootScope', '$location', '$cookieStore', '$http', 'Config', function ($rootScope, $location, $cookieStore, $http, Config) {
          $rootScope.user = $cookieStore.get('user');
          $rootScope.config = $cookieStore.get('config');
          $rootScope.url = '/';
          if (typeof $rootScope.config === "undefined") {
           Config.query({}, function(seyrenConfig) {
           $rootScope.config = seyrenConfig;
                if(seyrenConfig.authenticationEnabled) {
                    if ($rootScope.user) {
                        $location.path('/');
                    } else {
                        $rootScope.url = '/login';
                        $location.path('/login');
                    }
                }
                $cookieStore.put('config', seyrenConfig);
            });
           }
           if (typeof $rootScope.user !== "undefined") {
              $http.defaults.headers.common.authorization = 'Basic ' + $rootScope.user.token;
           }
           $rootScope.$on('$routeChangeStart', function (event) {
               $rootScope.url = '/';
               $rootScope.user = $cookieStore.get('user');
               $rootScope.config = $cookieStore.get('config');
               if (typeof $rootScope.config !== "undefined") {
                    if($rootScope.config.authenticationEnabled) {
                        if (!$rootScope.user) {
                              $rootScope.url = '/login';
                              $location.path('/login');
                         }
                 }
               }
           });
       }]);

}());

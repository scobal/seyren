/*global angular,console*/
var seyrenApp;

(function () {
    'use strict';

    seyrenApp = angular.module('seyrenApp', ['ngResource', 'ngRoute', 'seyrenApp.services', 'linkify', 'angularMoment', 'igTruncate']);

    seyrenApp.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/checks', {templateUrl: 'html/checks.html',   controller: 'ChecksController'}).
            when('/checks/:id', {templateUrl: 'html/check.html', controller: 'CheckController',
                resolve: {
                    configResults: ['Config', function (Config) {
                        return Config.query();
                    }]
                }
            }).
            otherwise({templateUrl: 'html/home.html', controller: 'HomeController'});
    }]);

}());

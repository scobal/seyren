/*global angular,console*/
var seyrenApp;

(function () {
    'use strict';

    seyrenApp = angular.module('seyrenApp', ['ngResource', 'seyrenApp.services']);

    seyrenApp.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/checks', {templateUrl: 'html/checks.html',   controller: 'ChecksController'}).
            when('/checks/:id', {templateUrl: 'html/check.html', controller: 'CheckController'}).
            when('/graphite-instances', {templateUrl: 'html/graphite-instances.html', controller: 'GraphiteInstancesController'}).
            when('/graphite-instances/:id', {templateUrl: 'html/graphite-instance.html', controller: 'GraphiteInstanceController'}).
            otherwise({templateUrl: 'html/home.html', controller: 'HomeController'});
    }]);

}());
/*global seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('MainController', function MainController($scope, Config) {
        $scope.config = Config.query();
    });

}());

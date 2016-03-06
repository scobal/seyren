/*global seyrenApp,angular,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('HomeController', function HomeController($scope, $rootScope, $location, Checks, Alerts, Seyren) {
        $scope.pollAlertsInSeconds = 5;
        $scope.checkNames = {};

        $scope.loadUnhealthyChecks = function () {
            Checks.query({state: ['ERROR', 'WARN', 'EXCEPTION', 'UNKNOWN'], enabled: true}, function(data) {
                $scope.unhealthyChecks = data;
            }, function (err) {
                console.log('Loading unhealthy checks failed');
            });
        };

        $scope.loadAlertStream = function () {
            Alerts.query({items: 10}, function (data) {
                $scope.alertStream = data;
                $scope.loadCheckNames(data.values);
            }, function (err) {
                console.log('Loading alert stream failed');
            });
        };

        $scope.selectCheck = function (id) {
            $location.path('/checks/' + id);
        };

        $scope.loadCheckNames = function (alerts) {
            $scope.loadingCheckNames = {};
            angular.forEach(alerts, function (alert) {
                var checkId = alert.checkId;
                if (angular.isDefined($scope.loadingCheckNames[checkId])) {
                    return;
                }
                $scope.loadingCheckNames[checkId] = null;
                $scope.loadCheckName(checkId);
            });
        };

        $scope.loadCheckName = function (checkId) {
            Checks.get({checkId: checkId}, function (data) {
                $scope.checkNames[checkId] = data.name;
            });
        };

        $scope.swapCheckEnabled = function (check) {
            Seyren.swapCheckEnabled(check);
        };

        $scope.countdownToRefresh = function () {
            $scope.loadUnhealthyChecks();
            $scope.loadAlertStream();
        };
        $scope.countdownToRefresh();

        // karma hangs with $timeout
        $scope.timerId = setInterval(function () {
            $scope.countdownToRefresh();
            $scope.$apply();
        }, $scope.pollAlertsInSeconds * 1000);

        $scope.$on("$locationChangeStart", function () {
            clearInterval($scope.timerId);
        });

    });
}());

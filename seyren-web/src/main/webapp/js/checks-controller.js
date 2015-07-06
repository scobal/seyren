/*global seyrenApp,console,$ */
(function () {
    'use strict';
    seyrenApp.controller('ChecksController', function ChecksController($scope, $location, Checks, Seyren) {
        $scope.pollChecksInSeconds = 30;

        if ($location.search().filter) {
            $scope.filter = $location.search().filter;
        }

        $scope.loadChecks = function () {
            Checks.query(function (data) {
                $scope.checks = data;
            }, function (err) {
                console.log('Loading checks failed');
            });
        };

        $scope.countdownToRefresh = function () {
            $scope.loadChecks();
        };
        $scope.countdownToRefresh();

        $scope.timerId = setInterval(function () {
            $scope.countdownToRefresh();
            $scope.$apply();
        }, $scope.pollChecksInSeconds * 1000);

        $scope.$on("$locationChangeStart", function () {
            clearInterval($scope.timerId);
        });

        $scope.selectCheck = function (id) {
            $location.path('/checks/' + id);
        };

        $scope.editCheck = function (check) {
            Seyren.editCheck(check);
        };

        $scope.$on('check:created', function () {
            $scope.loadChecks();
        });

        $scope.editSubscription = function (check) {
            Seyren.editSubscription(check);
        };

        $scope.swapCheckEnabled = function (check) {
            console.log('swap check enabled');
            Seyren.swapCheckEnabled(check);
        };

        $scope.$on('check:swapCheckEnabled', function () {
            $scope.loadChecks();
        });

        $scope.sortByState = function (o) {
            switch (o.state) {
                case 'UNKNOWN':
                    return 0;
                case 'OK':
                    return 1;
                case 'WARN':
                    return 2;
                case 'ERROR':
                    return 3;
                case 'EXCEPTION':
                    return 4;
                default:
                    return -1;
            }
        };

    });
}());

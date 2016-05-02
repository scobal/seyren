/*global angular,moment,seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('CheckController', function CheckController($scope, $sce, $route, $timeout, $location, Checks, Graph, Subscriptions, Seyren, linkify, configResults) {

        $scope.pollCheckInSeconds = 30;
        $scope.pollAlertsInSeconds = 5;

        $scope.alertStartIndex = 0;
        $scope.alertItemsPerPage = 10;

        configResults.$promise.then(function(data) {
            $scope.config = data;

            $scope.graphsEnabled = function () {
                return $scope.config.graphsEnabled;
            };

            if ($scope.graphsEnabled()) {
                $scope.graphs = [{
                    description : "15 minutes",
                    minutes : -15
                }, {
                    description : "1 hour",
                    minutes : -60
                }, {
                    description : "1 day",
                    minutes : -1440
                }, {
                    description : "1 week",
                    minutes : -10080
                }];
            } else {
                $scope.graphs = [];
            }

            $scope.liveEnabled = function () {
                return $scope.config.graphiteCarbonPickleEnabled;
            };

        });

        $scope.loadCheck = function () {
            Checks.get({checkId: $route.current.params.id}, function (data) {
                $scope.check = data;
                $scope.check.descriptionHtml = $sce.trustAsHtml(linkify.normal($scope.check.description));
                $scope.check.lastLoadTime = new Date().getTime();
            }, function (err) {
                console.log('Loading check failed');
            });
        };

        $scope.loadAlerts = function () {
            Checks.alerts({checkId: $route.current.params.id, start: $scope.alertStartIndex, items: $scope.alertItemsPerPage}, function(data) {
                $scope.alerts = data;
            }, function (err) {
                console.log('Loading alerts failed');
            });
        };

        $scope.countdownToRefreshCheck = function () {
            $scope.loadCheck();
        };
        $scope.countdownToRefreshCheck();

        $scope.timerCheckId = setInterval(function () {
            $scope.countdownToRefreshCheck();
            $scope.$apply();
        }, $scope.pollCheckInSeconds * 1000);

        $scope.countdownToRefreshAlerts = function () {
            $scope.loadAlerts();
        };
        $scope.countdownToRefreshAlerts();

        $scope.timerAlertsId = setInterval(function () {
            $scope.countdownToRefreshAlerts();
            $scope.$apply();
        }, $scope.pollAlertsInSeconds * 1000);

        $scope.$on("$locationChangeStart", function () {
            clearInterval($scope.timerCheckId);
            clearInterval($scope.timerAlertsId);
        });

        $scope.loadOlderAlerts = function () {
            if ($scope.alerts.values.length !== $scope.alertItemsPerPage) {
                return;
            }
            $scope.alertStartIndex += $scope.alertItemsPerPage;
            $scope.loadAlerts();
        };

        $scope.loadNewerAlerts = function () {
            if ($scope.alertStartIndex === 0) {
                return;
            }
            $scope.alertStartIndex -= $scope.alertItemsPerPage;
            $scope.loadAlerts();
        };

        $('#editCheckModal').on('hide', function () {
            $scope.loadCheck();
        });

        $scope.editCheck = function (check) {
            $("#editCheckModal").modal();
            Seyren.editCheck(check);
        };

        $scope.$on('check:updated', function () {
            $scope.loadCheck();
        });
        $scope.$on('subscription:created', function () {
            $scope.loadCheck();
        });

        $scope.$on('subscription:updated', function () {
            $scope.loadCheck();
        });

        $scope.$on('check:swapCheckEnabled', function () {
            $scope.loadCheck();
        });

        $scope.$on('check:swapSubscriptionEnabled', function () {
            $scope.loadCheck();
        });

        $scope.$on('check:swapCheckLiveEnabled', function () {
            $scope.loadCheck();
        });

        $scope.$on('check:swapCheckAllowNoDataEnabled', function () {
            $scope.loadCheck();
        });

        $scope.swapCheckEnabled = function (check) {
            Seyren.swapCheckEnabled(check);
        };

        $scope.swapSubscriptionEnabled = function (check, subscription) {
            Seyren.swapSubscriptionEnabled(check, subscription);
        };

        $scope.swapCheckLiveEnabled = function (check) {
            Seyren.swapCheckLiveEnabled(check);
        };

        $scope.swapCheckAllowNoDataEnabled = function (check) {
            Seyren.swapCheckAllowNoDataEnabled(check);
        };

        $scope.editSubscription = function (check, subscription) {
            Seyren.editSubscription(check, subscription);
        };

        $scope.deleteSubscription = function (check, subscription) {
            Subscriptions.remove({checkId: check.id, subscriptionId: subscription.id}, subscription, function () {
                $scope.loadCheck();
            }, function (err) {
                console.log('Deleting subscription failed');
            });
        };

        $scope.testSubscription = function (check, subscription) {
            Subscriptions.test({checkId: check.id, subscriptionId: subscription.id}, subscription, function () {
                $scope.loadCheck();
            }, function (err) {
                console.log('Testing subscription failed');
            });
        };

        $scope.deleteCheck = function (check) {
            $('#confirmDeleteCheckButton').addClass('disabled');
            Checks.remove({checkId: check.id}, check, function () {
                $("#confirmCheckDeleteModal").modal("hide");
                $('#confirmDeleteCheckButton').removeClass('disabled');
                $location.path('/checks');
            }, function (err) {
                $('#confirmDeleteCheckButton').removeClass('disabled');
                console.log('Deleting check failed');
            });
        };

        $scope.liveLink = function (check, minutes) {
            return Graph.liveLink(check, minutes);
        };

        $scope.liveImage = function (check, minutes) {
            return Graph.liveImage(check, minutes);
        };

        $scope.deleteAlerts = function (check, days) {
            var params = {checkId: check.id};
            if (angular.isDefined(days)) {
                params.before = moment().subtract('days', days).format();
            }
            Checks.deleteAlerts(params, function() {
                $scope.loadAlerts();
            }, function (err) {
                console.log('Deleting alerts failed');
            });
        };

    });
}());

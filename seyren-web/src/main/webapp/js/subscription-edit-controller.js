/*global seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('SubscriptionEditModalController', function SubscriptionEditModalController($scope, $rootScope, Subscriptions, Seyren) {

        $('#editSubscriptionModal').on('shown', function () {
            $('#subscription\\.target').focus();
        });

        $scope.create = function () {
            $("#createSubscriptionButton").addClass("disabled");
            Subscriptions.create({checkId: $scope.check.id}, $scope.subscription, function () {
                $("#createSubscriptionButton").removeClass("disabled");
                $("#editSubscriptionModal").modal("hide");
                $scope.$emit('subscription:created');
            }, function () {
                $("#createSubscriptionButton").removeClass("disabled");
                console.log('Create subscription failed');
            });
        };

        $scope.update = function () {
            $("#updateSubscriptionButton").addClass("disabled");
            Subscriptions.update({checkId: $scope.check.id, subscriptionId: $scope.subscription.id}, $scope.subscription, function () {
                $("#updateSubscriptionButton").removeClass("disabled");
                $("#editSubscriptionModal").modal("hide");
                $scope.$emit('subscription:updated');
            }, function () {
                console.log('Saving subscription failed');
            });
        };

        $rootScope.$on('subscription:edit', function () {
            var editSubscription = Seyren.subscriptionBeingEdited();
            if (editSubscription) {
                $scope.newSubscription = false;
                $scope.subscription = editSubscription;
            } else {
                $scope.newSubscription = true;
                $scope.subscription = {};
                $scope.subscription.fromTime = "0000";
                $scope.subscription.toTime = "2359";
                $scope.subscription.type = "EMAIL";
                $scope.subscription.enabled = true;
                $scope.subscription.su = true;
                $scope.subscription.mo = true;
                $scope.subscription.tu = true;
                $scope.subscription.we = true;
                $scope.subscription.th = true;
                $scope.subscription.fr = true;
                $scope.subscription.sa = true;
            }
        });
    });

}());

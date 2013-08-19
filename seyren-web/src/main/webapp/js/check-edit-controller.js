/*global angular,seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('CheckEditModalController', function CheckEditModalController($scope, $rootScope, Checks, Seyren, Graph) {
        $scope.master = {
            name: null,
            description: null,
            target: null,
            warn: null,
            error: null,
            previewImage: "./img/preview-nodata.png",
            enabled: true,
            totalMetric: '-'
        };

        $('#editCheckModal').on('shown', function () {
            $('#check\\.name').focus();
            $('#check\\.warn\\.hint').tooltip({
                placement: 'right',
                title: 'Setting your warn level higher than your error level will result in Seyren generating alerts when the target value goes below the threshold.'
            });
        });

        $scope.create = function () {
            $("#createCheckButton").addClass("disabled");
            Checks.create($scope.check, function () {
                $("#createCheckButton").removeClass("disabled");
                $("#editCheckModal").modal("hide");
                $scope.$emit('check:created');
            }, function () {
                $("#createCheckButton").removeClass("disabled");
                console.log('Creating check failed');
            });
        };

        $scope.update = function () {
            $("#updateCheckButton").addClass("disabled");
            Checks.update({checkId: $scope.check.id}, $scope.check, function () {
                $("#updateCheckButton").removeClass("disabled");
                $("#editCheckModal").modal("hide");
                $scope.$emit('check:updated');
            }, function () {
                console.log('Saving check failed');
            });
        };

        $scope.reset = function () {
            $scope.check = angular.copy($scope.master);
        };

        $rootScope.$on('check:edit', function () {
            var editCheck = Seyren.checkBeingEdited();
            if (editCheck) {
                $scope.newCheck = false;
                $scope.check = editCheck;
            } else {
                $scope.newCheck = true;
                $scope.reset();
            }
        });

        $scope.$watch('check.target + check.warn + check.error', function (value) {
            if (value !== undefined) {
                $scope.check.previewImage = Graph.previewImage($scope.check);
            } else {
                return "./img/preview-nodata.png";
            }
        });

    });

}());

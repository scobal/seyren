/*global angular,seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('CheckEditModalController', function CheckEditModalController($scope, $rootScope, Checks, Seyren, Graph, Metrics) {

        $scope.master = {
            name: null,
            description: null,
            target: null,
            warn: null,
            error: null,
            previewImage: "./img/preview-nodata.png",
            enabled: true,
            live: false,
            allowNoData: false,
            totalMetric: '-',
            tag: null
        };
        
        $scope.getAllTags = function() {
            var existing_tags = [];
            angular.forEach($rootScope.checks.values, function (check) {
                if (check.tag !== null && existing_tags.indexOf(check.tag) === -1) {
                    existing_tags.push(check.tag);
                }
                
                function insensitive(s1, s2) {
                    var s1lower = s1.toLowerCase(),
                    s2lower = s2.toLowerCase();
                    return s1lower > s2lower? 1 : (s1lower < s2lower? -1 : 0);
                }
                
                existing_tags.sort(insensitive);
                $scope.tags = existing_tags;
            });
        };

        $('#editCheckModal').on('shown.bs.modal', function () {
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
            $scope.getAllTags();
            if (editCheck) {
                $scope.newCheck = false;
                $scope.check = editCheck;
            } else {
                $scope.newCheck = true;
                $scope.reset();
            }
        });

        $scope.$watch('check.target + check.warn + check.error', function (value) {
            if ($scope.check !== undefined && ($scope.config === undefined || (value !== undefined && $scope.config.graphsEnabled))) {
                $scope.check.previewImage = Graph.previewImage($scope.check);
            } else {
                return "./img/preview-nodata.png";
            }
        });


        $scope.$watch('check.target', function(value) {
            if (value) {
                Metrics.totalMetric({target: value}, function (data) {
                    $scope.check.totalMetric = data[value];
                }, function () {
                    console.log('Metrics count failed');
                    $scope.check.totalMetric = '-';
                });
            }
        });
    });
}());

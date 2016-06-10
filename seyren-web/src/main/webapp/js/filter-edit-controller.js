/*global angular,seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('AddFilterModalController', function AddFilterModalController($scope, $rootScope, Filters, Seyren, Graph, Metrics) {
        $scope.master = {
            filter: null,
            name: null
        };

        $('#addFilterModal').on('shown.bs.modal', function () {
            $scope.filterToAdd = {filter: $scope.filter};
            $('#filter\\.name').focus();
            $('#filter\\.warn\\.hint').tooltip({
                placement: 'right',
                title: 'Setting your warn level higher than your error level will result in Seyren generating alerts when the target value goes below the threshold.'
            });
        });

        $scope.create = function () {
            $("#createFilterButton").addClass("disabled");
            Filters.create($scope.filterToAdd, function () {
                $("#createFilterButton").removeClass("disabled");
                $("#addFilterModal").modal("hide");
                $scope.$emit('filter:created');
            }, function () {
                $("#createFilterButton").removeClass("disabled");
                console.log('Creating filter failed');
            });
        };

        $scope.reset = function () {
            $scope.filterToAdd = angular.copy($scope.master);
        };

        $rootScope.$on('filter:created', function () {
          $scope.reset();
        });

    });

}());

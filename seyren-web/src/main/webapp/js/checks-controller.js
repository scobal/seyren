/*global seyrenApp,console,$ */
(function () {
    'use strict';
    seyrenApp.controller('ChecksController', function ChecksController($scope, $location, Checks, Filters, Seyren) {
        $scope.pollChecksInSeconds = 30;

        if ($location.search().filter) {
            $scope.filter = $location.search().filter;
        }

        $scope.deleteFilter = function (filter, event) {
          event.stopPropagation();
          Filters.remove({filterId: filter.id}, filter, function () {
              $scope.loadFilters();
          }, function (err) {
              console.log('Deleting filter failed');
          });
        };

        $scope.searchByFilter = function (filter, event) {
          event.preventDefault();
          $scope.toggleSavedFiltersPanel();
          $scope.filter = filter;
          $scope.filterToUrl();
        };

        $scope.addFilter = function () {
          $("#addFilterModal").modal();
        };

        $scope.loadFilters = function () {
            Filters.get(function (data) {
                console.log(data);
                $scope.savedFilters = data;
            }, function (err) {
                console.log('Loading filters failed');
            });
        };

        $scope.toggleSavedFiltersPanel = function () {
          var elem = $("#panel-saved-filters");
          if (elem.is(":visible")) {
            elem.hide();
          } else {
            elem.show();
          }
        };

        $scope.loadChecks = function () {
            Checks.query(function (data) {
                $scope.checks = data;
            }, function (err) {
                console.log('Loading checks failed');
            });
        };

        $scope.filterToUrl = function () {
            $location.search('filter', $scope.filter);
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

        $scope.selectCheck = function (id, $event) {
            $location.search('filter', null);
            $location.path('/checks/' + id);
        };

        $scope.editCheck = function (check) {
            Seyren.editCheck(check);
        };

        $scope.$on('check:created', function () {
            $scope.loadChecks();
        });

        $scope.$on('filter:created', function () {
            $scope.loadFilters();
        });
        $scope.loadFilters();

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

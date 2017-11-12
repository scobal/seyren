/*global angular,seyrenApp,console,$ */
(function () {
    'use strict';

    seyrenApp.controller('CheckEditModalController', function CheckEditModalController($scope, $rootScope, Checks, Seyren, Graph, Metrics, Config) {
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
            graphiteBaseUrl: '',
            enableConsecutiveChecks: false,
            relativeDiff : null,
            absoluteDiff : null,
            minConsecutiveViolations : null,
            asgName : null,
            type : null

        };

        Config.query({}, function(config) {
            $scope.master.graphiteBaseUrl = config.graphiteUrl;
        });

        $('#editCheckModal').on('shown.bs.modal', function () {
            $('#check\\.name').focus();
            $('#check\\.target\\.hint').tooltip({
                placement: 'right',
                title: 'The target parameter specifies a path identifying one or several metrics, optionally with functions acting on those metrics.'
            });
            $('#check\\.warn\\.hint').tooltip({
                placement: 'right',
                title: 'Setting your warn level higher than your error level will result in Seyren generating alerts when the target value goes below the threshold.'
            });
            $('#check\\.typeoutlier\\.hint').tooltip({
                            placement: 'right',
                            title: ' Outlier check detects if any instance in a cluster is running in a degraded state as compared to rest of cluster. AVAILABLE ONLY FOR AWS EC2 CLOUD-FORMATIONS.'
                        });

            $('#check\\.from\\.hint').tooltip({
                placement: 'right',
                html: true,
                title: '"From" and "To" are optional parameters that specify the relative or absolute time period to' +
                ' retrieve from the server. See ' +
                '<a href="http://graphite.readthedocs.io/en/latest/render_api.html#from-until">the Graphite' +
                ' documentation</a>. Only the most recent value of each series returned will be used.',
                trigger: 'click'
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

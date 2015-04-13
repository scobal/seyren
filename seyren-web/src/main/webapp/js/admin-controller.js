/*global angular,seyrenApp,console,$ */
(function() {
    'use strict';

    seyrenApp.controller('AdminController', ['$scope', '$rootScope', 'PermissionsSubscription', '$http', function AdminController($scope, $rootScope, PermissionsSubscription, $http) {
        var init = function() {
            $scope.users = [];
            $scope.selectedIndex = -1;
        };

        init();

        $scope.getPermissionsChecks = function(index) {
            PermissionsSubscription.get({
                name: $scope.users[index]
            }, function(data) {
                $scope.pername = $scope.users[index];
                $scope.users = [];
                $scope.error = false;
                $scope.types = data.types;
                $scope.writes = data.write;
                if (!$scope.writes) {
                    $scope.writes = [];
                }
            }, function(error) {
                $scope.error = "User could not be found";
                $scope.types = [];
                $scope.writes = [];
            });
        };

        $scope.togglePermission = function togglePermission(typeName) {
            var idx = $scope.writes.indexOf(typeName);
            if (idx > -1) {
                $scope.writes.splice(idx, 1);
            } else {
                $scope.writes.push(typeName);
            }
        };
        $scope.updatePermissionsChecks = function() {
            PermissionsSubscription.save({
                name: $scope.pername,
                write: $scope.writes
            });
        };

        $scope.checkKeyDown = function(event) {
            if (event.keyCode === 40) {
                event.preventDefault();
                if ($scope.selectedIndex + 1 !== $scope.users.length) {
                    $scope.selectedIndex++;
                }
            } else if (event.keyCode === 38) {
                event.preventDefault();
                if ($scope.selectedIndex - 1 !== -1) {
                    $scope.selectedIndex--;
                }
            }
        };

        $scope.search = function() {
            $scope.types = [];
            $scope.writes = [];
            $http.get("api/permissions/users?name=" + $scope.pername).success(function(data) {
                if (data.indexOf($scope.pername) === -1) {
                    data.unshift($scope.pername);
                }
                $scope.users = data;
                $scope.selectedIndex = -1;
            });
        };
    }]);

}());
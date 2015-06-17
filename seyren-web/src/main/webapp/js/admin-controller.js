/*global angular,seyrenApp,console,$ */
(function() {
    'use strict';

    seyrenApp.controller('AdminController', ['$scope', '$rootScope', 'Admin', '$http', 'User', function AdminController($scope, $rootScope, Admin, $http, User) {
        var init = function() {
            $scope.users = [];
            $scope.selectedIndex = -1;
        };

        init();

        $scope.getPermissionsChecks = function(index) {
            Admin.get({
                name: $scope.users[index],
                type: 'subscription'
            }, function(data) {
                $scope.clearAlert();
                $scope.pername = $scope.users[index];
                $scope.users = [];
                $scope.types = data.types;
                $scope.writes = data.write;
                if (!$scope.writes) {
                    $scope.writes = [];
                }
            }, function(error) {
                $scope.state = "error";
                 $scope.statemsg = "User " + $scope.pername +  " could not be found.";
                $scope.types = [];
                $scope.writes = [];
            });
        };

        $scope.openAddUser = function () {
            $scope.clearAlert();
            $("#addUserModal").modal();
            $scope.addusererror = null;
            $scope.addusersuccess = null;
        };

        $scope.createUser = function () {
               User.add({username: $scope.pername, password: $scope.password}, function() {
                 $scope.addusersuccess = "User " + $scope.pername +  " has been added.";
                 $scope.addusererror = null;
           }, function(error) {
             $scope.addusersuccess = null;
             $scope.addusererror = "User " + $scope.pername +  " could not be added.";
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
            Admin.save({
                type: 'subscription',
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

        $scope.clearAlert = function() {
            $scope.state = null;
            $scope.statemsg = null;
        };

        $scope.search = function() {
            $scope.types = [];
            $scope.writes = [];
            $http.get("api/admin/users?name=" + $scope.pername).success(function(data) {
                if(data.length === 0) {
                if($scope.pername) {
                    $scope.state = "warning";
                    $scope.statemsg = "User " + $scope.pername +  " does not exist.";
                } else {
                    $scope.clearAlert();
                }
                    return;
                }
                $scope.users = data;
                $scope.selectedIndex = -1;
            });
        };
    }]);

}());
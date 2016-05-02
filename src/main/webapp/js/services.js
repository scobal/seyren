/*global angular,console,$*/
(function () {
    'use strict';

    angular.module('seyrenApp.services', ['ngResource']).
        config(function ($httpProvider) {
            $httpProvider.responseInterceptors.push('spinnerHttpInterceptor');
            var spinnerFunction = function (data, headersGetter) {
                $('#spinnerG').show();
                $('#banner').hide();
                return data;
            };
            $httpProvider.defaults.transformRequest.push(spinnerFunction);
        }).
        factory('spinnerHttpInterceptor', function ($q, $window) {
            return function (promise) {
                return promise.then(function (response) {
                    $('#spinnerG').hide();
                    $('#banner').hide();
                    return response;

                }, function (response) {
                    $('#spinnerG').hide();
                    $('#banner').show();
                    return $q.reject(response);
                });
            };
        }).
        factory('Alerts', function ($resource) {
            return $resource('api/alerts', {}, {
                'query':    {method: 'GET', isArray: false}
            });
        }).
        factory('Checks', function ($resource) {
            return $resource('api/checks/:checkId/:action', {checkId: "@checkId"}, {
                'query':        {method: 'GET', isArray: false},
                'get':          {method: 'GET'},
                'create':       {method: 'POST'},
                'update':       {method: 'PUT'},
                'remove':       {method: 'DELETE'},
                'alerts':       {method: 'GET', params: {action: "alerts"}},
                'deleteAlerts': {method: 'DELETE', params: {action: "alerts"}}
            });
        }).
        factory('Config', function ($resource) {
            return $resource('api/config', {}, {
                'query':    {method: 'GET', isArray: false, cache : true}
            });
        }).
        factory('Subscriptions', function ($resource) {
            return $resource('api/checks/:checkId/subscriptions/:subscriptionId/:action', {checkId: "@checkId", subscriptionId: "@subscriptionId"}, {
                'create':    {method: 'POST'},
                'update':    {method: 'PUT'},
                'remove':    {method: 'DELETE'},
                'test':      {method: 'PUT', params: {action: "test"}}
            });
        }).
        factory('Metrics', function ($resource) {
            return $resource('api/metrics/:target/:action', {target: "@target"}, {
                'totalMetric':      {method: 'GET', params: {action: 'total'}}
            });
        }).
        factory('Graph', function ($resource) {
            var chart = function (baseurl, chart) {
                var result = baseurl + '/?';
                if (chart.width) {
                    result += '&width=' + chart.width;
                }
                if (chart.height) {
                    result += '&height=' + chart.height;
                }
                if (chart.from) {
                    result += '&from=' + chart.from + 'Minutes';
                } else {
                    result += '&from=-1day';
                }
                if (chart.to) {
                    result += '&to=' + chart.to + 'Minutes';
                }
                if (chart.warn) {
                    result += '&warn=' + chart.warn;
                }
                if (chart.error) {
                    result += '&error=' + chart.error;
                }
                if (typeof chart.hideLegend !== 'undefined') {
                    result += '&hideLegend=' + chart.hideLegend;
                }
                if (typeof chart.hideAxes !== 'undefined') {
                    result += '&hideAxes=' + chart.hideAxes;
                }
                if (chart.uniq) {
                    result += '&uniq=' + chart.uniq;
                }
                return result;
            };
            return {
                previewImage: function (check) {
                    if (check && check.target) {
                        return chart('./api/chart/' + check.target, {
                            target: check.target,
                            width: 365,
                            height: 70,
                            warn: check.warn,
                            error: check.error,
                            hideLegend: true
                        });
                    }
                },
                liveLink: function (check, minutes) {
                    if (check && check.id) {
                        return chart('./api/checks/' + check.id + '/image', {
                            target: check.target,
                            width: 1200,
                            height: 350,
                            from: minutes
                        });
                    }
                },
                liveImage: function (check, minutes) {
                    if (check && check.id) {
                        return chart('./api/checks/' + check.id + '/image', {
                            target: check.target,
                            width: 365,
                            height: 70,
                            from: minutes,
                            hideAxes: true,
                            hideLegend: true,
                            uniq: check.lastLoadTime
                        });
                    }
                }
            };
        }).
        factory('Seyren', function ($rootScope, Checks, Subscriptions) {
            var checkBeingEdited = null,
                subscriptionBeingEdited = null;
            return {
                editCheck: function (check) {
                    if (check === 'new') {
                        checkBeingEdited = null;
                    } else {
                        checkBeingEdited = {};
                        angular.copy(check, checkBeingEdited);
                    }
                    $rootScope.$broadcast('check:edit');
                },
                checkBeingEdited: function () {
                    return checkBeingEdited;
                },
                editSubscription: function (check, subscription) {
                    if (subscription === 'new') {
                        subscriptionBeingEdited = null;
                    } else {
                        subscriptionBeingEdited = {};
                        angular.copy(subscription, subscriptionBeingEdited);
                    }
                    $rootScope.$broadcast('subscription:edit');
                },
                subscriptionBeingEdited: function () {
                    return subscriptionBeingEdited;
                },
                swapCheckEnabled: function (check) {
                    check.enabled = !check.enabled;
                    Checks.update({ checkId:  check.id}, check, function (data) {
                        $rootScope.$broadcast('check:swapCheckEnabled');
                    }, function (err) {
                        console.log('Saving check failed');
                    });
                },
                swapSubscriptionEnabled: function (check, subscription) {
                    subscription.enabled = !subscription.enabled;
                    Subscriptions.update({ checkId:  check.id, subscriptionId:  subscription.id}, subscription, function (data) {
                        $rootScope.$broadcast('subscription:swapCheckEnabled');
                    }, function (err) {
                        console.log('Saving subscription failed');
                    });
                },
                swapCheckLiveEnabled: function (check) {
                    check.live = !check.live;
                    Checks.update({ checkId:  check.id}, check, function (data) {
                        $rootScope.$broadcast('check:swapCheckLiveEnabled');
                    }, function (err) {
                        console.log('Saving check failed');
                    });
                },
                swapCheckAllowNoDataEnabled: function (check) {
                    check.allowNoData = !check.allowNoData;
                    Checks.update({ checkId:  check.id}, check, function (data) {
                        $rootScope.$broadcast('check:swapCheckAllowNoDataEnabled');
                    }, function (err) {
                        console.log('Saving check failed');
                    });
                }
            };
        });

}());
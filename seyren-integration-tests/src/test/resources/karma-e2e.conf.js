module.exports = function (config) {
    config.set({
        basePath : '../..',
        frameworks: ['ng-scenario', 'jasmine'],
        plugins: [
            'karma-ng-scenario',
            'karma-jasmine',
            'karma-phantomjs-launcher'
        ],
        files: [
            'test/e2e/**/*.js',
            '../../seyren-web/src/main/webapp/js/lib/jquery-1.7.2.min.js'
        ],
        proxies: {
            '/': 'http://localhost:8080/seyren/'
        },
        captureTimeout: 50000,
        urlRoot: '/_karma_/',
        junitReporter: {
            outputFile: 'test_out/e2e.xml',
            suite: 'e2e'
        },
        autoWatch: false,
        singleRun: true,
        logLevel: config.LOG_INFO,
        logColors: true,
        browsers: [
            'PhantomJS'
        ],
        reporters: 'dots'

    });
};


// Karma configuration

module.exports = function (config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '../',


        // frameworks to use
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            'bower_components/jquery/dist/jquery.js',
            'bower_components/angular/angular.js',
            'bower_components/angular-base64/angular-base64.js',
            'bower_components/angular-base64-upload/src/angular-base64-upload.js',
            'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
            'bower_components/jquery-ui/jquery-ui.js',
            'bower_components/angular-dragdrop/src/angular-dragdrop.js',
            'bower_components/angular-filter/dist/angular-filter.min.js',
            'bower_components/angular-md5/angular-md5.js',
            'bower_components/perfect-scrollbar/src/perfect-scrollbar.js',
            'bower_components/angular-perfect-scrollbar/src/angular-perfect-scrollbar.js',
            'bower_components/angular-mocks/angular-mocks.js',
            'bower_components/angular-resource/angular-resource.js',
            'bower_components/angular-sanitize/angular-sanitize.js',
            'bower_components/angular-tooltips/dist/angular-tooltips.min.js',
            'bower_components/angular-translate/angular-translate.js',
            'bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
            'bower_components/angular-ui-router/release/angular-ui-router.js',
            'bower_components/angular-uuid4/angular-uuid4.js',
            'bower_components/bootstrap/dist/js/bootstrap.js',
            'bower_components/checklist-model/checklist-model.js',
            'bower_components/angular-clipboard/angular-clipboard.js',
            'bower_components/angular-resizable/src/angular-resizable.js',
            'bower_components/angular-ui-notification/src/angular-ui-notification.js',
            'bower_components/lodash/lodash.js',
            'bower_components/restangular/dist/restangular.js',
            'bower_components/jspdf/dist/jspdf.min.js',
            'app/scripts/utils/**/*.js',
            'app/scripts/services/**/*.js',
            'app/scripts/models/**/*.js',
            'app/scripts/view-models/**/*.js',
            'app/scripts/filters/**/*.js',
            'app/scripts/directives/**/*.js',
            'app/scripts/modules/**/*.js',

            'app/scripts/app.js',
            'app/languages/**/*.js',

            'app/scripts/templates.js',





            //'app/scripts/view-models/dashboard/dashboard-view-model-tests.js',


            'app/scripts/**/*-tests.js',
            //'app/scripts/app.js',


            //definition to allow to debug TS tests  file in browser
            {pattern: 'app/scripts/**/*-tests.ts', included: false},
            {pattern: 'app/scripts/**/*-tests.js.map', included: false},


            //definition to allow to debug TS sources files in browser
            {pattern: 'app/scripts/**/*.ts', included: false},
            {pattern: 'app/scripts/**/*.js.map', included: false}

        ],

        // list of files to exclude
        exclude: [

        ],

        junitReporter: {
            outputFile: 'tests/testOutput.xml',
            suite: ''
        },

        //NOTE: This is handled from gruntfile.js
        coverageReporter : {
            type : 'html',
            dir: 'tests/Coverage'
        },
        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: [
            'mocha',//uncomment this line if you need to debug your unit test and print out the 'describe' of the test
            'junit',
            'dots',
            'progress',
            'coverage'
        ],

        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
      //  logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        preprocessors: {
            '**/*.html': 'html2js',
            'app/scripts/**/!(*-tests|*Tests|*Test).js': 'coverage'
        },

        //ngHtml2JsPreprocessor: {
        //    stripPrefix: 'client/'
        //},

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera (has to be installed with `npm install karma-opera-launcher`)
        // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
        // - PhantomJS
        // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)

        //NOTE: This is handled from gruntfile.js

         browsers: ['Chrome'],


        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false
    });
};

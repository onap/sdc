// Generated on 2015-04-28 using
// generator-webapp 0.5.1
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// If you want to recursively match all subfolders, use:
// 'test/spec/**/*.js'

module.exports = function (grunt) {

    // Time how long tasks take. Can help when optimizing build times
    require('time-grunt')(grunt);

    // Load grunt tasks automatically
    require('load-grunt-tasks')(grunt);

    // Configurable paths
    var config = {
        app: 'app',
        appModuleName: 'sdcApp',
        dist: 'app/dist'
    };

    // Define the configuration for all the tasks
    grunt.initConfig({

        // Project settings
        config: config,

        // Watches files for changes and runs tasks based on the changed files
        watch: {
            html: {
                files: ['<%= config.app %>/scripts/**/*.html'],
                tasks: ['ngtemplates:app']
            },
            less: {
                files: ['<%= config.app %>/**/*.less'],
                tasks: ['less:all']
            },
            ts: {
                files: ['<%= config.app %>/scripts/**/*.ts'],
                tasks: ['ts:all']
            },
            bower: {
                files: ['bower.json'],
                tasks: ['wiredep']
            },

            gruntfile: {
                files: ['Gruntfile.js']
            },

            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [
                    '<%= config.app %>/{,*/}*.html',
                    '<%= config.app %>/scripts/**/*.html',
                    '<%= config.app %>/scripts/**/*.css',
                    '.tmp/styles/{,*/}*.css',
                    '<%= config.app %>/images/{,*/}*'
                ]
            },
            configurations: {
                files: [
                    'configurations/*.json'
                ],
                tasks: ['ngconstant']
            }
        },

        ngconstant: {
            options: {
                dest: 'app/scripts/modules/configurations.js',
                name: 'Sdc.Config'
            },
            main: {
                constants: {
                    sdcConfig: grunt.file.readJSON(grunt.option('env') ? 'configurations/' + grunt.option('env') + '.json' : 'configurations/prod.json'),
                    sdcMenu: grunt.file.readJSON('configurations/menu.json')
                }
            }
        },

        express: {
            options: {
                port: process.env.PORT || 9000
            },
            mock: {
                options: {
                    script: 'server-mock/mock-server.js'
                }
            }
        },

        ts: {
            all: {
                src: [
                    'app/scripts/**/*.ts',
                    'typings/**/*.ts'
                ],
                reference: 'app/scripts/references.ts'
            },
            single: {
                src: []
            }
        },
        ngtemplates: {
            app: {
                options: {
                    module: '<%= config.appModuleName %>',
                    prefix: '/'
                },
                src: [
                    '<%= config.app %>/scripts/**/*.html',
                    '!index.html'
                ],
                dest: '<%= config.app %>/scripts/templates.js'
            }
        },
        less: {
            all: {
                options: {
                    paths: ['<%= config.app %>/scripts',
                        '<%= config.app %>/styles']
                },
                files: {
                    '<%= config.app %>/styles/app.css': '<%= config.app %>/styles/app.less'
                }
            },
            single: {
                paths: ['<%= config.app %>/scripts',
                    '<%= config.app %>/styles'],
                files: []
            }
        },

        injector: {
            options: {},
            // Inject application script files into index.html (doesn't include bower)
            scripts_models: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_models -->',
                    endtag: '<!-- endinjector:js_models -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        [
                            '<%= config.app %>/scripts/models/**/*.js',
                            '!<%= config.app %>/scripts/models/**/*-tests.js'
                        ]
                    ]
                }
            },

            scripts_utils: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_utils -->',
                    endtag: '<!-- endinjector:js_utils -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        [
                            '<%= config.app %>/scripts/utils/**/*.js',
                            '!<%= config.app %>/scripts/models/**/*-tests.js'
                        ]
                    ]
                }
            },

            scripts_filters: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_filters -->',
                    endtag: '<!-- endinjector:js_filters -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        ['<%= config.app %>/scripts/filters/**/*.js',
                            '!<%= config.app %>/scripts/filters/**/*-tests.js'
                        ]
                    ]
                }
            },

            scripts_directives: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_directives -->',
                    endtag: '<!-- endinjector:js_directives -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        ['<%= config.app %>/scripts/directives/**/*.js',
                            '!<%= config.app %>/scripts/directives/**/*-tests.js'
                        ]
                    ]
                }
            },

            scripts_services: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_services -->',
                    endtag: '<!-- endinjector:js_services -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        ['<%= config.app %>/scripts/services/**/*.js',
                            '!<%= config.app %>/scripts/services/**/*-tests.js'
                        ]
                    ]
                }
            },

            scripts_view_models: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        return '<script src="' + filePath + '"></script>';
                    },
                    starttag: '<!-- injector:js_view_models -->',
                    endtag: '<!-- endinjector:js_view_models -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        ['<%= config.app %>/scripts/view-models/**/*.js',
                            '!<%= config.app %>/scripts/view-models/**/*-tests.js']
                    ]
                }
            },

            // Inject component less into app.less
            less: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/scripts/', '../scripts/');
                        filePath = filePath.replace('/app/styles/', '');
                        return '@import \'' + filePath + '\';';
                    },
                    starttag: '// injector:less',
                    endtag: '// endinjector:less'
                },
                files: {
                    '<%= config.app %>/styles/app.less': [
                        '<%= config.app %>/styles/**/*.less',
                        '<%= config.app %>/scripts/**/*.less',
                        '!<%= config.app %>/styles/app.less'
                    ]
                }
            },

            // Inject component css into index.html
            css: {
                options: {
                    transform: function (filePath) {
                        filePath = filePath.replace('/app/', '');
                        filePath = filePath.replace('/.tmp/', '');
                        return '<link rel="stylesheet" href="' + filePath + '">';
                    },
                    starttag: '<!-- injector:css -->',
                    endtag: '<!-- endinjector -->'
                },
                files: {
                    '<%= config.app %>/index.html': [
                        '<%= config.app %>/scripts/**/*.css',
                        '<%= config.app %>/styles/**/*.css',
                        '!<%= config.app %>/styles/app.css'
                    ]
                }
            }
        },

        // The actual grunt server settings
        connect: {
            options: {
                port: 9000,
                open: true,
                livereload: 35729,
                // Change this to '0.0.0.0' to access the server from outside
                hostname: 'localhost'
            },
            livereload: {
                options: {
                    middleware: function (connect) {
                        return [
                            connect().use(function (req, res, next) {
                                var mockApis = require('./configurations/mock.json').sdcConfig;
                                var userType;
                                switch (grunt.option('role')) {
                                    case "admin":
                                        userType = mockApis.userTypes.admin;
                                        break;
                                    case "tester":
                                        userType = mockApis.userTypes.tester;
                                        break;
                                    case "governor":
                                        userType = mockApis.userTypes.governor;
                                        break;
                                    case "ops":
                                        userType = mockApis.userTypes.ops;
                                        break;
                                    case "designer":
                                        userType = mockApis.userTypes.designer;
                                        break;
                                    case "product_strategist":
                                        userType = mockApis.userTypes.product_strategist;
                                        break;
                                    case "product_manager":
                                        userType = mockApis.userTypes.product_manager;
                                        break;
                                    default:
                                        userType = mockApis.userTypes.designer;
                                }
                                res.cookie(mockApis.cookie.userIdSuffix, req.headers[mockApis.cookie.userIdSuffix] || userType.userId);
                                res.cookie(mockApis.cookie.userEmail, req.headers[mockApis.cookie.userEmail] || userType.email);
                                res.cookie(mockApis.cookie.userFirstName, req.headers[mockApis.cookie.userFirstName] || userType.firstName);
                                res.cookie(mockApis.cookie.userLastName, req.headers[mockApis.cookie.userLastName] || userType.lastName);
                                next();
                            }),
                            connect().use(require('http-proxy-middleware')(['/onboarding', '/onboarding-api'], {
                                target: 'http://feHost:8181/',
                                changeOrigin: true,
                                secure: false
                            })),
                            connect().use('/bower_components', connect.static('./bower_components')),
                            connect().use('/non_bower_components', connect.static('./non_bower_components')),
                            connect.static(config.app)
                        ];
                    }
                }
            },
            dist: {
                options: {
                    base: '<%= config.dist %>',
                    livereload: false
                }
            }
        },

        // Empties folders to start fresh
        clean: {
            generated: {
                files: [{
                    dot: true,
                    src: [
                        '<%= config.app %>/scripts/**/*.js',
                        '<%= config.app %>/scripts/**/*.css',
                        '!<%= config.app %>/scripts/**/welcome/styles/*.css',
                        '<%= config.app %>/styles/**/*.css',
                        '<%= config.app %>/scripts/**/*.js.map'
                    ]
                }]
            },
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= config.dist %>/*',
                        '!<%= config.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },
        // Add vendor prefixed styles
        autoprefixer: {
            options: {
                browsers: ['> 1%', 'last 2 versions', 'Firefox ESR', 'Opera 12.1']
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: '.tmp/css/',
                    src: '{,*/**/}*.css',
                    dest: '.tmp/css/'
                }]
            }
        },

        // Automatically inject Bower components into the HTML file
        wiredep: {
            app: {
                ignorePath: /^\/|\.\.\//,
                src: ['<%= config.app %>/index.html']
            }
        },

        // Renames files for browser caching purposes
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= config.dist %>/scripts/{,*/}*.js',
                        '<%= config.dist %>/styles/{,*/}*.css',
                        '<%= config.dist %>/images/{,*/}*.*',
                        '!<%= config.dist %>/images/resource-icons/{,*/}*.*',
                        '!<%= config.dist %>/images/service-icons/{,*/}*.*',
                        '!<%= config.dist %>/images/relationship-icons/{,*/}*.*',
                        '<%= config.dist %>/*.{ico,png}'
                    ]
                }
            }
        },

        // Reads HTML for usemin blocks to enable smart builds that automatically
        // concat, minify and revision files. Creates configurations in memory so
        // additional tasks can operate on them
        useminPrepare: {
            options: {
                dest: '<%= config.dist %>'
            },
            sdc: {
                src: ['<%= config.app %>/index.html']
            },
            html: '<%= config.app %>/index.html'
        },

        // Performs rewrites based on rev and the useminPrepare configuration
        usemin: {
            options: {
                assetsDirs: [
                    '<%= config.dist %>',
                    '<%= config.dist %>/images',
                    '<%= config.dist %>/styles'
                ],
                // This is so we update image references in our ng-templates
                patterns: {
                    js: [
                        [/(assets\/images\/.*?\.(?:gif|jpeg|jpg|png|webp|svg))/gm, 'Update the JS to reference our revved images']
                    ]
                }
            },
            html: ['<%= config.dist %>/{,*/}*.html'],
            css: ['<%= config.dist %>/styles/{,*/}*.css'],
            js: ['<%= config.dist %>/public/{,*/}*.js']
        },

        // The following *-min tasks produce minified files in the dist folder
        imagemin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= config.app %>/images',
                    src: '<%= config.app %>/**/*.{gif,jpeg,jpg,png}',
                    dest: '<%= config.dist %>/images'
                }]
            }
        },

        svgmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= config.app %>/images',
                    src: '{,*/}*.svg',
                    dest: '<%= config.dist %>/images'
                }]
            }
        },

        htmlmin: {
            dist: {
                options: {
                    collapseBooleanAttributes: true,
                    collapseWhitespace: true,
                    conservativeCollapse: true,
                    removeAttributeQuotes: true,
                    removeCommentsFromCDATA: true,
                    removeEmptyAttributes: true,
                    removeOptionalTags: true,
                    removeRedundantAttributes: true,
                    useShortDoctype: true
                },
                files: [{
                    expand: true,
                    cwd: '<%= config.dist %>',
                    src: '{,*/}*.html',
                    dest: '<%= config.dist %>'
                }]
            }
        },

//    By default, your `index.html`'s <!-- Usemin block --> will take care
//    of minification. These next options are pre-configured if you do not
//    wish to use the Usemin blocks.
        cssmin: {
            dist: {
                files: {
                    '<%= config.dist %>/styles/main.css': [
                        '.tmp/css/{,*/**/}*.css',
                        '<%= config.app %>/scripts/{,*/**/}*.css',
                        '<%= config.app %>/styles/app.css'
                    ]
                }
            }
        },
        uglify: {
            dist: {
                files: {
                    '<%= config.dist %>/scripts/scripts.js': [
                        '<%= config.dist %>/scripts/scripts.js'
                    ]
                }
            }
        },
        replace: {
            cssReplace: {
                src: ['<%= config.app %>/scripts/{,*/**/}*.css',
                    '<%= config.app %>/styles/{,*/**/}*.css'],
                overwrite: true,
                replacements: [
                    {
                        from: '../../../images/',
                        to: '../images/'
                    },
                    {
                        from: '../../images/',
                        to: '../images/'
                    },
                    {
                        from: '../../../fonts/',
                        to: '../fonts/'
                    },
                    {
                        from: '../../fonts/',
                        to: '../fonts/'
                    },
                    {
                        from: '../../../styles/images/',
                        to: 'images/'
                    }
                ]
            },
        },
        concat: {
            dist: {
                options: {
                    separator: ';\n'
                },
                src: ['<%= config.app %>/scripts/{,*/**/}*.js'],
                dest: '.tmp/concat/scripts/scripts.js'
            },
            generated: {
                options: {
                    separator: '\n'
                }
            }
        },

        // Copies remaining files to places other tasks can use
        copy: {
            dist: {
                files: [{
                    expand: true,
                    dot: true,
                    cwd: '<%= config.app %>',
                    dest: '<%= config.dist %>',
                    src: [
                        '*.{ico,png,txt}',
                        '.htaccess',
                        // 'bower_components/**/*',
                        'styles/images/**/*',
                        'styles/fonts/**/*',
                        'languages/**/*',
                        'index.html'
                    ]
                }, {
                    src: 'node_modules/apache-server-configs/dist/.htaccess',
                    dest: '<%= config.dist %>/.htaccess'
                },
                    {
                        expand: true,
                        cwd: '.tmp/images',
                        dest: '<%= config.dist %>/images',
                        src: ['generated/*']
                    },
                    //TODO to remove this section after integration onboard finished
                    {
                        expand: true,
                        cwd: '<%= config.app %>/third-party',
                        dest: '<%= config.dist %>/third-party',
                        src: ['onboard_bundle_full.js']
                    },
                    {
                        expand: true,
                        dest: '<%= config.dist %>',
                        src: [
                            'package.json'
                        ]
                    }
                ]
            },
            styles: {
                expand: true,
                cwd: '<%= config.app %>/styles',
                dest: '.tmp/css/',
                src: '{,*/**/}*.css'
            }
        },

        // Run some tasks in parallel to speed up build process
        concurrent: {
            server: ['copy:styles'],
            test: ['copy:styles'],
            dist: [
                'copy:styles',
                'imagemin'
            ]
        },

        //   Test settings
        karma: {
            dev: {
                configFile: "tests/karma.unit.conf.js",
                singleRun: true,
                options: {
                    browsers: ['Chrome'],
                    coverageReporter: {
                        type: 'html',
                        dir: 'tests/Coverage'
                    }
                }
            },
            debug: {
                configFile: "tests/karma.unit.conf.js",
                singleRun: false,
                //comment out this line if you want to cancel the watch and see the UT log
                background: true,
                options: {
                    browsers: ['Chrome'],
                    reporters: [
                        'junit',
                        'dots',
                        'progress'
                    ]
                }
            },
            jenkins: {
                configFile: "tests/karma.unit.conf.js",
                singleRun: true,
                options: {
                    browsers: ['PhantomJS'],
                    coverageReporter: {
                        type: 'text-summary',
                        dir: 'tests/Coverage',
                        file: 'coverage.txt'
                    }
                }
            }
        },

        tslint: {
            options: {
                configuration: 'tslint.json'
            },
            files: {
                src: ['<%= config.app %>/**/*.ts']
            }
        }
    });

    grunt.registerTask('serve', 'start the server and preview your app, --allow-remote for remote access', function (target) {

        var env = grunt.option('env');

        if (grunt.option('allow-remote')) {
            grunt.config.set('connect.options.hostname', '0.0.0.0');
        }
        if (target === 'dist') {
            return grunt.task.run(['build', 'connect:dist:keepalive']);
        }


        if (env === 'mock') {
            grunt.task.run([
                'express:mock',
                'clean:generated',
                'ts:all',
                'ngtemplates:app',
                'injector',
                'less:all',
                'ngconstant',
                'wiredep',
                'concurrent:server',
                'autoprefixer',
                'connect:livereload',
                'watch:html',
                'watch:less'
            ]);
        }

        grunt.task.run([
            'clean:generated',
            'ts:all',
            'ngtemplates:app',
            'injector',
            'less:all',
            'ngconstant',
            'wiredep',
            'concurrent:server',
            'autoprefixer',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('build', [
        'clean:generated',
        'less:all',
        'ts:all',
        'ngconstant',
        'ngtemplates:app',
        'wiredep',
        'replace',
        'clean:dist',
        'useminPrepare:sdc',
        'concurrent:dist',
        'autoprefixer:dist',
        'concat',
        'copy:dist',
        'cssmin',
        'uglify',
        'rev',
        'usemin'
    ]);

    grunt.registerTask("test", function (target) {

        if (!(target === 'debug' || target === 'dev' || target === 'jenkins')) {
            throw new Error("target available for test are <dev|debug|jenkins>");
        }
        var tasks = [
            // "tslint:karma",
            // "ngconstant",
            ////  "concurrent:test",
            // "servicesIconConstants",
            // "autoprefixer",
            // "ngtemplates:testsTemplates",
            // "connect:test"
        ];

        tasks.push('karma:' + target);
        if (target === 'debug') {
            if (grunt.config.get('watch.ts')) {
                tasks.push("watch:ts");
            } else {
                throw new Error("target watch:ts is not available, verify that it exists in your Gruntfile");
            }
        }
        grunt.task.run(tasks);
    });


    var lessSingleTask = function (filePath) {
        var lessSingleFiles = [{
            expand: true,
            src: [filePath.replace(/\\/g, '/')],
            ext: '.css'
        }];
        grunt.config('less.single.files', lessSingleFiles);
        grunt.config('watch.less.tasks', 'less:single');

    };

    var tsSingleTask = function (filePath) {

        var tsSingleData = {
            src: [filePath.replace(/\\/g, '/')]
        };
        // grunt.config('ts.single', tsSingleData);

    };

    var singleTaskByTaskName = {
        //less: lessSingleTask,
        ts: tsSingleTask
    };

    var onGruntWatchEvent = function (action, filepath, target) {
        if (singleTaskByTaskName[target]) {
            singleTaskByTaskName[target].call(undefined, filepath);
        }
    };
    grunt.event.on('watch', onGruntWatchEvent);
};

const path = require('path');
const webpack = require('webpack');
const ProgressPlugin = require('webpack/lib/ProgressPlugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const autoprefixer = require('autoprefixer');
const postcssUrl = require('postcss-url');
const {GlobCopyWebpackPlugin, BaseHrefWebpackPlugin} = require('@angular/cli/plugins/webpack');
const {CommonsChunkPlugin} = require('webpack').optimize;
const {AotPlugin, AngularCompilerPlugin} = require('@ngtools/webpack');
var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const nodeModules = path.join(process.cwd(), 'node_modules');

const bundledScripts = [
    "script-loader!./node_modules/jquery/dist/jquery.min.js",
    "script-loader!./node_modules/lodash/lodash.min.js",
    "script-loader!./node_modules/jqueryui/jquery-ui.min.js",
    "script-loader!./node_modules/cytoscape/dist/cytoscape.min.js",
    "script-loader!./node_modules/perfect-scrollbar/dist/js/perfect-scrollbar.jquery.min.js",
    "script-loader!./node_modules/qtip2/dist/jquery.qtip.min.js",
    "script-loader!./node_modules/@bardit/cytoscape-qtip/cytoscape-qtip.js",
    "script-loader!./node_modules/js-md5/build/md5.min.js"
];
const baseHref = undefined;
const deployUrl = undefined;

// Arguments pass from webpack
const prod = process.argv.indexOf('-p') !== -1;

module.exports = function(params) {

    const webpackCommonConfig = {
        resolve: {
            extensions: [
                ".ts",
                ".js",
                ".less"
            ],
            modules: [
                "./node_modules"
            ],
            alias: {
                directives: path.join(__dirname, 'app/directives/'),
                // onap-ui-angular's package.json "main" is dist/index.umd.js, and there is no
                // index.umd.metadata.json beside it — only dist/index.metadata.json. AOT reads the
                // resolved file's adjacent metadata, so via the UMD entry every exported NgModule
                // looks un-annotated ("Please add a @NgModule annotation"). Point at the ESM entry,
                // which does have metadata next to it.
                'onap-ui-angular$': path.join(nodeModules, 'onap-ui-angular/dist/index.js'),
            }
        },
        resolveLoader: {
            modules: [
                "./node_modules"
            ]
        },
        entry: {
            'scripts/main': [ './src/main.ts' ],
            'scripts/polyfills': [ './src/polyfills.ts' ],
            'scripts/vendor': bundledScripts,
            'scripts/styles': [ "./src/styles.less" ]
        },
        module: {
            rules: [
                {
                    enforce: "pre",
                    test: /\.js$/,
                    loader: "source-map-loader",
                    // The ngfactory/ngstyle exclusions are for AOT: it synthesises those files
                    // without .map siblings, and source-map-loader warns once per file — 320 lines
                    // of noise in the build log, which is where a reviewer looks for real problems.
                    exclude: [ path.join(__dirname, 'node_modules'), /\.(ngfactory|ngstyle)\.js$/ ]
                },
                { test: /\.json$/, loader: "json-loader" },
                { test: /\.html$/, loader: "html-loader" },
                {
                    exclude: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.css$/,
                    loaders: [
                        "exports-loader?module.exports.toString()",
                        "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                        "postcss-loader"
                    ]
                },
                {
                    exclude: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.scss$|\.sass$/,
                    loaders: [
                        "exports-loader?module.exports.toString()",
                        "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                        "postcss-loader",
                        "sass-loader"
                    ]
                },
                {
                    exclude: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.less$/,
                    loaders: [
                        "exports-loader?module.exports.toString()",
                        "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                        "postcss-loader",
                        "less-loader"
                    ]
                },
                {
                    exclude: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.styl$/,
                    loaders: [
                        "exports-loader?module.exports.toString()",
                        "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                        "postcss-loader",
                        "stylus-loader?{\"sourceMap\":false,\"paths\":[]}"
                    ]
                },
                {
                    include: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.css$/,
                    loaders: ExtractTextPlugin.extract({
                        use: [
                            "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                            "postcss-loader"
                        ],
                        fallback: "style-loader",
                        publicPath: ""
                    })
                },
                {
                    include: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.scss$|\.sass$/,
                    loaders: ExtractTextPlugin.extract({
                        use: [
                            "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                            "postcss-loader",
                            "sass-loader"
                        ],
                        fallback: "style-loader",
                        publicPath: ""
                    })
                },
                {
                    include: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.less$/,
                    loaders: ExtractTextPlugin.extract({
                        use: [
                            "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                            "postcss-loader",
                            "less-loader"
                        ],
                        fallback: "style-loader",
                        publicPath: ""
                    })
                },
                {
                    include: [ path.join(process.cwd(), "src/styles.less") ],
                    test: /\.styl$/,
                    loaders: ExtractTextPlugin.extract({
                        use: [
                            "css-loader?{\"sourceMap\":false,\"importLoaders\":1}",
                            "postcss-loader",
                            "stylus-loader?{\"sourceMap\":false,\"paths\":[]}"
                        ],
                        fallback: "style-loader",
                        publicPath: ""
                    })
                },
                { test: /\.ts$/, loader: "@ngtools/webpack" }
            ]
        },
        plugins: [
            new CleanWebpackPlugin(['dist', 'build'], {
                root: path.join(__dirname, ''),
                verbose: true,
                dry: false,
                exclude: ['shared.js']
            }),
            new webpack.LoaderOptionsPlugin({
                debug: false
            }),
            new webpack.DefinePlugin({
                process: {
                    env: {
                        sdcConfig: prod? '"production"': '"development"'
                    }
                }
            }),
            new webpack.NoEmitOnErrorsPlugin(),
            new ProgressPlugin(),
            // new BundleAnalyzerPlugin(),
            // new HtmlWebpackPlugin({
            //     template: "./src/index.html",
            //     filename: "./index.html",
            //     hash: false,
            //     inject: true,
            //     compile: true,
            //     favicon: false,
            //     minify: false,
            //     cache: true,
            //     showErrors: true,
            //     chunks: "all",
            //     excludeChunks: [],
            //     title: "Webpack App",
            //     xhtml: true,
            //     chunksSortMode: function sort(left, right) {
            //         let paramsString = params.entryPoints + '';
            //         let leftString = left.names[0].replace('scripts/','');
            //         let rightString = right.names[0].replace('scripts/','');
            //         let leftIndex = paramsString.indexOf(leftString);
            //         let rightindex = paramsString.indexOf(rightString);
            //         //console.log("left: " + leftString + " | leftIndex: " + leftIndex);
            //         //console.log("right: " + rightString + " | rightindex: " + rightindex);
            //         //console.log("result: " + leftIndex-rightindex);
            //         //console.log("----------------------------------------");
            //         return leftIndex-rightindex;
            //     }
            // }),
            new GlobCopyWebpackPlugin({
                patterns: [
                    "assets/preloading.css",
                    "assets/languages",
                    "assets/styles/fonts",
                    "assets/styles/images",
                    "assets/styles/app.css"
                ],
                globOptions: {
                    cwd: path.join(process.cwd(), "src"),
                    dot: true,
                    ignore: "**/.gitkeep"
                }
            }),
            new GlobCopyWebpackPlugin({
                patterns: [
                    "configurations"
                ],
                globOptions: {
                    cwd: path.join(process.cwd(), ""),
                    dot: true,
                    ignore: "**/.gitkeep"
                }
            }),
            new BaseHrefWebpackPlugin({}),
            new CommonsChunkPlugin({
                name: "scripts/inline",
                minChunks: null
            }),
            new CommonsChunkPlugin({
                name: "scripts/vendor",
                minChunks: (module) => module.resource && module.resource.startsWith(nodeModules),
                chunks: [
                    "main"
                ]
            }),
            new ExtractTextPlugin({
                filename: "[name].bundle.css",
                disable: true
            }),
            new webpack.LoaderOptionsPlugin({
                sourceMap: false,
                options: {
                    postcss: [
                        autoprefixer(),
                        postcssUrl({
                            url: (URL) => {
                                // Only convert absolute URLs, which CSS-Loader won't process into require().
                                if (!URL.startsWith('/')) {
                                    return URL;
                                }
                                // Join together base-href, deploy-url and the original URL.
                                // Also dedupe multiple slashes into single ones.
                                return `/${baseHref || ''}/${deployUrl || ''}/${URL}`.replace(/\/\/+/g, '/');
                            }
                        })
                    ],
                    sassLoader: {
                        sourceMap: false,
                        includePaths: []
                    },
                    lessLoader: {
                        sourceMap: false
                    },
                    context: ""
                }
            }),
            // AOT is production-only on purpose. AngularCompilerPlugin with
            // skipCodeGeneration:false generates a factory per component and type-checks every
            // template on each rebuild, which is far too slow for the dev server's HMR loop.
            // webpack.config.js sets no `aot` key, so the dev server keeps the JIT plugin.
            params.aot
                ? new AngularCompilerPlugin({
                    mainPath: "main.ts",
                    exclude: ["**/*.spec.ts"],
                    tsConfigPath: "src/tsconfig.aot.json",
                    skipCodeGeneration: false,
                    // Swaps environment.ts for environment.prod.ts *inside the TypeScript compiler
                    // host*, which is the only place that works. A NormalModuleReplacementPlugin
                    // rewrites the webpack module request, and by then AOT has already read
                    // `environment.production` off the AST and folded the literal into
                    // AppModuleNgFactory — silently shipping the dev value.
                    hostReplacementPaths: {
                        [path.join(__dirname, 'src/environments/environment.ts')]:
                            path.join(__dirname, 'src/environments/environment.prod.ts')
                    }
                })
                : new AotPlugin({
                    mainPath: "main.ts",
                    exclude: ["**/*.spec.ts"],
                    tsConfigPath: "src/tsconfig.json",
                    skipCodeGeneration: true
                })
        ],
        node: {
            fs: "empty",
            global: true,
            crypto: "empty",
            tls: "empty",
            net: "empty",
            process: true,
            module: false,
            clearImmediate: false,
            setImmediate: false
        }
    }

    return webpackCommonConfig;
}

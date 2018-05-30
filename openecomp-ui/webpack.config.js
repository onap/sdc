'use strict';

const path = require('path');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const { DefinePlugin, HotModuleReplacementPlugin } = require('webpack');

const devConfig = require('./tools/getDevConfig');
const proxyServer = require('./proxy-server');
const fs = require('fs');

let DEV = process.argv[1].indexOf('webpack-dev-server') >= 0;
let devPort = process.env.PORT || devConfig.port;
let publicPath = 'http://localhost:' + devPort + '/onboarding/';

module.exports = env => {
    let language = null;
    if (
        env === undefined ||
        env.language === undefined ||
        env.language === ''
    ) {
        console.log('Setting language to default "en".');
        language = 'en';
    } else {
        language = env.language;
        console.log('Setting language to  "' + env.language + '".');
    }

    var webpackConfig = {
        entry: {
            'punch-outs': ['sdc-app/punch-outs.js']
        },
        mode: DEV ? 'development' : 'production',
        cache: true,
        devtool: DEV ? 'eval-source-map' : undefined,
        performance: { hints: false },
        resolve: {
            modules: [path.resolve('.'), path.join(__dirname, 'node_modules')],
            alias: {
                i18nJson: 'nfvo-utils/i18n/' + language + '.json',
                'nfvo-utils': 'src/nfvo-utils',
                'nfvo-components': 'src/nfvo-components',
                'sdc-app': 'src/sdc-app',
                // TODO - this is needed for heatValidation standalone. Can be deprecated down the line
                'react-select/dist/': 'node_modules' + '/react-select/dist/'
            }
        },
        output: {
            path: path.join(__dirname, 'dist'),
            publicPath: DEV ? publicPath : '/onboarding/',
            filename: DEV ? '[name]_.js' : '[name]_' + language + '.js'
        },
        module: {
            rules: [
                {
                    enforce: 'pre',
                    test: /\.(js|jsx)$/,
                    include: path.resolve(__dirname, 'src'),
                    use: [{ loader: 'eslint-loader' }]
                },
                {
                    test: /\.(js|jsx)$/,
                    include: path.resolve(__dirname, 'src'),
                    use: [{ loader: 'babel-loader' }]
                },
                {
                    test: /\.(js|jsx)$/,
                    loader: 'source-map-loader',
                    include: path.resolve(__dirname, 'src'),
                    enforce: 'pre'
                },
                {
                    test: /\.(css|scss)$/,
                    use: [
                        {
                            loader: 'style-loader'
                        },
                        {
                            loader: 'css-loader'
                        },
                        {
                            loader: 'sass-loader',
                            options: {
                                output: { path: path.join(__dirname, 'dist') }
                            }
                        }
                    ],
                    include: [
                        /resources/,
                        path.join(
                            __dirname,
                            'node_modules/dox-sequence-diagram-ui/'
                        ),
                        path.join(
                            __dirname,
                            'node_modules/react-datepicker/'
                        ),
                        path.join(
                            __dirname,
                            'node_modules/react-select/'
                        ),
                        path.join(
                            __dirname,
                            'node_modules/sdc-ui/'
                        )
                    ]
                },
                {
                    test: /\.(svg)(\?.*)?$/,
                    loader: 'url-loader',
                    options: {
                        limit: 16384,
                        mimetype: 'image/svg+xml'
                    },
                    include: [
                        path.join(
                            __dirname,
                            'node_modules/dox-sequence-diagram-ui/'
                        ),
                        path.join(
                            __dirname,
                            'node_modules/sdc-ui/'
                        )
                    ]
                }
            ]
        },
        plugins: DEV
            ? [
                  new CleanWebpackPlugin(['dist'], { watch: false }),
                  new DefinePlugin({
                      DEBUG: DEV === true,
                      DEV: DEV === true
                  })
              ]
            : [
                  new DefinePlugin({
                      DEBUG: DEV === true,
                      DEV: DEV === true
                  })
              ]
    };
    if (DEV) {
        webpackConfig.plugins.push(new HotModuleReplacementPlugin());
        webpackConfig.entry['punch-outs'].push('react-hot-loader/patch');
        webpackConfig.entry['punch-outs'].push(
            'webpack-dev-server/client?http://localhost:' + devPort
        );
        webpackConfig.entry['punch-outs'].push('webpack/hot/only-dev-server');
        webpackConfig.devServer = {
            port: devPort,
            historyApiFallback: true,
            publicPath: publicPath,
            contentBase: path.join(__dirname, 'dist'),
            inline: true,
            hot: true,
            stats: {
                colors: true,
                exclude: [path.join(__dirname, 'node_modules')]
            },
            before: proxyServer
        };
    }
    console.log('Running build for : ' + webpackConfig.mode);
    return webpackConfig;
};

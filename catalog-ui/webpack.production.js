'use strict';

const path = require('path');
const merge = require('webpack-merge');
const webpack = require('webpack');
const ServerConfig = require('./webpack.server');
const webpackCommonConfig = require('./webpack.common');
const {GlobCopyWebpackPlugin, BaseHrefWebpackPlugin} = require('@angular/cli/plugins/webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');
var CompressionPlugin = require('compression-webpack-plugin');
var currentTime = new Date().getTime();

const params = {};

const webpackProdConfig = {
    module: {
        rules: [
            {test: /\.(eot|svg)$/, loader: "file-loader?name=/scripts/fonts/[name].[hash:20].[ext]"},
            {
                test: /\.(jpg|png|gif|otf|ttf|woff|woff2|cur|ani)$/,
                loader: "url-loader?name=/scripts/images/[name].[hash:20].[ext]&limit=10000"
    }
        ]
    },
    output: {
        path: path.join(process.cwd(), "dist"),
        filename: "[name]." + currentTime + ".bundle.js",
        chunkFilename: "[id].chunk.js",
        publicPath: "/sdc1"
    },
    plugins: [
        new webpack.DefinePlugin({
            __DEBUG__: JSON.stringify(false),
            __ENV__: JSON.stringify('prod')
        }),

        new CopyWebpackPlugin([
            {
                from: './src/index.html', transform: function (content, path) {
                content = (content + '').replace(/\.bundle.js/g, '.' + currentTime + '.bundle.jsgz');

                return content;
            }
            }
        ]),
        new webpack.optimize.UglifyJsPlugin({
            beautify: false,
            mangle: {
                screw_ie8: true,
                keep_fnames: true
            },
            compress: {
                warnings: false,
                screw_ie8: true
            },
            comments: false
        }),
        new webpack.optimize.AggressiveMergingPlugin(),//Merge chunks
        new CompressionPlugin({
            asset: "[path]gz",
            algorithm: "gzip",
            test: /\.js$|\.css$|\.html$/
        })
    ]
};

module.exports = merge(webpackProdConfig, webpackCommonConfig(params));

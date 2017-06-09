'use strict';

const path = require('path');
const merge = require('webpack-merge');
const webpack = require('webpack');
const ServerConfig = require('./webpack.server');
const webpackCommonConfig = require('./webpack.common');
const { BaseHrefWebpackPlugin} = require('@angular/cli/plugins/webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

// Print server configuration
//process.stdout.write('webpack.server: ' + JSON.stringify(ServerConfig) + '\n');
//process.stdout.write('webpack.common: ' + JSON.stringify(webpackCommonConfig) + '\n');
const params = {
    // entryPoints: [
    //     '/scripts/inline',
    //     '/scripts/polyfills',
    //     '/scripts/vendor',
    //     '/scripts/main',
    //     '/scripts/sw-register',
    //     '/scripts/scripts',
    //     '/scripts/styles'
    // ]
};

module.exports = function(env) {

    const webpackDevConfig = {
        devtool: "source-map",
        devServer: ServerConfig(env),
        module: {
            rules: [
                { test: /\.(eot|svg)$/, loader: "file-loader?name=scripts/fonts/[name].[hash:20].[ext]" },
                { test: /\.(jpg|png|gif|otf|ttf|woff|woff2|cur|ani)$/, loader: "url-loader?name=scripts/images/[name].[hash:20].[ext]&limit=10000" }
            ]
        },
        output: {
            path: path.join(process.cwd(), "dist"),
            filename: "[name].bundle.js",
            chunkFilename: "[id].chunk.js"
            //publicPath: "/"
        },
        plugins: [
            // Replace /sdc1 inside index.html with '' (because /sdc1 is used only in production).
            new CopyWebpackPlugin([
                { 
                    from: './src/index.html', transform: function(content, path) {
                        content = (content+'').replace(/\/sdc1/g,'');
                        return content;
                    } 
                }  
            ]),
            new webpack.DefinePlugin({
                __DEBUG__: JSON.stringify(true),
                __ENV__: JSON.stringify('dev'),
                __HMR__: JSON.stringify('HMR')
            }),
            new webpack.HotModuleReplacementPlugin()
        ]

    };

    return merge(webpackDevConfig, webpackCommonConfig(params));
}
'use strict';

let path = require('path');
let webpack = require('webpack');

let webpackCommon = require('./webpack.common');

let webpackDevConfig = Object.assign({}, webpackCommon, {
	devtool: undefined,
	cache: true,
	output: {
		path: path.join(__dirname, 'dist'),
		publicPath: '/onboarding/',
		filename: '[name].js'
	},
	resolveLoader: {
		modules: [path.join(__dirname, 'node_modules'), path.resolve('.')],
		alias: {
			'config-json-loader': 'tools/webpack/config-json-loader/index.js'
		}
	},
	plugins: [
		new webpack.DefinePlugin({
			'process.env': {
				// This has effect on the react lib size
				'NODE_ENV': JSON.stringify('production')
			},
			DEBUG: false,
			DEV: false
		}),
		new webpack.optimize.UglifyJsPlugin(),
		new webpack.LoaderOptionsPlugin({
			options: {
				eslint: {
					configFile: './.eslintrc',
					emitError: true,
					emitWarning: true,
					failOnError: true
				}
			}
		})
	]
});

module.exports = webpackDevConfig;

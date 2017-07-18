'use strict';

let path = require('path');
let webpack = require('webpack');

let cloneDeep = require('lodash/cloneDeep');
let assign = require('lodash/assign');
let webpackCommon = require('./webpack.common');

// copying the common config
let webpackProdConfig = cloneDeep(webpackCommon);
// setting production settings
assign( webpackProdConfig, {
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

webpackProdConfig.module.rules = webpackProdConfig.module.rules.filter(rule => ((rule.enforce !== 'pre') || (rule.enforce === 'pre' && rule.loader !== 'source-map-loader')));
webpackProdConfig.module.rules.forEach(loader => {
	if (loader.use && loader.use[0].loader === 'style-loader') {
		loader.use = loader.use.map(loaderObj => loaderObj.loader.replace('?sourceMap', ''));
	}
});
webpackProdConfig.module.rules.push({test: /config.json$/, use: [{loader:'config-json-loader'}]});
module.exports = webpackProdConfig;

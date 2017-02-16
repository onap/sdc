/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

'use strict';

var path = require('path');
var webpack = require('webpack');

var localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch(e) {}
var devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);
var devPort = process.env.PORT || devConfig.port;
var latestProgress = 0;

module.exports = {
	devtool: 'eval-source-map',
	entry: {
		bundle: [
			'sdc-app/sdc.app.jsx',
			`webpack-dev-server/client?http://localhost:${devPort}`,
			'webpack/hot/only-dev-server'
		],
		'punch-outs': [
			'sdc-app/punch-outs.js',
			`webpack-dev-server/client?http://localhost:${devPort}`,
			'webpack/hot/only-dev-server'
		],
		'heat-validation': [
			'sdc-app/heatValidation.app.jsx',
			`webpack-dev-server/client?http://localhost:${devPort}`,
			'webpack/hot/only-dev-server'
		]
	},
	resolve: {
		root: [path.resolve('.')],
		alias: {
			i18nJson: 'nfvo-utils/i18n/locale.json',
			'nfvo-utils': 'src/nfvo-utils',
			'nfvo-components': 'src/nfvo-components',
			'sdc-app': 'src/sdc-app'
		}
	},
	output: {
		path: path.join(__dirname, 'dist/dev'),
		publicPath: `http://localhost:${devPort}/onboarding/`,
		filename: '[name].js'
	},
	devServer: {
		port: devPort,
		historyApiFallback: true,
		publicPath: `http://localhost:${devPort}/onboarding/`,
		contentBase: path.join(__dirname, 'dist/dev'),
		hot: true,
		progress: true,
		inline: true,
		debug: true,
		stats: {
			colors: true
		}
	},
	module: {
		preLoaders: [
			{test: /\.(js|jsx)$/, loader: 'source-map-loader', exclude: /node_modules/}
		],
		loaders: [
			{test: /\.(js|jsx)$/, loaders: ['react-hot', 'babel-loader', 'eslint-loader'], exclude: /node_modules/},
			{test: /\.(css|scss)$/, loaders: ['style', 'css?sourceMap', 'sass?sourceMap']},

			// required for font icons
			{test: /\.(woff|woff2)(\?.*)?$/,   loader: 'url-loader?limit=16384&mimetype=application/font-woff' },
			{test: /\.(ttf|eot|otf)(\?.*)?$/,    loader: 'file-loader' },
			{test: /\.(png|jpg|svg)(\?.*)?$/, loader: 'url-loader?limit=16384'},

			{test: /\.json$/, loaders: ['json']},
			{test: /\.html$/, loaders: ['html']}
		]
	},
	eslint: {
		configFile: './.eslintrc',
		emitError: true,
		emitWarning: true
	},
	plugins: [
		new webpack.DefinePlugin({
			DEV: true,
			DEBUG: true
		}),
		new webpack.HotModuleReplacementPlugin(),
		new webpack.ProgressPlugin(function (percentage, msg) {
			if (percentage == 0) {
				latestProgress = 0;
				console.log(); //new line
			}
			var progressVal = (percentage * 100).toFixed(0);
			if (progressVal > latestProgress) {
				latestProgress = progressVal
				//process.stdout.clearLine();
				process.stdout.write(msg + ' ' + progressVal + '%\r');
			}
		})
	]

};

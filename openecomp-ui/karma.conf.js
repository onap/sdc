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

var path = require('path');
var isparta = require('isparta');

module.exports = function (config) {
	config.set({

		browsers: [process.env.JENKINS_HOME ? 'Firefox' : 'Chrome'],

		singleRun: true,

		autoWatchBatchDelay: 50,

		frameworks: ['mocha'],

		files: [
			'tests.webpack.js'
		],

		preprocessors: {
			'tests.webpack.js': ['webpack', 'sourcemap'],
			'src/**/*.jsx': ['coverage']
		},

		reporters: ['progress', 'coverage'],

		coverageReporter: {
			dir: 'dist/coverage/',
			reporters: [
				{type: 'html'},
				{type: 'text-summary'}
			],
			includeAllSources: true,
			instrumenters: {isparta: isparta},
			instrumenter: {
				'**/*.js': 'isparta',
				'**/*.jsx': 'isparta'
			},
			instrumenterOptions: {
				isparta: {
					embedSource: true,
					noAutoWrap: true,
				}
			}
		},

		webpack: {
			devtool: 'inline-source-map',
			resolve: {
				root: [path.resolve('.')],
				alias: {
					i18nJson: 'nfvo-utils/i18n/locale.json',
					'nfvo-utils/RestAPIUtil.js': 'test-utils/MockRest.js',
					'nfvo-utils': 'src/nfvo-utils',
					'nfvo-components': 'src/nfvo-components',
					'sdc-app': 'src/sdc-app'
				}
			},
			module: {
				preLoaders: [
					{test: /\.js$/, exclude: /(src|node_modules)/, loader: 'eslint-loader'},
					{test: /\.(js|jsx)$/, exclude: /(test|test\.js|node_modules)/, loader: 'isparta'}
				],
				loaders: [
					{test: /\.(js|jsx)$/, exclude: /node_modules/, loader: 'babel-loader'},
					{test: /\.json$/, loaders: ['json']},
					{test: /\.(css|scss|png|jpg|svg|ttf|eot|otf|woff|woff2)(\?.*)?$/, loader: 'ignore-loader'},
				]
			},
			eslint: {
				configFile: './.eslintrc',
				emitError: true,
				emitWarning: true,
				failOnError: true
			},
		},

		webpackServer: {
			noInfo: true
		}

	});
};

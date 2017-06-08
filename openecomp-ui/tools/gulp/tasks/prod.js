/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
'use strict';

let gulp, replace, Promise, webpack, webpackProductionConfig;

const supportedLanguages = ['en'];

function start(options) {

	let promises = [buildIndex(options)];
	supportedLanguages.forEach(function (lang) {
		promises.push(bundleJS(options, lang));
	});
	return Promise.all(promises);
}

function bundleJS(options, lang) {
	return new Promise(function (resolve, reject) {
		let prodConfig = webpackProductionConfig;
		prodConfig.resolve.alias.i18nJson = options.outDir + '/i18n/' + lang + '/locale.json';
		prodConfig.output.filename = jsFileByLang(options.outFileName, lang);
		webpack(prodConfig, function (err, stats) {
			console.log('[webpack:build]', stats.toString());
			if (err || stats.hasErrors()) {
				console.log('bundleJS : Failure!!', '\n		-language: ', lang);
				reject(err || stats.toJson().errors);
			}
			else {
				console.log('bundleJS : Done', '\n		-language: ', lang);
				resolve();
			}
		});
	});
}

function buildIndex(options) {

	return new Promise(function (resolve, reject) {

		// gulp.src returns a stream object
		gulp.src(options.outDir + '/index.html')
			.pipe(replace(/\/\/<!--prod:delete-->(.|[\r\n])*?<!--\/prod:delete-->/g, ''))//in script occurrences.
			.pipe(replace(/<!--prod:delete-->(.|[\r\n])*?<!--\/prod:delete-->/g, ''))//out of script occurrences.
			.pipe(replace(/<!--prod:add(-->)?/g, ''))
			.pipe(replace(/\/\/<!--prod:supported-langs-->(.|[\r\n])*?<!--\/prod:supported-langs-->/g, supportedLanguages.map(function (val) {
				return "'" + val + "'";
			}).toString()))
			.pipe(gulp.dest(options.outDir))
			.on('end', function () {
				console.log('buildIndex : Done');
				resolve();
			})
			.on('error', function (e) {
				console.log('buildIndex : Failure!!');
				reject(e);
			});
	});

}

function jsFileByLang(fileName, lang) {
	return fileName.replace(/.js$/, '_' + lang + '.js');
}

/**
 * @param options
 * @param options.outFileName optional <default build>
 */
function prodTask(options) {
	gulp = require('gulp');
	replace = require('gulp-replace');
	Promise = require('bluebird');
	webpack = require('webpack');

	webpackProductionConfig = require('../../../webpack.production');
	webpackProductionConfig.module.rules = webpackProductionConfig.module.rules.filter(rule => ((rule.enforce !== 'pre') || (rule.enforce === 'pre' && rule.loader !== 'source-map-loader')));
	webpackProductionConfig.module.rules.forEach(loader => {
		if (loader.use && loader.use[0].loader === 'style-loader') {
			loader.use = loader.use.map(loaderObj => loaderObj.loader.replace('?sourceMap', ''));
		}
	});


	webpackProductionConfig.module.rules.push({test: /config.json$/, use: [{loader:'config-json-loader'}]});

	return start({
		outFileName: options.outFileName || '[name].js',
		outDir: options.outDir
	});
}

module.exports = prodTask;

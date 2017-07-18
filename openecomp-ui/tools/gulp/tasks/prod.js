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

let gulp, replace, Promise, webpack, webpackProductionConfig,cloneDeep, tap;
let langs = [];

/*
Runs the webpack build.
Will first seach for the resource bundles to see how many languages are supported and then run a build per langauage
 */
function buildWebPackForLanguage(prodConfig, lang) {
	return new Promise(function (resolve, reject) {
		webpack(prodConfig, function (err, stats) {
			console.log('[webpack:build ' + prodConfig.output.filename + ']', stats.toString());
			if (err || stats.hasErrors()) {
				console.log('webpack:build : Failure!! ' + prodConfig.output.filename + ']');
				reject(err || stats.toJson().errors);
			}
			else {
				console.log('webpack:build : Done ' + prodConfig.output.filename + ']');
				resolve();
			}
		});
	});
}
/*
 // this will check in the src directory which language bundles we have and will
 // create the array to that we can run a webpack build per language afterwards
 */
function getSupportedLanguages(options) {
	return new Promise((resolve, reject) => {
		gulp.src(options.i18nBundles)
			.pipe(tap(function(file) {
				let languageStartIndex = file.path.lastIndexOf('i18n') + 5;
				let languageStr =  file.path.indexOf('.json') - languageStartIndex;
				let currentLang = file.path.substr(languageStartIndex, languageStr);
				console.log('Found bundle ' +  file.path + ' for [' + currentLang  + ']');
				langs[currentLang] = file.path;
			}))
			.pipe(gulp.dest(options.outDir))
			.on('end', function () {
				resolve();
			})
			.on('error', function (e) {
				console.log('getLanguages : Failure!!');
				reject(e);
			});
	});
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
	cloneDeep = require('lodash/cloneDeep');
	tap = require('gulp-tap');


	// updating webpack for the production build. no need for sourcemaps in this case.
	webpackProductionConfig = require('../../../webpack.production');

	// get the languages so that we can bulid per language with the correct bundle
	let getLanguages =getSupportedLanguages(options);
	// this will run a webpack build per language
	return getLanguages.then(() => {
		let promises = [];
		for (var lang in langs) {
			let prodConfig = cloneDeep(webpackProductionConfig);
			prodConfig.resolve.alias.i18nJson = langs[lang];
			prodConfig.output.filename = (options.outFileName || '[name].js').replace(/.js$/, '_' + lang + '.js');
			promises.push(buildWebPackForLanguage(prodConfig, lang));
		}
		return Promise.all(promises);
	});

}

module.exports = prodTask;

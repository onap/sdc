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
var gulp = require('gulp');
var fs = require('fs');
var replace = require('gulp-replace');
var clean = require('gulp-clean');
var tap = require('gulp-tap');
/**
 *
 * @param options
 * @param options.outDir
 * @param options.srcDir
 * @param options.i18nBundles - optional. if given will check the that all keys from js are mapped
 *
 */
function i18nTask(options) {

	var i18nJson = {};

	function addWord(expr) {
		var word = expr.substring('i18n(\''.length, expr.length - 1);
		if (word !== '') {
			i18nJson[word] = word;
		}
		return expr;
	}

	let createBundle = new Promise(function(resolve, reject) {
		gulp.src(options.srcDir + '**/*.{js,jsx}', {base: './'})
			.pipe(replace(/i18n\('.*?'/g, addWord))
			.pipe(clean())
			.pipe(gulp.dest('./'))
			.on('end', function () {
				console.log('Retrieved keys from static references.');
				if (options.i18nBundles === undefined) {
					// creating the file from the words saved during the replace
					let outfile = options.outDir + '/bundleForStaticKeys.json';
					fs.writeFile(outfile,JSON.stringify(i18nJson, null, '\t'), function (err) {
						if (err) {
							reject(err);
						}
						else resolve();
					});
					console.log('Bundle with static keys was created under: ' + outfile);
				}
				resolve();
			}).on('error', function (err) {
			reject(err);
		});
	});


	if (options.i18nBundles === undefined) {
		return createBundle;
	} else {
		return createBundle.then(() => {
			new Promise(function (resolve, reject) {
				gulp.src(options.i18nBundles)
					.pipe(tap(function (file) {
						console.log('Checking against bundle: ' + file.path);
						let bundle = JSON.parse(file.contents.toString());
						for (entry in i18nJson) {
							if (!bundle[entry]) {
								console.log('Missing Key: ' + entry);
							} else {
								delete bundle[entry];
							}
						}
						for (entry in bundle) {
							console.log('Unused in static files: ' + entry);
						}
					}))
					.pipe(gulp.dest('./'))
					.on('end', function () {
						console.log('done');
					}).on('error', function (err) {
					reject(err);
				});
			});
		});
	}
}

module.exports = i18nTask;

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

var gulp = require('gulp');
var fs = require('fs');
var replace = require('gulp-replace');
var clean = require('gulp-clean');
var mkdirp = require('mkdirp');

/**
 *
 * @param options.outputPath
 * @param options.localesPath
 * @param options.lang = options.lang
 *
 * @returns {string}
 */
function composeLocalesDirPath(options) {
	return options.outputPath + options.localesPath + options.lang;
}

/**
 *
 * @param options.outputPath
 * @param options.localesPath
 * @param options.lang
 *
 * @returns {string}
 */
function composeLocaleFilePath(options) {
	return composeLocalesDirPath(options) + '/locale.json';
}


/**
 * @param options
 * @param options.outputPath
 * @param options.localesPath
 * @param options.lang = options.lang
 *
 */
function ensureLocalesDir(options) {

	return new Promise(function (resolve, reject) {
		mkdirp(composeLocalesDirPath(options), function (err) {
			if (err) {
				reject(err);
			}
			else {
				resolve();
			}
		});
	});

}

/**
 *
 * @param options
 * @param options.outputPath
 * @param options.localesPath
 * @param options.lang = options.lang
 *
 */
function i18nTask(options) {

	var i18nJson = {};

	function addWord(expr) {
		var word = expr.substring('i18n(\''.length, expr.length - 1);
		i18nJson[word] = word;
		return expr;
	}

	return ensureLocalesDir(options).then(function () {
		return new Promise(function(resolve, reject) {
			gulp.src(options.outputPath + '**/*.js', {base: './'})
				.pipe(replace(/i18n\('.*?'/g, addWord))
				.pipe(clean())
				.pipe(gulp.dest('./'))
				.on('end', function () {

					var i18nJsonWrapper = { dataWrapperArr: ["I18N_IDENTIFIER_START", i18nJson, "I18N_IDENTIFIER_END"] , i18nDataIdx: 1};

					fs.writeFile(composeLocaleFilePath(options), JSON.stringify(i18nJsonWrapper), function (err) {
						if (err) {
							reject(err);
						}
						else resolve();
					});
				}).on('error', function (err) {
					reject(err);
				});
		});
	});
}

module.exports = i18nTask;

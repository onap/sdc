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

var gulp, replace, rename, fs, prompt, Promise;

function mergePromptOptions(options) {

	return new Promise(function(resolve, reject) {
		var lang = options.lang;
		var warDir = options.warDir;
		prompt.start();
		prompt.get([
			{
				description: 'Enter war directory',
				default: warDir,
				name: 'warDir'
			},
			{
				description: 'Enter locale.json parent directory name',
				default: lang,
				name: 'lang'
			}
		], function (err, result) {

			if(err) {
				reject(new Error('mergePromptOptions::>\n    ' + err));
				return;
			}

			var warDir = result.warDir;
			var lang = result.lang;

			console.log('\nlocale.json parent directory name> "' + lang + '"');
			console.log('war director>"' + warDir + '"');

			resolve({
				warDir: warDir,
				lang: lang
			});
		});
	});
}

function isBundleExists(path) {
	return new Promise(function(resolve) {
		fs.stat(path, function(err) {
			resolve(null == err);
			/*if null == err then file exists.*/
		});
	});
}

function copyEnglishBundle(enBundlePath, lang) {
	return new Promise(function(resolve, reject) {
		gulp.src(enBundlePath, {base: './'})
			.pipe(rename({basename: 'bundle_' + lang}))
			.pipe(gulp.dest('./'))
			.on('end', function() {
				resolve();
			})
			.on('error', function(err) {
				reject(new Error('copyEnglishBundle::>\n    ' + err));
			});
	});
}

function getLocaleContent(localePath) {

	return new Promise(function(resolve, reject) {
		fs.readFile(localePath, {encoding: 'utf-8'}, function(err,data){
			if(err) {
				reject('getLocaleContent()::>\n    ' + err);
				return;
			}
			resolve(data);
		});
	});

}

function extractLocaleJsonContent(localeDataStr) {

	var localeJsonStrI18nStartIdx = localeDataStr.indexOf('I18N_IDENTIFIER_START');
	var localeJsonStrI18nEndIdx = localeDataStr.indexOf('I18N_IDENTIFIER_END');

	if(-1 === localeJsonStrI18nStartIdx || -1 === localeJsonStrI18nEndIdx) {
		return Promise.reject(new Error('extractLocaleJsonContent::> localeDataStr must contain %I18N_IDENTIFIER_START% and %I18N_IDENTIFIER_END%'));
	}

	var localeJsonStr = localeDataStr.substring(
		localeDataStr.indexOf('{', localeJsonStrI18nStartIdx),
		localeDataStr.lastIndexOf('}', localeJsonStrI18nEndIdx) + 1
	);

	try {
		JSON.parse(localeJsonStr);
	} catch(e) {
		return Promise.reject(new Error('extractLocaleJsonContent::> localeDataStr must contain a valid json between %I18N_IDENTIFIER_START% and %I18N_IDENTIFIER_END%=>' + e));
	}

	return Promise.resolve(localeJsonStr);
}

function setBundleLocaleContent(bundlePath, localeJsonStr) {
	return new Promise(function(resolve, reject) {
		gulp.src(bundlePath, {base: './'})
			.pipe(replace(/I18N_IDENTIFIER_START(.|[\r\n])*?I18N_IDENTIFIER_END/i, function(expr) {
				return expr.substring(0, expr.indexOf('{')) + localeJsonStr + expr.substring(expr.lastIndexOf('}') + 1);
			}))
			.pipe(gulp.dest('./'))
			.on('end', function() {
				resolve();
			})
			.on('error', function(err) {
				reject(new Error('setBundleLocaleContent::>\n    ' + err));
			});
	});
}


function update(options) {

	gulp = require('gulp');
	replace = require('gulp-replace');
	rename = require('gulp-rename');
	fs = require('fs');
	prompt = require('prompt');
	Promise = require('bluebird');

	return mergePromptOptions(options).then(function(mergedOptions) {
		var lang = mergedOptions.lang;
		var warDir = mergedOptions.warDir;

		var bundlePath = warDir + '/js/bundle_' + lang + '.js';
		var localePath = warDir + '/i18n/' + lang + '/locale.json';

		return isBundleExists(bundlePath)
			.then(function(isBundleExist) {
				var englishBundlePath;
				if(!isBundleExist) {
					englishBundlePath = warDir + '/js/bundle_en.js';
					return copyEnglishBundle(englishBundlePath, lang);
				}
			})
			.then(getLocaleContent.bind(null, localePath))
			.then(extractLocaleJsonContent)
			.then(setBundleLocaleContent.bind(null, bundlePath));
	});

}



module.exports = update;

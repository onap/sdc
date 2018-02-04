/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
const request = require('request');
const fs = require('fs');
require('node-zip');

function _request(context, method, path, data, isBinary=false, isVFCall=false) {
	let server = (isVFCall) ? context.vf_server : context.onboarding_server;
	let options = {
		method: method,
		url: server + path,
		headers: context.headers
	};
	return new Promise(function (resolve, reject) {
		if (method === 'POST' || method === 'PUT') {
			if (isBinary) {
				var formData = {
					upload: fs.createReadStream(data),
				};
				options.formData = formData;
			} else {
				options.json = data;
			}
		}
		request(options, function (err, result, data) {
			context.inputData = null;
			if (err) {
				console.error('Request URL: ' + result.request.uri.href);
				console.error('Request Method: ' + result.request.method);
				console.error('Response Status Code: ' +result.statusCode);
				console.log(err);
				reject(err);
			} else {
				let isExpected = (context.shouldFail) ? (result.statusCode != 200 && result.statusCode != 201) : (result.statusCode == 200 || result.statusCode == 201);
				if (!isExpected) {
					console.error('Request URL: ' + result.request.uri.href);
					console.error('Request Method: ' + result.request.method);
					console.error('Response Status Code: ' +result.statusCode);
					console.error(result.body);
					reject('Status Code was ' + result.statusCode);
				}
				if (context.shouldFail && context.errorCode) {
					let errorCode = data.errorCode;
					let contextErrorCode = context.errorCode;
					context.errorCode = null;
					if (errorCode !== contextErrorCode) {
						reject('Error Code was ' + errorCode + ' instead of ' + contextErrorCode);
					}
				}
				if (context.shouldFail) {
					context.shouldFail = false;
					resolve({statusCode: result.statusCode, data: {}});
					return;
				}
				if (method === 'GET' && isBinary) {
					// downloading (NetworkPackage) files
					return ({
						blob: blobUtil.createBlob([data], {type: 'text/plain'}),
						headers: result.headers
					});
				} else {
					if (typeof data === 'string' && data) {
						data = JSON.parse(data);
					}
					context.responseData = data;
					context.inputData = data;
					resolve({statusCode: result.statusCode, data: data});
				}
			}
		});
	});
}

function download(context, path, filePath,  callback){
		let options = {
			method: 'GET',
			url: context.onboarding_server + path,
			headers: context.headers
		};
		var file = fs.createWriteStream(filePath);
		var r = request(options).pipe(file);
		r.on('error', function (err) {
			console.log(err);
			callback(err);
		});
		r.on('finish', function () {
			file.close();
			let zipFile = fs.readFileSync(filePath, 'binary');
			let zip = new JSZip(zipFile, {base64: false, checkCRC32: true});
			if (zip.files['MANIFEST.json']) {
				let manifestData = zip.files['MANIFEST.json']._data;
				manifestData = manifestData.replace(/\\n/g, '');
				context.responseData = JSON.parse(manifestData);
			}
			callback();
		});

};

function _random() {
	let d = new Date();
	return d.getTime().toString().split('').reverse().join('');
}

function _getJSONFromFile(file) {
	return JSON.parse(fs.readFileSync(file, 'utf8'));
}


module.exports = {
	request: _request,
	random : _random,
	getJSONFromFile: _getJSONFromFile,
	download: download
};

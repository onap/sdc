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
const needle = require('needle');
const fs = require('fs');
require('node-zip');
var btoa = require('btoa');
const md5 = require('md5');
const _ = require('lodash');

function getOptionsForRequest(context, method, path, type) {
	if (type == undefined || type == null) {
		type = context.defaultServerType
	}
	let server = context.getUrlForType(type);
	let options = {
		method: method,
		url: server + path,
		headers: _.clone(context.headers[type])
	};
//	options.headers["Content-Type"] = "application/json";
//	options.headers["accept"] = "application/json";
	return options;
}

function _requestBinaryFormData(context, method, path, fileName, formInputName, type) {
	let options = getOptionsForRequest(context, method, path, type);
	let formData = {};
	if (method === 'POST' || method === 'PUT') {
		//formData[formInputName] = fs.createReadStream(fileName);
		//options.formData = formData;
		let fileData =  {
			file: fileName
		};
		fileData['content_type'] = 'multipart/form-data';
		options.formData = {};
		options.formData[formInputName] = fileData;
	}
	return _request(context, method, path, options);
}
function _requestBinaryBody(context, method, path, fileName, type) {
	let options = getOptionsForRequest(context, method, path, type);
	if (method === 'POST' || method === 'PUT') {
		options.body =  fs.createReadStream(fileName);
		options.headers['Content-Type'] = 'application/octet-stream';

	}
	return _request(context, method, path, options);
}


function _requestPayload(context, method, path, filePath, type) {
	let options = getOptionsForRequest(context, method, path, type);
	options.json = _createPayload(filePath);
	options.headers['Content-MD5'] = addCheckSum(options.json);
	return _request(context, method, path, options);
}

function _requestRest(context, method, path, data, type) {
	let options = getOptionsForRequest(context, method, path, type);
	if (method === 'POST' || method === 'PUT') {
		options.json = data;
	}
	return _request(context, method, path, options);
}

function _request(context, method, path, options) {
	console.log('--> Calling REST ' + options.method +' url: ' + options.url);
	let inputData = options.json;
	let needleOptions = {headers: options.headers, rejectUnauthorized: false};
	if (inputData == undefined) {
		if (options.formData != undefined) {
			inputData = options.formData;
			needleOptions.multipart = true;
		}
		if (inputData && inputData.body != undefined) {
			inputData = options.body;
		}
	} else {
		needleOptions.json = true;
	}
	return needle(method, options.url, inputData, needleOptions)
		.then(function(result) {
			context.inputData = null;
			let isExpected = (context.shouldFail) ? (result.statusCode != 200 && result.statusCode != 201) : (result.statusCode == 200 || result.statusCode == 201);
			data = result.body;
			if (!isExpected) {
				console.log('Did not get expected response code');
				throw  'Status Code was ' + result.statusCode ;
			}
			if (context.shouldFail && context.errorCode) {
				if (typeof data === 'string' && data) {
					data = JSON.parse(data);
				}
				let errorCode = data.errorCode;
				let contextErrorCode = context.errorCode;
				context.errorCode = null;
				if (errorCode !== contextErrorCode) {
					throw 'Error Code was ' + errorCode + ' instead of ' + contextErrorCode;
				}
			}
			if (context.shouldFail && context.errorMessage) {
				if (typeof data === 'string' && data) {
					data = JSON.parse(data);
				}
				let errorMessage = data.message;
				let contextErrorMessage = context.errorMessage;
				context.errorMessage = null;
				if (errorMessage !== contextErrorMessage) {
					throw 'Error Message was ' + errorMessage + ' instead of ' + contextErrorMessage;
				}
			}
			if (context.shouldFail) {
				context.shouldFail = false;
				return({statusCode: result.statusCode, data: {}});
			}

			if (typeof data === 'string' && data) {
				if (data.startsWith('[') || data.startsWith('{')) {
					data = JSON.parse(data);
				}
			}
			context.responseData = data;
			context.inputData = data;
			return({statusCode: result.statusCode, data: data});

		})
		.catch(function(err) {
			console.error('Request URL: ' + options.url);
			console.error('Request Method: ' + options.method);
			console.log(err);
			throw err;
		})
}

function download(context, path, filePath,  type) {
	if (type == undefined || type == null) {
		type = context.defaultServerType
	}
	let server = context.getUrlForType(type);
	let options = {
		method: 'GET',
		url: server + path,
		headers: context.headers[type]
	};

	console.log('--> Calling REST download url: ' + options.url);
	return needle('GET', options.url, {}, {
		headers: options.headers,
		rejectUnauthorized: false,
		output: filePath
	})
		.then(function (result) {
			let zipFile = fs.readFileSync(filePath, 'binary');
			let zip = new JSZip(zipFile, {base64: false, checkCRC32: true});
			if (zip.files['MANIFEST.json']) {
				let manifestData = zip.files['MANIFEST.json']._data;
				manifestData = manifestData.replace(/\\n/g, '');
				context.responseData = JSON.parse(manifestData);
			}
			return zip;
		})
		.catch(function (err) {
			console.error('Request URL: ' + options.url);
			console.error('Request Method: ' + options.method);
			throw err;
		})
}

function _random() {
	let d = new Date();
	return d.getTime().toString().split('').reverse().join('');
}

function _getJSONFromFile(file) {
	return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function _createPayload(fileName) {
	var body = fs.readFileSync(fileName);
	let payload = {
		payloadData: body.toString('base64'),
		payloadName: fileName.substring(fileName.lastIndexOf("/") + 1 )
	};
	return payload;
}

function addCheckSum(payloadData) {
	let _md5 = md5(JSON.stringify(payloadData));
	return btoa(_md5.toLowerCase());
}

function _getFile(file, format) {
	if(format === '' ){
		return fs.readFileSync(file)
	}
	return fs.readFileSync(file, format);
}


module.exports = {
	getFile: _getFile,
	request: _requestRest,
	requestPayload: _requestPayload,
	requestBinaryFormData: _requestBinaryFormData,
	requestBinaryBody: _requestBinaryBody,
	random : _random,
	getJSONFromFile: _getJSONFromFile,
	download: download,
	payload: _createPayload
};

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

import _extend from 'lodash/extend.js';
import _clone from 'lodash/clone.js';
import _defaults from 'lodash/defaults.js';
import $ from 'jquery';
import uuid from 'uuid-js';
import md5 from 'md5';

import store from 'sdc-app/AppStore.js';
import {actionTypes as LoaderConstants} from 'nfvo-components/loader/LoaderConstants.js';
import Configuration from 'sdc-app/config/Configuration.js';
import errorResponseHandler from './ErrorResponseHandler.js';

const methodMap = {
	'create': 'POST',
	'update': 'PUT',
	'delete': 'DELETE',
	'read': 'GET'
};
const AUTHORIZATION_HEADER = 'X-AUTH-TOKEN';
const STORAGE_AUTH_KEY = 'sdc-auth-token';
const REQUEST_ID_HEADER = 'X-ECOMP-RequestID';
const CONTENT_MD5_HEADER = 'Content-MD5';
const namedParam = /{(\w+)}/g;
const queryParamsNames = {
	pageStart: 'pageStart',
	pageSize: 'pageSize',
	sortField: 'sortField',
	sortDir: 'sortDir',
	filtering: 'filter'
};


// jQuery binary transport to download files through XHR
// http://www.henryalgus.com/reading-binary-files-using-jquery-ajax/
// https://github.com/henrya/js-jquery/tree/master/BinaryTransport
$.ajaxTransport('+binary', function (options/*, originalOptions , jqXHR*/) {
	// check for conditions and support for blob / arraybuffer response type
	if (window.FormData && ((options.dataType && (options.dataType === 'binary')) ||
		(options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) ||
		(window.Blob && options.data instanceof Blob))))
	) {
		return {
			// create new XMLHttpRequest
			send: function (headers, callback) {
				// setup all variables
				var xhr = new XMLHttpRequest(),
					url = options.url,
					type = options.type,
					async = options.async || true,
				// blob or arraybuffer. Default is blob
					dataType = options.responseType || 'blob',
					data = options.data || null,
					username = options.username || null,
					password = options.password || null;

				xhr.addEventListener('load', function () {
					var data = {};
					data[options.dataType] = xhr.response;
					// make callback and send data
					callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
				});

				xhr.open(type, url, async, username, password);

				// setup custom headers
				for (var i in headers) {
					xhr.setRequestHeader(i, headers[i]);
				}

				xhr.responseType = dataType;
				xhr.send(data);
			},
			abort: function () {
			}
		};
	}
});

$(document).ajaxStart(()=> store.dispatch({type: LoaderConstants.SHOW}));
$(document).ajaxStop(()=> store.dispatch({type: LoaderConstants.HIDE}));

function urlError() {
	throw new Error('A "url" property or function must be specified');
};

export function makeQueryParams(options) {
	var qParams = {};
	if (options.pagination) {
		qParams[queryParamsNames.pageStart] = options.pagination.pageStart;
		qParams[queryParamsNames.pageSize] = options.pagination.pageSize;
	}
	if (options.sorting) {
		qParams[queryParamsNames.sortField] = options.sorting.sortField;
		qParams[queryParamsNames.sortDir] = options.sorting.sortDir;
	}
	if (options.filtering) {
		qParams[queryParamsNames.filtering] = JSON.stringify(options.filtering);
	}

	return _defaults(qParams, options.qParams);
}

function appendQueryParam(p, value) {
	var str = '';

	if (value instanceof Array) {
		if (value.length === 1) {
			str = appendQueryParam(p, value[0]);
		} else if (value.length > 1) {
			str = appendQueryParam(p, value.shift()) + '&' + appendQueryParam(p, value);
		}
	} else {
		str = p + '=' + encodeURIComponent(value);
	}

	return str;
}

function appendQueryString(url, qParams) {
	var str = '';
	for (var param in qParams) {
		str += (str ? '&' : '') + appendQueryParam(param, qParams[param]);
	}
	return url + (str ? '?' : '') + str;
}

function composeURL(baseUrl, options) {
	var url = baseUrl || urlError();
	if (options.url) {
		delete options.url;
	}

	var qParams = makeQueryParams(options);
	url = appendQueryString(url, qParams);

	var matches = url.match(namedParam);
	if (matches) {
		for (var i = 0; i < matches.length; i++) {
			var param = matches[i].substring(1, matches[i].length - 1);
			var value = (options.params && options.params[param]);

			if (value === undefined) {
				value = options[param];
			}
			url = url.replace(matches[i], encodeURIComponent(value));
		}
	}

	return url;
}

function applyMD5Header(options, data) {
	if (options.md5) {
		let headers = options.headers;
		headers[CONTENT_MD5_HEADER] = window.btoa(md5(JSON.stringify(data)).toLowerCase());
	}
}

function applySecurity(options, data) {
	var headers = options.headers || (options.headers = {});

	var authToken = localStorage.getItem(STORAGE_AUTH_KEY);
	if (authToken) {
		headers[AUTHORIZATION_HEADER] = authToken;
	}

	var attApiHeaders = Configuration.get('ATTApiHeaders'),
		attUidHeader = attApiHeaders && attApiHeaders.userId;
	if (attUidHeader) {
		headers[attUidHeader.name] = attUidHeader.value;
	}

	headers[REQUEST_ID_HEADER] = uuid.create().toString();

	applyMD5Header(options, data);
}

function handleResponse(options) {
	var authToken = options.xhr.getResponseHeader(AUTHORIZATION_HEADER);
	var prevToken = options.headers && options.headers[AUTHORIZATION_HEADER];
	if (authToken && authToken !== prevToken) {
		if (authToken === 'null') {
			localStorage.removeItem(STORAGE_AUTH_KEY);
		} else {
			localStorage.setItem(STORAGE_AUTH_KEY, authToken);
		}
	}
}

function sync(baseUrl, method, options, data) {

	options = options ? _clone(options) : {};

	var type = methodMap[method];
	_defaults(options || (options = {}));
	var params = {
		type: type,
		dataType: 'json'
	};
	params.url = composeURL(baseUrl, options);

	if ((method === 'create' || method === 'update') && data instanceof FormData) {
		params.contentType = 'multipart/form-data';
		params.data = data;
	}
	else if (method === 'create' || method === 'update') {
		params.contentType = 'application/json';
		params.data = JSON.stringify(data);
	}

	if (params.type !== 'GET') {
		params.processData = false;
	}
	var success = options.success;
	options.success = function (resp) {
		if (success) {
			handleResponse(options);
			success.call(options.context, _clone(resp), resp, options);
		}
	};

	options.error = options.error || errorResponseHandler;

	if (typeof options.progressCallback === 'function' && options.fileSize) {
		const {fileSize} = options;
		options.xhrFields = {
			// add listener to XMLHTTPRequest object directly for progress (jquery doesn't have this yet)
			onprogress: function (progress) {
				// calculate upload progress
				let percentage = Math.floor((progress.loaded / fileSize) * 100);
				// log upload progress to console
				//console.log('progress', percentage);
				options.progressCallback(percentage);
				if (percentage === 100) {
					console.log('DONE!');
				}
			}
		};
	}

	applySecurity(options, data);

	if (DEBUG) {
		console.log('--> Making REST call (' + type + '): ' + params.url);
	}
	var xhr = options.xhr = $.ajax(_extend(params, options));
	return xhr;
}

export default {

	fetch(baseUrl, options) {
		return sync(baseUrl, 'read', options);
	},

	save(baseUrl, data, options) {
		return sync(baseUrl, 'update', options, data);
	},

	create(baseUrl, data, options) {
		return sync(baseUrl, 'create', options, data);
	},

	destroy(baseUrl, options) {
		return sync(baseUrl, 'delete', options);
	}

};

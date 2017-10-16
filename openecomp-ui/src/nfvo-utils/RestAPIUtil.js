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
import {RestfulAPI} from 'restful-js';
import uuid from 'uuid-js';
import md5 from 'md5';

import store from 'sdc-app/AppStore.js';
import {actionTypes as LoaderConstants} from 'nfvo-components/loader/LoaderConstants.js';
import Configuration from 'sdc-app/config/Configuration.js';
import errorResponseHandler from './ErrorResponseHandler.js';

const AUTHORIZATION_HEADER = 'X-AUTH-TOKEN';
const STORAGE_AUTH_KEY = 'sdc-auth-token';
const REQUEST_ID_HEADER = 'X-ECOMP-RequestID';
const CONTENT_MD5_HEADER = 'Content-MD5';




function applyMD5Header(options, data) {
	if (options.md5) {
		let headers = options.headers;
		headers[CONTENT_MD5_HEADER] = window.btoa(md5(JSON.stringify(data)).toLowerCase());
	}
}

function handleResponse(xhr) {
	let authToken = xhr.getResponseHeader(AUTHORIZATION_HEADER);
	let prevToken = this && this.headers && this.headers[AUTHORIZATION_HEADER];
	if (authToken && authToken !== prevToken) {
		if (authToken === 'null') {
			localStorage.removeItem(STORAGE_AUTH_KEY);
		} else {
			localStorage.setItem(STORAGE_AUTH_KEY, authToken);
		}
	}
}


class RestAPIUtil extends RestfulAPI {

	applySecurity(options, data) {
		let headers = options.headers || (options.headers = {});

		if (options.isAnonymous) {
			return;
		}

		let authToken = localStorage.getItem(STORAGE_AUTH_KEY);
		if (authToken) {
			headers[AUTHORIZATION_HEADER] = authToken;
		}

		let attApiHeaders = Configuration.get('ATTApiHeaders'),
			attUidHeader = attApiHeaders && attApiHeaders.userId;
		if (attUidHeader) {
			headers[attUidHeader.name] = attUidHeader.value;
		}

		headers[REQUEST_ID_HEADER] = uuid.create().toString();
		applyMD5Header(options, data);
	}

	handleRequest(url, type, options = {}, data){
		let success = options.success;
		options.success = function (resp, textStatus, xhr) {
			handleResponse.call(this, xhr);
			if (success) {
				success.call(options.context, {...resp}, textStatus, xhr);
			}
		};

		if (DEBUG) {
			console.log('--> Making REST call (' + type + '): ' + url);
		}
		return super.handleRequest(url, type, options, data);
	}

}

const instance = new RestAPIUtil({
	errorResponseHandler,
	ajaxStartHandler: () => store.dispatch({type: LoaderConstants.SHOW}),
	ajaxStopHandler: () => store.dispatch({type: LoaderConstants.HIDE})
});

// jQuery binary transport to download files through XHR
// http://www.henryalgus.com/reading-binary-files-using-jquery-ajax/
// https://github.com/henrya/js-jquery/tree/master/BinaryTransport
instance.$.ajaxTransport('+binary', function (options/*, originalOptions , jqXHR*/) {
	// check for conditions and support for blob / arraybuffer response type
	if (window.FormData && ((options.dataType && (options.dataType === 'binary')) ||
		(options.data && ((window.ArrayBuffer && options.data instanceof ArrayBuffer) ||
		(window.Blob && options.data instanceof Blob))))
	) {
		return {
			// create new XMLHttpRequest
			send: function (headers, callback) {
				// setup all letiables
				let xhr = new XMLHttpRequest(),
					url = options.url,
					type = options.type,
					async = options.async || true,
					// blob or arraybuffer. Default is blob
					dataType = options.responseType || 'blob',
					data = options.data || null,
					username = options.username || null,
					password = options.password || null;

				xhr.addEventListener('load', function () {
					let data = {};
					data[options.dataType] = xhr.response;
					// make callback and send data
					callback(xhr.status, xhr.statusText, data, xhr.getAllResponseHeaders());
				});

				xhr.open(type, url, async, username, password);

				// setup custom headers
				for (let i in headers) {
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

export default instance;

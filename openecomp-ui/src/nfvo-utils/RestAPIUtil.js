/*
* Copyright © 2018 European Support Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http: //www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import uuid from 'uuid-js';
import md5 from 'md5';
import axios from 'axios';

import store from 'sdc-app/AppStore.js';
import { actionTypes as LoaderConstants } from 'nfvo-components/loader/LoaderConstants.js';
import Configuration from 'sdc-app/config/Configuration.js';
import errorResponseHandler from './ErrorResponseHandler.js';

//methods
const GET = 'GET';
const POST = 'POST';
const PUT = 'PUT';
const DELETE = 'DELETE';
const BINARY = 'binary';

const AUTHORIZATION_HEADER = 'X-AUTH-TOKEN';
const STORAGE_AUTH_KEY = 'sdc-auth-token';
const REQUEST_ID_HEADER = 'X-ECOMP-RequestID';
const CONTENT_MD5_HEADER = 'Content-MD5';

export function applySecurity(options, data) {
    let headers = options.headers || (options.headers = {});
    if (options.isAnonymous) {
        return;
    }

    let authToken = localStorage.getItem(STORAGE_AUTH_KEY);
    if (authToken) {
        headers[AUTHORIZATION_HEADER] = authToken;
    }

    let catalogApiHeaders = Configuration.get('CatalogApiHeaders'),
        catalogUidHeader = catalogApiHeaders && catalogApiHeaders.userId;
    if (catalogUidHeader) {
        headers[catalogUidHeader.name] = catalogUidHeader.value;
    }

    headers[REQUEST_ID_HEADER] = uuid.create().toString();
    if (options.md5) {
        let headers = options.headers;
        headers[CONTENT_MD5_HEADER] = window.btoa(
            md5(JSON.stringify(data)).toLowerCase()
        );
    }
}

function handleSuccess(responseHeaders, requestHeaders) {
    let authToken = responseHeaders[AUTHORIZATION_HEADER];
    let prevToken = requestHeaders && requestHeaders[AUTHORIZATION_HEADER];
    if (authToken && authToken !== prevToken) {
        if (authToken === 'null') {
            localStorage.removeItem(STORAGE_AUTH_KEY);
        } else {
            localStorage.setItem(STORAGE_AUTH_KEY, authToken);
        }
    }
}

class RestAPIUtil {
    handleRequest(url, type, options = {}, data = {}) {
        applySecurity(options, data);

        const config = {
            method: type,
            url: url,
            headers: options.headers,
            data: data
        };

        if (options.validateStatus) {
            config.validateStatus = options.validateStatus;
        }

        if (options.onUploadProgress) {
            config.onUploadProgress = options.onUploadProgress;
        }

        if (!options.noLoading) {
            store.dispatch({ type: LoaderConstants.SEND_REQUEST, url: url });
        }
        if (options.dataType === BINARY) {
            config.responseType = 'arraybuffer';
            return axios(config)
                .then(result => {
                    if (!options.noLoading) {
                        store.dispatch({
                            type: LoaderConstants.RECEIVE_RESPONSE,
                            url: result.config.url
                        });
                    }

                    return {
                        blob: new Blob([result.data]),
                        headers: result.headers
                    };
                })
                .catch(error => {
                    if (!options.noLoading) {
                        store.dispatch({
                            type: LoaderConstants.RECEIVE_RESPONSE,
                            url: error.config.url
                        });
                    }
                    errorResponseHandler(error.response);
                });
        }
        return axios(config)
            .then(result => {
                store.dispatch({
                    type: LoaderConstants.RECEIVE_RESPONSE,
                    url: result.config.url
                });
                handleSuccess(result.headers, result.config.headers);
                return result.data;
            })
            .catch(error => {
                store.dispatch({
                    type: LoaderConstants.RECEIVE_RESPONSE,
                    url: error.config.url
                });
                errorResponseHandler(error.response);
                return Promise.reject({
                    responseJSON: error.response.data
                });
            });
    }

    fetch(url, options) {
        return this.handleRequest(url, GET, options);
    }

    get(url, options) {
        return this.fetch(url, options);
    }

    post(url, data, options) {
        return this.handleRequest(url, POST, options, data);
    }

    put(url, data, options) {
        return this.handleRequest(url, PUT, options, data);
    }

    destroy(url, options) {
        return this.handleRequest(url, DELETE, options);
    }
}

const instance = new RestAPIUtil();

export default instance;

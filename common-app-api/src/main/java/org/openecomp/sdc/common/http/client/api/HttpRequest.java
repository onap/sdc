/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.http.client.api;

import org.apache.http.HttpEntity;
import org.openecomp.sdc.common.http.config.HttpClientConfig;

import java.util.Properties;

//TODO- remove all static and use instance methods for better testing
public abstract class HttpRequest {

    static final  Properties defaultHeaders = null;
    static final HttpClientConfig defaultConfig = new HttpClientConfig();



    private HttpRequest() {
    }

    /*
     * GET response as string
     */
    public static HttpResponse<String> get(String url) throws HttpExecuteException {
        return get(url, defaultHeaders, defaultConfig);
    }

    public static HttpResponse<String> get(String url, Properties headers) throws HttpExecuteException {
        return get(url, headers, defaultConfig);
    }
    
    public static HttpResponse<String> get(String url, HttpClientConfig config) throws HttpExecuteException {
        return get(url, defaultHeaders, config);
    }

    public static HttpResponse<String> get(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().get(url, headers, config);
    }

    /*
     * GET response as byte array
     */
    public static HttpResponse<byte[]> getAsByteArray(String url) throws HttpExecuteException {
        return getAsByteArray(url, defaultHeaders, defaultConfig);
    }

    public static HttpResponse<byte[]> getAsByteArray(String url, Properties headers) throws HttpExecuteException {
        return getAsByteArray(url, headers, defaultConfig);
    }

    public static HttpResponse<byte[]> getAsByteArray(String url, HttpClientConfig config) throws HttpExecuteException {
        return getAsByteArray(url, defaultHeaders, config);
    }

    public static HttpResponse<byte[]> getAsByteArray(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().getAsByteArray(url, headers, config);
    }

    /*
     * PUT
     */
    public static HttpResponse<String> put(String url, HttpEntity entity) throws HttpExecuteException {
        return put(url, defaultHeaders, entity, defaultConfig);
    }

    public static HttpResponse<String> put(String url, Properties headers, HttpEntity entity) throws HttpExecuteException {
        return put(url, headers, entity, defaultConfig);
    }
    
    public static HttpResponse<String> put(String url, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return put(url, defaultHeaders, entity, config);
    }

    public static HttpResponse<String> put(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().put(url, headers, entity, config);
    }

    /*
     * POST
     */
    public static HttpResponse<String> post(String url, HttpEntity entity) throws HttpExecuteException {
        return post(url, defaultHeaders, entity, defaultConfig);
    }

    public static HttpResponse<String> post(String url, Properties headers, HttpEntity entity) throws HttpExecuteException {
        return post(url, headers, entity, defaultConfig);
    }
    
    public static HttpResponse<String> post(String url, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return post(url, defaultHeaders, entity, config);
    }

    public static HttpResponse<String> post(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().post(url, headers, entity, config);
    }
    
    /*
     * PATCH
     */
    public static HttpResponse<String> patch(String url, HttpEntity entity) throws HttpExecuteException {
        return patch(url, defaultHeaders, entity, defaultConfig);
    }

    public static HttpResponse<String> patch(String url, Properties headers, HttpEntity entity) throws HttpExecuteException {
        return patch(url, headers, entity, defaultConfig);
    }
    
    public static HttpResponse<String> patch(String url, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return patch(url, defaultHeaders, entity, config);
    }

    public static HttpResponse<String> patch(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().patch(url, headers, entity, config);
    }
    
    /*
     * DELETE
     */
    public static HttpResponse<String> delete(String url) throws HttpExecuteException {
        return delete(url, defaultHeaders, defaultConfig);
    }

    public static HttpResponse<String> delete(String url, Properties headers) throws HttpExecuteException {
        return delete(url, headers, defaultConfig);
    }
    
    public static HttpResponse<String> delete(String url, HttpClientConfig config) throws HttpExecuteException {
        return delete(url, defaultHeaders, config);
    }

    public static HttpResponse<String> delete(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        return HttpRequestHandler.get().delete(url, headers, config);
    }
    
    public static void destroy() {
        HttpRequestHandler.get().destroy();
    }
}

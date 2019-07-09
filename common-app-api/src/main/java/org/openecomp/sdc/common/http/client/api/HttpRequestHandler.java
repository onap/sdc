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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.FunctionThrows;
import org.openecomp.sdc.common.http.config.HttpClientConfig;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequestHandler {
    private static HttpRequestHandler handlerInstance = new HttpRequestHandler();
    private static final String HTTPS_PREFIX = "https://";
    private static final String HTTP_PREFIX = "http://";
    
    private Map<HttpClientConfigImmutable, HttpClient> clients = new ConcurrentHashMap<>();
    private HttpClientFactory clientFactory;
    
    private FunctionThrows<CloseableHttpResponse, HttpResponse<byte[]>, Exception> byteResponseBuilder = (CloseableHttpResponse httpResponse) -> {
        HttpEntity entity = httpResponse.getEntity();
        byte[] response = null;
        if (entity != null) {
            InputStream content = entity.getContent();
            if (content != null) {
                response = IOUtils.toByteArray(content);
            }
        }
        return new HttpResponse<>(response, 
                httpResponse.getStatusLine().getStatusCode(), 
                httpResponse.getStatusLine().getReasonPhrase());
    };

    private FunctionThrows<CloseableHttpResponse, HttpResponse<String>, Exception> stringResponseBuilder = (CloseableHttpResponse httpResponse) -> {
        HttpEntity entity = httpResponse.getEntity();
        String response = null;
        if (entity != null) {
            response = EntityUtils.toString(entity);
        }
        return new HttpResponse<>(response, 
                httpResponse.getStatusLine().getStatusCode(),
                httpResponse.getStatusLine().getReasonPhrase());
    };

    private HttpRequestHandler() {
        HttpConnectionMngFactory connectionMngFactory = new HttpConnectionMngFactory();
        clientFactory = new HttpClientFactory(connectionMngFactory);
    }
    
    public static HttpRequestHandler get() {
        return handlerInstance;
    }

    public HttpResponse<String> get(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config);
        return client.<String>get(url, headers, stringResponseBuilder);
    }

    public HttpResponse<byte []> getAsByteArray(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config);
        return client.<byte[]>get(url, headers, byteResponseBuilder);
    }

    public HttpResponse<String> put(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config);
        return client.<String>put(url, headers, entity, stringResponseBuilder);
    }

    public HttpResponse<String> post(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config);
        return client.<String>post(url, headers, entity, stringResponseBuilder);
    }

    public HttpResponse<String> patch(String url, Properties headers, HttpEntity entity, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config);
        return client.<String>patch(url, headers, entity, stringResponseBuilder);
    }

    public HttpResponse<String> delete(String url, Properties headers, HttpClientConfig config) throws HttpExecuteException {
        HttpClient client = getOrCreateClient(url, config != null ? config : HttpRequest.defaultConfig);
        return client.<String>delete(url, headers, stringResponseBuilder);
    }
    
    public void destroy() {
        clients.forEach((k, v) -> v.close());
        clients.clear();
    }
    
    private HttpClient getOrCreateClient(String url, HttpClientConfig config) throws HttpExecuteException {
        String protocol = getProtocol(url);
        HttpClientConfigImmutable httpClientConfigImmutable = HttpClientConfigImmutable.getOrCreate(config);
        return clients.computeIfAbsent(httpClientConfigImmutable, k -> createClient(protocol, httpClientConfigImmutable));
    }

    private HttpClient createClient(String protocol, HttpClientConfigImmutable config) {
        return clientFactory.createClient(protocol, config);
    }

    @VisibleForTesting
    public static void setTestInstance(HttpRequestHandler handlerInstance) {
        HttpRequestHandler.handlerInstance = handlerInstance;
    }

    private String getProtocol(String url) throws HttpExecuteException {
        if (url.startsWith(HTTPS_PREFIX)) {
            return Constants.HTTPS;
        }
        else if (url.startsWith(HTTP_PREFIX)) {
            return Constants.HTTP;
        }
        else {
            throw new HttpExecuteException(String.format("Failed to create http client. Requested protocol is not supported \"%s\"", url));
        }        
    }
}

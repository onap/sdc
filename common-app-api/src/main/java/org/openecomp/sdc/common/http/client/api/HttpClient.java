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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.FunctionThrows;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

public class HttpClient {
    private static final Logger logger = Logger.getLogger(HttpClient.class.getName());
    
    private final CloseableHttpClient client;
    private final HttpClientConfigImmutable configImmutable;
    
    public HttpClient(CloseableHttpClient client, HttpClientConfigImmutable configImmutable) {
        this.client = client;
        this.configImmutable = configImmutable; 
    }
    
    public <T> HttpResponse<T> get(String url, Properties headers, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        HttpGet httpGet = new HttpGet(url);
        return execute(httpGet, headers, responseBuilder);
    }

    <T> HttpResponse<T> put(String url, Properties headers, HttpEntity entity, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(entity);
        return execute(httpPut, headers, responseBuilder);
    }

    <T> HttpResponse<T> post(String url, Properties headers, HttpEntity entity, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        return execute(httpPost, headers, responseBuilder);
    }

    <T> HttpResponse<T> patch(String url, Properties headers, HttpEntity entity, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setEntity(entity);
        return execute(httpPatch, headers, responseBuilder);
    }

    <T> HttpResponse<T> delete(String url, Properties headers, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        HttpDelete httpDelete = new HttpDelete(url);
        return execute(httpDelete, headers, responseBuilder);
    }
    
    void close() {
        try {
            client.close();
        }
        catch (IOException e) {
            logger.debug("Close http client failed with exception ", e);
        }
    }
    
    private <T> HttpResponse<T> execute(HttpRequestBase request, Properties headers, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
        if(configImmutable.getHeaders() != null) {
            configImmutable.getHeaders().forEach(request::addHeader);
        }

        if (headers != null) {
            headers.forEach((k, v) -> request.addHeader(k.toString(), v.toString()));
        }

        HttpClientContext httpClientContext = null;
        if(request.getHeaders(HttpHeaders.AUTHORIZATION).length == 0) {
            httpClientContext = createHttpContext(request.getURI());
        }

        logger.debug("Execute request {}", request.getRequestLine());
        try (CloseableHttpResponse response = client.execute(request, httpClientContext)) {
            return responseBuilder.apply(response);
        }
        catch (Exception e) {
            String description = String.format("Execute request %s failed with exception: %s", request.getRequestLine(), e.getMessage()); 
            BeEcompErrorManager.getInstance().logInternalFlowError("ExecuteRestRequest", description, ErrorSeverity.ERROR);
            logger.debug("{}: ",description, e);

            throw new HttpExecuteException(description, e);
        } 
    }

    private HttpClientContext createHttpContext(URI uri) {
        if(StringUtils.isEmpty(configImmutable.getBasicAuthUserName()) || StringUtils.isEmpty(configImmutable.getBasicAuthPassword())) {
            return null;
        }

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        int port = getPort(uri);
        credentialsProvider.setCredentials(new AuthScope(uri.getHost(), port), 
                new UsernamePasswordCredentials(configImmutable.getBasicAuthUserName(), configImmutable.getBasicAuthPassword()));

        HttpClientContext localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(credentialsProvider);

        AuthCache authCache = new BasicAuthCache();
        HttpHost target = new HttpHost(uri.getHost(), port, "https");
        authCache.put(target, (AuthScheme) new BasicScheme());
        localContext.setAuthCache(authCache);

        return localContext;
    }

    private int getPort(URI uri) {
        int port = uri.getPort(); 
        if(port < 0) {
            if(Constants.HTTPS.equals(uri.getScheme())) {
                port = 443;
            }
            else
            if (Constants.HTTP.equals(uri.getScheme())) {
                port = 80;
            }
            else {
                port = AuthScope.ANY_PORT;
                logger.debug("Protocol \"{}\" is not supported, set port to {}", uri.getScheme(), port);
            }
        }
        return port;
    }
}

package org.openecomp.sdc.common.http.client.api;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.FunctionThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    
    private final CloseableHttpClient client;
    private final HttpClientConfigImmutable configImmutable;
    
    HttpClient(CloseableHttpClient client, HttpClientConfigImmutable configImmutable) {
        this.client = client;
        this.configImmutable = configImmutable; 
    }
    
    <T> HttpResponse<T> get(String url, Properties headers, FunctionThrows<CloseableHttpResponse, HttpResponse<T>, Exception> responseBuilder) throws HttpExecuteException {
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
            configImmutable.getHeaders().forEach((k, v) -> request.addHeader(k, v));
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
        authCache.put(new HttpHost(uri.getHost(), port), (AuthScheme) new BasicScheme());
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

package org.openecomp.sdc.common.http.client.api;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.FunctionThrows;
import org.openecomp.sdc.common.http.config.HttpClientConfig;

public enum HttpRequestHandler {
    HANDLER;
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

    HttpRequestHandler() {
        HttpConnectionMngFactory connectionMngFactory = new HttpConnectionMngFactory();
        clientFactory = new HttpClientFactory(connectionMngFactory);
    }
    
    static HttpRequestHandler get() {
        return HANDLER;
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

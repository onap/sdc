package org.openecomp.sdc.common.http.client.api;

import java.util.Properties;

import org.apache.http.HttpEntity;
import org.openecomp.sdc.common.http.config.HttpClientConfig;

public abstract class HttpRequest {

    final static HttpClientConfig defaultConfig = new HttpClientConfig();
    final static Properties defaultHeaders = null;
    
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

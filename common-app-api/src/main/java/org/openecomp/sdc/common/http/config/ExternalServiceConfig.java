package org.openecomp.sdc.common.http.config;

public class ExternalServiceConfig {
    
    private HttpRequestConfig httpRequestConfig;
    private HttpClientConfig httpClientConfig;

    public HttpRequestConfig getHttpRequestConfig() {
        return httpRequestConfig;
    }
    
    public void setHttpRequestConfig(HttpRequestConfig httpRequestConfig) {
        this.httpRequestConfig = httpRequestConfig;
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExternalServiceConfig [httpRequestConfig=");
        builder.append(httpRequestConfig);
        builder.append(", httpClientConfig=");
        builder.append(httpClientConfig);
        builder.append("]");
        return builder.toString();
    }

}

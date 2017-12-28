package org.openecomp.sdc.common.http.config;

import java.util.Map;

import org.openecomp.sdc.common.http.client.api.ComparableHttpRequestRetryHandler;

public class HttpClientConfig {

    private BasicAuthorization basicAuthorization;
    private ClientCertificate clientCertificate;
    private ComparableHttpRequestRetryHandler retryHandler;
    private Timeouts timeouts = Timeouts.DEFAULT;
    private Map<String, String> headers;
    private int numOfRetries;

    public HttpClientConfig() {    
    }
    
    public HttpClientConfig(Timeouts timeouts) {
        setTimeouts(timeouts);
    }

    public HttpClientConfig(Timeouts timeouts, ClientCertificate clientCertificate) {
        setTimeouts(timeouts);
        setClientCertificate(clientCertificate);
    }

    public HttpClientConfig(Timeouts timeouts, BasicAuthorization basicAuthorization) {
        setTimeouts(timeouts);
        setBasicAuthorization(basicAuthorization);
    }

    public ComparableHttpRequestRetryHandler getRetryHandler() {
        return retryHandler;
    }

    public void setRetryHandler(ComparableHttpRequestRetryHandler retryHandler) {
        this.retryHandler = retryHandler;
    }

    public Timeouts getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(Timeouts timeouts) {
        this.timeouts = timeouts;
    }

    public BasicAuthorization getBasicAuthorization() {
        return basicAuthorization;
    }

    public void setBasicAuthorization(BasicAuthorization basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    public ClientCertificate getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(ClientCertificate clientCertificate) {
        this.clientCertificate = clientCertificate;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getNumOfRetries() {
        return numOfRetries;
    }
    
    public void setNumOfRetries(int numOfRetries) {
        this.numOfRetries = numOfRetries;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpClientConfig [basicAuthorization=");
        builder.append(basicAuthorization);
        builder.append(", clientCertificate=");
        builder.append(clientCertificate);
        builder.append(", retryHandler=");
        builder.append(retryHandler);
        builder.append(", timeouts=");
        builder.append(timeouts);
        builder.append(", headers=");
        builder.append(headers);
        builder.append(", numOfRetries=");
        builder.append(numOfRetries);
        builder.append("]");
        return builder.toString();
    }
}

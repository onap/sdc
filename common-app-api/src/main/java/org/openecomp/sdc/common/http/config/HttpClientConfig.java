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

package org.openecomp.sdc.common.http.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.nustaq.serialization.annotations.Serialize;
import org.openecomp.sdc.common.http.client.api.ComparableHttpRequestRetryHandler;

import java.util.Map;

public class HttpClientConfig {

    private BasicAuthorization basicAuthorization;
    private ClientCertificate clientCertificate;
    private ComparableHttpRequestRetryHandler retryHandler;
    private Timeouts timeouts = Timeouts.DEFAULT;
    private Map<String, String> headers;
    private int numOfRetries;
    @JsonIgnore
    private boolean enableMetricLogging = false;

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

    public boolean isEnableMetricLogging() {
        return enableMetricLogging;
    }

    public void setEnableMetricLogging(boolean enableMetricLogging) {
        this.enableMetricLogging = enableMetricLogging;
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
        builder.append(", enableMetricLogging=");
        builder.append(enableMetricLogging);
        builder.append("]");
        return builder.toString();
    }
}

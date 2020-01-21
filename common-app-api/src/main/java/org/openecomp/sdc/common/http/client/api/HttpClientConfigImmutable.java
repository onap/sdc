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

import org.openecomp.sdc.common.http.config.BasicAuthorization;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class HttpClientConfigImmutable {

    private final Map<String, String> headers;
    private final BasicAuthorization basicAuthorization;
    private final ClientCertificate clientCertificate;
    private final Timeouts timeouts;
    private boolean enableMetricLogging;
    /*
     * use ComparableHttpRequestRetryHandler.compare instead of default generated equals 
     */
    private final ComparableHttpRequestRetryHandler retryHandler;
    private final int numOfRetries;
    
    static HttpClientConfigImmutable getOrCreate(HttpClientConfig httpClientConfig) {
        // TODO: retrieve from a pool if exist, otherwise create new
        return new HttpClientConfigImmutable(httpClientConfig); 
    }

    public HttpClientConfigImmutable(HttpClientConfig httpClientConfig) {
        timeouts = httpClientConfig.getTimeouts() != null ? new Timeouts(httpClientConfig.getTimeouts()) : null;
        basicAuthorization = httpClientConfig.getBasicAuthorization() != null ? new BasicAuthorization(httpClientConfig.getBasicAuthorization()) : null;
        clientCertificate = httpClientConfig.getClientCertificate() != null ? new ClientCertificate(httpClientConfig.getClientCertificate()) : null;
        headers = httpClientConfig.getHeaders() != null ? Collections.unmodifiableMap(new HashMap<>(httpClientConfig.getHeaders())) : null;
        retryHandler = httpClientConfig.getRetryHandler();
        numOfRetries = httpClientConfig.getNumOfRetries();
        enableMetricLogging = httpClientConfig.isEnableMetricLogging();
    }

    public boolean isEnableMetricLogging() {
        return enableMetricLogging;
    }

    Map<String, String> getHeaders() {
        return headers;
    }

    public int getNumOfRetries() {
        return numOfRetries;
    }
    
    String getBasicAuthPassword() {
        return basicAuthorization != null ? basicAuthorization.getPassword() : null;
    }

    String getBasicAuthUserName() {
        return basicAuthorization != null ? basicAuthorization.getUserName() : null;
    }

    String getClientCertKeyStore() {
        return clientCertificate != null ? clientCertificate.getKeyStore() : null;
    }

    String getClientCertKeyPassword() {
        return clientCertificate != null ? clientCertificate.getKeyStorePassword() : null;
    }

    ClientCertificate getClientCertificate() {
        return clientCertificate != null ? new ClientCertificate(clientCertificate) : null;
    }
    
    public int getReadTimeoutMs() {
        return timeouts.getReadTimeoutMs();
    }

    public int getConnectTimeoutMs() {
        return timeouts.getConnectTimeoutMs();
    }

    public int getConnectPoolTimeoutMs() {
        return timeouts.getConnectPoolTimeoutMs();
    }

    public ComparableHttpRequestRetryHandler getRetryHandler() {
        return retryHandler;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((basicAuthorization == null) ? 0 : basicAuthorization.hashCode());
        result = prime * result + ((clientCertificate == null) ? 0 : clientCertificate.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + ((retryHandler == null) ? 0 : retryHandler.hashCode());
        result = prime * result + ((timeouts == null) ? 0 : timeouts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HttpClientConfigImmutable other = (HttpClientConfigImmutable) obj;
        if (basicAuthorization == null) {
            if (other.basicAuthorization != null)
                return false;
        }
        else if (!basicAuthorization.equals(other.basicAuthorization))
            return false;
        if (clientCertificate == null) {
            if (other.clientCertificate != null)
                return false;
        }
        else if (!clientCertificate.equals(other.clientCertificate))
            return false;
        if (headers == null) {
            if (other.headers != null)
                return false;
        }
        else if (!headers.equals(other.headers))
            return false;
        if (retryHandler == null) {
            if (other.retryHandler != null)
                return false;
        }
        else if (!retryHandler.compare(other.retryHandler))
            return false;
        if (timeouts == null) {
            if (other.timeouts != null)
                return false;
        }
        else if (!timeouts.equals(other.timeouts))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HttpClientConfigImmutable [basicAuthorization=");
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

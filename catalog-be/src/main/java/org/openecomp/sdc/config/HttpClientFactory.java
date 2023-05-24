/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.config;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.onap.config.api.JettySSLUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.http.client.api.HttpClientConfigImmutable;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;

import static org.openecomp.sdc.common.api.Constants.HTTP;
import static org.openecomp.sdc.common.api.Constants.HTTPS;

public class HttpClientFactory {

    private static final int DEFAULT_CONNECTION_POOL_SIZE = 30;
    private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 5;
    private static final int VALIDATE_CONNECTION_AFTER_INACTIVITY_MS = 10000;
    private static final int CONNECT_TIMEOUT_MS = 15000;

    private HttpClientConnectionManager createConnectionManager() throws Exception {
        final SSLConnectionSocketFactory sslConnectionSocketFactory = getSslConnectionSocketFactory();
        final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTP, PlainConnectionSocketFactory.getSocketFactory())
                .register(HTTPS, sslConnectionSocketFactory).build();
        final PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(DEFAULT_CONNECTION_POOL_SIZE);
        manager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTION_PER_ROUTE);
        manager.setValidateAfterInactivity(VALIDATE_CONNECTION_AFTER_INACTIVITY_MS);
        manager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(CONNECT_TIMEOUT_MS).build());
        return manager;
    }

    private SSLConnectionSocketFactory getSslConnectionSocketFactory() throws Exception {
        return new SSLConnectionSocketFactory(JettySSLUtils.getSslContext());
    }

    /*
    The difference between this client factory and the one in common api,
    is that this one returns an apache httpclient instance, rather than a custom created custom.
    */
    public CloseableHttpClient createHttpClient() throws Exception {
        final int connectTimeoutMs = 5000;
        final int readTimeoutMs = 10000;
        final HttpClientConfig httpClientConfig = new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs));
        final HttpClientConfigImmutable immutableHttpClientConfig = new HttpClientConfigImmutable(httpClientConfig);
        final RequestConfig requestConfig = createClientTimeoutConfiguration(immutableHttpClientConfig);
        final HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(createConnectionManager())
                .setDefaultRequestConfig(requestConfig);
        final HttpRequestRetryHandler retryHandler = resolveRetryHandler(immutableHttpClientConfig);
        if (retryHandler != null) {
            httpClientBuilder.setRetryHandler(retryHandler);
        }
        if (HTTPS.equals(ConfigurationManager.getConfigurationManager().getConfiguration().getBeProtocol())) {
            httpClientBuilder.setSSLContext(JettySSLUtils.getSslContext());
        }

        return httpClientBuilder.build();
    }

    private RequestConfig createClientTimeoutConfiguration(final HttpClientConfigImmutable config) {
        return RequestConfig.custom()
                .setConnectTimeout(config.getConnectTimeoutMs())
                .setSocketTimeout(config.getReadTimeoutMs())
                .setConnectionRequestTimeout(config.getConnectPoolTimeoutMs())
                .build();
    }

    private HttpRequestRetryHandler resolveRetryHandler(HttpClientConfigImmutable config) {
        return config.getNumOfRetries() > 0 ? config.getRetryHandler() : null;
    }
}

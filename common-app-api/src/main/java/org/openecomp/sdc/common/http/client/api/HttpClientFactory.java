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

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.common.log.interceptors.ApacheClientLogRequestInterceptor;
import org.openecomp.sdc.common.log.interceptors.ApacheClientLogResponseInterceptor;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class HttpClientFactory {

    private static final Logger logger = Logger.getLogger(HttpClientFactory.class.getName());
    private static final UserTokenHandler userTokenHandler = context -> null;
    private final HttpConnectionMngFactory connectionMngFactory;
    
    HttpClientFactory(HttpConnectionMngFactory connectionMngFactory) {
        this.connectionMngFactory = connectionMngFactory;
    }

    HttpClient createClient(String protocol, HttpClientConfigImmutable config) {
        logger.debug("Create {} client based on {}", protocol, config);

        ClientCertificate clientCertificate = Constants.HTTPS.equals(protocol) ? config.getClientCertificate() : null; 
        HttpClientConnectionManager connectionManager = connectionMngFactory.getOrCreate(clientCertificate);
        RequestConfig requestConfig = createClientTimeoutConfiguration(config);
        CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(connectionManager)
                    .setUserTokenHandler(userTokenHandler)
                    .setRetryHandler(resolveRetryHandler(config))
                    .build();

        return new HttpClient(client, config);
    }

    private HttpRequestRetryHandler resolveRetryHandler(HttpClientConfigImmutable config) {
        return config.getNumOfRetries() > 0 ? config.getRetryHandler() : null;
    }
        
    private RequestConfig createClientTimeoutConfiguration(HttpClientConfigImmutable config) {
        return RequestConfig.custom()
                .setConnectTimeout(config.getConnectTimeoutMs())
                .setSocketTimeout(config.getReadTimeoutMs())
                .setConnectionRequestTimeout(config.getConnectPoolTimeoutMs())
                .build();
    }
}

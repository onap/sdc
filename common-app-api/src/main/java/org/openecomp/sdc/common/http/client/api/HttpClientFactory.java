package org.openecomp.sdc.common.http.client.api;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);
    private static final UserTokenHandler userTokenHandler = context -> null;
    private final HttpConnectionMngFactory connectionMngFactory;
    
    HttpClientFactory(HttpConnectionMngFactory connectionMngFactory) {
        this.connectionMngFactory = connectionMngFactory;
    }

    HttpClient createClient(String protocol, HttpClientConfigImmutable config) {
        LOGGER.debug("Create {} client based on {}", protocol, config);

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

package org.openecomp.sdc.config;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.client.api.HttpClientConfigImmutable;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class HttpClientFactory {

    private static final int DEFAULT_CONNECTION_POOL_SIZE = 30;
    private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 5;
    private static final int VALIDATE_CONNECTION_AFTER_INACTIVITY_MS = 10000;
    private static final int CONNECT_TIMEOUT_MS = 15000;

    private static final Logger log = Logger.getLogger(HttpClientFactory.class);
    private static final UserTokenHandler userTokenHandler = context -> null;

    private HttpClientConnectionManager createConnectionManager(){
        SSLConnectionSocketFactory sslsf = getSslConnectionSocketFactory();

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Constants.HTTP, PlainConnectionSocketFactory.getSocketFactory())
                .register(Constants.HTTPS, sslsf).build();

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);

        manager.setMaxTotal(DEFAULT_CONNECTION_POOL_SIZE);
        manager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTION_PER_ROUTE);
        manager.setValidateAfterInactivity(VALIDATE_CONNECTION_AFTER_INACTIVITY_MS);

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(CONNECT_TIMEOUT_MS)
                .build();
        manager.setDefaultSocketConfig(socketConfig);

        return manager;
    }

    private SSLConnectionSocketFactory getSslConnectionSocketFactory() {
        return new SSLConnectionSocketFactory(SSLContexts.createSystemDefault());
    }


    /*
    The difference between this client factory and the one in common api,
    is that this one returns an apache httpclient instance, rather than a custom created custom.
    */
    public CloseableHttpClient createHttpClient() {
        int connectTimeoutMs = 5000;
        int readTimeoutMs = 10000;
        HttpClientConnectionManager connManager = createConnectionManager();
        HttpClientConfig httpClientConfig = new HttpClientConfig(new Timeouts(connectTimeoutMs, readTimeoutMs));
        HttpClientConfigImmutable immutableHttpClientConfig = new HttpClientConfigImmutable(httpClientConfig);
        RequestConfig requestConfig = createClientTimeoutConfiguration(immutableHttpClientConfig);
        return HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .setUserTokenHandler(userTokenHandler)
                .setRetryHandler(resolveRetryHandler(immutableHttpClientConfig))
                .build();
    }

    private  RequestConfig createClientTimeoutConfiguration(HttpClientConfigImmutable config) {
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

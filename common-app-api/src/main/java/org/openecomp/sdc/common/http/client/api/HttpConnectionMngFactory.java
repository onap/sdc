package org.openecomp.sdc.common.http.client.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConnectionMngFactory {

    private static final String P12_KEYSTORE_EXTENTION = ".p12";
    private static final String PFX_KEYSTORE_EXTENTION = ".pfx";
    private static final String JKS_KEYSTORE_EXTENTION = ".jks";
    
    private static final String P12_KEYSTORE_TYPE = "pkcs12";
    private static final String JKS_KEYSTORE_TYPE = "jks";
    
    private static final Logger logger = LoggerFactory.getLogger(HttpConnectionMngFactory.class);
    private static final int DEFAULT_CONNECTION_POOL_SIZE = 30;
    private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 5;
    private static final int VALIDATE_CONNECTION_AFTER_INACTIVITY_MS = 10000;
    
    private Map<ClientCertificate, HttpClientConnectionManager> sslClientConnectionManagers = new ConcurrentHashMap<>();
    private HttpClientConnectionManager plainClientConnectionManager;
    
    HttpConnectionMngFactory() {
        plainClientConnectionManager = createConnectionMng(null);
    }

    HttpClientConnectionManager getOrCreate(ClientCertificate clientCertificate) {
        if(clientCertificate == null) {
            return plainClientConnectionManager;
        }
        return sslClientConnectionManagers.computeIfAbsent(clientCertificate, k -> createConnectionMng(clientCertificate));
    }
    
    private HttpClientConnectionManager createConnectionMng(ClientCertificate clientCertificate) {

        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try {
            sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            
            if(clientCertificate != null) {
                setClientSsl(clientCertificate, sslContextBuilder);
            }
            sslsf = new SSLConnectionSocketFactory(sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE);
        }
        catch (GeneralSecurityException e) {
            logger.debug("Create SSL connection socket factory failed with exception, use default SSL factory ", e);
            sslsf = SSLConnectionSocketFactory.getSocketFactory();
        }

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(Constants.HTTP, PlainConnectionSocketFactory.getSocketFactory())
                .register(Constants.HTTPS, sslsf).build();

        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);

        manager.setMaxTotal(DEFAULT_CONNECTION_POOL_SIZE);
        manager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTION_PER_ROUTE);
        manager.setValidateAfterInactivity(VALIDATE_CONNECTION_AFTER_INACTIVITY_MS);

        return manager;
    }

    private void setClientSsl(ClientCertificate clientCertificate, SSLContextBuilder sslContextBuilder) {
        try {
            char[] keyStorePassword = clientCertificate.getKeyStorePassword().toCharArray();
            KeyStore clientKeyStore = createClientKeyStore(clientCertificate.getKeyStore(), keyStorePassword);
            sslContextBuilder.loadKeyMaterial(clientKeyStore, keyStorePassword);
            logger.debug("Set Client Certificate authentication based on {}", clientCertificate);
        }
        catch (IOException | GeneralSecurityException e) {
            logger.debug("Set Client Certificate authentication failed with exception, diasable client SSL authentication ", e);
        }
    }
        
    private KeyStore createClientKeyStore(String keyStorePath, char[] keyStorePassword) throws IOException, GeneralSecurityException {
        KeyStore keyStore = null;
        try (InputStream stream = new FileInputStream(keyStorePath)) {
            keyStore = KeyStore.getInstance(getKeyStoreType(keyStorePath));
            keyStore.load(stream, keyStorePassword);
        }
        return keyStore;
    }
    
    private String getKeyStoreType(String keyStore) {
        if(!StringUtils.isEmpty(keyStore)) {
            if(keyStore.endsWith(P12_KEYSTORE_EXTENTION) || keyStore.endsWith(PFX_KEYSTORE_EXTENTION)) {
                return P12_KEYSTORE_TYPE;
            }
            else if(keyStore.endsWith(JKS_KEYSTORE_EXTENTION)) {
                return JKS_KEYSTORE_TYPE;
            }            
        }
        return KeyStore.getDefaultType();
    }
}

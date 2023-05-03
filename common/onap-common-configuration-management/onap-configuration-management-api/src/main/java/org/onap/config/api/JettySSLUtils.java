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
package org.onap.config.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JettySSLUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettySSLUtils.class);
    private static final String JETTY_BASE = System.getenv("JETTY_BASE");

    public static JettySslConfig getSSLConfig() {
        final Properties sslProperties = new Properties();
        final String sslPropsPath = JETTY_BASE + "/start.d/ssl.ini";
        try (final InputStream fis = new FileInputStream(sslPropsPath)) {
            sslProperties.load(fis);
        } catch (Exception e) {
            LOGGER.error("Failed to read '{}'", sslPropsPath, e);
            throw new RuntimeException(e);
        }
        return new JettySslConfig(sslProperties);
    }

    public static SSLContext getSslContext() {
        final JettySslConfig sslProperties = getSSLConfig();
        final KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(sslProperties.getTruststoreType());
        } catch (final KeyStoreException e) {
            LOGGER.error("Failed to get KeyStore instance for '{}'", sslProperties.getTruststoreType(), e);
            throw new RuntimeException(e);
        }
        try (final InputStream fis = new FileInputStream(sslProperties.getTruststorePath())) {
            trustStore.load(fis, (sslProperties.getTruststorePass()).toCharArray());
        } catch (final Exception e) {
            LOGGER.error("Failed to load trustStore '{}'", sslProperties.getTruststorePath(), e);
            throw new RuntimeException(e);
        }
        final KeyStore keystore;
        try {
            keystore = KeyStore.getInstance(sslProperties.getKeystoreType());
        } catch (final KeyStoreException e) {
            LOGGER.error("Failed to get KeyStore instance for '{}'", sslProperties.getKeystoreType(), e);
            throw new RuntimeException(e);
        }
        try (final InputStream fis = new FileInputStream(sslProperties.getKeystorePath())) {
            keystore.load(fis, sslProperties.getKeystorePass().toCharArray());
        } catch (final Exception e) {
            LOGGER.error("Failed to load keystore '{}'", sslProperties.getTruststorePath(), e);
            throw new RuntimeException(e);
        }
        // Trust own CA and all self-signed certs
        try {
            return SSLContexts.custom()
                    .loadKeyMaterial(keystore, sslProperties.getKeystorePass().toCharArray())
                    .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Failed to getSslContext", e);
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    public static class JettySslConfig {

        private final Properties sslProperties;

        public String getKeystorePath() {
            return sslProperties.getProperty("jetty.sslContext.keyStorePath");
        }

        public String getKeystorePass() {
            return sslProperties.getProperty("jetty.sslContext.keyStorePassword");
        }

        public String getKeystoreType() {
            return sslProperties.getProperty("jetty.sslContext.keyStoreType", KeyStore.getDefaultType());
        }

        public String getTruststorePath() {
            return sslProperties.getProperty("jetty.sslContext.trustStorePath");
        }

        public String getTruststorePass() {
            return sslProperties.getProperty("jetty.sslContext.trustStorePassword");
        }

        public String getTruststoreType() {
            return sslProperties.getProperty("jetty.sslContext.trustStoreType", KeyStore.getDefaultType());
        }

        public String getKeyManagerPassword() {
            return sslProperties.getProperty("jetty.sslContext.keyManagerPassword");
        }

        public Boolean getNeedClientAuth() {
            if (sslProperties.containsKey("jetty.sslContext.needClientAuth")) {
                return Boolean.valueOf(sslProperties.getProperty("jetty.sslContext.needClientAuth"));
            } else {
                return Boolean.FALSE;
            }
        }

    }
}

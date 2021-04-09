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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

public class JettySSLUtils {

    private JettySSLUtils() {
    }

    public static JettySslConfig getSSLConfig() throws IOException {
        Properties sslProperties = new Properties();
        String sslPropsPath = System.getenv("JETTY_BASE") + File.separator + "/start.d/ssl.ini";
        File sslPropsFile = new File(sslPropsPath);
        try (FileInputStream fis = new FileInputStream(sslPropsFile)) {
            sslProperties.load(fis);
        }
        return new JettySslConfig(sslProperties);
    }

    public static SSLContext getSslContext() throws GeneralSecurityException, IOException {
        JettySslConfig sslProperties = JettySSLUtils.getSSLConfig();
        KeyStore trustStore = KeyStore.getInstance(sslProperties.getTruststoreType());
        try (FileInputStream instream = new FileInputStream(new File(sslProperties.getTruststorePath()));) {
            trustStore.load(instream, (sslProperties.getTruststorePass()).toCharArray());
        }
        KeyStore keystore = KeyStore.getInstance(sslProperties.getKeystoreType());
        try (FileInputStream instream = new FileInputStream(new File(sslProperties.getKeystorePath()));) {
            keystore.load(instream, sslProperties.getKeystorePass().toCharArray());
        }
        // Trust own CA and all self-signed certs
        return SSLContexts.custom().loadKeyMaterial(keystore, sslProperties.getKeystorePass().toCharArray())
            .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy()).build();
    }

    public static class JettySslConfig {

        static final String JETTY_BASE = System.getenv("JETTY_BASE");
        static final String KEY_STORE_TYPE_PROPERTY_NAME = "jetty.sslContext.keyStoreType";
        static final String TRUST_STORE_TYPE_PROPERTY_NAME = "jetty.sslContext.trustStoreType";
        Properties sslProperties;

        JettySslConfig(Properties sslProperties) {
            this.sslProperties = sslProperties;
        }

        public String getJettyBase() {
            return JettySslConfig.JETTY_BASE;
        }

        public String getKeystorePath() {
            return sslProperties.getProperty("jetty.sslContext.keyStorePath");
        }

        public String getKeystorePass() {
            return sslProperties.getProperty("jetty.sslContext.keyStorePassword");
        }

        public String getKeystoreType() {
            return sslProperties.getProperty(KEY_STORE_TYPE_PROPERTY_NAME, KeyStore.getDefaultType());
        }

        public String getTruststorePath() {
            return sslProperties.getProperty("jetty.sslContext.trustStorePath");
        }

        public String getTruststorePass() {
            return sslProperties.getProperty("jetty.sslContext.trustStorePassword");
        }

        public String getTruststoreType() {
            return sslProperties.getProperty(TRUST_STORE_TYPE_PROPERTY_NAME, KeyStore.getDefaultType());
        }

        public String getKeyStoreManager() {
            return sslProperties.getProperty("jetty.sslContext.keyManagerPassword");
        }

        public Boolean getNeedClientAuth() {
            if (sslProperties.containsKey("jetty.sslContext.needClientAuth")) {
                return Boolean.valueOf(sslProperties.getProperty("jetty.sslContext.needClientAuth"));
            } else {
                return Boolean.FALSE;
            }
        }

        public String getProperty(String key) {
            return sslProperties.getProperty(key);
        }
    }
}

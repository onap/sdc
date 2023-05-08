/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import nl.altindag.ssl.util.KeyManagerUtils;
import nl.altindag.ssl.util.KeyStoreUtils;
import nl.altindag.ssl.util.TrustManagerUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.onap.config.api.JettySSLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.stream.Stream;

@Component
public class FileAlterationListenerComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAlterationListenerComponent.class);

    private SwappableSslService swappableSslService;

    @Autowired
    public FileAlterationListenerComponent(final SwappableSslService swappableSslService) {
        this.swappableSslService = swappableSslService;
        runListener();
    }

    protected FileAlterationListenerComponent() {
    }

    protected void runListener() {
        final JettySSLUtils.JettySslConfig sslConfig = JettySSLUtils.getSSLConfig();
        final String keystoreLocation = sslConfig.getKeystorePath();
        final String keystorePass = sslConfig.getKeystorePass();
        final String truststoreLocation = sslConfig.getTruststorePath();
        final String truststorePass = sslConfig.getTruststorePass();
        final String keystoreType = sslConfig.getKeystoreType();
        final String truststoreType = sslConfig.getTruststoreType();
        final FileAlterationMonitor monitor = new FileAlterationMonitor();
        final FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(final File file) {
                LOGGER.error("Something changed: {}", file);
                final Path path = Path.of(String.valueOf(file));
                if (file.getAbsolutePath().endsWith(keystoreLocation)) {
                    try {
                        LOGGER.error("it was key store file: {}", keystoreLocation);
                        final char[] passCharArray = keystorePass.toCharArray();
                        final KeyStore keystore = KeyStoreUtils.loadKeyStore(path, passCharArray, keystoreType);
                        FileAlterationListenerComponent.this.swappableSslService.updateKeyManager(KeyManagerUtils.createKeyManager(keystore, passCharArray));
                        LOGGER.error("KeyManager updated successfully");
                    } catch (Exception e) {
                        LOGGER.error("Failed to reload Key Store", e);
                    }
                } else if (file.getAbsolutePath().endsWith(truststoreLocation)) {
                    try {
                        LOGGER.error("it was trust store file: {}", truststoreLocation);
                        final KeyStore truststore = KeyStoreUtils.loadKeyStore(path, truststorePass.toCharArray(), truststoreType);
                        FileAlterationListenerComponent.this.swappableSslService.updateTrustManager(TrustManagerUtils.createTrustManager(truststore));
                        LOGGER.error("TrustManager updated successfully");
                    } catch (Exception e) {
                        LOGGER.error("Failed to reload Trust Store", e);
                    }
                }
            }
        };

        Stream.of(Path.of(sslConfig.getJettyBase(), keystoreLocation).getParent(), Path.of(sslConfig.getJettyBase(), truststoreLocation).getParent())
                .distinct().forEach(path -> {
                    final FileAlterationObserver observer = new FileAlterationObserver(path.toFile());
                    observer.addListener(listener);
                    monitor.addObserver(observer);
                });
        try {
            monitor.start();
        } catch (final Exception e) {
            LOGGER.error("Failed to start monitor ", e);
        }
    }

}

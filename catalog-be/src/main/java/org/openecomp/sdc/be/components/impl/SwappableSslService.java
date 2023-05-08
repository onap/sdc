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
import nl.altindag.ssl.util.SSLSessionUtils;
import nl.altindag.ssl.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

@Service
public class SwappableSslService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwappableSslService.class);

    private final SSLSessionContext sslSessionContext;
    private final X509ExtendedKeyManager swappableKeyManager;
    private final X509ExtendedTrustManager swappableTrustManager;
//    private final RestClientSSLConfig restClientSSLConfig;

    @Autowired
    public SwappableSslService(final SSLSessionContext sslSessionContext,
                               final X509ExtendedKeyManager swappableKeyManager,
                               final X509ExtendedTrustManager swappableTrustManager/*,
                               final RestClientSSLConfig restClientSSLConfig*/) {
        this.sslSessionContext = sslSessionContext;
        this.swappableKeyManager = swappableKeyManager;
        this.swappableTrustManager = swappableTrustManager;
//        this.restClientSSLConfig = restClientSSLConfig;
    }

    public void updateKeyManager(final X509ExtendedKeyManager keyManager) {
        LOGGER.error("Updating Key Manager...");
        KeyManagerUtils.swapKeyManager(swappableKeyManager, keyManager);
        LOGGER.error("Invalidating caches...");
        SSLSessionUtils.invalidateCaches(sslSessionContext);
//        restClientSSLConfig.updateRestTemplate(keyManager, swappableTrustManager);
    }

    public void updateTrustManager(final X509ExtendedTrustManager trustManager) {
        LOGGER.error("Updating Trust Manager...");
        TrustManagerUtils.swapTrustManager(swappableTrustManager, trustManager);
        LOGGER.error("Invalidating caches...");
        SSLSessionUtils.invalidateCaches(sslSessionContext);
//        restClientSSLConfig.updateRestTemplate(swappableKeyManager, trustManager);
    }

}

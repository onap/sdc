/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import org.apache.http.conn.HttpClientConnectionManager;
import org.junit.Test;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.security.SecurityUtil;

import static org.junit.Assert.assertNotNull;

public class HttpConnectionMngFactoryTest {

    @Test
    public void validateFactoryCreatesValidHttpClientConnectionManager() {
        final String testKeyStore = "testKeyStore ";
        final String testKeyStorePassword = SecurityUtil.INSTANCE.encrypt("testKeyStorePassword").left().value();

        ClientCertificate clientCertificate = new ClientCertificate();
        clientCertificate.setKeyStore(testKeyStore);
        clientCertificate.setKeyStorePassword(testKeyStorePassword);
        HttpClientConnectionManager httpClientConnectionManager = new HttpConnectionMngFactory().getOrCreate(clientCertificate);

        assertNotNull(httpClientConnectionManager);

        httpClientConnectionManager.shutdown();
    }

}

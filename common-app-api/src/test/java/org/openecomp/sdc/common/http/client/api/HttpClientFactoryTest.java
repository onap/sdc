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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.security.SecurityUtil;
import org.openecomp.sdc.common.http.config.BasicAuthorization;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;

import java.util.Collections;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientFactoryTest {

    @Mock
    HttpConnectionMngFactory httpConnectionMngFactory;

    @Test
    public void validateNewClientCreationReturnsValidClient() throws HttpExecuteException {
        HttpClient httpClient = new HttpClientFactory(httpConnectionMngFactory).createClient("Http",prepareTestClientConfigImmutable());
        assertNotNull(httpClient);
        httpClient.close();
    }

    private HttpClientConfigImmutable prepareTestClientConfigImmutable() {
        final String testUserName = "testUser";
        final String testUserPassword = SecurityUtil.encrypt("testPassword").left().value();
        final int timeouts = 10;
        final String testKeyStore = "testKeyStore";
        final String testKeyStorePassword = SecurityUtil.encrypt("testKeyStorePassword").left().value();

        int testNumOfRetries = 10;
        ComparableHttpRequestRetryHandler testRetryHandler = Mockito.mock(ComparableHttpRequestRetryHandler.class);
        Map<String, String> testHeaders = Collections.emptyMap();
        Timeouts testTimeouts = new Timeouts(timeouts, timeouts);
        testTimeouts.setConnectPoolTimeoutMs(timeouts);
        BasicAuthorization testBasicAuthorization = new BasicAuthorization();
        testBasicAuthorization.setUserName(testUserName);
        testBasicAuthorization.setPassword(testUserPassword);
        ClientCertificate testClientCertificate = new ClientCertificate();
        testClientCertificate.setKeyStore(testKeyStore);
        testClientCertificate.setKeyStorePassword(testKeyStorePassword);

        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.setNumOfRetries(testNumOfRetries);
        httpClientConfig.setTimeouts(testTimeouts);
        httpClientConfig.setBasicAuthorization(testBasicAuthorization);
        httpClientConfig.setClientCertificate(testClientCertificate);
        httpClientConfig.setRetryHandler(testRetryHandler);
        httpClientConfig.setHeaders(testHeaders);

        return new HttpClientConfigImmutable(httpClientConfig);
    }
}

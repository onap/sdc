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
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.security.SecurityUtil;
import org.openecomp.sdc.common.http.config.BasicAuthorization;
import org.openecomp.sdc.common.http.config.ClientCertificate;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientConfigImmutableTest {

    @Mock
    private ComparableHttpRequestRetryHandler testRetryHandler;

    private ClientCertificate testClientCertificate;

    private Map<String, String> testHeaders;

    private BasicAuthorization testBasicAuthorization;


    private Timeouts testTimeouts;


    private int testNumOfRetries;

    @Test
    public void validateArgConstructorCreatesValidImmutableConfig() {
        HttpClientConfigImmutable httpClientConfigImmutable = new HttpClientConfigImmutable(prepareTestClientConfig());
        validateAllVieldsInImmutableConfig(httpClientConfigImmutable);
    }

    @Test
    public void validateStaticMethodCreatesValidImmutableConfig() {
        HttpClientConfigImmutable httpClientConfigImmutable = HttpClientConfigImmutable.getOrCreate(prepareTestClientConfig());
        validateAllVieldsInImmutableConfig(httpClientConfigImmutable);
    }

    @Test
    public void validateToString() {
        HttpClientConfigImmutable httpClientConfigImmutable = HttpClientConfigImmutable.getOrCreate(prepareTestClientConfig());
        final String result = httpClientConfigImmutable.toString();

        assertTrue(result.contains(testBasicAuthorization.toString()));
        assertTrue(result.contains(testClientCertificate.toString()));
        assertTrue(result.contains(testRetryHandler.toString()));
        assertTrue(result.contains(testTimeouts.toString()));
        assertTrue(result.contains(testHeaders.toString()));
        assertTrue(result.contains(Integer.toString(testNumOfRetries)));
    }

    @Test
    public void validateHashCode() {
        HttpClientConfigImmutable httpClientConfigImmutable01 = new HttpClientConfigImmutable(prepareTestClientConfig());
        HttpClientConfigImmutable httpClientConfigImmutable02 = HttpClientConfigImmutable.getOrCreate(prepareTestClientConfig());

        assertEquals(
                httpClientConfigImmutable01.hashCode(),
                httpClientConfigImmutable02.hashCode()
        );
    }

    private void validateAllVieldsInImmutableConfig(HttpClientConfigImmutable httpClientConfigImmutable) {
        assertEquals(
                httpClientConfigImmutable.getNumOfRetries(),
                testNumOfRetries);
        assertEquals(
                httpClientConfigImmutable.getReadTimeoutMs(),
                testTimeouts.getReadTimeoutMs());
        assertEquals(
                httpClientConfigImmutable.getConnectTimeoutMs(),
                testTimeouts.getConnectTimeoutMs());
        assertEquals(
                httpClientConfigImmutable.getConnectPoolTimeoutMs(),
                testTimeouts.getConnectPoolTimeoutMs());
        assertEquals(
                httpClientConfigImmutable.getBasicAuthUserName(),
                testBasicAuthorization.getUserName());
        assertEquals(
                httpClientConfigImmutable.getBasicAuthPassword(),
                testBasicAuthorization.getPassword());
        assertEquals(
                httpClientConfigImmutable.getClientCertificate().getClass(),
                testClientCertificate.getClass());
        assertEquals(
                httpClientConfigImmutable.getClientCertKeyStore(),
                testClientCertificate.getKeyStore());
        assertEquals(
                httpClientConfigImmutable.getClientCertKeyPassword(),
                testClientCertificate.getKeyStorePassword());
        assertEquals(
                httpClientConfigImmutable.getRetryHandler(),
                testRetryHandler);
        assertEquals(
                httpClientConfigImmutable.getHeaders(),
                testHeaders);
    }

    private HttpClientConfig prepareTestClientConfig() {
        final String testUserName = "testUser";
        final String testUserPassword = SecurityUtil.encrypt("testPassword").left().value();
        final int timeouts = 10;
        final String testKeyStore = "testKeyStore";
        final String testKeyStorePassword = SecurityUtil.encrypt("testKeyStorePassword").left().value();

        testNumOfRetries = 10;
        testHeaders = Collections.emptyMap();
        testTimeouts = new Timeouts(timeouts,timeouts);
        testTimeouts.setConnectPoolTimeoutMs(timeouts);
        testBasicAuthorization = new BasicAuthorization();
        testBasicAuthorization.setUserName(testUserName);
        testBasicAuthorization.setPassword(testUserPassword);
        testClientCertificate = new ClientCertificate();
        testClientCertificate.setKeyStore(testKeyStore);
        testClientCertificate.setKeyStorePassword(testKeyStorePassword);

        HttpClientConfig httpClientConfig = new HttpClientConfig();
        httpClientConfig.setNumOfRetries(testNumOfRetries);
        httpClientConfig.setTimeouts(testTimeouts);
        httpClientConfig.setBasicAuthorization(testBasicAuthorization);
        httpClientConfig.setClientCertificate(testClientCertificate);
        httpClientConfig.setRetryHandler(testRetryHandler);
        httpClientConfig.setHeaders(testHeaders);

        return httpClientConfig;
    }
}

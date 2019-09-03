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

import java.io.IOException;
import java.util.Properties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces.FunctionThrows;
import org.openecomp.sdc.common.http.config.HttpClientConfig;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientTest {

    public static final String URL = "URL";
    @Mock
    private CloseableHttpClient closeableHttpClient;
    @Mock
    private FunctionThrows<CloseableHttpResponse, HttpResponse<String>, Exception> responseBuilder;
    @Mock
    private HttpClientConfig config;

    private HttpClientConfigImmutable configImmutable;
    private HttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        configImmutable = new HttpClientConfigImmutable(config);
        httpClient = new HttpClient(closeableHttpClient, configImmutable);
    }

    @Test
    public void shouldSendPutRequest() throws HttpExecuteException, IOException {
        httpClient.put(URL, new Properties(), new BasicHttpEntity(), responseBuilder);
        Mockito.verify(closeableHttpClient).execute(Mockito.any(HttpPut.class), Mockito.<HttpContext>isNull());
    }

    @Test
    public void shouldSendPostRequest() throws HttpExecuteException, IOException {
        httpClient.post(URL, new Properties(), new BasicHttpEntity(), responseBuilder);
        Mockito.verify(closeableHttpClient).execute(Mockito.any(HttpPost.class), Mockito.<HttpContext>isNull());
    }

    @Test
    public void shouldSendPatchRequest() throws HttpExecuteException, IOException {
        httpClient.patch(URL, new Properties(), new BasicHttpEntity(), responseBuilder);
        Mockito.verify(closeableHttpClient).execute(Mockito.any(HttpPatch.class), Mockito.<HttpContext>isNull());
    }

    @Test
    public void shouldSendGetRequest() throws HttpExecuteException, IOException {
        httpClient.get(URL, new Properties(), responseBuilder);
        Mockito.verify(closeableHttpClient).execute(Mockito.any(HttpGet.class), Mockito.<HttpContext>isNull());
    }

}
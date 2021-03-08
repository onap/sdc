/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.common.log.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ApacheClientLogResponseInterceptorTest {
    private ApacheClientLogResponseInterceptor testSubject = new ApacheClientLogResponseInterceptor();
    private HttpRequest httpRequest = Mockito.mock(HttpRequest.class);
    private HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
    private RequestLine requestLine = Mockito.mock(RequestLine.class);
    private StatusLine statusLine = Mockito.mock(StatusLine.class);

    @Test
    public void testGetTargetServiceName() {
        when(requestLine.getUri()).thenReturn("/testuri");
        when(httpRequest.getRequestLine()).thenReturn(requestLine);

        assertEquals("/testuri", testSubject.getTargetServiceName(httpRequest));
    }

    @Test
    public void testGetServiceName() {
        when(requestLine.getUri()).thenReturn("https://localhost:8080/testuri/test");
        when(httpRequest.getRequestLine()).thenReturn(requestLine);

        assertEquals("/testuri/test", testSubject.getServiceName(httpRequest));
    }

    @Test
    public void testGetHttpStatusCode() {
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        assertEquals("200", testSubject.getResponseCode(httpResponse));
    }

    @Test
    public void testGetTargetEntity() {
        assertNull(testSubject.getTargetEntity(httpRequest));
    }
}

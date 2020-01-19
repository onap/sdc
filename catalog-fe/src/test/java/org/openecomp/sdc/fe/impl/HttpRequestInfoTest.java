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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.fe.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;

@RunWith(MockitoJUnitRunner.class)
public class HttpRequestInfoTest {

    @Mock
    private HttpServletRequest request;
    private Map<String, String> headersMap = new HashMap<>();

    @Test
    public void shouldHaveGettersAndSetters() {
        Assert.assertThat(HttpRequestInfo.class, hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidConstructor() throws IOException {
        String any_url = "ANY_URL";
        Mockito.when(request.getRequestURI()).thenReturn(any_url);
        String context = "PATH";
        Mockito.when(request.getContextPath()).thenReturn(context);
        String data = "ABC";
        HttpRequestInfo httpRequestInfo = new HttpRequestInfo(request, headersMap, data);
        byte[] requestData = IOUtils.toByteArray(httpRequestInfo.getRequestData());
        Assert.assertArrayEquals(requestData, data.getBytes());
        Assert.assertEquals(httpRequestInfo.getRequestURL(), any_url);
        Assert.assertEquals(httpRequestInfo.getOriginServletContext(), context);
    }
}

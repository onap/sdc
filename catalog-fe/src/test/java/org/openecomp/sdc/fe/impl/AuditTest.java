/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.fe.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AuditTest {

    private static final int STATUS = 12345;
    private static final String[] PARAMETERS = {"abc", "def", "ghi", "jkl", "mno", "http"};
    private static final String EXPECTED = "EVENT = ARTIFACT_UPLOAD  USER_ID=abc USER_NAME=def ghi ACCESS_IP=jkl ACCESS_TYPE=mno RURL=http SC=12345";
    private static final String INTERNAL_ERROR = "Internal Error";

    @Mock
    private Logger log;

    @Mock
    private HttpRequestInfo info;

    @Mock
    private HttpServletRequest request;

    @Test
    public void testErrorWithEmptyHttpRequestInfo() {
        // when
        Audit.error(log, (HttpRequestInfo) null, 0);

        // then
        verify(log).error(eq(INTERNAL_ERROR));
    }

    @Test
    public void testErrorWithHttpRequestInfo() {
        // given
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.USER_ID_HEADER, PARAMETERS[0]);
        headers.put(Constants.FIRST_NAME_HEADER, PARAMETERS[1]);
        headers.put(Constants.LAST_NAME_HEADER, PARAMETERS[2]);
        headers.put(Constants.ORIGIN_HEADER, PARAMETERS[3]);
        headers.put(Constants.ACCESS_HEADER, PARAMETERS[4]);

        when(info.getHeaders()).thenReturn(headers);
        when(info.getRequestURL()).thenReturn(PARAMETERS[5]);

        // when
        Audit.error(log, info, STATUS);

        // then
        verify(log).error(eq(EXPECTED));
    }

    @Test
    public void testErrorWithEmptyHttpServletRequest() {
        // when
        Audit.error(log, (HttpServletRequest) null, 0);

        // then
        verify(log).error(eq(INTERNAL_ERROR));
    }

    @Test
    public void testErrorWithHttpServletRequest() {
        // given
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(PARAMETERS[0]);
        when(request.getHeader(Constants.FIRST_NAME_HEADER)).thenReturn(PARAMETERS[1]);
        when(request.getHeader(Constants.LAST_NAME_HEADER)).thenReturn(PARAMETERS[2]);
        when(request.getHeader(Constants.ORIGIN_HEADER)).thenReturn(PARAMETERS[3]);
        when(request.getHeader(Constants.ACCESS_HEADER)).thenReturn(PARAMETERS[4]);
        when(request.getRequestURL()).thenReturn(new StringBuffer(PARAMETERS[5]));

        // when
        Audit.error(log, request, STATUS);

        // then
        verify(log).error(eq(EXPECTED));
    }

    @Test
    public void testInfoWithEmptyHttpRequestInfo() {
        // when
        Audit.info(log, null, 0);

        // then
        verify(log).info(eq(INTERNAL_ERROR));
    }

    @Test
    public void testInfoWithHttpRequestInfo() {
        // given
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.USER_ID_HEADER, PARAMETERS[0]);
        headers.put(Constants.FIRST_NAME_HEADER, PARAMETERS[1]);
        headers.put(Constants.LAST_NAME_HEADER, PARAMETERS[2]);
        headers.put(Constants.ORIGIN_HEADER, PARAMETERS[3]);
        headers.put(Constants.ACCESS_HEADER, PARAMETERS[4]);

        when(info.getHeaders()).thenReturn(headers);
        when(info.getRequestURL()).thenReturn(PARAMETERS[5]);

        // when
        Audit.info(log, info, STATUS);

        // then
        verify(log).info(eq(EXPECTED));
    }
}

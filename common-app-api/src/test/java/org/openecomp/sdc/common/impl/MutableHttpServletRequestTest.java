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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 */

package org.openecomp.sdc.common.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;

public class MutableHttpServletRequestTest {

    private HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    private MutableHttpServletRequest mutableHttpServletRequest;

    @Before
    public void setUp() {
        mutableHttpServletRequest = new MutableHttpServletRequest(servletRequest);
        mutableHttpServletRequest.putHeader("host", "Host");
    }

    @Test
    public void testGetHeader() {
        //when
        String customHeaderValue = mutableHttpServletRequest.getHeader("host");
        //then
        assertEquals(customHeaderValue, "Host");

        when(servletRequest.getHeader("notExistsKey")).thenReturn("wrappedValue");
        String wrappedHeaderValue = mutableHttpServletRequest.getHeader("notExistsKey");
        //then
        assertEquals(wrappedHeaderValue, "wrappedValue");
    }

    @Test
    public void testGetHeaderNames() {
        Set<String> set = new HashSet<>();
        when(servletRequest.getHeaderNames()).thenReturn(Collections.enumeration(set));
        Enumeration<String> headerNames = mutableHttpServletRequest.getHeaderNames();
        //then
        assertEquals(headerNames.nextElement(), "host");
    }
}

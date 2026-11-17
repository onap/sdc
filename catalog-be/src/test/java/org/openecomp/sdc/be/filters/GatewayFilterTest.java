/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.be.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class GatewayFilterTest {

    private static final String ENCRYPTED_USER_ID = "ZW5jcnlwdGVkLXVzZXItaWQ=";
    private static final String USER_ID = "cs0008";

    @InjectMocks
    private GatewayFilter gatewayFilter;
    @Mock
    private ThreadLocalUtils threadLocalUtils;
    @Mock
    private ComponentExceptionMapper componentExceptionMapper;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearUserContext() {
        ThreadLocalsHolder.setUserContext(null);
    }

    @Test
    void replacesEncryptedUserIdHeaderWithResolvedUserId() throws Exception {
        resolveUserTo(USER_ID);
        final MockHttpServletRequest request = requestWithUserId(ENCRYPTED_USER_ID);

        gatewayFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        final HttpServletRequest forwarded = forwardedRequest();
        assertEquals(USER_ID, forwarded.getHeader("USER_ID"));
        assertEquals(USER_ID, forwarded.getHeader("user_id"));
        assertEquals(Collections.singletonList(USER_ID), Collections.list(forwarded.getHeaders("USER_ID")));
        assertEquals("application/json", forwarded.getHeader("Accept"));
    }

    @Test
    void forwardsRequestUnchangedWhenHeaderIsAlreadyThePlainUserId() throws Exception {
        resolveUserTo(USER_ID);
        final MockHttpServletRequest request = requestWithUserId(USER_ID);

        gatewayFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertSame(request, forwardedRequest());
    }

    @Test
    void doesNotInventUserIdHeaderWhenNoneWasSent() throws Exception {
        resolveUserTo(USER_ID);
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/sdc2/rest/v1/catalog/services/1/properties");

        gatewayFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertSame(request, forwardedRequest());
    }

    private void resolveUserTo(final String userId) {
        doAnswer(invocation -> {
            ThreadLocalsHolder.setUserContext(new UserContext(userId, Set.of("DESIGNER"), "Carlos", "Santana"));
            return null;
        }).when(threadLocalUtils).setUserContextFromDB(any());
    }

    private static MockHttpServletRequest requestWithUserId(final String userId) {
        final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/sdc2/rest/v1/catalog/services/1/properties");
        request.addHeader("USER_ID", userId);
        request.addHeader("Accept", "application/json");
        return request;
    }

    private HttpServletRequest forwardedRequest() throws Exception {
        final ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), any());
        return (HttpServletRequest) captor.getValue();
    }
}

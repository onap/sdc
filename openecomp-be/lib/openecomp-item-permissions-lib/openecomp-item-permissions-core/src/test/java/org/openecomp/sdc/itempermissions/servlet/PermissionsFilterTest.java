/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.itempermissions.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.junit.Test;
import org.junit.Before;


public class PermissionsFilterTest {

    @Mock
    private PermissionsServices permissionsServicesMock;

    @InjectMocks
    @Spy
    private PermissionsFilter permissionsFilter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDoFilter() throws ServletException, IOException {
        HttpServletRequest httpServletRequest = Mockito.spy(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.spy(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        initializeMocking(httpServletRequest, httpServletResponse, filterChain);
        Mockito.when(httpServletRequest.getPathInfo()).thenReturn("onboardingci/onbrest/onboarding-api/v1.0");

        permissionsFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(filterChain, Mockito.times(1)).doFilter(Mockito.any(), Mockito.any());
    }

    @Test
    public void testDoFilterPass() throws ServletException, IOException {
        HttpServletRequest httpServletRequest = Mockito.spy(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = Mockito.spy(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);

        initializeMocking(httpServletRequest, httpServletResponse, filterChain);
        Mockito.when(httpServletRequest.getPathInfo()).thenReturn("onboardingci/onbrest/onboarding-api");
        permissionsFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(filterChain, Mockito.times(1)).doFilter(Mockito.any(), Mockito.any());
    }

    private void initializeMocking(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                   FilterChain filterChain) throws ServletException, IOException {
        PrintWriter printWriter = new PrintWriter(new ByteArrayOutputStream());
        Mockito.when(httpServletRequest.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(httpServletRequest.getHeader("USER_ID")).thenReturn("cs0008");
        Mockito.when(httpServletResponse.getWriter()).thenReturn(printWriter);
        Mockito.doNothing().when(filterChain).doFilter(Mockito.any(), Mockito.any());
        Mockito.when(permissionsServicesMock.isAllowed(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
    }
}

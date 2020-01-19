/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ReqValidationFilterTest {

    @InjectMocks
    private ReqValidationFilter reqValidationFilter;
    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ComponentExceptionMapper componentExceptionMapper;

    static ResponseFormatManager responseFormatManager = new ResponseFormatManager();
    private static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() {
        reqValidationFilter = new ReqValidationFilter();
    }

    @Test
    public void testValidRequestWithNoUser() throws IOException, ServletException {
        ThreadLocalsHolder.setUserContext(null);
        reqValidationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testValidRequestWithDesignerRole() throws IOException, ServletException {
        UserContext userContext = new UserContext("designer", new HashSet<>(Collections.singletonList(UserRoleEnum.DESIGNER.getName())),"f", "l");
        ThreadLocalsHolder.setUserContext(userContext);
        reqValidationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testValidRequestWithNoRoles() throws IOException, ServletException {
        UserContext userContext = new UserContext("designer", null,"f", "l");
        ThreadLocalsHolder.setUserContext(userContext);
        reqValidationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testValidRequestWithEmptyRoles() throws IOException, ServletException {
        UserContext userContext = new UserContext("designer", new HashSet<>(),"f", "l");
        ThreadLocalsHolder.setUserContext(userContext);
        reqValidationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testValidRequestWithAdminRole() throws IOException, ServletException {
        UserContext userContext = new UserContext("admin", new HashSet<>(Collections.singletonList(UserRoleEnum.ADMIN.getName())),"f", "l");
        ThreadLocalsHolder.setUserContext(userContext);
        reqValidationFilter.doFilter(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test(expected = ComponentException .class)
    public void testValidRequestWithTesterRole() throws IOException, ServletException {
        UserContext userContext = new UserContext("tester", new HashSet<>(Collections.singletonList(UserRoleEnum.TESTER.getName())),"f", "l");
        ThreadLocalsHolder.setUserContext(userContext);
        doThrow(ByActionStatusComponentException.class).when(componentExceptionMapper).writeToResponse(any(ComponentException.class),eq(response));
        reqValidationFilter.doFilter(request, response, filterChain);
    }
}

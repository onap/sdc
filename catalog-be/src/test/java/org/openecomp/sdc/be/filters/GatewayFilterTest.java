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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GatewayFilterTest {

    private static final List<String> excludedUrls = Arrays.asList("test1", "test2");
    private static final String cookieName = "myCookie";

    static ResponseFormatManager responseFormatManager = new ResponseFormatManager();
    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @InjectMocks
    private GatewayFilter filter;
    @Spy
    private ThreadLocalUtils threadLocalUtils;
    @Mock
    private Configuration.CookieConfig authCookieConf;
    @Mock
    private Configuration configuration;
    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ComponentExceptionMapper componentExceptionMapper;



    @Before
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Before
    public void setUp() throws ServletException {
        doNothing().when(threadLocalUtils).setUserContextFromDB(request);
        when(configuration.getAuthCookie()).thenReturn(authCookieConf);
        this.filter = new GatewayFilter(configuration);
        ThreadLocalsHolder.setApiType(null);
        assertNotNull(filter);
    }

    @Test
    public void validateRequestFromWhiteList() throws ServletException, IOException {
        when(authCookieConf.getExcludedUrls()).thenReturn(excludedUrls);
        when(request.getPathInfo()).thenReturn("test1");
        filter.doFilter(request, response, filterChain);
        assertTrue(ThreadLocalsHolder.getApiType().equals(FilterDecisionEnum.NA));
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    private Enumeration getHeaderEnumerationObj(List<String> arrlist){

        // creating object of type Enumeration<String>
        Enumeration<String> enumer = Collections.enumeration(arrlist);
        return enumer;
    }






    private Cookie[] getCookiesFromReq(boolean isFromRequest) {
        Cookie[] cookies = new Cookie [1];
        if (isFromRequest) {
            cookies[0] = new Cookie(cookieName, "cookieData");
        }
        else {
            cookies[0] = new Cookie("dummy", "cookieData");
        }
        return cookies;
    }

    private String getCookieNameFromConf(boolean isFromConfiguration) {
        Cookie[] cookies = new Cookie [1];
        if (isFromConfiguration) {
            cookies[0] = new Cookie(cookieName, "cookieData");
        }
        else {
            cookies[0] = new Cookie("dummy", "cookieData");
        }
        return cookies[0].getName();
    }
}
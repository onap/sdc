package org.openecomp.sdc.be.filters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
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
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


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

    @Test(expected = ComponentException.class)
    public void validateRequestFromWhiteListNegative() throws ServletException, IOException {
        when(authCookieConf.getExcludedUrls()).thenReturn(excludedUrls);
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        doThrow(ByActionStatusComponentException.class).when(componentExceptionMapper).writeToResponse(any(ComponentException.class),eq(response));
        filter.doFilter(request, response, filterChain);
        assertNull(ThreadLocalsHolder.getApiType());
    }

    @Test
    public void validateRequestFromUI() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        when(request.getCookies()).thenReturn(getCookiesFromReq(true));
        when(authCookieConf.getCookieName()).thenReturn(getCookieNameFromConf(true));
        filter.doFilter(request, response, filterChain);
        assertTrue(ThreadLocalsHolder.getApiType().equals(FilterDecisionEnum.INTERNAL));
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test(expected = ComponentException.class)
    public void validateRequestFromUItNegative() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        when(request.getCookies()).thenReturn(getCookiesFromReq(true));
        when(authCookieConf.getCookieName()).thenReturn(getCookieNameFromConf(false));
        doThrow(ByActionStatusComponentException.class).when(componentExceptionMapper).writeToResponse(any(ComponentException.class),eq(response));
        filter.doFilter(request, response, filterChain);
        assertNull(ThreadLocalsHolder.getApiType());
    }

    @Test(expected = ComponentException.class)
    public void validateRequestFromUItNegative2() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        when(request.getCookies()).thenReturn(getCookiesFromReq(false));
        when(authCookieConf.getCookieName()).thenReturn(getCookieNameFromConf(true));
        doThrow(ByActionStatusComponentException.class).when(componentExceptionMapper).writeToResponse(any(ComponentException.class),eq(response));
        filter.doFilter(request, response, filterChain);
        assertNull(ThreadLocalsHolder.getApiType());
    }

    @Test
    public void validateRequestWithAuth() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        when(request.getCookies()).thenReturn(getCookiesFromReq(true));
        when(authCookieConf.getCookieName()).thenReturn(getCookieNameFromConf(false));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(HttpHeaders.AUTHORIZATION);
        filter.doFilter(request, response, filterChain);
        assertTrue(ThreadLocalsHolder.getApiType().equals(FilterDecisionEnum.EXTERNAL));
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test (expected = ComponentException.class)
    public void validateRequestWithAuthNegative() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("NOT_FROM_LIST");
        when(request.getCookies()).thenReturn(getCookiesFromReq(true));
        when(authCookieConf.getCookieName()).thenReturn(getCookieNameFromConf(false));
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        doThrow(ByActionStatusComponentException.class).when(componentExceptionMapper).writeToResponse(any(ComponentException.class),eq(response));
        filter.doFilter(request, response, filterChain);
        assertNull(ThreadLocalsHolder.getApiType());
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
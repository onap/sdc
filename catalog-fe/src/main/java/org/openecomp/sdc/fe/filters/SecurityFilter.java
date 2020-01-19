package org.openecomp.sdc.fe.filters;


import org.apache.commons.lang3.StringUtils;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.openecomp.sdc.common.log.wrappers.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class SecurityFilter implements Filter {
    private static final Logger log = Logger.getLogger(SecurityFilter.class.getName());

    private static final String PORTAL_COOKIE_NAME_IS_NOT_SET = "Portal cookie name is not set in portal.properties file";

    private List<String> excludedUrls;

    static final String PORTAL_COOKIE_NAME_KEY = "portal_cookie_name";
    static final String PORTAL_REDIRECT_URL_KEY = "ecomp_redirect_url";
    static final String FILTER_EXLUDED_URLS_KEY ="excludedUrls";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        excludedUrls = Arrays.asList(filterConfig.getInitParameter(FILTER_EXLUDED_URLS_KEY).split(","));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        log.debug("SecurityFilter received request with URL {}", httpRequest.getRequestURL());
        //add redirecting to Portal if cookie is not provided
        if (!excludedUrls.contains(httpRequest.getServletPath()) && !isRequestFromPortal(httpRequest.getCookies())) {
            //redirect to portal app
            log.debug("Request {} is not from Portal, redirecting there", httpRequest.getServletPath());
            httpResponse.sendRedirect(PortalApiProperties.getProperty(PORTAL_REDIRECT_URL_KEY));
        }
        else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private boolean isRequestFromPortal(Cookie[] cookies) {
        String portalCookieValue = PortalApiProperties.getProperty(PORTAL_COOKIE_NAME_KEY);
        if (StringUtils.isEmpty(portalCookieValue)) {
            log.error(PORTAL_COOKIE_NAME_IS_NOT_SET);
            throw new NoSuchElementException(PORTAL_COOKIE_NAME_IS_NOT_SET);
        }
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(c->StringUtils.equals(c.getName(), portalCookieValue));
    }
}

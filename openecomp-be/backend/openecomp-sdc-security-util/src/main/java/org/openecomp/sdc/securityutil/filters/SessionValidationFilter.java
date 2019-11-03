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
 */

package org.openecomp.sdc.securityutil.filters;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.securityutil.AuthenticationCookieUtils;
import org.openecomp.sdc.securityutil.CipherUtilException;
import org.openecomp.sdc.securityutil.ISessionValidationFilterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.apache.commons.lang.StringUtils;

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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class SessionValidationFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(SessionValidationFilter.class.getName());
    private ISessionValidationFilterConfiguration filterConfiguration;
    private List<String> excludedUrls;

    private static final String REQUEST_ID = ONAPLogConstants.MDCs.REQUEST_ID;
    private static final String ONAP_REQUEST_ID_HEADER = ONAPLogConstants.Headers.REQUEST_ID;
    private static final String REQUEST_ID_HEADER = "X-RequestID";
    private static final String TRANSACTION_ID_HEADER = "X-TransactionId";
    private static final String ECOMP_REQUEST_ID_HEADER = "X-ECOMP-RequestID";

    private static final String PARTNER_NAME = ONAPLogConstants.MDCs.PARTNER_NAME;
    private static final String USER_ID_HEADER = "USER_ID";
    private static final String ONAP_PARTNER_NAME_HEADER = ONAPLogConstants.Headers.PARTNER_NAME;
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String UNKNOWN = "UNKNOWN";


    public abstract ISessionValidationFilterConfiguration getFilterConfiguration();
    protected abstract Cookie addRoleToCookie(Cookie updatedCookie);
    protected abstract boolean isRoleValid(Cookie cookie);

    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
        filterConfiguration = getFilterConfiguration();
        excludedUrls = filterConfiguration.getExcludedUrls();
    }

    @Override
    public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        long startTime = System.nanoTime();
        fillMDCFromHeaders(httpRequest);
        log.debug("SessionValidationFilter: Validation started, received request with URL {}", httpRequest.getRequestURL());

        // request preprocessing
        boolean isContinueProcessing = preProcessingRequest(servletRequest, servletResponse, filterChain, httpRequest, httpResponse, startTime);
        List<Cookie> cookies = null;
        Cookie extractedCookie = null;

        // request processing
        if (isContinueProcessing) {
            cookies = extractAuthenticationCookies(httpRequest.getCookies());
            extractedCookie = cookies.get(0);
            isContinueProcessing = processRequest(httpRequest, httpResponse, extractedCookie);
        }

        // response processing
        if(isContinueProcessing){
            log.debug("SessionValidationFilter: Cookie from request {} is valid, passing request to session extension ...", httpRequest.getRequestURL());
            Cookie updatedCookie = processResponse(extractedCookie);
            cleanResponceFromLeftoverCookies(httpResponse, cookies);
            log.debug("SessionValidationFilter: request {} passed all validations, passing request to endpoint ...", httpRequest.getRequestURL());
            httpResponse.addCookie(updatedCookie);
            long durationSec = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            long durationMil = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            log.debug("SessionValidationFilter: Validation ended, running time for URL {} is: {} seconds {} miliseconds", httpRequest.getPathInfo(), durationSec, durationMil);
            filterChain.doFilter(servletRequest, httpResponse);
        }
    }


    private boolean preProcessingRequest(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain, HttpServletRequest httpRequest, HttpServletResponse httpResponse, long startTime) throws IOException, ServletException {

        boolean isPreProcessingSucceeded = true;
        if (isUrlFromWhiteList(httpRequest)) {
            log.debug("SessionValidationFilter: URL {} excluded from access validation , passing request to endpoint ... ", httpRequest.getRequestURL());
            long durationSec = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            long durationMil = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            log.debug("SessionValidationFilter: Validation ended, running time for URL {} is: {} seconds {} miliseconds", httpRequest.getPathInfo(), durationSec, durationMil);
            filterChain.doFilter(servletRequest, servletResponse);
            isPreProcessingSucceeded = false;

        } else if (!isCookiePresent(httpRequest.getCookies())) {
            //redirect to portal app
            log.debug("SessionValidationFilter: Cookie from request {} is not valid, redirecting request to portal", httpRequest.getRequestURL());
            httpResponse.sendRedirect(filterConfiguration.getRedirectURL());
            isPreProcessingSucceeded = false;
        }
        return isPreProcessingSucceeded;
    }

    private boolean processRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Cookie cookie) throws IOException {
        boolean isProcessSuccessful= true;
        try {
            if (AuthenticationCookieUtils.isSessionExpired(cookie, filterConfiguration)) {
                //redirect to portal app
                log.debug("SessionValidationFilter: Session is expired, redirecting request {} to portal", httpRequest.getRequestURL());
                httpResponse.sendRedirect(filterConfiguration.getRedirectURL());
                isProcessSuccessful = false;
            }
        } catch (CipherUtilException e) {
            log.error("SessionValidationFilter: Cookie decryption error : {}", e.getMessage());
            log.debug("SessionValidationFilter: Cookie decryption error : {}", e.getMessage(), e);
            isProcessSuccessful = false;
        }

        if (!isRoleValid(cookie)) {
            //redirect to portal app
            log.debug("SessionValidationFilter: Role is not valid, redirecting request {} to portal", httpRequest.getRequestURL());
            httpResponse.sendRedirect(filterConfiguration.getRedirectURL());
            isProcessSuccessful = false;
        }
        return isProcessSuccessful;
    }

    private Cookie processResponse(Cookie cookie) throws IOException, ServletException {
        Cookie updatedCookie;
        try {
            updatedCookie = AuthenticationCookieUtils.updateSessionTime(cookie, filterConfiguration);
        } catch (CipherUtilException e) {
            log.error("SessionValidationFilter: Cookie cipher error ...");
            log.debug("SessionValidationFilter: Cookie cipher error : {}", e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
        updatedCookie = addRoleToCookie(updatedCookie);
        return updatedCookie;
    }

    private boolean isCookiePresent(Cookie[] cookies) {
        if (cookies == null) {
            return false;
        }
        String actualCookieName = filterConfiguration.getCookieName();
        boolean isPresent = Arrays.stream(cookies).anyMatch(c -> isCookieNameMatch(actualCookieName, c));
        if (!isPresent) {
            log.error("SessionValidationFilter: Session Validation Cookie missing ...");
            return false;
        }
        return true;
    }

    private List<Cookie> extractAuthenticationCookies(Cookie[] cookies) {
        String actualCookieName = filterConfiguration.getCookieName();
        log.debug("SessionValidationFilter: Extracting authentication cookies, {} cookies in request", cookies.length);
        List<Cookie> authenticationCookies = Arrays.stream(cookies).filter(c -> isCookieNameMatch(actualCookieName, c)).collect(Collectors.toList());
        log.debug("SessionValidationFilter: Extracted {} authentication cookies from request", authenticationCookies.size());
        if( authenticationCookies.size() > 1 ){
            authenticationCookies.forEach( cookie -> log.debug("SessionValidationFilter: Multiple cookies found cookie name, {} cookie value {}", cookie.getName(), cookie.getValue()));
        }
        return authenticationCookies;
    }


    // use contains for matching due issue with ecomp portal ( change cookie name, add prefix ), temp solution
    private boolean isCookieNameMatch(String actualCookieName, Cookie c) {
        return c.getName().contains(actualCookieName);
    }

    private boolean isUrlFromWhiteList(HttpServletRequest httpRequest) {
        if (httpRequest.getPathInfo() == null){
            final String servletPath = httpRequest.getServletPath().toLowerCase();
            log.debug("SessionValidationFilter: pathInfo is null, trying to check by servlet path white list validation -> ServletPath: {} ", servletPath);
            return excludedUrls.stream().
                    anyMatch( e -> servletPath.matches(e));
        }
        String pathInfo = httpRequest.getPathInfo().toLowerCase();
        log.debug("SessionValidationFilter: white list validation ->  PathInfo: {} ", pathInfo);
        return excludedUrls.stream().
                anyMatch( e -> pathInfo.matches(e));
    }

    private void cleanResponceFromLeftoverCookies(HttpServletResponse httpResponse, List<Cookie> cookiesList) {
        for (Cookie cookie:cookiesList){
            Cookie cleanCookie = AuthenticationCookieUtils.createUpdatedCookie(cookie, null, filterConfiguration);
            cleanCookie.setMaxAge(0);
            log.debug("SessionValidationFilter Cleaning Cookie cookie name: {} added to responce", cleanCookie.getName());
            httpResponse.addCookie(cleanCookie);
        }
    }

    public static void fillMDCFromHeaders(HttpServletRequest httpServletRequest) {
        fillRequestIdFromHeader(httpServletRequest);
        fillPartnerNameFromHeader(httpServletRequest);

    }

    private static void fillRequestIdFromHeader(HttpServletRequest httpServletRequest){
        if (MDC.get(REQUEST_ID) == null) {
            if (StringUtils.isNotEmpty(httpServletRequest.getHeader(ONAP_REQUEST_ID_HEADER))) {
                MDC.put(REQUEST_ID, httpServletRequest.getHeader(ONAP_REQUEST_ID_HEADER));
            } else if (StringUtils.isNotEmpty(httpServletRequest.getHeader(REQUEST_ID_HEADER))) {
                MDC.put(REQUEST_ID, httpServletRequest.getHeader(REQUEST_ID_HEADER));
            } else if (StringUtils.isNotEmpty(httpServletRequest.getHeader(TRANSACTION_ID_HEADER))) {
                MDC.put(REQUEST_ID, httpServletRequest.getHeader(TRANSACTION_ID_HEADER));
            } else if (StringUtils.isNotEmpty(httpServletRequest.getHeader(ECOMP_REQUEST_ID_HEADER))) {
                MDC.put(REQUEST_ID, httpServletRequest.getHeader(ECOMP_REQUEST_ID_HEADER));
            } else {
                MDC.put(REQUEST_ID, UUID.randomUUID().toString());
            }
        }
    }

    private static void fillPartnerNameFromHeader(HttpServletRequest httpServletRequest){
        if (MDC.get(PARTNER_NAME) == null) {
            if (StringUtils.isNotEmpty(httpServletRequest.getHeader(USER_ID_HEADER))) {
                MDC.put(PARTNER_NAME, httpServletRequest.getHeader(USER_ID_HEADER));
            } else if (StringUtils.isNotEmpty(httpServletRequest.getHeader(ONAP_PARTNER_NAME_HEADER))) {
                MDC.put(PARTNER_NAME, httpServletRequest.getHeader(ONAP_PARTNER_NAME_HEADER));
            } else if (StringUtils.isNotEmpty(httpServletRequest.getHeader(USER_AGENT_HEADER))) {
                MDC.put(PARTNER_NAME, httpServletRequest.getHeader(USER_AGENT_HEADER));
            }  else {
                MDC.put(PARTNER_NAME, UNKNOWN);
            }
        }
    }


    @Override
    public void destroy() {

    }
}

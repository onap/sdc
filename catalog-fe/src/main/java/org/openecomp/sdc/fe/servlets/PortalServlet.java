/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.fe.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.sdc.security.AuthenticationCookie;
import org.onap.sdc.security.RepresentationUtils;
import org.openecomp.sdc.common.impl.MutableHttpServletRequest;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.fe.Constants;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;

/**
 * Root resource (exposed at "/" path)
 */
@Path("/")
public class PortalServlet extends HttpServlet {

    public static final String MISSING_HEADERS_MSG = "Missing Headers In Request";
    private static final long serialVersionUID = 1L;
    private static final String AUTHORIZATION_ERROR_MSG = "Autherization error";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static Logger log = Logger.getLogger(PortalServlet.class.getName());

    private static String getUserIdFromCookie(HttpServletRequest request) throws CipherUtilException {
        String userId = "";
        Cookie[] cookies = request.getCookies();
        Cookie userIdcookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Constants.ECOMP_PORTAL_COOKIE)) {
                    userIdcookie = cookie;
                }
            }
        }
        if (userIdcookie != null) {
            userId = CipherUtil.decrypt(userIdcookie.getValue());
        }
        return userId;
    }

    private static String getValueFromCookie(HttpServletRequest request, String cookieName) {
        String value = "";
        Cookie[] cookies = request.getCookies();
        Cookie valueFromCookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().endsWith(cookieName)) {
                    valueFromCookie = cookie;
                }
            }
        }
        if (valueFromCookie != null) {
            value = valueFromCookie.getValue();
        }
        return value;
    }

    /**
     * Entry point from ECOMP portal
     */
    @GET
    @Path("/portal")
    @Override
    public void doGet(@Context final HttpServletRequest request, @Context final HttpServletResponse response) {
        try {
            addRequestHeadersUsingWebseal(request, response);
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFePortalServletError("Portal Servlet");
            log.error("Error during getting portal page", e);
        }
    }

    /**
     * Building new HTTP request and setting headers for the request The request will dispatch to index.html
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void addRequestHeadersUsingWebseal(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/html");
        // Create new request object to dispatch
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        // Get configuration object (reads data from configuration.yaml)
        Configuration configuration = getConfiguration(request);
        // Check if we got header from webseal
        String userId = request.getHeader(Constants.WEBSEAL_USER_ID_HEADER);
        if (null == userId) {
            // Authentication via ecomp portal
            try {
                String userIdFromCookie = getUserIdFromCookie(request);
                if (("").equals(userIdFromCookie)) {
                    // This is probably a webseal request, so missing header in request should be printed.
                    response.sendError(HttpServletResponse.SC_USE_PROXY, MISSING_HEADERS_MSG);
                }
                userId = userIdFromCookie;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_USE_PROXY, AUTHORIZATION_ERROR_MSG);
                log.error("Error during adding request header", e);
            }
        }
        // Replace webseal header with open source header
        mutableRequest.putHeader(Constants.USER_ID, userId);
        // Getting identification headers from configuration.yaml

        // (identificationHeaderFields) and setting them to new request

        // mutableRequest
        List<List<String>> identificationHeaderFields = configuration.getIdentificationHeaderFields();
        for (List<String> possibleHeadersToRecieve : identificationHeaderFields) {
            String allowedHeaderToPass = possibleHeadersToRecieve.get(0);
            setNewHeader(possibleHeadersToRecieve, allowedHeaderToPass, request, mutableRequest);
        }
        // Getting optional headers from configuration.yaml

        // (optionalHeaderFields) and setting them to new request mutableRequest
        List<List<String>> optionalHeaderFields = configuration.getOptionalHeaderFields();
        for (List<String> possibleHeadersToRecieve : optionalHeaderFields) {
            String allowedHeaderToPass = possibleHeadersToRecieve.get(0);
            setNewHeader(possibleHeadersToRecieve, allowedHeaderToPass, request, mutableRequest);
        }
        // Print headers from original request for debug purposes
        printHeaders(request);
        // In case using webseal, validate all mandatory headers (identificationHeaderFields) are included in the new request (mutableRequest).

        // Via ecomp portal do not need to check the headers.
        boolean allHeadersExist = true;
        if (null != request.getHeader(Constants.WEBSEAL_USER_ID_HEADER)) {
            allHeadersExist = checkHeaders(mutableRequest);
        }
        if (allHeadersExist) {
            addCookies(response, mutableRequest, getMandatoryHeaders(request));
            addCookies(response, mutableRequest, getOptionalHeaders(request));
            getValueFromCookie(request, Constants.HTTP_CSP_FIRSTNAME);
            getValueFromCookie(request, Constants.HTTP_CSP_LASTNAME);
            //To be fixed

            //addAuthCookie(response, userId, firstNameFromCookie, lastNameFromCookie);
            RequestDispatcher rd = request.getRequestDispatcher("index.html");
            rd.forward(mutableRequest, response);
        } else {
            response.sendError(HttpServletResponse.SC_USE_PROXY, MISSING_HEADERS_MSG);
        }
    }

    boolean addAuthCookie(HttpServletResponse response, String userId, String firstName, String lastName) throws IOException {
        boolean isBuildCookieCompleted = true;
        Cookie authCookie = null;
        Configuration.CookieConfig confCookie = ConfigurationManager.getConfigurationManager().getConfiguration().getAuthCookie();
        //create authentication and send it to encryption
        String encryptedCookie = "";
        try {
            AuthenticationCookie authenticationCookie = new AuthenticationCookie(userId, firstName, lastName);
            String cookieAsJson = RepresentationUtils.toRepresentation(authenticationCookie);
            encryptedCookie = org.onap.sdc.security.CipherUtil.encryptPKC(cookieAsJson, confCookie.getSecurityKey());
        } catch (Exception e) {
            isBuildCookieCompleted = false;
            log.error(" Cookie Encryption failed ", e);
        }
        authCookie = new Cookie(confCookie.getCookieName(), encryptedCookie);
        authCookie.setPath(confCookie.getPath());
        authCookie.setDomain(confCookie.getDomain());
        authCookie.setHttpOnly(true);
        // add generated cookie to response
        if (isBuildCookieCompleted) {
            response.addCookie(authCookie);
            return true;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, AUTHORIZATION_ERROR_MSG);
        return false;
    }

    /**
     * Print all request headers to the log
     *
     * @param request
     */
    private void printHeaders(HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            String sessionId = "";
            if (request.getSession() != null) {
                String id = request.getSession().getId();
                if (id != null) {
                    sessionId = id;
                }
            }
            builder.append("Receiving request with headers:" + NEW_LINE);
            log.debug("{}", request.getHeaderNames());
            @SuppressWarnings("unchecked") Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    String headerValue = request.getHeader(headerName);
                    builder.append("session " + sessionId + " header: name = " + headerName + ", value = " + headerValue + NEW_LINE);
                }
            }
            log.debug(builder.toString());
        }
    }

    /**
     * Add cookies (that where set in the new request headers) in the response Using DefaultHTTPUtilities Object to prevent CRLF injection in HTTP
     * headers.
     *
     * @param response
     * @param request
     * @param headers
     */
    private void addCookies(final HttpServletResponse response, final HttpServletRequest request, final String[] headers) {
        for (var i = 0; i < headers.length; i++) {
            final var currHeader = ValidationUtils.sanitizeInputString(headers[i]);
            final var headerValue = ValidationUtils.sanitizeInputString(request.getHeader(currHeader));
            if (headerValue != null) {
                final var cookie = new Cookie(currHeader, headerValue);
                cookie.setSecure(true);
                response.addCookie(cookie);
            }
        }
    }

    /**
     * Get mandatory headers (identificationHeaderFields) String array, and checks that each header exists in the new request
     *
     * @param request
     * @return boolean
     */
    private boolean checkHeaders(HttpServletRequest request) {
        String[] mandatoryHeaders = getMandatoryHeaders(request);
        boolean allHeadersExist = true;
        for (int i = 0; i < mandatoryHeaders.length; i++) {
            String headerValue = request.getHeader(mandatoryHeaders[i]);
            if (headerValue == null) {
                allHeadersExist = false;
                break;
            }
        }
        return allHeadersExist;
    }

    /**
     * Get mandatory headers (identificationHeaderFields) from configuration.yaml file and return String[]
     *
     * @param request
     * @return String[]
     */
    private String[] getMandatoryHeaders(HttpServletRequest request) {
        Configuration configuration = getConfiguration(request);
        List<List<String>> identificationHeaderFields = configuration.getIdentificationHeaderFields();
        String[] mandatoryHeaders = new String[identificationHeaderFields.size()];
        for (int i = 0; i < identificationHeaderFields.size(); i++) {
            mandatoryHeaders[i] = identificationHeaderFields.get(i).get(0);
        }
        return mandatoryHeaders;
    }

    /**
     * Get optional headers (optionalHeaderFields) from configuration.yaml file and return String[]
     *
     * @param request
     * @return String[]
     */
    private String[] getOptionalHeaders(HttpServletRequest request) {
        Configuration configuration = getConfiguration(request);
        List<List<String>> optionalHeaderFields = configuration.getOptionalHeaderFields();
        String[] optionalHeaders = new String[optionalHeaderFields.size()];
        for (int i = 0; i < optionalHeaderFields.size(); i++) {
            optionalHeaders[i] = optionalHeaderFields.get(i).get(0);
        }
        return optionalHeaders;
    }

    /**
     * Return Configuration object to read from configuration.yaml
     *
     * @param request
     * @return Configuration
     */
    private Configuration getConfiguration(HttpServletRequest request) {
        ConfigurationManager configManager = (ConfigurationManager) request.getSession().getServletContext()
            .getAttribute(org.openecomp.sdc.common.api.Constants.CONFIGURATION_MANAGER_ATTR);
        return configManager.getConfiguration();
    }

    private boolean setNewHeader(List<String> possibleOldHeaders, String newHeaderToSet, HttpServletRequest oldRequest,
                                 MutableHttpServletRequest newRequest) {
        boolean newHeaderIsSet = false;
        for (int i = 0; i < possibleOldHeaders.size() && !newHeaderIsSet; i++) {
            String headerValue = oldRequest.getHeader(possibleOldHeaders.get(i));
            if (headerValue != null) {
                newRequest.putHeader(newHeaderToSet, headerValue);
                newHeaderIsSet = true;
            }
        }
        return newHeaderIsSet;
    }
}

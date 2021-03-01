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

package org.openecomp.sdc.be.filters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.server.ContainerRequest;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.StringTokenizer;

@Priority(10)
public class BasicAuthenticationFilter implements ContainerRequestFilter {


	private static LoggerSdcAudit audit = new LoggerSdcAudit(BasicAuthenticationFilter.class);
    private static final Logger log = Logger.getLogger(BasicAuthenticationFilter.class);
    private static final String COMPONENT_UTILS_FAILED = "Authentication Filter Failed to get component utils.";
    private static final ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();
    private static final Configuration.BasicAuthConfig basicAuthConf = configurationManager.getConfiguration().getBasicAuth();

    @Context
    private HttpServletRequest sr;

    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private String realm = "ASDC";


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        audit.startLog(requestContext);

        if (!basicAuthConf.getEnabled()) {
            return;
        }
        List<String> excludedUrls = Arrays.asList(basicAuthConf.getExcludedUrls().split(","));
        if (excludedUrls.contains(((ContainerRequest) requestContext).getRequestUri().getPath())) {
            return;
        }

        String authHeader = requestContext.getHeaderString(Constants.AUTHORIZATION_HEADER);
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            String failedToRetrieveAuthErrorMsg = "Authentication Filter Failed Couldn't retrieve authentication, no basic authentication.";
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();
                if ("Basic".equalsIgnoreCase(basic)) {
                    String credentials = new String(Base64.decodeBase64(st.nextToken()), StandardCharsets.UTF_8);
                    log.debug("Credentials: {}", credentials);
                    checkUserCredentials(requestContext, credentials);
                } else {
                    log.error(failedToRetrieveAuthErrorMsg);
                    authInvalidHeaderError(requestContext);
                }
            } else {
                log.error(failedToRetrieveAuthErrorMsg);
                authInvalidHeaderError(requestContext);
            }
        } else {
            log.error("Authentication Filter Failed no authorization header");
            authRequiredError(requestContext);
        }

    }

	private void checkUserCredentials(ContainerRequestContext requestContext, String credentials) {
        int p = credentials.indexOf(':');
        if (p != -1) {
            String userName = credentials.substring(0, p).trim();
            String password = credentials.substring(p + 1).trim();

            if (!userName.equals(basicAuthConf.getUserName()) || !password.equals(basicAuthConf.getUserPass())) {
                log.error("Authentication Failed. Invalid userName or password");
                authInvalidPasswordError(requestContext, userName);
            }
          authSuccessful(requestContext, userName);
        } else {
            log.error("Authentication Filter Failed Couldn't retrieve authentication, no basic authentication.");
            authInvalidHeaderError(requestContext);
        }
    }

    private void authSuccessful(ContainerRequestContext requestContext, String userName) {
        ComponentsUtils componentUtils = getComponentsUtils();
        if (componentUtils == null) {
            abortWith(requestContext, COMPONENT_UTILS_FAILED, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        } else {
            componentUtils
                .auditAuthEvent(requestContext.getUriInfo().getPath(), userName, AuthStatus.AUTH_SUCCESS.toString(),
                    realm);
        }
    }

    private void authInvalidPasswordError(ContainerRequestContext requestContext, String userName) {
        ComponentsUtils componentUtils = getComponentsUtils();
        if (componentUtils == null) {
            abortWith(requestContext, COMPONENT_UTILS_FAILED, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        } else {
            componentUtils.auditAuthEvent(requestContext.getUriInfo().getPath(), userName,
                AuthStatus.AUTH_FAILED_INVALID_PASSWORD.toString(), realm);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED);
            abortWith(requestContext, responseFormat.getFormattedMessage(), buildErrorResponse(responseFormat, false));
        }
    }

    private void authInvalidHeaderError(ContainerRequestContext requestContext) {
        ComponentsUtils componentUtils = getComponentsUtils();
        if (componentUtils == null) {
			abortWith(requestContext, COMPONENT_UTILS_FAILED, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
        getComponentsUtils().auditAuthEvent(requestContext.getUriInfo().getPath(), "", AuthStatus.AUTH_FAILED_INVALID_AUTHENTICATION_HEADER.toString(), realm);
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED_INVALIDE_HEADER);
		abortWith(requestContext, responseFormat.getFormattedMessage(), buildErrorResponse(responseFormat, false));
    }

    private void authRequiredError(ContainerRequestContext requestContext) {
        ComponentsUtils componentUtils = getComponentsUtils();
        if (componentUtils == null) {
			abortWith(requestContext, COMPONENT_UTILS_FAILED, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
        getComponentsUtils().auditAuthEvent(requestContext.getUriInfo().getPath(), "", AuthStatus.AUTH_REQUIRED.toString(), realm);
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_REQUIRED);
		abortWith(requestContext, responseFormat.getFormattedMessage(), buildErrorResponse(responseFormat, true));
    }

    private ComponentsUtils getComponentsUtils() {
        ServletContext context = sr.getSession().getServletContext();
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ComponentsUtils.class);
    }

    public enum AuthStatus {
        AUTH_REQUIRED, AUTH_FAILED_USER_NOT_FOUND, AUTH_FAILED_INVALID_PASSWORD, AUTH_FAILED_INVALID_AUTHENTICATION_HEADER, AUTH_SUCCESS
    }

    protected Response buildErrorResponse(ResponseFormat requestErrorWrapper, boolean addWwwAuthenticationHeader) {
        ResponseBuilder responseBuilder = Response.status(requestErrorWrapper.getStatus());
        if (addWwwAuthenticationHeader) {
            responseBuilder = responseBuilder.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        }
		return responseBuilder.entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
    }

	private void abortWith(ContainerRequestContext requestContext, String message, Response response) {

		audit.logEntry(sr.getRemoteAddr(),
				requestContext,
//				response.getStatusInfo(),
				LogLevel.ERROR,
				Severity.WARNING,
				message, null);

		log.error(message);
		audit.clearMyData();
		requestContext.abortWith(response);
	}
}

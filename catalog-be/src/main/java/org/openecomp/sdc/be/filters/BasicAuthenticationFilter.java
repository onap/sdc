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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.security.Passwords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

@Priority(10)
public class BasicAuthenticationFilter implements ContainerRequestFilter {

	@Context
	private HttpServletRequest sr;

	protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private String realm = "ASDC";

	private static Logger log = LoggerFactory.getLogger(BasicAuthenticationFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String authHeader = requestContext.getHeaderString(Constants.AUTHORIZATION_HEADER);
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
						log.debug("Credentials: {}" , credentials);
						checkUserCredentiles(requestContext, credentials);
					} catch (UnsupportedEncodingException e) {
						log.error("Authentication Filter Failed Couldn't retrieve authentication", e);
						authInvalidHeaderError(requestContext);
					}
				} else {
					log.error("Authentication Filter Failed Couldn't retrieve authentication, no basic autantication.");
					authInvalidHeaderError(requestContext);
				}
			} else {
				log.error("Authentication Filter Failed Couldn't retrieve authentication, no basic autantication.");
				authInvalidHeaderError(requestContext);
			}

		} else {
			log.error("Authentication Filter Failed no autharization header");
			authRequiredError(requestContext);
		}
	}

	private void checkUserCredentiles(ContainerRequestContext requestContext, String credentials) {
		int p = credentials.indexOf(":");
		if (p != -1) {
			String _username = credentials.substring(0, p).trim();
			String _password = credentials.substring(p + 1).trim();

			ConsumerBusinessLogic consumerBL = getConsumerBusinessLogic();
			if (consumerBL == null) {
				log.error("Authentication Filter Failed to get consumerBL.");
				requestContext.abortWith(Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			} else {
				Either<ConsumerDefinition, ResponseFormat> result = consumerBL.getConsumer(_username);
				validatePassword(requestContext, _username, _password, result);
			}
		} else {
			log.error("Authentication Filter Failed Couldn't retrieve authentication, no basic autantication.");
			authInvalidHeaderError(requestContext);

		}
	}

	private void validatePassword(ContainerRequestContext requestContext, String _username, String _password, Either<ConsumerDefinition, ResponseFormat> result) {
		if (result.isRight()) {
			Integer status = result.right().value().getStatus();
			if (status == Status.NOT_FOUND.getStatusCode()) {
				log.error("Authentication Filter Failed Couldn't find user");
				authUserNotFoundError(requestContext, _username);
			} else {
				log.error("Authentication Filter Failed to get consumerBL.");
				requestContext.abortWith(Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build());
			}
		} else {
			ConsumerDefinition consumerCredentials = result.left().value();
			if (!Passwords.isExpectedPassword(_password, consumerCredentials.getConsumerSalt(), consumerCredentials.getConsumerPassword())) {
				log.error("Authentication Filter Failed invalide password");
				authInvalidePasswordError(requestContext, _username);
			} else {
				authSuccesessful(requestContext, _username);
			}
		}
	}

	private void authSuccesessful(ContainerRequestContext requestContext, String _username) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Authentication Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		componentUtils.auditAuthEvent(AuditingActionEnum.AUTH_REQUEST, requestContext.getUriInfo().getPath(), _username, AuthStatus.AUTH_SUCCESS.toString(), realm);
	}

	private void authInvalidePasswordError(ContainerRequestContext requestContext, String _username) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Authentication Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		componentUtils.auditAuthEvent(AuditingActionEnum.AUTH_REQUEST, requestContext.getUriInfo().getPath(), _username, AuthStatus.AUTH_FAILED_INVALID_PASSWORD.toString(), realm);
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED);
		requestContext.abortWith(buildErrorResponse(responseFormat, false));
	}

	private void authUserNotFoundError(ContainerRequestContext requestContext, String _username) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Authentication Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		getComponentsUtils().auditAuthEvent(AuditingActionEnum.AUTH_REQUEST, requestContext.getUriInfo().getPath(), _username, AuthStatus.AUTH_FAILED_USER_NOT_FOUND.toString(), realm);
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED);
		requestContext.abortWith(buildErrorResponse(responseFormat, false));
	}

	private void authInvalidHeaderError(ContainerRequestContext requestContext) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Authentication Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		getComponentsUtils().auditAuthEvent(AuditingActionEnum.AUTH_REQUEST, requestContext.getUriInfo().getPath(), "", AuthStatus.AUTH_FAILED_INVALID_AUTHENTICATION_HEADER.toString(), realm);
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_FAILED_INVALIDE_HEADER);
		requestContext.abortWith(buildErrorResponse(responseFormat, false));
	}

	private void authRequiredError(ContainerRequestContext requestContext) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Authentication Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		getComponentsUtils().auditAuthEvent(AuditingActionEnum.AUTH_REQUEST, requestContext.getUriInfo().getPath(), "", AuthStatus.AUTH_REQUIRED.toString(), realm);
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.AUTH_REQUIRED);
		requestContext.abortWith(buildErrorResponse(responseFormat, true));
	}

	private ComponentsUtils getComponentsUtils() {
		ServletContext context = sr.getSession().getServletContext();
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ComponentsUtils componentsUtils = webApplicationContext.getBean(ComponentsUtils.class);
		return componentsUtils;
	}

	private ConsumerBusinessLogic getConsumerBusinessLogic() {
		ServletContext context = sr.getSession().getServletContext();
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ConsumerBusinessLogic consumerBusinessLogic = webApplicationContext.getBean(ConsumerBusinessLogic.class);
		return consumerBusinessLogic;
	}

	public enum AuthStatus {
		AUTH_REQUIRED, AUTH_FAILED_USER_NOT_FOUND, AUTH_FAILED_INVALID_PASSWORD, AUTH_FAILED_INVALID_AUTHENTICATION_HEADER, AUTH_SUCCESS
	}

	protected Response buildErrorResponse(ResponseFormat requestErrorWrapper, boolean addWwwAuthenticationHeader) {
		ResponseBuilder responseBuilder = Response.status(requestErrorWrapper.getStatus());
		if (addWwwAuthenticationHeader) {
			responseBuilder = responseBuilder.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		}
		Response response = responseBuilder.entity(gson.toJson(requestErrorWrapper.getRequestError())).build();
		return response;
	}

}

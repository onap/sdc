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
import java.util.UUID;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.GsonBuilder;

@Provider
@Priority(1)
public class BeServletFilter implements ContainerRequestFilter, ContainerResponseFilter {

	@Context
	private HttpServletRequest sr;

	private static Logger log = LoggerFactory.getLogger(BeServletFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		try {

			MDC.clear();

			// In case of 405 response code, this function is not entered, then
			// we'll process
			// the MDC fields and UUID during the response
			ThreadLocalsHolder.setMdcProcessed(true);

			// Timing HTTP request
			ThreadLocalsHolder.setRequestStartTime(System.currentTimeMillis());

			String uuid = processMdcFields(requestContext);

			ThreadLocalsHolder.setUuid(uuid);

			inHttpRequest();

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Error during request filter");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Error during request filter");
			log.debug("Error during request filter: {} ", e);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		try {
			// Formatting the response in case of 405
			if (responseContext.getStatus() == Response.Status.METHOD_NOT_ALLOWED.getStatusCode()) {
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NOT_ALLOWED);
				responseContext.setEntity(new GsonBuilder().setPrettyPrinting().create().toJson(responseFormat.getRequestError()));
			}

			if (ThreadLocalsHolder.isMdcProcessed()) {
				// filter() was executed during request - this is the regular
				// flow
				responseContext.getHeaders().add(Constants.X_ECOMP_REQUEST_ID_HEADER, ThreadLocalsHolder.getUuid());
				Long startTime = ThreadLocalsHolder.getRequestStartTime();
				if (startTime != null) {
					long endTime = System.currentTimeMillis();
					MDC.put("timer", Long.toString(endTime - startTime));
				}
			} else {
				// this is the 405 response code case
				// we have no MDC fields since filter() wasn't executed during
				// request
				String uuid = processMdcFields(requestContext);
				responseContext.getHeaders().add(Constants.X_ECOMP_REQUEST_ID_HEADER, uuid);
			}

			outHttpResponse(responseContext);

			log.debug("Close transaction from filter");
			TitanDao titanDao = getTitanDao();
			if ( titanDao != null ){
				if (responseContext.getStatus() == Response.Status.OK.getStatusCode() || responseContext.getStatus() == Response.Status.CREATED.getStatusCode() ){
					titanDao.commit();
					log.debug("Doing commit from filter");
				}else{
					titanDao.rollback();
					log.debug("Doing rollback from filter");
				}
			}
			// Cleaning up
			MDC.clear();
			ThreadLocalsHolder.cleanup();
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Error during request filter");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Error during request filter");
			log.debug("Error during response filter: {} ", e);
		}
	}

	private String processMdcFields(ContainerRequestContext requestContext) {
		// UserId for logging
		String userId = requestContext.getHeaderString(Constants.USER_ID_HEADER);
		MDC.put("userId", userId);

		String serviceInstanceID = requestContext.getHeaderString(Constants.X_ECOMP_SERVICE_ID_HEADER);
		MDC.put("serviceInstanceID", serviceInstanceID);

		MDC.put("remoteAddr", sr.getRemoteAddr());
		MDC.put("localAddr", sr.getLocalAddr());

		// UUID
		String uuid = requestContext.getHeaderString(Constants.X_ECOMP_REQUEST_ID_HEADER);
		if (uuid == null) {
			// Generate the UUID
			uuid = UUID.randomUUID().toString();

			// Add to MDC for logging
			MDC.put("uuid", uuid);

			// This log message should already be with the UUID
			uuidGeneration(uuid);

		} else {
			// According to Ella, in case this header exists, we don't have to
			// perform any validations
			// since it's not our responsibilty, so we log the UUID just as it
			// was received.
			MDC.put("uuid", uuid);
		}
		return uuid;
	}

	private ComponentsUtils getComponentsUtils() {
		ServletContext context = this.sr.getSession().getServletContext();

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		return webApplicationContext.getBean(ComponentsUtils.class);
	}
	private TitanDao getTitanDao() {
		ServletContext context = this.sr.getSession().getServletContext();

		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		return webApplicationContext.getBean(TitanDao.class);
	}
	// Extracted for purpose of clear method name, for logback %M parameter
	private void inHttpRequest() {
		if (isInfoLog()) {
			log.info("{} {} {}", sr.getMethod(), sr.getRequestURI(), sr.getProtocol());
		} else {
			log.debug("{} {} {}", sr.getMethod(), sr.getRequestURI(), sr.getProtocol());
		}
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void outHttpResponse(ContainerResponseContext responseContext) {
		if (isInfoLog()) {
			log.info("{} {} {} SC=\"{}\"", sr.getMethod(), sr.getRequestURI(), sr.getProtocol(), responseContext.getStatus());
		} else {
			log.debug("{} {} {} SC=\"{}\"", sr.getMethod(), sr.getRequestURI(), sr.getProtocol(), responseContext.getStatus());
		}
	}

	private boolean isInfoLog() {
		boolean logRequest = true;
		Configuration configuration = ConfigurationManager.getConfigurationManager().getConfiguration();
		String requestURI = sr.getRequestURI();
		if (requestURI != null && configuration.getUnLoggedUrls() != null) {
			logRequest = !configuration.getUnLoggedUrls().contains(requestURI);
		}

		return logRequest;
	}

	// Extracted for purpose of clear method name, for logback %M parameter
	private void uuidGeneration(String uuid) {
		log.info("No requestID  provided -> Generated UUID {}", uuid);
	}
}

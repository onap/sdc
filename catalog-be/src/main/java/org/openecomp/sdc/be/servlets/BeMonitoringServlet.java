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

package org.openecomp.sdc.be.servlets;

import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.components.impl.MonitoringBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckComponent;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.monitoring.MonitoringEvent;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.TRACE, trim = false)
@Path("/")
@Api(value = "BE Monitoring", description = "BE Monitoring")
@Singleton
public class BeMonitoringServlet extends BeGenericServlet {

	Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	private static Logger log = LoggerFactory.getLogger(ConfigServlet.class.getName());

	@GET
	@Path("/healthCheck")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "return aggregate BE health check of Titan, ES and BE", notes = "return BE health check", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Titan, ES and BE are all up"), @ApiResponse(code = 500, message = "One or more BE components (Titan, ES, BE) are down") })
	public Response getHealthCheck(@Context final HttpServletRequest request) {
		try {
			HealthCheckBusinessLogic healthCheckBusinessLogic = getHealthCheckBL(request.getSession().getServletContext());
			List<HealthCheckInfo> beHealthCheckInfos = healthCheckBusinessLogic.getBeHealthCheckInfosStatus();

			// List<HealthCheckInfo> beHealthCheckInfos =
			// HealthCheckBusinessLogic.getInstance().getBeHealthCheckInfos(request.getSession().getServletContext());
			ActionStatus status = getAggregateBeStatus(beHealthCheckInfos);
			String sdcVersion = getVersionFromContext(request);
			if (sdcVersion == null || sdcVersion.isEmpty()) {
				sdcVersion = "UNKNOWN";
			}
			String siteMode = healthCheckBusinessLogic.getSiteMode();
			HealthCheckWrapper healthCheck = new HealthCheckWrapper(beHealthCheckInfos, sdcVersion, siteMode);
			// The response can be either with 200 or 500 aggregate status - the
			// body of individual statuses is returned either way

			String healthCheckStr = prettyGson.toJson(healthCheck);
			return buildOkResponse(getComponentsUtils().getResponseFormat(status), healthCheckStr);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeHealthCheckError, "BeHealthCheck");
			BeEcompErrorManager.getInstance().logBeHealthCheckError("BeHealthCheck");
			log.debug("BE health check unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	@POST
	@Path("/monitoring")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response processMonitoringMetrics(@Context final HttpServletRequest request, String json) {
		try {
			MonitoringEvent monitoringEvent = convertContentToJson(json, MonitoringEvent.class);
			if (monitoringEvent == null) {
				return buildErrorResponse(getComponentsUtils().getResponseFormatAdditionalProperty(ActionStatus.GENERAL_ERROR));
			}
			log.trace("Received monitoring metrics: {}", monitoringEvent.toString());
			ServletContext context = request.getSession().getServletContext();
			MonitoringBusinessLogic bl = getMonitoringBL(context);
			Either<Boolean, ResponseFormat> result = bl.logMonitoringEvent(monitoringEvent);
			if (result.isRight()) {
				return buildErrorResponse(result.right().value());
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), null);

		} catch (Exception e) {
			log.debug("BE system metrics unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormatAdditionalProperty(ActionStatus.GENERAL_ERROR));
		}
	}

	@GET
	@Path("/version")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "return the ASDC application version", notes = "return the ASDC application version", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "return ASDC version"), @ApiResponse(code = 500, message = "Internal Error") })
	public Response getSdcVersion(@Context final HttpServletRequest request) {
		try {
			String url = request.getMethod() + " " + request.getRequestURI();
			log.debug("Start handle request of {}", url);

			String version = getVersionFromContext(request);
			log.debug("asdc version from manifest is: {}", version);
			if (version == null || version.isEmpty()) {
				return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.ASDC_VERSION_NOT_FOUND));
			}

			HealthCheckInfo versionInfo = new HealthCheckInfo();
			versionInfo.setVersion(version);

			// The response can be either with 200 or 500 aggregate status - the
			// body of individual statuses is returned either way
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), versionInfo);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "getSDCVersion");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getSDCVersion");
			log.debug("BE get ASDC version unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	private String getVersionFromContext(HttpServletRequest request) {
		ServletContext servletContext = request.getSession().getServletContext();
		String version = (String) servletContext.getAttribute(Constants.ASDC_RELEASE_VERSION_ATTR);
		return version;
	}

	private ActionStatus getAggregateBeStatus(List<HealthCheckInfo> beHealthCheckInfos) {
		ActionStatus status = ActionStatus.OK;
		for (HealthCheckInfo healthCheckInfo : beHealthCheckInfos) {
			if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN) && healthCheckInfo.getHealthCheckComponent() != HealthCheckComponent.DE) {
				status = ActionStatus.GENERAL_ERROR;
				break;
			}
		}
		return status;
	}

	protected MonitoringEvent convertContentToJson(String content, Class<MonitoringEvent> clazz) {

		MonitoringEvent object = null;
		try {
			object = gson.fromJson(content, clazz);
			object.setFields(null);
		} catch (Exception e) {
			log.debug("Failed to convert the content {} to object.", content.substring(0, Math.min(50, content.length())), e);
		}

		return object;
	}

	private HealthCheckBusinessLogic getHealthCheckBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		HealthCheckBusinessLogic healthCheckBl = webApplicationContext.getBean(HealthCheckBusinessLogic.class);
		return healthCheckBl;
	}

}

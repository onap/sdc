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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openecomp.sdc.be.components.impl.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Priority(11)
public class ComponentsAvailabilityFilter implements ContainerRequestFilter {

	@Context
	protected HttpServletRequest sr;
	protected Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static Logger log = LoggerFactory.getLogger(ComponentsAvailabilityFilter.class.getName());

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String requestUrl = requestContext.getUriInfo().getPath();
		if (!requestUrl.equals("healthCheck")) {
			List<HealthCheckInfo> beHealthCheckInfos = getBeHealthCheckInfos(this.sr.getSession().getServletContext());
			ActionStatus status = getAggregateBeStatus(beHealthCheckInfos);

			if (!status.equals(ActionStatus.OK)) {
				log.error("Components Availability Filter Failed - ES/Cassandra is DOWN");
				availabilityError(requestContext);
			}
		}

	}

	protected ActionStatus getAggregateBeStatus(List<HealthCheckInfo> beHealthCheckInfos) {
		ActionStatus status = ActionStatus.OK;
		for (HealthCheckInfo healthCheckInfo : beHealthCheckInfos) {
			if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckStatus.DOWN)) {
				status = ActionStatus.GENERAL_ERROR;
				break;
			}
		}
		return status;
	}

	protected List<HealthCheckInfo> getBeHealthCheckInfos(ServletContext servletContext) {

		List<HealthCheckInfo> healthCheckInfos = new ArrayList<HealthCheckInfo>();
		HealthCheckBusinessLogic healthCheckBusinessLogic = getHealthCheckBL(servletContext);
		healthCheckBusinessLogic.getTitanHealthCheck(healthCheckInfos); // Titan
		return healthCheckInfos;
	}

	protected ComponentsUtils getComponentsUtils() {
		ServletContext context = sr.getSession().getServletContext();
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ComponentsUtils componentsUtils = webApplicationContext.getBean(ComponentsUtils.class);
		return componentsUtils;
	}

	protected void availabilityError(ContainerRequestContext requestContext) {
		ComponentsUtils componentUtils = getComponentsUtils();
		if (componentUtils == null) {
			log.error("Components Availability Filter Failed to get component utils.");
			requestContext.abortWith(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		}
		ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
		ResponseBuilder responseBuilder = Response.status(responseFormat.getStatus());
		Response response = responseBuilder.entity(gson.toJson(responseFormat.getRequestError())).build();
		requestContext.abortWith(response);
	}

	private HealthCheckBusinessLogic getHealthCheckBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		HealthCheckBusinessLogic healthCheckBl = webApplicationContext.getBean(HealthCheckBusinessLogic.class);
		return healthCheckBl;
	}

}

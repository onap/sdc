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
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
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
import java.util.ArrayList;
import java.util.List;

@Priority(11)
public class ComponentsAvailabilityFilter implements ContainerRequestFilter {

	private static LoggerSdcAudit audit = new LoggerSdcAudit(ComponentsAvailabilityFilter.class);

    @Context
    protected HttpServletRequest sr;
    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = Logger.getLogger(ComponentsAvailabilityFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

		audit.startLog(requestContext);

        String requestUrl = requestContext.getUriInfo().getPath();
        if (!"healthCheck".equals(requestUrl)) {
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

        List<HealthCheckInfo> healthCheckInfos = new ArrayList<>();
        HealthCheckBusinessLogic healthCheckBusinessLogic = getHealthCheckBL(servletContext);
        healthCheckBusinessLogic.getJanusGraphHealthCheck(healthCheckInfos); // JanusGraph
        return healthCheckInfos;
    }

    protected ComponentsUtils getComponentsUtils() {
        ServletContext context = sr.getSession().getServletContext();
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ComponentsUtils.class);
    }

    protected void availabilityError(ContainerRequestContext requestContext) {
        ComponentsUtils componentUtils = getComponentsUtils();
        if (componentUtils == null) {
			String message = "Components Availability Filter Failed to get component utils.";
			abortWith(requestContext, message, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
        ResponseBuilder responseBuilder = Response.status(responseFormat.getStatus());
        Response response = responseBuilder.entity(gson.toJson(responseFormat.getRequestError())).build();
		abortWith(requestContext, responseFormat.getRequestError().toString(), response);
    }

    private HealthCheckBusinessLogic getHealthCheckBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(HealthCheckBusinessLogic.class);
    }


	private void abortWith(ContainerRequestContext requestContext, String message, Response response) {

		audit.log(sr.getRemoteAddr(),
				requestContext,
				response.getStatusInfo(),
				LogLevel.ERROR,
				Severity.OK,
				message);

		log.error(message);
		audit.clearMyData();
		requestContext.abortWith(response);
	}
}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckWrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.TRACE, trim = false)
@Path("/")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Server(url = "/sdc2/rest")
@Controller
public class BeMonitoringServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(BeMonitoringServlet.class);
    private final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    private final HealthCheckBusinessLogic healthCheckBusinessLogic;

    @Inject
    public BeMonitoringServlet(ComponentsUtils componentsUtils,
                               HealthCheckBusinessLogic healthCheckBusinessLogic) {
        super(componentsUtils);
        this.healthCheckBusinessLogic = healthCheckBusinessLogic;
    }

    @GET
    @Path("/healthCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Return aggregate BE health check of SDC BE components", summary = "return BE health check", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "SDC BE components are all up"),
        @ApiResponse(responseCode = "500", description = "One or more SDC BE components are down")})
    public Response getHealthCheck(@Context final HttpServletRequest request) {
        try {
            Pair<Boolean, List<HealthCheckInfo>> beHealthCheckInfosStatus = healthCheckBusinessLogic.getBeHealthCheckInfosStatus();
            Boolean aggregateStatus = beHealthCheckInfosStatus.getLeft();
            ActionStatus status = aggregateStatus ? ActionStatus.OK : ActionStatus.GENERAL_ERROR;
            String sdcVersion = getVersionFromContext(request);
            if (sdcVersion == null || sdcVersion.isEmpty()) {
                sdcVersion = "UNKNOWN";
            }
            String siteMode = healthCheckBusinessLogic.getSiteMode();
            HealthCheckWrapper healthCheck = new HealthCheckWrapper(beHealthCheckInfosStatus.getRight(), sdcVersion, siteMode);
            // The response can be either with 200 or 500 aggregate status - the

            // body of individual statuses is returned either way
            String healthCheckStr = prettyGson.toJson(healthCheck);
            return buildOkResponse(getComponentsUtils().getResponseFormat(status), healthCheckStr);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeHealthCheckError("BeHealthCheck");
            log.debug("BE health check unexpected exception", e);
            throw e;
        }
    }

    private String getVersionFromContext(HttpServletRequest request) {
        ServletContext servletContext = request.getSession().getServletContext();
        return (String) servletContext.getAttribute(Constants.ASDC_RELEASE_VERSION_ATTR);
    }
}

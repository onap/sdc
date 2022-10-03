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
package org.openecomp.sdc.be.servlets;

import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.upgrade.UpgradeBusinessLogic;
import org.openecomp.sdc.be.components.upgrade.UpgradeRequest;
import org.openecomp.sdc.be.components.upgrade.UpgradeStatus;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.utils.JsonParserUtils;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AutomatedUpgradeEndpoint extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(PolicyTypesEndpoint.class);
    private final UpgradeBusinessLogic businessLogic;

    @Inject
    public AutomatedUpgradeEndpoint(ComponentsUtils componentsUtils, UpgradeBusinessLogic businessLogic) {
        super(componentsUtils);
        this.businessLogic = businessLogic;
    }

    @POST
    @Path("/{componentType}/{componentId}/automatedupgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Autometed upgrade", method = "POST", summary = "....", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response autometedUpgrade(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request,
                                     @PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                     @Parameter(description = "json describes upgrade request", required = true) String data) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(POST) Start handle request of {}", url);
        try {
            List<UpgradeRequest> inputsToUpdate = JsonParserUtils.toList(data, UpgradeRequest.class);
            if (log.isDebugEnabled()) {
                log.debug("Received upgrade requests size is {}", inputsToUpdate == null ? 0 : inputsToUpdate.size());
            }
            UpgradeStatus actionResponse = businessLogic.automatedUpgrade(componentId, inputsToUpdate, userId);
            return actionResponse.getStatus() == ActionStatus.OK ? buildOkResponse(actionResponse) : buildErrorResponse(actionResponse.getError());
        } catch (Exception e) {
            log.error("#autometedUpgrade - Exception occurred during autometed Upgrade", e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/dependencies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Autometed upgrade", method = "POST", summary = "....", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getComponentDependencies(@PathParam("componentType") final String componentType, @Context final HttpServletRequest request,
                                             @PathParam("componentId") final String componentId,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                             @Parameter(description = "Consumer Object to be created", required = true) List<String> data) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        try {
            return businessLogic.getComponentDependencies(componentId, userId).either(this::buildOkResponse, this::buildErrorResponse);
        } catch (Exception e) {
            log.error("#getServicesForComponent - Exception occurred during autometed Upgrade", e);
            throw e;
        }
    }
}

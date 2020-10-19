/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.OutputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Controller
@Produces(MediaType.APPLICATION_JSON)
public class OutputsServlet extends BeGenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutputsServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle {} request of {}";

    private final OutputsBusinessLogic outputsBusinessLogic;

    public OutputsServlet(final UserBusinessLogic userAdminManager,
                          final ComponentsUtils componentsUtils,
                          final OutputsBusinessLogic outputsBusinessLogic) {
        super(userAdminManager, componentsUtils);
        this.outputsBusinessLogic = outputsBusinessLogic;
    }

    @POST
    @Path("/{componentType}/{componentId}/create/outputs")
    @Operation(description = "Create outputs on component", method = "POST", summary = "Return outputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response createMultipleOutputs(@Parameter(description = "valid value: services",
        schema = @Schema(allowableValues = {ComponentTypeEnum.SERVICE_PARAM_NAME}))
                                          @PathParam("componentType") final String componentType,
                                          @PathParam("componentId") final String componentId,
                                          @Context final HttpServletRequest request,
                                          @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                          @Parameter(description = "ComponentIns Outputs Object to be created",
                                              required = true) String componentOutputsMapObj) {

        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        return super.declareProperties(userId, componentId, componentType, componentOutputsMapObj,
            DeclarationTypeEnum.OUTPUT, request);
    }

    @DELETE
    @Path("/{componentType}/{componentId}/delete/{outputId}/output")
    @Operation(description = "Delete output from component", method = "DELETE", summary = "Delete Component Output",
        responses = {
            @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Component.class)))),
            @ApiResponse(responseCode = "200", description = "Output deleted"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "404", description = "Output not found")})
    public Response deleteComponentOutput(@Parameter(description = "valid value: services",
        schema = @Schema(allowableValues = {ComponentTypeEnum.SERVICE_PARAM_NAME}))
                                          @PathParam("componentType") final String componentType,
                                          @PathParam("componentId") final String componentId,
                                          @PathParam("outputId") final String outputId,
                                          @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                          @Context final HttpServletRequest request,
                                          @Parameter(description = "Component Output to be deleted",
                                              required = true) String componentOutputsMapObj) {
        try {
            final OutputDefinition deleteOutput = outputsBusinessLogic.deleteOutput(componentId, userId, outputId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteOutput);
        } catch (final ComponentException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete output for service + "
                + componentId + " + with id: " + outputId);
            LOGGER.debug("Delete output failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

}

/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.OutputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ComponentInstOutputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Tags({@Tag(name = "SDCE-2 APIs")})
@Server(url = "/sdc2/rest")
@Path("/v1/catalog")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OutputsServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(OutputsServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "(get) Start handle request of {}";
    private final OutputsBusinessLogic outputsBusinessLogic;

    @Inject
    public OutputsServlet(final OutputsBusinessLogic outputsBusinessLogic,
                          final ComponentInstanceBusinessLogic componentInstanceBL, final ComponentsUtils componentsUtils,
                          final ServletUtils servletUtils, final ResourceImportManager resourceImportManager) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.outputsBusinessLogic = outputsBusinessLogic;
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComponentUid}/outputs")
    @Operation(description = "Get Outputs only", method = "GET", summary = "Returns Outputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getComponentInstanceOutputs(@PathParam("componentType") final String componentType,
                                                @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
                                                @PathParam("originComponentUid") final String originComponentUid,
                                                @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            final Either<List<ComponentInstanceOutput>, ResponseFormat> outputsResponse = outputsBusinessLogic
                .getComponentInstanceOutputs(userId, componentId, instanceId);
            if (outputsResponse.isRight()) {
                log.debug("failed to get component instance outputs {}", componentType);
                return buildErrorResponse(outputsResponse.right().value());
            }
            final Object outputs = RepresentationUtils.toRepresentation(outputsResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), outputs);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Outputs " + componentType);
            log.debug("getOutputs failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @POST
    @Path("/{componentType}/{componentId}/create/outputs")
    @Operation(description = "Create outputs on service", method = "POST", summary = "Return outputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response createMultipleOutputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                          @Context final HttpServletRequest request,
                                          @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
                                          @Parameter(description = "ComponentIns Outputs Object to be created", required = true) final String componentInstOutputsMapObj) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            return declareAttributes(userId, componentId, componentType, componentInstOutputsMapObj, DeclarationTypeEnum.OUTPUT, request);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create outputs for service with id: " + componentId);
            log.debug("Attributes declaration failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Response declareAttributes(final String userId, final String componentId, final String componentType,
                                       final String componentInstOutputsMapObj, final DeclarationTypeEnum typeEnum,
                                       final HttpServletRequest request) {
        final ServletContext context = request.getSession().getServletContext();
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            final BaseBusinessLogic businessLogic = getBlForDeclaration(typeEnum, context);
            // get modifier id
            final User modifier = new User(userId);
            log.debug("modifier id is {}", userId);
            final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            final Either<ComponentInstOutputsMap, ResponseFormat> componentInstOutputsMapRes = parseToComponentInstanceMap(componentInstOutputsMapObj,
                modifier, componentTypeEnum, ComponentInstOutputsMap.class);
            if (componentInstOutputsMapRes.isRight()) {
                log.debug("failed to parse componentInstOutMap");
                return buildErrorResponse(componentInstOutputsMapRes.right().value());
            }
            final Either<List<ToscaDataDefinition>, ResponseFormat> attributesAfterDeclaration = businessLogic
                .declareAttributes(userId, componentId, componentTypeEnum, componentInstOutputsMapRes.left().value());
            if (attributesAfterDeclaration.isRight()) {
                log.debug("failed to create outputs  for service: {}", componentId);
                return buildErrorResponse(attributesAfterDeclaration.right().value());
            }
            final Object attributes = RepresentationUtils.toRepresentation(attributesAfterDeclaration.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), attributes);
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create outputs for service with id: " + componentId);
            log.debug("Attributes declaration failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/{componentType}/{componentId}/delete/{outputId}/output")
    @Operation(description = "Delete output from service", method = "DELETE", summary = "Delete service output", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Output deleted"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Output not found")})
    public Response deleteOutput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                 @PathParam("outputId") final String outputId, @Context final HttpServletRequest request,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
                                 @Parameter(description = "Service Output to be deleted", required = true) final String componentInstOutputsMapObj) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            final OutputDefinition deleteOutput = outputsBusinessLogic.deleteOutput(componentId, userId, outputId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteOutput);
        } catch (final ComponentException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete output for service + " + componentId + " + with id: " + outputId);
            log.debug("Delete output failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}

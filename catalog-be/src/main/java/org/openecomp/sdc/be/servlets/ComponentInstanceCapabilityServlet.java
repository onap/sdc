/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
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
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.servlets.builder.ServletResponseBuilder;
import org.openecomp.sdc.be.ui.mapper.CapabilityMapper;
import org.openecomp.sdc.be.ui.model.ComponentInstanceCapabilityUpdateModel;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Handles component instance capabilities operations
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class ComponentInstanceCapabilityServlet {

    private static final Logger LOGGER = Logger.getLogger(ComponentInstanceCapabilityServlet.class);
    private static final LoggerSupportability LOGGER_SUPPORTABILITY = LoggerSupportability.getLogger(ComponentInstanceCapabilityServlet.class);

    private final ResponseFormatManager responseFormatManager;
    private final ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    private final CapabilityMapper capabilityMapper;
    private final ServletResponseBuilder servletResponseBuilder;

    public ComponentInstanceCapabilityServlet(final ComponentInstanceBusinessLogic componentInstanceBusinessLogic,
                                              final CapabilityMapper capabilityMapper, final ServletResponseBuilder servletResponseBuilder) {
        this.capabilityMapper = capabilityMapper;
        this.servletResponseBuilder = servletResponseBuilder;
        this.responseFormatManager = ResponseFormatManager.getInstance();
        this.componentInstanceBusinessLogic = componentInstanceBusinessLogic;
    }

    @PUT
    @Path("/{containerComponentType}/{containerComponentId}/componentInstances/{componentInstanceUniqueId}/capability/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Component Instance Capability", method = "PUT", summary = "Returns updated Component Instance Capability",
        responses = {
            @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
            @ApiResponse(responseCode = "200", description = "Resource instance capability successfully updated"),
            @ApiResponse(responseCode = "403", description = "Restricted operation"),
            @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
            @ApiResponse(responseCode = "404", description = "Component/Component Instance/Capability not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateInstanceRequirement(@PathParam("containerComponentType") final String containerComponentType,
                                              @PathParam("containerComponentId") final String containerComponentId,
                                              @PathParam("componentInstanceUniqueId") final String componentInstanceUniqueId,
                                              @Parameter(description = "Component instance capability to update", required = true)
                                              @Valid @RequestBody @NotNull final ComponentInstanceCapabilityUpdateModel capabilityUpdateModel,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        if (LOGGER.isDebugEnabled()) {
            final var url = request.getMethod() + " " + request.getRequestURI();
            LOGGER.debug("Start handle request of {}", url);
        }
        try {
            var componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            if (componentTypeEnum == null) {
                LOGGER.debug("Unsupported component type {}", containerComponentType);
                return servletResponseBuilder
                    .buildErrorResponse(responseFormatManager.getResponseFormat(ActionStatus.UNSUPPORTED_ERROR, containerComponentType));
            }
            final var capabilityDefinition = capabilityMapper.mapToCapabilityDefinition(capabilityUpdateModel);
            LOGGER_SUPPORTABILITY.log(LoggerSupportabilityActions.UPDATE_INSTANCE_CAPABILITY, StatusCode.STARTED,
                "Starting to update capability '{}' in component instance '{}' by '{}'",
                capabilityDefinition.getName(), componentInstanceUniqueId, userId);
            final Either<CapabilityDefinition, ResponseFormat> response = componentInstanceBusinessLogic
                .updateInstanceCapability(componentTypeEnum, containerComponentId, componentInstanceUniqueId, capabilityDefinition, userId);
            if (response.isRight()) {
                return servletResponseBuilder.buildErrorResponse(response.right().value());
            }
            return servletResponseBuilder.buildOkResponse(responseFormatManager.getResponseFormat(ActionStatus.OK), response.left().value());
        } catch (final BusinessException e) {
            //leave to the handlers deal with it
            throw e;
        } catch (final Exception e) {
            var errorMsg = String
                .format("Unexpected error while updating component '%s' of type '%s' instance '%s'. Payload '%s'",
                    containerComponentId, containerComponentType, componentInstanceUniqueId, capabilityUpdateModel);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            LOGGER.debug(errorMsg, e);
            return servletResponseBuilder.buildErrorResponse(responseFormatManager.getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

}

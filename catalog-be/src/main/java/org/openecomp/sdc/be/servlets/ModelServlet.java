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

import com.googlecode.jmapper.JMapper;
import com.jcabi.aspects.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ModelTypeEnum;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.ModelOperationExceptionSupplier;
import org.openecomp.sdc.be.ui.model.ModelCreateRequest;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
public class ModelServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(ModelServlet.class);
    private final ModelBusinessLogic modelBusinessLogic;
    private final UserValidations userValidations;

    @Inject
    public ModelServlet(final ComponentInstanceBusinessLogic componentInstanceBL,
                        final ComponentsUtils componentsUtils, final ServletUtils servletUtils, final ResourceImportManager resourceImportManager,
                        final ModelBusinessLogic modelBusinessLogic, final UserValidations userValidations) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.modelBusinessLogic = modelBusinessLogic;
        this.userValidations = userValidations;
    }

    @POST
    @Path("/model")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    @Operation(description = "Create a TOSCA model, along with its imports files", method = "POST", summary = "Create a TOSCA model", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Model created"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "409", description = "Model already exists")})
    public Response createModel(@Parameter(description = "model to be created", required = true)
                                @NotNull @Valid @FormDataParam("model") final ModelCreateRequest modelCreateRequest,
                                @Parameter(description = "the model TOSCA imports zipped", required = true)
                                @NotNull @FormDataParam("modelImportsZip") final InputStream modelImportsZip,
                                @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        validateUser(ValidationUtils.sanitizeInputString(userId));
        final var modelName = ValidationUtils.sanitizeInputString(modelCreateRequest.getName().trim());
        try {
            final Model createdModel = modelBusinessLogic
                .createModel(new JMapper<>(Model.class, ModelCreateRequest.class).getDestination(modelCreateRequest));
            modelBusinessLogic.createModelImports(modelName, modelImportsZip);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED),
                RepresentationUtils.toRepresentation(createdModel));
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = String.format("Unexpected error while creating model '%s' imports", modelName);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    @Operation(method = "GET", summary = "List TOSCA models", description = "List all the existing TOSCA models",
        responses = {
            @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Model.class)))),
            @ApiResponse(responseCode = "200", description = "Listing successful"),
            @ApiResponse(responseCode = "403", description = "Restricted operation")
        }
    )
    public Response listModels(@HeaderParam(value = Constants.USER_ID_HEADER) final String userId, @QueryParam("modelType") final String modelType) {
        validateUser(ValidationUtils.sanitizeInputString(userId));
        try {
            final List<Model> modelList =
                StringUtils.isEmpty(modelType) ? modelBusinessLogic.listModels() : modelBusinessLogic.listModels(getModelTypeEnum(modelType));
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(modelList));
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = "Unexpected error while listing the models";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private ModelTypeEnum getModelTypeEnum(final String modelType) {
        final ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        if (modelTypeEnum == null) {
            throw ModelOperationExceptionSupplier.unknownModelType(modelType).get();
        }
        return modelTypeEnum;
    }

    @PUT
    @Path("/model/imports")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    @Operation(description = "Update a model TOSCA imports", method = "PUT", summary = "Update a model TOSCA imports", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "Model imports updated"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Model not found")})
    public Response updateModelImports(@Parameter(description = "model to be created", required = true)
                                       @NotNull @FormDataParam("modelName") String modelName,
                                       @Parameter(description = "the model TOSCA imports zipped", required = true)
                                       @NotNull @FormDataParam("modelImportsZip") final InputStream modelImportsZip,
                                       @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        validateUser(ValidationUtils.sanitizeInputString(userId));
        modelName = ValidationUtils.sanitizeInputString(modelName);
        try {
            modelBusinessLogic.createModelImports(modelName, modelImportsZip);
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = String.format("Unexpected error while creating model '%s' imports", modelName);
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    private void validateUser(final String userId) {
        userValidations.validateUserRole(userValidations.validateUserExists(userId), Arrays.asList(Role.DESIGNER, Role.ADMIN));
    }

}

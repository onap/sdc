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
import java.util.Arrays;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ModelBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Model;
import org.openecomp.sdc.be.ui.model.ModelCreateRequest;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ModelServlet(final UserBusinessLogic userBusinessLogic, final ComponentInstanceBusinessLogic componentInstanceBL,
                        final ComponentsUtils componentsUtils, final ServletUtils servletUtils, final ResourceImportManager resourceImportManager,
                        final ModelBusinessLogic modelBusinessLogic, final UserValidations userValidations) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.modelBusinessLogic = modelBusinessLogic;
        this.userValidations = userValidations;
    }

    @POST
    @Path("/model")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create model", method = "POST", summary = "Returns created model", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Model created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource already exists")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createModel(@Parameter(description = "model to be created", required = true)
                                    @Valid @RequestBody @NotNull final ModelCreateRequest modelCreateRequest,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        validateUser(userId);
        try {
            final Model modelCreateResponse = modelBusinessLogic
                .createModel(new JMapper<>(Model.class, ModelCreateRequest.class).getDestination(modelCreateRequest));
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED),
                RepresentationUtils.toRepresentation(modelCreateResponse));
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            var errorMsg = String
                .format("Unexpected error while creating model '%s'", modelCreateRequest.getName());
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private void validateUser(final String userId) {
        userValidations.validateUserRole(userValidations.validateUserExists(userId), Arrays.asList(Role.DESIGNER, Role.ADMIN));
    }

}

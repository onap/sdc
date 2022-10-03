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
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.AttributeBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Server(url = "/sdc2/rest")
@Singleton
public class ComponentAttributeServlet extends BeGenericServlet {

    private static final Logger log = LoggerFactory.getLogger(ComponentAttributeServlet.class);
    private static final String CREATE_ATTRIBUTE = "Create Attribute";
    private static final String DEBUG_MESSAGE = "Start handle request of {} modifier id is {}";
    private final AttributeBusinessLogic attributeBusinessLogic;

    @Inject
    public ComponentAttributeServlet(final ComponentsUtils componentsUtils,
                                     final AttributeBusinessLogic attributeBusinessLogic) {
        super(componentsUtils);
        this.attributeBusinessLogic = attributeBusinessLogic;
    }

    @GET
    @Path("services/{serviceId}/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Service Attribute", method = "GET", summary = "Returns attribute list of service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "attribute"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service attribute not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAttributeListInService(
        @Parameter(description = "service id of attribute", required = true) @PathParam("serviceId") final String serviceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        return getAttributeList(serviceId, request, userId);
    }

    @GET
    @Path("resources/{resourceId}/attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Resource Attribute", method = "GET", summary = "Returns attribute list of resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "attribute"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource attribute not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAttributeListInResource(
        @Parameter(description = "resource id of attribute", required = true) @PathParam("resourceId") final String resourceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) final String userId) {
        return getAttributeList(resourceId, request, userId);
    }

    private Response getAttributeList(final String componentId, final HttpServletRequest request, final String userId) {
        final String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(DEBUG_MESSAGE, url, userId);
        try {
            final Either<List<AttributeDefinition>, ResponseFormat> attributesList = attributeBusinessLogic.getAttributesList(componentId, userId);
            if (attributesList.isRight()) {
                return buildErrorResponse(attributesList.right().value());
            }
            return buildOkResponse(attributesList.left().value());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_ATTRIBUTE);
            log.debug("get attribute failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}

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

import com.google.common.collect.Sets;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
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
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiServiceDataTransfer;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog/services/{serviceId}/paths")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class ServiceForwardingPathServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ServiceForwardingPathServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private final ServiceBusinessLogic serviceBusinessLogic;

    @Inject
    public ServiceForwardingPathServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                        ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                        ServiceBusinessLogic serviceBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Operation(description = "Create Forwarding Path", method = "POST", summary = "Create Forwarding Path", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "201", description = "Create Forwarding Path"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Forwarding Path already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createForwardingPath(@Parameter(description = "Forwarding Path to create", required = true) String data,
                                         @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        return createOrUpdate(data, serviceId, request, userId, false);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Operation(description = "Update Forwarding Path", method = "PUT", summary = "Update Forwarding Path", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "201", description = "Update Forwarding Path"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Forwarding Path already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateForwardingPath(@Parameter(description = "Update Path to create", required = true) String data,
                                         @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        return createOrUpdate(data, serviceId, request, userId, true);
    }

    private Response createOrUpdate(String data, String serviceId, HttpServletRequest request, String userId, boolean isUpdate) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        try {
            String serviceIdLower = serviceId.toLowerCase();
            Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
            if (convertResponse.isRight()) {
                log.debug("failed to parse service");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            Service updatedService = convertResponse.left().value();
            Service actionResponse;
            if (isUpdate) {
                actionResponse = serviceBusinessLogic.updateForwardingPath(serviceIdLower, updatedService, modifier, true);
            } else {
                actionResponse = serviceBusinessLogic.createForwardingPath(serviceIdLower, updatedService, modifier, true);
            }
            Service service = actionResponse;
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Forward Path Creation or update");
            log.debug("create or update forwarding path with an error", e);
            throw e;
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{forwardingPathId}")
    @Operation(description = "Get Forwarding Path", method = "GET", summary = "GET Forwarding Path", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ForwardingPathDataDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Get Forwarding Path"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Forwarding Path already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getForwardingPath(@Parameter(description = "Forwarding Path to create", required = true) String datax,
                                      @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                      @Parameter(description = "Forwarding Path Id") @PathParam("forwardingPathId") String forwardingPathId,
                                      @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Either<UiComponentDataTransfer, ResponseFormat> serviceResponse = serviceBusinessLogic
                .getComponentDataFilteredByParams(serviceId, modifier, Collections.singletonList(ComponentFieldsEnum.FORWARDING_PATHS.getValue()));
            if (serviceResponse.isRight()) {
                return buildErrorResponse(serviceResponse.right().value());
            }
            UiServiceDataTransfer uiServiceDataTransfer = (UiServiceDataTransfer) serviceResponse.left().value();
            ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
            if (!MapUtils.isEmpty(uiServiceDataTransfer.getForwardingPaths())) {
                forwardingPathDataDefinition = uiServiceDataTransfer.getForwardingPaths().get(forwardingPathId);
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                RepresentationUtils.toRepresentation(forwardingPathDataDefinition));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Service Metadata");
            log.debug("update service metadata failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{forwardingPathId}")
    @Operation(description = "Delete Forwarding Path", method = "DELETE", summary = "Delete Forwarding Path", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Forwarding Path"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Forwarding Path already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteForwardingPath(@Parameter(description = "Forwarding Path Id") @PathParam("forwardingPathId") String forwardingPathId,
                                         @Parameter(description = "Service Id") @PathParam("serviceId") String serviceId,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        try {
            String serviceIdLower = serviceId.toLowerCase();
            Set<String> deletedPaths = serviceBusinessLogic.deleteForwardingPaths(serviceIdLower, Sets.newHashSet(forwardingPathId), modifier, true);
            Object result = RepresentationUtils.toRepresentation(deletedPaths);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete forward paths");
            log.debug("Delete service paths with an error", e);
            throw e;
        }
    }

    private Either<Service, ResponseFormat> parseToService(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, Service.class, AuditingActionEnum.CREATE_RESOURCE,
            ComponentTypeEnum.SERVICE);//TODO: change sSERVICE constant
    }
}

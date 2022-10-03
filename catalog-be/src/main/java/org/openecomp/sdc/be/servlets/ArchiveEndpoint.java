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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ArchiveBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ArchiveEndpoint extends BeGenericServlet {

    private static final String COMPONENT_ID = "Component ID= ";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ArchiveEndpoint.class.getName());
    private final ArchiveBusinessLogic archiveBusinessLogic;

    @Inject
    public ArchiveEndpoint(ComponentsUtils componentsUtils, ArchiveBusinessLogic archiveBusinessLogic) {
        super(componentsUtils);
        this.archiveBusinessLogic = archiveBusinessLogic;
    }

    @POST
    @Path("/resources/{componentId}/archive")
    @Operation(description = "Archive Resource", method = "POST", summary = "Marks a resource as archived. Can be restored with restore action", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Archive successful"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response archiveResources(@PathParam("componentId") final String componentId,
                                     @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        loggerSupportability
            .log(LoggerSupportabilityActions.ARCHIVE, StatusCode.STARTED, "Archive Resource " + COMPONENT_ID + " " + componentId + " by " + userId);
        archiveBusinessLogic.archiveComponent(ComponentTypeEnum.RESOURCE_PARAM_NAME, userId, componentId);
        loggerSupportability
            .log(LoggerSupportabilityActions.ARCHIVE, StatusCode.COMPLETE, "Archive Resource " + COMPONENT_ID + " " + componentId + " by " + userId);
        return Response.ok().build();
    }

    @POST
    @Path("/resources/{componentId}/restore")
    @Operation(description = "Restore Resource", method = "POST", summary = "Restores a resource from archive.", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Restore successful"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response restoreResource(@PathParam("componentId") final String componentId,
                                    @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        loggerSupportability.log(LoggerSupportabilityActions.RESTORE_FROM_ARCHIVE, StatusCode.STARTED,
            "Restore resource from archive " + COMPONENT_ID + " " + componentId + " by " + userId);
        archiveBusinessLogic.restoreComponent(ComponentTypeEnum.RESOURCE_PARAM_NAME, userId, componentId);
        loggerSupportability.log(LoggerSupportabilityActions.RESTORE_FROM_ARCHIVE, StatusCode.COMPLETE,
            "Restore resource from archive " + COMPONENT_ID + " " + componentId + " by " + userId);
        return Response.ok().build();
    }

    @POST
    @Path("/services/{componentId}/archive")
    @Operation(description = "Archive Service", method = "POST", summary = "Marks a service as archived. Can be restored with restore action", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Archive successful"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response archiveService(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        loggerSupportability.log(LoggerSupportabilityActions.ARCHIVE, StatusCode.STARTED,
            "Archive Service for " + COMPONENT_ID + " " + componentId + " by " + userId);
        archiveBusinessLogic.archiveComponent(ComponentTypeEnum.SERVICE_PARAM_NAME, userId, componentId);
        loggerSupportability.log(LoggerSupportabilityActions.ARCHIVE, StatusCode.COMPLETE,
            "Archive Service for " + COMPONENT_ID + " " + componentId + " by " + userId);
        return Response.ok().build();
    }

    @POST
    @Path("/services/{componentId}/restore")
    @Operation(description = "Restore Service", method = "POST", summary = "Restores a service from archive.", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Restore successful"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response restoreService(@PathParam("componentId") final String componentId, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        loggerSupportability.log(LoggerSupportabilityActions.RESTORE_FROM_ARCHIVE, StatusCode.STARTED,
            "Restore service from archive " + COMPONENT_ID + " " + componentId + " by " + userId);
        archiveBusinessLogic.restoreComponent(ComponentTypeEnum.SERVICE_PARAM_NAME, userId, componentId);
        loggerSupportability.log(LoggerSupportabilityActions.RESTORE_FROM_ARCHIVE, StatusCode.COMPLETE,
            "Restore service from archive " + COMPONENT_ID + " " + componentId + " by " + userId);
        return Response.ok().build();
    }

    @GET
    @Path("/archive")
    @Operation(description = "Get all Archived Components", method = "GET", summary = "Get all Archived Components", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Map<String, List<CatalogComponent>> getArchivedComponents(@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return this.archiveBusinessLogic.getArchiveComponents(userId, new LinkedList<>());
    }

    @POST
    @Path("/notif/vsp/archived")
    @Operation(description = "Notify about an archived VSP. All VFs with relation to the given CSAR IDs will be martked as vspArchived=true", method = "POST", responses = {
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "500", description = "Internal Error. A list of the failed CSAR IDs may be returned.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response onVspArchived(@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @RequestBody List<String> csarIds) {
        List<String> failedCsarIds = this.archiveBusinessLogic.onVspArchive(userId, csarIds);
        if (!failedCsarIds.isEmpty()) {
            //There are some failed CSAR IDs, return 500 and the list of failed CSAR IDs
            Map<String, List<String>> entity = new HashMap<>();
            entity.put("failedIds", failedCsarIds);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/notif/vsp/restored")
    @Operation(description = "Notify about a restored VSP. All VFs with relation to the given CSAR IDs will be martked as vspArchived=false", method = "POST", responses = {
        @ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "500", description = "Internal Error. A list of the failed CSAR IDs may be returned.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response onVspRestored(@HeaderParam(value = Constants.USER_ID_HEADER) String userId, @RequestBody List<String> csarIds) {
        List<String> failedCsarIds = this.archiveBusinessLogic.onVspRestore(userId, csarIds);
        if (!failedCsarIds.isEmpty()) {
            //There are some failed CSAR IDs, return 500 and the list of failed CSAR IDs
            Map<String, List<String>> entity = new HashMap<>();
            entity.put("failedIds", failedCsarIds);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(entity).build();
        }
        return Response.ok().build();
    }
}

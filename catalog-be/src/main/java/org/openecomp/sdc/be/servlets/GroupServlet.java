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
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class GroupServlet extends AbstractValidationsServlet {

    public static final String START_HANDLE_REQUEST = "Start handle request of {}";
    private static final Logger log = Logger.getLogger(GroupServlet.class);
    private final GroupBusinessLogic groupBL;

    @Inject
    public GroupServlet(GroupBusinessLogic groupBL, ComponentInstanceBusinessLogic componentInstanceBL,
                        ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.groupBL = groupBL;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/groups/{groupType}")
    @Operation(description = "Create group ", method = "POST", summary = "Creates new group in component and returns it", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupDefinition.class)))),
        @ApiResponse(responseCode = "201", description = "Group created"),
        @ApiResponse(responseCode = "400", description = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId, @PathParam("groupType") final String type,
                                @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(post) Start handle request of {}", url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition groupDefinition = groupBL.createGroup(componentId, componentTypeEnum, type, userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), groupDefinition);
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/groups/{groupId}")
    @Operation(description = "Get group artifacts ", method = "GET", summary = "Returns artifacts metadata according to groupId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "group found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Group not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getGroupById(@PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("componentId") final String componentId, @PathParam("groupId") final String groupId,
                                 @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<GroupDefinitionInfo, ResponseFormat> actionResponse = groupBL
                .getGroupWithArtifactsById(componentTypeEnum, componentId, groupId, userId, false);
            if (actionResponse.isRight()) {
                log.debug("failed to get all non abstract {}", containerComponentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
            log.debug("getGroupArtifactById unexpected exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}")
    @Operation(description = "Delete Group", method = "DELETE", summary = "Returns deleted group id", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "ResourceInstance deleted"),
        @ApiResponse(responseCode = "400", description = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found"), @ApiResponse(responseCode = "500", description = "Internal Error")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupId,
                                @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST, url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition groupDefinition = groupBL.deleteGroup(componentId, componentTypeEnum, groupId, userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), groupDefinition.getUniqueId());
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupId}")
    @Operation(description = "Update Group metadata", method = "PUT", summary = "Returns updated Group", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Group updated"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "component / group Not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId, @PathParam("groupId") final String groupId,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                @Parameter(description = "GroupDefinition", required = true) GroupDefinition groupData,
                                @Context final HttpServletRequest request) {
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition updatedGroup = groupBL.updateGroup(componentId, componentTypeEnum, groupId, userId, groupData);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updatedGroup);
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Group Metadata", method = "PUT", summary = "Returns updated group definition", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupDefinition.class)))),
        @ApiResponse(responseCode = "200", description = "Group Updated"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateGroupMetadata(@PathParam("containerComponentType") final String containerComponentType,
                                        @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
                                        @Parameter(description = "Service object to be Updated", required = true) String data,
                                        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST, url);
        User user = new User();
        user.setUserId(userId);
        log.debug("modifier id is {}", userId);
        try {
            Either<GroupDefinition, ResponseFormat> convertResponse = parseToObject(data, () -> GroupDefinition.class);
            if (convertResponse.isRight()) {
                log.debug("failed to parse group");
                return buildErrorResponse(convertResponse.right().value());
            }
            GroupDefinition updatedGroup = convertResponse.left().value();
            // Update GroupDefinition
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<GroupDefinition, ResponseFormat> actionResponse = groupBL
                .validateAndUpdateGroupMetadata(componentId, user, componentTypeEnum, updatedGroup, true, true);
            if (actionResponse.isRight()) {
                log.debug("failed to update GroupDefinition");
                return buildErrorResponse(actionResponse.right().value());
            }
            GroupDefinition group = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(group);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Group Metadata");
            log.debug("update group metadata failed with exception", e);
            throw e;
        }
    }
}

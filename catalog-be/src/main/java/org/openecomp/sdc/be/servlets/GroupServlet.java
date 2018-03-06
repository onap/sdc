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

import com.google.common.reflect.TypeToken;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
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
import java.util.List;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/catalog")
@Api(value = "Resource Group Servlet")
@Singleton
public class GroupServlet extends AbstractValidationsServlet {

    private static final Logger log = LoggerFactory.getLogger(GroupServlet.class);
    public static final String START_HANDLE_REQUEST = "Start handle request of {}";

    @POST
    @Path("/{containerComponentType}/{componentId}/groups/{groupType}")
    @ApiOperation(value = "Create group ", httpMethod = "POST", notes = "Creates new group in component and returns it", response = GroupDefinition.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Group created"),
            @ApiResponse(code = 400, message = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Component not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response createGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId,
                                @PathParam("groupType") final String type,
                                @Context final HttpServletRequest request,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(post) Start handle request of {}", url);

        GroupBusinessLogic businessLogic = getGroupBL(context);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition groupDefinition = businessLogic
                .createGroup(type, componentTypeEnum, componentId, userId);

        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED),
                groupDefinition);
    }

    @GET
    @Path("/{containerComponentType}/{componentId}/groups/{groupId}")
    @ApiOperation(value = "Get group artifacts ", httpMethod = "GET", notes = "Returns artifacts metadata according to groupId", response = Resource.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "group found"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Group not found")})
    public Response getGroupById(@PathParam("containerComponentType") final String containerComponentType,
                                 @PathParam("componentId") final String componentId, @PathParam("groupId") final String groupId,
                                 @Context final HttpServletRequest request,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);

        try {

            GroupBusinessLogic businessLogic = this.getGroupBL(context);
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<GroupDefinitionInfo, ResponseFormat> actionResponse = businessLogic
                    .getGroupWithArtifactsById(componentTypeEnum, componentId, groupId, userId, false);

            if (actionResponse.isRight()) {
                log.debug("failed to get all non abstract {}", containerComponentType);
                return buildErrorResponse(actionResponse.right().value());
            }

            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    actionResponse.left().value());

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
            log.debug("getGroupArtifactById unexpected exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @DELETE
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}")
    @ApiOperation(value = "Delete Group", httpMethod = "DELETE", notes = "Returns deleted group id", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "ResourceInstance deleted"),
            @ApiResponse(code = 400, message = "field name invalid type/length, characters;  mandatory field is absent, already exists (name)"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Component not found"),
            @ApiResponse(code = 500, message = "Internal Error")
    })
    public Response deleteGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId,
                                @PathParam("groupUniqueId") final String groupId,
                                @Context final HttpServletRequest request,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST, url);
        GroupBusinessLogic businessLogic = this.getGroupBL(context);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition groupDefinition = businessLogic
                .deleteGroup(componentTypeEnum, componentId, groupId, userId);

        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), groupDefinition.getUniqueId());
    }

    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupId}")
    @ApiOperation(value = "Update Group metadata", httpMethod = "PUT", notes = "Returns updated Group", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Group updated"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "component / group Not found")})
    public Response updateGroup(@PathParam("containerComponentType") final String containerComponentType,
                                @PathParam("componentId") final String componentId,
                                @PathParam("groupId") final String groupId,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                @ApiParam(value = "GroupDefinition", required = true) GroupDefinition groupData,
                                @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        GroupBusinessLogic businessLogic = this.getGroupBL(context);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
        GroupDefinition updatedGroup = businessLogic.updateGroup(componentTypeEnum, componentId, groupId, userId, groupData);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), updatedGroup);
    }

    /**
     * Updates List of properties on a group (only values)
     * @param containerComponentType
     * @param componentId
     * @param groupUniqueId
     * @param data
     * @param request
     * @param userId
     * @return
     */
    @PUT
    @Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/properties")
    @ApiOperation(value = "Updates List of properties on a group (only values)", httpMethod = "PUT", notes = "Returns updated list of properties", response = GroupDefinition.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Group Updated"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateGroupProperties(@PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
            @ApiParam(value = "Group Properties to be Updated", required = true) String data,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        init(log);
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST, url);

        User user = new User();
        user.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response = null;

        try {
            GroupBusinessLogic businessLogic = getGroupBL(context);
            Either<List<GroupProperty>, ResponseFormat> convertResponse = parseListOfObjects(data,
                    new TypeToken<List<GroupProperty>>() {
                    }.getType());

            if (convertResponse.isRight()) {
                log.debug("failed to parse group Property");
                response = buildErrorResponse(convertResponse.right().value());
                return response;
            }
            List<GroupProperty> groupPropertyToUpdate = convertResponse.left().value();

            // Update GroupDefinition
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
            Either<List<GroupProperty>, ResponseFormat> actionResponse = businessLogic.validateAndUpdateGroupProperties(
                    componentId, groupUniqueId, user, componentTypeEnum, groupPropertyToUpdate, false);

            if (actionResponse.isRight()) {
                log.debug("failed to update GroupDefinition");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }

            List<GroupProperty> groupProperty = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(groupProperty);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Group Properties");
            log.debug("update group properties failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

}

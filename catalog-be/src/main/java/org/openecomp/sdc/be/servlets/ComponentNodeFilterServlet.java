/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentNodeFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.tosca.utils.NodeFilterConverter;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ComponentNodeFilterServlet extends AbstractValidationsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentNodeFilterServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle {} request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private static final String FAILED_TO_PARSE_COMPONENT = "failed to parse component";
    private static final String FAILED_TO_CREATE_NODE_FILTER = "failed to create node filter";
    private static final String NODE_FILTER_CREATION = "Node Filter Creation";
    private static final String CREATE_NODE_FILTER_WITH_AN_ERROR = "create node filter with an error";
    private static final String FAILED_TO_UPDATE_NODE_FILTER = "failed to update node filter";
    private static final String NODE_FILTER_UPDATE = "Node Filter Update";
    private static final String UPDATE_NODE_FILTER_WITH_AN_ERROR = "update node filter with an error";
    private static final String FAILED_TO_DELETE_NODE_FILTER = "failed to delete node filter";
    private static final String NODE_FILTER_DELETE = "Node Filter Delete";
    private static final String DELETE_NODE_FILTER_WITH_AN_ERROR = "delete node filter with an error";
    private static final String INVALID_NODE_FILTER_CONSTRAINT_TYPE = "Invalid value for NodeFilterConstraintType enum {}";
    private final ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic;

    @Inject
    public ComponentNodeFilterServlet(final UserBusinessLogic userBusinessLogic, final ComponentInstanceBusinessLogic componentInstanceBL,
                                      final ComponentsUtils componentsUtils, final ServletUtils servletUtils,
                                      final ResourceImportManager resourceImportManager,
                                      final ComponentNodeFilterBusinessLogic componentNodeFilterBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.componentNodeFilterBusinessLogic = componentNodeFilterBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{componentType}/{componentId}/componentInstance/{componentInstanceId}/{constraintType}/nodeFilter")
    @Operation(description = "Add Component Filter Constraint", method = "POST", summary = "Add Component Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Component Filter"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response addComponentFilterConstraint(@Parameter(description = "UIConstraint data", required = true) String constraintData,
                                                 @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                                 @Parameter(description = "Component Instance Id") @PathParam("componentInstanceId") String componentInstanceId,
                                                 @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                     ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                     ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                                 @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                     NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                     NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                                 @Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentNodeFilterBusinessLogic.validateUser(userId);
        final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        try {
            final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintType.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_NODE_FILTER_CONSTRAINT_TYPE, constraintType));
            }
            final UIConstraint uiConstraint = componentsUtils.parseToConstraint(constraintData, userModifier, componentTypeEnum).orElse(null);
            if (uiConstraint == null) {
                LOGGER.error(FAILED_TO_PARSE_COMPONENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            final String constraint = new ConstraintConvertor().convert(uiConstraint);
            final Optional<CINodeFilterDataDefinition> actionResponse = componentNodeFilterBusinessLogic
                .addNodeFilter(componentId.toLowerCase(), componentInstanceId, NodeFilterConstraintAction.ADD, uiConstraint.getServicePropertyName(),
                    constraint, true, componentTypeEnum, nodeFilterConstraintType.get(),
                    StringUtils.isEmpty(uiConstraint.getCapabilityName()) ? "" : uiConstraint.getCapabilityName());
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_CREATE_NODE_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new NodeFilterConverter().convertToUi(actionResponse.get()));
        } catch (final ComponentException e) {
            throw e;
        } catch (final BusinessLogicException e) {
            return buildErrorResponse(e.getResponseFormat());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_CREATION);
            LOGGER.error(CREATE_NODE_FILTER_WITH_AN_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{componentType}/{componentId}/componentInstance/{componentInstanceId}/{constraintType}/{constraintIndex}/nodeFilter")
    @Operation(description = "Update Component Filter Constraint", method = "PUT", summary = "Update Component Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Create Component Filter"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateComponentFilterConstraint(@Parameter(description = "UIConstraint data", required = true) String constraintData,
                                                    @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                                    @Parameter(description = "Component Instance Id") @PathParam("componentInstanceId") String componentInstanceId,
                                                    @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                        ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                        ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                                    @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                        NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                        NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                                    @Parameter(description = "Constraint Index") @PathParam("constraintIndex") int index,
                                                    @Context final HttpServletRequest request,
                                                    @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentNodeFilterBusinessLogic.validateUser(userId);
        try {
            final Optional<NodeFilterConstraintType> nodeFilterConstraintTypeOptional = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintTypeOptional.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_NODE_FILTER_CONSTRAINT_TYPE, constraintType));
            }
            final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            final Optional<UIConstraint> convertResponse = componentsUtils.parseToConstraint(constraintData, userModifier, componentTypeEnum);
            if (convertResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_PARSE_COMPONENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            final NodeFilterConstraintType nodeFilterConstraintType = nodeFilterConstraintTypeOptional.get();
            final Optional<CINodeFilterDataDefinition> actionResponse = componentNodeFilterBusinessLogic
                .updateNodeFilter(componentId.toLowerCase(), componentInstanceId, convertResponse.get(), componentTypeEnum, nodeFilterConstraintType,
                    index);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_NODE_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new NodeFilterConverter().convertToUi(actionResponse.get()));
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_UPDATE);
            LOGGER.error(UPDATE_NODE_FILTER_WITH_AN_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{componentType}/{componentId}/componentInstance/{componentInstanceId}/{constraintType}/{constraintIndex}/nodeFilter")
    @Operation(description = "Delete Component Filter Constraint", method = "Delete", summary = "Delete Component Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Component Filter Constraint"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteComponentFilterConstraint(@Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                                    @Parameter(description = "Component Instance Id") @PathParam("componentInstanceId") String componentInstanceId,
                                                    @Parameter(description = "Constraint Index") @PathParam("constraintIndex") int index,
                                                    @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {
                                                        ComponentTypeEnum.RESOURCE_PARAM_NAME,
                                                        ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                                    @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                        NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                        NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                                    @Context final HttpServletRequest request,
                                                    @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        componentNodeFilterBusinessLogic.validateUser(userId);
        try {
            final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintType.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_NODE_FILTER_CONSTRAINT_TYPE, constraintType));
            }
            final Optional<CINodeFilterDataDefinition> actionResponse = componentNodeFilterBusinessLogic
                .deleteNodeFilter(componentId.toLowerCase(), componentInstanceId, NodeFilterConstraintAction.DELETE, null, index, true,
                    ComponentTypeEnum.findByParamName(componentType), nodeFilterConstraintType.get());
            if (actionResponse.isEmpty()) {
                LOGGER.debug(FAILED_TO_DELETE_NODE_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new NodeFilterConverter().convertToUi(actionResponse.get()));
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(NODE_FILTER_DELETE);
            LOGGER.debug(DELETE_NODE_FILTER_WITH_AN_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}

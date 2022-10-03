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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentSubstitutionFilterBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.BusinessLogicException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeFilterConstraintType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.tosca.utils.SubstitutionFilterConverter;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;
import org.openecomp.sdc.common.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/v1/catalog/{componentType}/{componentId}/substitutionFilter/{constraintType}")
@Tag(name = "SDCE-2 APIs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ComponentSubstitutionFilterServlet extends AbstractValidationsServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentSubstitutionFilterServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle {} request of {}";
    private static final String MODIFIER_ID_IS = "Modifier id is {}";
    private static final String FAILED_TO_PARSE_COMPONENT = "Failed to parse component";
    private static final String INVALID_CONSTRAINTYPE_ENUM = "Invalid value for NodeFilterConstraintType enum %s";
    private static final String FAILED_TO_ADD_SUBSTITUTION_FILTER = "Failed to add substitution filter";
    private static final String ADD_SUBSTITUTION_FILTER = "Add Substitution Filter";
    private static final String ADD_SUBSTITUTION_FILTER_WITH_AN_ERROR = "An unexpected error has occurred while adding a substitution filter";
    private static final String FAILED_TO_UPDATE_SUBSTITUTION_FILTER = "Failed to update substitution filter";
    private static final String SUBSTITUTION_FILTER_UPDATE = "Substitution Filter Update";
    private static final String UPDATE_SUBSTITUTION_FILTER_WITH_AN_ERROR = "Update substitution filter with an error {}";
    private static final String FAILED_TO_DELETE_SUBSTITUTION_FILTER = "Failed to delete substitution filter";
    private static final String SUBSTITUTION_FILTER_DELETE = "Substitution Filter Delete";
    private static final String DELETE_SUBSTITUTION_FILTER_WITH_AN_ERROR = "Delete substitution filter with an error";
    private static final List<ComponentTypeEnum> EXPECTED_COMPONENT_TYPES = List.of(ComponentTypeEnum.SERVICE, ComponentTypeEnum.RESOURCE);
    private static final String EXPECTED_COMPONENT_TYPES_AS_STRING = EXPECTED_COMPONENT_TYPES.stream()
        .map(ComponentTypeEnum::findParamByType)
        .collect(Collectors.joining(", "));
    private final ComponentSubstitutionFilterBusinessLogic componentSubstitutionFilterBusinessLogic;

    @Inject
    public ComponentSubstitutionFilterServlet(final ComponentInstanceBusinessLogic componentInstanceBL,
                                              final ComponentsUtils componentsUtils, final ServletUtils servletUtils,
                                              final ResourceImportManager resourceImportManager,
                                              final ComponentSubstitutionFilterBusinessLogic componentSubstitutionFilterBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.componentSubstitutionFilterBusinessLogic = componentSubstitutionFilterBusinessLogic;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Add Component Substitution Filter Constraint", method = "POST", summary = "Add Component Substitution Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Add Substitution Filter Constraint"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response addSubstitutionFilter(@Parameter(description = "UIConstraint data", required = true) String constraintData,
                                          @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                          @Parameter(description = "valid value: resources / services", schema = @Schema(allowableValues = {
                                              ComponentTypeEnum.SERVICE_PARAM_NAME,
                                              ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                          @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                              NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                              NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                          @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentSubstitutionFilterBusinessLogic.validateUser(userId);
        final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        try {
            final Optional<UIConstraint> convertResponse = componentsUtils.parseToConstraint(constraintData, userModifier, componentTypeEnum);
            if (convertResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_PARSE_COMPONENT);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            final FilterConstraintDto filterConstraintDto = new FilterConstraintMapper().mapFrom(convertResponse.get());
            final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintType.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_CONSTRAINTYPE_ENUM, constraintType));
            }
            final Optional<SubstitutionFilterDataDefinition> actionResponse = componentSubstitutionFilterBusinessLogic
                .addSubstitutionFilter(componentId.toLowerCase(), filterConstraintDto, true, componentTypeEnum);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_ADD_SUBSTITUTION_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            final UINodeFilter uiFilter = new SubstitutionFilterConverter().convertToUi(actionResponse.get());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), uiFilter);
        } catch (final BusinessLogicException e) {
            return buildErrorResponse(e.getResponseFormat());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(ADD_SUBSTITUTION_FILTER);
            LOGGER.error(ADD_SUBSTITUTION_FILTER_WITH_AN_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Component Substitution Filter Constraint", method = "PUT", summary = "Update Component Substitution Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Update Substitution Filter Constraint"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateSubstitutionFilters(@Parameter(description = "UIConstraint data", required = true) String constraintData,
                                              @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                              @Parameter(description = "valid value: resources / services", schema = @Schema(allowableValues = {
                                                  ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                  ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                              @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                  NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                  NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                              @Context final HttpServletRequest request,
                                              @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        final User userModifier = componentSubstitutionFilterBusinessLogic.validateUser(userId);
        try {
            final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            final List<UIConstraint> uiConstraints = componentsUtils.validateAndParseConstraint(componentTypeEnum, constraintData, userModifier);
            if (CollectionUtils.isEmpty(uiConstraints)) {
                LOGGER.error("Failed to Parse Constraint data {} when executing {} ", constraintData, SUBSTITUTION_FILTER_UPDATE);
                return buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR, "Failed to parse constraint data"));
            }
            final List<FilterConstraintDto> filterConstraintList = uiConstraints.stream()
                .map(uiConstraint -> new FilterConstraintMapper().mapFrom(uiConstraint))
                .collect(Collectors.toList());
            final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintType.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_CONSTRAINTYPE_ENUM, constraintType));
            }
            final Optional<SubstitutionFilterDataDefinition> actionResponse = componentSubstitutionFilterBusinessLogic
                .updateSubstitutionFilter(componentId.toLowerCase(), filterConstraintList, true, componentTypeEnum);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_SUBSTITUTION_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new SubstitutionFilterConverter().convertToUi(actionResponse.get()));
        } catch (final BusinessLogicException e) {
            return buildErrorResponse(e.getResponseFormat());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(SUBSTITUTION_FILTER_UPDATE);
            LOGGER.error(UPDATE_SUBSTITUTION_FILTER_WITH_AN_ERROR, e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{constraintIndex}")
    @Operation(description = "Update Component Substitution Filter Constraint", method = "PUT", summary = "Update Component Substitution Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Update Substitution Filter Constraint"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateSubstitutionFilter(@Parameter(description = "Filter constraint information", required = true) UIConstraint uiConstraint,
                                             @Parameter(description = "Constraint Index") @PathParam("constraintIndex") int index,
                                             @Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                             @Parameter(description = "The component type", schema = @Schema(allowableValues = {
                                                 ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                 ComponentTypeEnum.RESOURCE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                             @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                 NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                 NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                             @Context final HttpServletRequest request,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        componentSubstitutionFilterBusinessLogic.validateUser(userId);
        try {
            final ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            if (componentTypeEnum == null || !EXPECTED_COMPONENT_TYPES.contains(componentTypeEnum)) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_COMPONENT_TYPE, componentType, EXPECTED_COMPONENT_TYPES_AS_STRING));
            }
            final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
            if (nodeFilterConstraintType.isEmpty()) {
                return buildErrorResponse(
                    getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_CONSTRAINTYPE_ENUM, constraintType));
            }
            final FilterConstraintDto filterConstraintDto = new FilterConstraintMapper().mapFrom(uiConstraint);

            final Optional<SubstitutionFilterDataDefinition> actionResponse = componentSubstitutionFilterBusinessLogic
                .updateSubstitutionFilter(componentId.toLowerCase(), filterConstraintDto, index, true);
            if (actionResponse.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_SUBSTITUTION_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new SubstitutionFilterConverter().convertToUi(actionResponse.get()));
        } catch (final BusinessLogicException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(SUBSTITUTION_FILTER_UPDATE);
            LOGGER.error(UPDATE_SUBSTITUTION_FILTER_WITH_AN_ERROR, e.getMessage(), e);
            return buildErrorResponse(e.getResponseFormat());
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(SUBSTITUTION_FILTER_UPDATE);
            LOGGER.error(UPDATE_SUBSTITUTION_FILTER_WITH_AN_ERROR, e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{constraintIndex}")
    @Operation(description = "Delete Component Substitution Filter Constraint", method = "Delete", summary = "Delete Component Substitution Filter Constraint", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Delete Substitution Filter Constraint"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteSubstitutionFilterConstraint(@Parameter(description = "Component Id") @PathParam("componentId") String componentId,
                                                       @Parameter(description = "Constraint Index") @PathParam("constraintIndex") int index,
                                                       @Parameter(description = "valid value: resources / services", schema = @Schema(allowableValues = {
                                                           ComponentTypeEnum.SERVICE_PARAM_NAME,
                                                           ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("componentType") final String componentType,
                                                       @Parameter(description = "Constraint type. Valid values: properties / capabilities", schema = @Schema(allowableValues = {
                                                           NodeFilterConstraintType.PROPERTIES_PARAM_NAME,
                                                           NodeFilterConstraintType.CAPABILITIES_PARAM_NAME})) @PathParam("constraintType") final String constraintType,
                                                       @Context final HttpServletRequest request,
                                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        LOGGER.debug(START_HANDLE_REQUEST_OF, request.getMethod(), request.getRequestURI());
        LOGGER.debug(MODIFIER_ID_IS, userId);
        componentSubstitutionFilterBusinessLogic.validateUser(userId);
        final Optional<NodeFilterConstraintType> nodeFilterConstraintType = NodeFilterConstraintType.parse(constraintType);
        if (!nodeFilterConstraintType.isPresent()) {
            return buildErrorResponse(
                getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT_PARAM, INVALID_CONSTRAINTYPE_ENUM, constraintType));
        }
        try {
            final Optional<SubstitutionFilterDataDefinition> actionResponse = componentSubstitutionFilterBusinessLogic
                .deleteSubstitutionFilter(componentId.toLowerCase(), index, true, ComponentTypeEnum.findByParamName(componentType));
            if (!actionResponse.isPresent()) {
                LOGGER.debug(FAILED_TO_DELETE_SUBSTITUTION_FILTER);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                new SubstitutionFilterConverter().convertToUi(actionResponse.get()));
        } catch (final Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(SUBSTITUTION_FILTER_DELETE);
            LOGGER.debug(DELETE_SUBSTITUTION_FILTER_WITH_AN_ERROR, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.ListUtils;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.RelationshipTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class TypesFetchServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(TypesFetchServlet.class);
    private static final String FAILED_TO_GET_ALL_NON_ABSTRACT = "failed to get all non abstract {}";
    private final PropertyBusinessLogic propertyBusinessLogic;
    private final RelationshipTypeBusinessLogic relationshipTypeBusinessLogic;
    private final CapabilitiesBusinessLogic capabilitiesBusinessLogic;
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;
    private final ResourceBusinessLogic resourceBusinessLogic;
    private final ArtifactsBusinessLogic artifactsBusinessLogic;

    @Inject
    public TypesFetchServlet(UserBusinessLogic userBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                             ServletUtils servletUtils, ResourceImportManager resourceImportManager, PropertyBusinessLogic propertyBusinessLogic,
                             RelationshipTypeBusinessLogic relationshipTypeBusinessLogic, CapabilitiesBusinessLogic capabilitiesBusinessLogic,
                             InterfaceOperationBusinessLogic interfaceOperationBusinessLogic, ResourceBusinessLogic resourceBusinessLogic,
                             ArtifactsBusinessLogic artifactsBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.propertyBusinessLogic = propertyBusinessLogic;
        this.relationshipTypeBusinessLogic = relationshipTypeBusinessLogic;
        this.capabilitiesBusinessLogic = capabilitiesBusinessLogic;
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
        this.resourceBusinessLogic = resourceBusinessLogic;
        this.artifactsBusinessLogic = artifactsBusinessLogic;
    }

    @GET
    @Path("dataTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get data types", method = "GET", summary = "Returns data types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "datatypes"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Data types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllDataTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                           @Parameter(description = "model") @QueryParam("model") String modelName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        init();
        validateUserExist(responseWrapper, userWrapper, userId);
        if (responseWrapper.isEmpty()) {
            String url = request.getMethod() + " " + request.getRequestURI();
            log.debug("Start handle request of {} - modifier id is {}", url, userId);
            resourceBusinessLogic.getApplicationDataTypeCache().refreshDataTypesCacheIfStale();
            final Map<String, DataTypeDefinition> dataTypes = resourceBusinessLogic.getComponentsUtils()
                .getAllDataTypes(resourceBusinessLogic.getApplicationDataTypeCache(), modelName);
            String dataTypeJson = gson.toJson(dataTypes);
            Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
            responseWrapper.setInnerElement(okResponse);
        }
        return responseWrapper.getInnerElement();
    }

    @GET
    @Path("interfaceLifecycleTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get interface lifecycle types", method = "GET", summary = "Returns interface lifecycle types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Interface lifecycle types"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Interface lifecycle types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInterfaceLifecycleTypes(@Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                               @Parameter(description = "model") @QueryParam("model") String modelName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        try {
            validateUserExist(responseWrapper, userWrapper, userId);
            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.info("Start handle request of {} | modifier id is {}", url, userId);
                Either<Map<String, InterfaceDefinition>, ResponseFormat> allInterfaceLifecycleTypes = interfaceOperationBusinessLogic
                    .getAllInterfaceLifecycleTypes(modelName);
                if (allInterfaceLifecycleTypes.isRight()) {
                    log.info("Failed to get all interface lifecycle types. Reason - {}", allInterfaceLifecycleTypes.right().value());
                    Response errorResponse = buildErrorResponse(allInterfaceLifecycleTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);
                } else {
                    String interfaceLifecycleTypeJson = gson.toJson(allInterfaceLifecycleTypes.left().value());
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), interfaceLifecycleTypeJson);
                    responseWrapper.setInnerElement(okResponse);
                }
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            log.debug("get all interface lifecycle types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("capabilityTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get capability types", method = "GET", summary = "Returns capability types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "capabilityTypes"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Capability types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllCapabilityTypesServlet(@Context final HttpServletRequest request,
                                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                 @Parameter(description = "model", required = false) @QueryParam("model") String modelName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);
            modelName = ValidationUtils.sanitizeInputString(modelName);
            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);
                Either<Map<String, CapabilityTypeDefinition>, ResponseFormat> allDataTypes = capabilitiesBusinessLogic.getAllCapabilityTypes(modelName);
                if (allDataTypes.isRight()) {
                    log.info("Failed to get all capability types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);
                } else {
                    Map<String, CapabilityTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
                    responseWrapper.setInnerElement(okResponse);
                }
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Capability Types");
            log.debug("get all capability types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("relationshipTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get relationship types", method = "GET", summary = "Returns relationship types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "relationshipTypes"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Relationship types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllRelationshipTypesServlet(@Context final HttpServletRequest request,
                                                   @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                   @Parameter(description = "model", required = false) @QueryParam("model") String modelName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);
            modelName = ValidationUtils.sanitizeInputString(modelName);
            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);
                Either<Map<String, RelationshipTypeDefinition>, ResponseFormat> allDataTypes = relationshipTypeBusinessLogic
                    .getAllRelationshipTypes(modelName);
                if (allDataTypes.isRight()) {
                    log.info("Failed to get all relationship types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);
                } else {
                    Map<String, RelationshipTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
                    responseWrapper.setInnerElement(okResponse);
                }
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Relationship Types");
            log.debug("get all relationship types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("nodeTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get node types", method = "GET", summary = "Returns node types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "nodeTypes"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Node types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllNodeTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();
        Either<Map<String, Component>, Response> response;
        Map<String, Component> componentMap;
        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);
            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);
                response = getComponent(resourceBusinessLogic, true, userId);
                if (response.isRight()) {
                    return response.right().value();
                }
                componentMap = new HashMap<>(response.left().value());
                response = getComponent(resourceBusinessLogic, false, userId);
                if (response.isRight()) {
                    return response.right().value();
                }
                componentMap.putAll(response.left().value());
                String nodeTypesJson = gson.toJson(componentMap);
                Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), nodeTypesJson);
                responseWrapper.setInnerElement(okResponse);
            }
            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Node Types");
            log.debug("get all node types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("/artifactTypes")
    @Operation(description = "Get Tosca ArtifactTypes", method = "GET", summary = "Returns tosca artifact types", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Listing successful"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Tosca Artifact Types not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllToscaArtifactTypes(@Parameter(description = "Model name") @QueryParam("model") String model,
                                             @Context final HttpServletRequest request, @HeaderParam("USER_ID") String creator) {
        try {
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), artifactsBusinessLogic.getAllToscaArtifacts(model));
        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            final var errorMsg = "Unexpected error while listing the Tosca Artifact types";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(errorMsg);
            log.error(EcompLoggerErrorCode.UNKNOWN_ERROR, this.getClass().getName(), errorMsg, e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }


    private Either<Map<String, Component>, Response> getComponent(ComponentBusinessLogic resourceBL, boolean isAbstract, String userId) {
        Either<List<Component>, ResponseFormat> actionResponse;
        List<Component> componentList;
        actionResponse = resourceBL
            .getLatestVersionNotAbstractComponentsMetadata(isAbstract, HighestFilterEnum.HIGHEST_ONLY, ComponentTypeEnum.RESOURCE, null, userId,
                null, false);
        if (actionResponse.isRight()) {
            log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, ComponentTypeEnum.RESOURCE.getValue());
            return Either.right(buildErrorResponse(actionResponse.right().value()));
        }
        componentList = actionResponse.left().value();
        return Either.left(ListUtils.emptyIfNull(componentList).stream().filter(component ->
            ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition()).getToscaResourceName() != null)
            .collect(Collectors.toMap(
                component -> ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition().getMetadataDataDefinition())
                    .getToscaResourceName(), component -> component, (component1, component2) -> component1)));
    }
}

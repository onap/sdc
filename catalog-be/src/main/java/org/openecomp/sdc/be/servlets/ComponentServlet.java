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

import static org.openecomp.sdc.common.util.GeneralUtility.getCategorizedComponents;

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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogicProvider;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.mixin.GroupCompositionMixin;
import org.openecomp.sdc.be.mixin.PolicyCompositionMixin;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.IComponentInstanceConnectedElement;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiLeftPaletteComponent;
import org.openecomp.sdc.be.view.ResponseView;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class ComponentServlet extends BeGenericServlet {

    private static final String GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION = "getCertifiedNotAbstractComponents failed with exception";
    private static final String GET_CERTIFIED_NON_ABSTRACT = "Get Certified Non Abstract";
    private static final String FAILED_TO_GET_ALL_NON_ABSTRACT = "failed to get all non abstract {}";
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final Logger log = Logger.getLogger(ComponentServlet.class);
    private final ComponentBusinessLogicProvider componentBusinessLogicProvider;

    @Inject
    public ComponentServlet(ComponentsUtils componentsUtils,
                            ComponentBusinessLogicProvider componentBusinessLogicProvider) {
        super(componentsUtils);
        this.componentBusinessLogicProvider = componentBusinessLogicProvider;
    }

    @GET
    @Path("/{componentType}/{componentUuid}/conformanceLevelValidation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Validate Component Conformance Level", method = "GET", summary = "Returns the result according to conformance level in BE config", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response conformanceLevelValidation(@PathParam("componentType") final String componentType,
                                               @PathParam("componentUuid") final String componentUuid, @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        Response response;
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum != null) {
            ComponentBusinessLogic compBL = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            Either<Boolean, ResponseFormat> eitherConformanceLevel = compBL.validateConformanceLevel(componentUuid, componentTypeEnum, userId);
            if (eitherConformanceLevel.isRight()) {
                response = buildErrorResponse(eitherConformanceLevel.right().value());
            } else {
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    gson.toJson(eitherConformanceLevel.left().value()));
            }
        } else {
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        return response;
    }

    @GET
    @Path("/{componentType}/{componentId}/requirmentsCapabilities")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Component Requirments And Capabilities", method = "GET", summary = "Returns Requirements And Capabilities according to componentId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getRequirementAndCapabilities(@PathParam("componentType") final String componentType,
                                                  @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
                                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        Response response;
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
        if (componentTypeEnum != null) {
            try {
                ComponentBusinessLogic compBL = componentBusinessLogicProvider.getInstance(componentTypeEnum);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK),
                    RepresentationUtils.toRepresentation(compBL.getRequirementsAndCapabilities(componentId, componentTypeEnum, userId)));
            } catch (IOException e) {
                BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Capabilities and requirements for " + componentId);
                log.debug("getRequirementAndCapabilities failed with exception", e);
                throw e;
            }
        } else {
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        return response;
    }

    @GET
    @Path("/{componentType}/latestversion/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Component Requirments And Capabilities", method = "GET", summary = "Returns Requirments And Capabilities according to componentId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getLatestVersionNotAbstractCheckoutComponents(@PathParam("componentType") final String componentType,
                                                                  @Context final HttpServletRequest request,
                                                                  @QueryParam("internalComponentType") String internalComponentType,
                                                                  @QueryParam("componentUids") List<String> componentUids,
                                                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            log.debug("Received componentUids size is {}", componentUids == null ? 0 : componentUids.size());
            Either<List<Component>, ResponseFormat> actionResponse = businessLogic
                .getLatestVersionNotAbstractComponents(false, componentTypeEnum, internalComponentType, componentUids, userId);
            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @POST
    @Path("/{componentType}/latestversion/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Component Requirments And Capabilities", method = "GET", summary = "Returns Requirments And Capabilities according to componentId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getLatestVersionNotAbstractCheckoutComponentsByBody(@PathParam("componentType") final String componentType,
                                                                        @Context final HttpServletRequest request,
                                                                        @QueryParam("internalComponentType") String internalComponentType,
                                                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                                        @Parameter(description = "Consumer Object to be created", required = true) List<String> data)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        Response response;
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            if (log.isDebugEnabled()) {
                log.debug("Received componentUids size is {}", data == null ? 0 : data.size());
            }
            Either<List<Component>, ResponseFormat> actionResponse = businessLogic
                .getLatestVersionNotAbstractComponents(false, componentTypeEnum, internalComponentType, data, userId);
            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/latestversion/notabstract/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Component uid only", method = "GET", summary = "Returns componentId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getLatestVersionNotAbstractCheckoutComponentsIdesOnly(@PathParam("componentType") final String componentType,
                                                                          @Context final HttpServletRequest request,
                                                                          @QueryParam("internalComponentType") String internalComponentType,
                                                                          @QueryParam("componentModel") String internalComponentModel,
                                                                          @QueryParam("includeNormativeExtensionModels") boolean includeNormativeExtensionModels,
                                                                          @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                                          @Parameter(description = "uid list", required = true) String data)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            if (internalComponentModel != null) {
                internalComponentModel = ValidationUtils.sanitizeInputString(internalComponentModel.trim());
            }
            Either<List<Component>, ResponseFormat> actionResponse = businessLogic
                .getLatestVersionNotAbstractComponentsMetadata(false, HighestFilterEnum.HIGHEST_ONLY, componentTypeEnum,
                    internalComponentType, userId, internalComponentModel, includeNormativeExtensionModels);
            if (actionResponse.isRight()) {
                log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            List<UiLeftPaletteComponent> uiLeftPaletteComponents = getComponentsUtils()
                .convertComponentToUiLeftPaletteComponentObject(actionResponse.left().value());
            Map<String, Map<String, List<UiLeftPaletteComponent>>> categorizedComponents = getCategorizedComponents(uiLeftPaletteComponents);
            Object components = RepresentationUtils.toRepresentation(categorizedComponents);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(GET_CERTIFIED_NON_ABSTRACT + componentType);
            log.debug(GET_CERTIFIED_NOT_ABSTRACT_COMPONENTS_FAILED_WITH_EXCEPTION, e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Component instances", method = "GET", summary = "Returns component instances", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getComponentInstancesFilteredByPropertiesAndInputs(@PathParam("componentType") final String componentType,
                                                                       @PathParam("componentId") final String componentId,
                                                                       @Context final HttpServletRequest request,
                                                                       @QueryParam("searchText") String searchText,
                                                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                                                       @Parameter(description = "uid" + " " + "list", required = true) String data)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            Either<List<ComponentInstance>, ResponseFormat> actionResponse = businessLogic
                .getComponentInstancesFilteredByPropertiesAndInputs(componentId, userId);
            if (actionResponse.isRight()) {
                log.debug("failed to get all component instances filtered by properties and inputs", componentType);
                return buildErrorResponse(actionResponse.right().value());
            }
            Object components = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), components);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Component Instances filtered by properties & inputs" + componentType);
            log.debug("getComponentInstancesFilteredByPropertiesAndInputs failed with exception", e);
            throw e;
        }
    }

    /**
     * This API is a generic api for ui - the api get a list of strings and return the data on the component according to to list. for example: list
     * of the string "properties, inputs" will return component with the list of properties and inputs.
     *
     * @param componentType
     * @param componentId
     * @param dataParamsToReturn
     * @param request
     * @param userId
     * @return
     */
    @GET
    @Path("/{componentType}/{componentId}/filteredDataByParams")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Resource", method = "GET", summary = "Returns resource according to resourceId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @ResponseView(mixin = {GroupCompositionMixin.class, PolicyCompositionMixin.class})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getComponentDataFilteredByParams(@PathParam("componentType") final String componentType,
                                                     @PathParam("componentId") final String componentId,
                                                     @QueryParam("include") final List<String> dataParamsToReturn,
                                                     @Context final HttpServletRequest request,
                                                     @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        try {
            String resourceIdLower = componentId.toLowerCase();
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            log.trace("get component with id {} filtered by ui params", componentId);
            Either<UiComponentDataTransfer, ResponseFormat> actionResponse = businessLogic
                .getComponentDataFilteredByParams(resourceIdLower, modifier, dataParamsToReturn);
            if (actionResponse.isRight()) {
                log.debug("failed to get component data filtered by ui params");
                return buildErrorResponse(actionResponse.right().value());
            }
            RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get component filtered by ui params");
            log.debug("get resource failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/filteredproperties/{propertyNameFragment}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve properties belonging to component instances of specific component by name and optionally resource type", method = "GET", summary = "Returns properties belonging to component instances of specific component by name and optionally resource type", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getFilteredComponentInstanceProperties(@PathParam("componentType") final String componentType,
                                                           @PathParam("componentId") final String componentId,
                                                           @PathParam("propertyNameFragment") final String propertyNameFragment,
                                                           @QueryParam("resourceType") List<String> resourceTypes,
                                                           @Context final HttpServletRequest request,
                                                           @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        User user = new User();
        user.setUserId(userId);
        log.debug("User Id is {}", userId);
        Response response;
        try {
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentBusinessLogic businessLogic = componentBusinessLogicProvider.getInstance(componentTypeEnum);
            Map<FilterKeyEnum, List<String>> filters = new EnumMap<>(FilterKeyEnum.class);
            List<String> propertyNameFragments = new ArrayList<>();
            propertyNameFragments.add(propertyNameFragment);
            filters.put(FilterKeyEnum.NAME_FRAGMENT, propertyNameFragments);
            if (CollectionUtils.isNotEmpty(resourceTypes)) {
                filters.put(FilterKeyEnum.RESOURCE_TYPE, resourceTypes);
            }
            Either<Map<String, List<IComponentInstanceConnectedElement>>, ResponseFormat> actionResponse = businessLogic
                .getFilteredComponentInstanceProperties(componentId, filters, userId);
            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object resource = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Filtered Component Instance Properties");
            log.debug("Getting of filtered component instance properties failed with exception", e);
            throw e;
        }
    }
}

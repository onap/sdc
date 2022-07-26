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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.DataTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Path("/v1/catalog")
@Controller
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InputsServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(InputsServlet.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(InputsServlet.class.getName());
    private static final String START_HANDLE_REQUEST_OF = "(get) Start handle request of {}";
    private static final String CREATE_INPUT = "CreateInput";
    private final DataTypeBusinessLogic businessLogic;
    private final InputsBusinessLogic inputsBusinessLogic;

    @Inject
    public InputsServlet(UserBusinessLogic userBusinessLogic, InputsBusinessLogic inputsBusinessLogic,
                         ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils, ServletUtils servletUtils,
                         ResourceImportManager resourceImportManager, DataTypeBusinessLogic dataTypeBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.inputsBusinessLogic = inputsBusinessLogic;
        this.businessLogic = dataTypeBusinessLogic;
    }

    @POST
    @Path("/{containerComponentType}/{componentId}/update/inputs")
    @Operation(description = "Update resource inputs", method = "POST", summary = "Returns updated input", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Input updated"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    public Response updateComponentInputs(
        @Parameter(description = "valid values: resources / services", schema = @Schema(allowableValues = {ComponentTypeEnum.RESOURCE_PARAM_NAME,
            ComponentTypeEnum.SERVICE_PARAM_NAME})) @PathParam("containerComponentType") final String containerComponentType,
        @PathParam("componentId") final String componentId, @Parameter(description = "json describe the input", required = true) String data,
        @Context final HttpServletRequest request) throws JsonProcessingException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        try {
            User modifier = new User();
            modifier.setUserId(userId);
            log.debug("modifier id is {}", userId);
            Either<InputDefinition[], ResponseFormat> inputsEither = getComponentsUtils()
                .convertJsonToObjectUsingObjectMapper(data, modifier, InputDefinition[].class, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
                    ComponentTypeEnum.SERVICE);
            if (inputsEither.isRight()) {
                log.debug("Failed to convert data to input definition. Status is {}", inputsEither.right().value());
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            List<InputDefinition> inputsToUpdate = Arrays.asList(inputsEither.left().value());
            log.debug("Start handle request of updateComponentInputs. Received inputs are {}", inputsToUpdate);
            ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(containerComponentType);
            if (businessLogic == null) {
                log.debug("Unsupported component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR));
            }
            Either<List<InputDefinition>, ResponseFormat> actionResponse = inputsBusinessLogic
                .updateInputsValue(componentType, componentId, inputsToUpdate, userId, true);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            List<InputDefinition> componentInputs = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(componentInputs);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComponentUid}/inputs")
    @Operation(description = "Get Inputs only", method = "GET", summary = "Returns Inputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getComponentInstanceInputs(@PathParam("componentType") final String componentType,
                                               @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
                                               @PathParam("originComponentUid") final String originComponentUid,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            Either<List<ComponentInstanceInput>, ResponseFormat> inputsResponse = inputsBusinessLogic
                .getComponentInstanceInputs(userId, componentId, instanceId);
            if (inputsResponse.isRight()) {
                log.debug("failed to get component instance inputs {}", componentType);
                return buildErrorResponse(inputsResponse.right().value());
            }
            Object inputs = RepresentationUtils.toRepresentation(inputsResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputs);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs " + componentType);
            log.debug("getInputs failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{inputId}/properties")
    @Operation(description = "Get properties", method = "GET", summary = "Returns properties list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getInputPropertiesForComponentInstance(@PathParam("componentType") final String componentType,
                                                           @PathParam("componentId") final String componentId,
                                                           @PathParam("instanceId") final String instanceId,
                                                           @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
                                                           @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            Either<List<ComponentInstanceProperty>, ResponseFormat> inputPropertiesRes = inputsBusinessLogic
                .getComponentInstancePropertiesByInputId(userId, componentId, instanceId, inputId);
            if (inputPropertiesRes.isRight()) {
                log.debug("failed to get properties of input: {}, with instance id: {}", inputId, instanceId);
                return buildErrorResponse(inputPropertiesRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance()
                .logBeRestApiGeneralError("Get Properites by input id: " + inputId + " for instance with id: " + instanceId);
            log.debug("getInputPropertiesForComponentInstance failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/inputs/{inputId}/inputs")
    @Operation(description = "Get inputs", method = "GET", summary = "Returns inputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getInputsForComponentInput(@PathParam("componentType") final String componentType,
                                               @PathParam("componentId") final String componentId, @PathParam("inputId") final String inputId,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            Either<List<ComponentInstanceInput>, ResponseFormat> inputsRes = inputsBusinessLogic
                .getInputsForComponentInput(userId, componentId, inputId);
            if (inputsRes.isRight()) {
                log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
                return buildErrorResponse(inputsRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance()
                .logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
            log.debug("getInputsForComponentInput failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/{componentType}/{componentId}/inputs/{inputId}")
    @Operation(description = "Get inputs", method = "GET", summary = "Returns inputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response getInputsAndPropertiesForComponentInput(@PathParam("componentType") final String componentType,
                                                            @PathParam("componentId") final String componentId,
                                                            @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
                                                            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        try {
            Either<InputDefinition, ResponseFormat> inputsRes = inputsBusinessLogic
                .getInputsAndPropertiesForComponentInput(userId, componentId, inputId, false);
            if (inputsRes.isRight()) {
                log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
                return buildErrorResponse(inputsRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance()
                .logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
            log.debug("getInputsForComponentInput failed with exception", e);
            throw e;
        }
    }

    private Either<ComponentInstListInput, ResponseFormat> parseToComponentInstListInput(String json, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(json, user, ComponentInstListInput.class, AuditingActionEnum.CREATE_RESOURCE,
            ComponentTypeEnum.SERVICE);
    }

    @POST
    @Path("/{componentType}/{componentId}/create/inputs")
    @Operation(description = "Create inputs on service", method = "POST", summary = "Return inputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response createMultipleInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                         @Parameter(description = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {
        return super.declareProperties(userId, componentId, componentType, componentInstInputsMapObj, DeclarationTypeEnum.INPUT, request);
    }

    @POST
    @Path("/{componentType}/{componentId}/create/input")
    @Operation(description = "Create inputs on service", method = "POST", summary = "Return inputs list", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response createInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                @Parameter(description = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {
        return createInput(componentId, componentInstInputsMapObj, request, userId);
    }

    /**
     * Creates a "list input" and updates given list of properties to get value from the input. also a data type which has same properties is created.
     * the data type will be the entry_schema of the list input.
     *
     * @param componentType             the container type (service, resource, ...)
     * @param componentId               the container ID
     * @param request                   HttpServletRequest object
     * @param userId                    the User ID
     * @param componentInstInputsMapObj the list of properties to be declared and the "list input" to be created. the type of the input must be
     *                                  "list". schema.type of the input will be the name of new data type.
     * @return the created input
     */
    @POST
    @Path("/{componentType}/{componentId}/create/listInput")
    @Operation(description = "Create a list input on service", method = "POST", summary = "Return input", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Component found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    public Response createListInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                    @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                    @Parameter(description = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("#createListInput: Start handle request of {}", url);
        try {
            // get modifier id
            User modifier = new User();
            modifier.setUserId(userId);
            log.debug("modifier id is {}", userId);
            Either<ComponentInstListInput, ResponseFormat> componentInstInputsMapRes = parseToComponentInstListInput(componentInstInputsMapObj,
                modifier);
            if (componentInstInputsMapRes.isRight()) {
                log.debug("failed to parse componentInstInputsMap");
                return buildErrorResponse(componentInstInputsMapRes.right().value());
            }
            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentInstListInput componentInstInputsMap = componentInstInputsMapRes.left().value();
            if (log.isDebugEnabled()) {
                // for inspection on debug
                log.debug("parsed componentInstInputsMap={}", ReflectionToStringBuilder.toString(componentInstInputsMap));
            }
            Either<List<InputDefinition>, ResponseFormat> inputPropertiesRes = inputsBusinessLogic
                .createListInput(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
            if (inputPropertiesRes.isRight()) {
                log.debug("failed to create list input for service: {}", componentId);
                return buildErrorResponse(inputPropertiesRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create list input for service with id: " + componentId);
            log.debug("createListInput failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @DELETE
    @Path("/{componentType}/{componentId}/delete/{inputId}/input")
    @Operation(description = "Delete input from service", method = "DELETE", summary = "Delete service input", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Input deleted"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Input not found")})
    public Response deleteInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                @Parameter(description = "Service Input to be deleted", required = true) String componentInstInputsMapObj) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        loggerSupportability.log(LoggerSupportabilityActions.DELETE_INPUTS, StatusCode.STARTED, "Starting to delete Inputs for component {} ",
            componentId + " by " + userId);
        try {
            InputDefinition deleteInput = inputsBusinessLogic.deleteInput(componentId, userId, inputId);
            loggerSupportability.log(LoggerSupportabilityActions.DELETE_INPUTS, StatusCode.COMPLETE, "Ended delete Inputs for component {} ",
                componentId + " by " + userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteInput);
        } catch (ComponentException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete input for service + " + componentId + " + with id: " + inputId);
            log.debug("Delete input failed with exception", e);
            throw e;
        }
    }

    /**
     * Gets a specific data type associated with a component.
     *
     * @param componentType the container type (service, resource, ...)
     * @param componentId   the container ID
     * @param dataTypeName  the data type name
     * @param request       HttpServletRequest object
     * @return the data type info
     */
    @GET
    @Path("/{componentType}/{componentId}/dataType/{dataTypeName}")
    @Operation(description = "Get data type in service", method = "GET", summary = "Get data type in service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = DataTypeDefinition.class)))),
        @ApiResponse(responseCode = "200", description = "Data type found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Data type not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getDataType(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                @PathParam("dataTypeName") final String dataTypeName, @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getDataType) Start handle request of {}", url);
        Response response;
        try {
            Either<DataTypeDefinition, StorageOperationStatus> getResult = businessLogic.getPrivateDataType(componentId, dataTypeName);
            if (getResult.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getResult.right().value());
                return buildErrorResponse(componentsUtils.getResponseFormat(actionStatus));
            }
            Object json = RepresentationUtils.toRepresentation(getResult.left().value());
            return buildOkResponse(componentsUtils.getResponseFormat(ActionStatus.OK), json);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance()
                .logBeRestApiGeneralError("Get data type from service + " + componentId + " + with name: " + dataTypeName);
            log.debug("Get data type failed with exception", e);
            response = buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    /**
     * Gets a list of data types which a component has.
     *
     * @param componentType the container type (service, resource, ...)
     * @param componentId   the container ID
     * @param request       HttpServletRequest object
     * @return the list of data types in the component
     */
    @GET
    @Path("/{componentType}/{componentId}/dataTypes")
    @Operation(description = "Get data types that service has", method = "GET", summary = "Get data types in service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Data type found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Component not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getDataTypes(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                 @Context final HttpServletRequest request) {
        ComponentsUtils componentsUtils = getComponentsUtils();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getDataType) Start handle request of {}", url);
        Response response;
        try {
            Either<List<DataTypeDefinition>, StorageOperationStatus> getResult = businessLogic.getPrivateDataTypes(componentId);
            if (getResult.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getResult.right().value());
                return buildErrorResponse(componentsUtils.getResponseFormat(actionStatus));
            }
            Object json = RepresentationUtils.toRepresentation(getResult.left().value());
            return buildOkResponse(componentsUtils.getResponseFormat(ActionStatus.OK), json);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get data type from service + " + componentId);
            log.debug("Get data type failed with exception", e);
            response = buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    /**
     * Deletes a data type from a component.
     *
     * @param componentType the container type (service, resource, ...)
     * @param componentId   the container ID
     * @param dataTypeName  the data type name to be deleted
     * @param request       HttpServletRequest object
     * @return operation result
     */
    @DELETE
    @Path("/{componentType}/{componentId}/dataType/{dataTypeName}")
    @Operation(description = "Delete data type from service", method = "DELETE", summary = "Delete service input", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Data type deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Data type not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteDataType(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId,
                                   @PathParam("dataTypeName") final String dataTypeName, @Context final HttpServletRequest request) {
        ComponentsUtils componentsUtils = getComponentsUtils();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        Response response;
        try {
            Either<DataTypeDefinition, StorageOperationStatus> deleteResult = businessLogic.deletePrivateDataType(componentId, dataTypeName);
            if (deleteResult.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteResult.right().value());
                return buildErrorResponse(componentsUtils.getResponseFormat(actionStatus));
            }
            Object json = RepresentationUtils.toRepresentation(deleteResult.left().value());
            return buildOkResponse(componentsUtils.getResponseFormat(ActionStatus.OK), json);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance()
                .logBeRestApiGeneralError("Delete data type for service + " + componentId + " + with name: " + dataTypeName);
            log.debug("Delete data type failed with exception", e);
            response = buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    private Response createInput(String componentId, String data, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, StatusCode.STARTED, "CREATE_INPUTS by user {} ", userId);
        try {
            Either<Map<String, InputDefinition>, ActionStatus> inputDefinition = getInputModel(componentId, data);
            if (inputDefinition.isRight()) {
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(inputDefinition.right().value());
                return buildErrorResponse(responseFormat);
            }
            Map<String, InputDefinition> inputs = inputDefinition.left().value();
            if (inputs == null || inputs.size() != 1) {
                log.info("Input content is invalid - {}", data);
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
                return buildErrorResponse(responseFormat);
            }
            Map.Entry<String, InputDefinition> entry = inputs.entrySet().iterator().next();
            InputDefinition newInputDefinition = entry.getValue();
            newInputDefinition.setParentUniqueId(componentId);
            String inputName = newInputDefinition.getName();
            Either<EntryData<String, InputDefinition>, ResponseFormat> addInputEither = inputsBusinessLogic
                .addInputToComponent(componentId, inputName, newInputDefinition, userId);
            if (addInputEither.isRight()) {
                return buildErrorResponse(addInputEither.right().value());
            }
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_INPUTS, StatusCode.COMPLETE, "CREATE_INPUTS by user {} ", userId);
            return buildOkResponse(newInputDefinition);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_INPUT);
            log.debug("create input failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }
}

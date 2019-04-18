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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
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
import org.openecomp.sdc.be.components.impl.DataTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.InputsBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.DeclarationTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.ComponentInstListInput;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Api(value = "Input Catalog", description = "Input Servlet")
@Path("/v1/catalog")
@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InputsServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(InputsServlet.class);

    @POST
    @Path("/{containerComponentType}/{componentId}/update/inputs")
    @ApiOperation(value = "Update resource  inputs", httpMethod = "POST", notes = "Returns updated input", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Input updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response updateComponentInputs(
            @ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("containerComponentType") final String containerComponentType,
            @PathParam("componentId") final String componentId,
            @ApiParam(value = "json describe the input", required = true) String data, @Context final HttpServletRequest request) {

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        String userId = request.getHeader(Constants.USER_ID_HEADER);

        try {
            User modifier = new User();
            modifier.setUserId(userId);
            log.debug("modifier id is {}", userId);

            Either<InputDefinition[], ResponseFormat> inputsEither = getComponentsUtils()
                    .convertJsonToObjectUsingObjectMapper(data, modifier, InputDefinition[].class,
                            AuditingActionEnum.UPDATE_RESOURCE_METADATA, ComponentTypeEnum.SERVICE);
            if(inputsEither.isRight()){
                log.debug("Failed to convert data to input definition. Status is {}", inputsEither.right().value());
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            List<InputDefinition> inputsToUpdate = Arrays.asList(inputsEither.left().value());

            log.debug("Start handle request of updateComponentInputs. Received inputs are {}", inputsToUpdate);

            ServletContext context = request.getSession().getServletContext();
            ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(containerComponentType);

            InputsBusinessLogic businessLogic = getInputBL(context);
            if (businessLogic == null) {
                log.debug("Unsupported component type {}", containerComponentType);
                return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.UNSUPPORTED_ERROR));
            }

            Either<List<InputDefinition>, ResponseFormat> actionResponse = businessLogic.updateInputsValue(componentType, componentId, inputsToUpdate, userId, true, false);

            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }

            List<InputDefinition> componentInputs = actionResponse.left().value();
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(componentInputs);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

        }
        catch (Exception e) {
            log.error("create and associate RI failed with exception: {}", e.getMessage(), e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }


    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{originComponentUid}/inputs")
    @ApiOperation(value = "Get Inputs only", httpMethod = "GET", notes = "Returns Inputs list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getComponentInstanceInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
                                               @PathParam("originComponentUid") final String originComponentUid, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response;

        try {
            InputsBusinessLogic businessLogic = getInputBL(context);

            Either<List<ComponentInstanceInput>, ResponseFormat> inputsResponse = businessLogic.getComponentInstanceInputs(userId, componentId, instanceId);
            if (inputsResponse.isRight()) {
                log.debug("failed to get component instance inputs {}", componentType);
                return buildErrorResponse(inputsResponse.right().value());
            }
            Object inputs = RepresentationUtils.toRepresentation(inputsResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), inputs);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Inputs " + componentType);
            log.debug("getInputs failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/{componentType}/{componentId}/componentInstances/{instanceId}/{inputId}/properties")
    @ApiOperation(value = "Get properties", httpMethod = "GET", notes = "Returns properties list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getInputPropertiesForComponentInstance(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("instanceId") final String instanceId,
            @PathParam("inputId") final String inputId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(GET) Start handle request of {}", url);
        Response response = null;

        try {
            InputsBusinessLogic businessLogic = getInputBL(context);

            Either<List<ComponentInstanceProperty>, ResponseFormat> inputPropertiesRes = businessLogic.getComponentInstancePropertiesByInputId(userId, componentId, instanceId, inputId);
            if (inputPropertiesRes.isRight()) {
                log.debug("failed to get properties of input: {}, with instance id: {}", inputId, instanceId);
                return buildErrorResponse(inputPropertiesRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Properites by input id: " + inputId + " for instance with id: " + instanceId);
            log.debug("getInputPropertiesForComponentInstance failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/{componentType}/{componentId}/inputs/{inputId}/inputs")
    @ApiOperation(value = "Get inputs", httpMethod = "GET", notes = "Returns inputs list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getInputsForComponentInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response;
        try {
            InputsBusinessLogic businessLogic = getInputBL(context);

            Either<List<ComponentInstanceInput>, ResponseFormat> inputsRes = businessLogic.getInputsForComponentInput(userId, componentId, inputId);

            if (inputsRes.isRight()) {
                log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
                return buildErrorResponse(inputsRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
            log.debug("getInputsForComponentInput failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/{componentType}/{componentId}/inputs/{inputId}")
    @ApiOperation(value = "Get inputs", httpMethod = "GET", notes = "Returns inputs list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response getInputsAndPropertiesForComponentInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @PathParam("inputId") final String inputId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response;

        try {
            InputsBusinessLogic businessLogic = getInputBL(context);

            Either<InputDefinition, ResponseFormat> inputsRes = businessLogic.getInputsAndPropertiesForComponentInput(userId, componentId, inputId, false);

            if (inputsRes.isRight()) {
                log.debug("failed to get inputs of input: {}, with instance id: {}", inputId, componentId);
                return buildErrorResponse(inputsRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputsRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get inputs by input id: " + inputId + " for component with id: " + componentId);
            log.debug("getInputsForComponentInput failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    private Either<ComponentInstInputsMap, ResponseFormat> parseToComponentInstanceMap(String serviceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(serviceJson, user, ComponentInstInputsMap.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
    }

    private Either<ComponentInstListInput, ResponseFormat> parseToComponentInstListInput(String json, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(json, user, ComponentInstListInput.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
    }

    @POST
    @Path("/{componentType}/{componentId}/create/inputs")
    @ApiOperation(value = "Create inputs on service", httpMethod = "POST", notes = "Return inputs list", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response createMultipleInputs(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {

        return super.declareProperties(userId, componentId, componentType, componentInstInputsMapObj,
                DeclarationTypeEnum.INPUT, request);
    }


    /**
     * Creates a "list input" and updates given list of properties to get value from the input.
     * also a data type which has same properties is created.
     * the data type will be the entry_schema of the list input.
     * @param componentType the container type (service, resource, ...)
     * @param componentId the container ID
     * @param request HttpServletRequest object
     * @param userId the User ID
     * @param componentInstInputsMapObj the list of properties to be declared and the "list input" to be created.
     *                                  the type of the input must be "list".
     *                                  schema.type of the input will be the name of new data type.
     * @return the created input
     */
    @POST
    @Path("/{componentType}/{componentId}/create/listInput")
    @ApiOperation(value = "Create a list input on service", httpMethod = "POST", notes = "Return input", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Component found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Component not found") })
    public Response createListInput(@PathParam("componentType") final String componentType, @PathParam("componentId") final String componentId, @Context final HttpServletRequest request,
                                         @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @ApiParam(value = "ComponentIns Inputs Object to be created", required = true) String componentInstInputsMapObj) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response = null;

        try {
            InputsBusinessLogic businessLogic = getInputBL(context);

            // get modifier id
            User modifier = new User();
            modifier.setUserId(userId);
            log.debug("modifier id is {}", userId);

            Either<ComponentInstListInput, ResponseFormat> componentInstInputsMapRes = parseToComponentInstListInput(componentInstInputsMapObj, modifier);
            if (componentInstInputsMapRes.isRight()) {
                log.debug("failed to parse componentInstInputsMap");
                response = buildErrorResponse(componentInstInputsMapRes.right().value());
                return response;
            }

            ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(componentType);
            ComponentInstListInput componentInstInputsMap = componentInstInputsMapRes.left().value();
            log.debug("converted json into: {}", ReflectionToStringBuilder.toString(componentInstInputsMap));

            Either<List<InputDefinition>, ResponseFormat> inputPropertiesRes = businessLogic.createListInput(userId, componentId, componentTypeEnum, componentInstInputsMap, true, false);
            if (inputPropertiesRes.isRight()) {
                log.debug("failed to create inputs  for service: {}", componentId);
                return buildErrorResponse(inputPropertiesRes.right().value());
            }
            Object properties = RepresentationUtils.toRepresentation(inputPropertiesRes.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), properties);

        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create inputs for service with id: " + componentId);
            log.debug("createMultipleInputs failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }


    @DELETE
    @Path("/{componentType}/{componentId}/delete/{inputId}/input")
    @ApiOperation(value = "Delete input from service", httpMethod = "DELETE", notes = "Delete service input", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Input deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Input not found") })
    public Response deleteInput (
            @PathParam("componentType") final String componentType,
            @PathParam("componentId") final String componentId,
            @PathParam("inputId") final String inputId,
            @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @ApiParam(value = "Service Input to be deleted", required = true) String componentInstInputsMapObj) {

        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response = null;

        try {
            InputsBusinessLogic businessLogic = getInputBL(context);
            Either<InputDefinition, ResponseFormat> deleteInput = businessLogic.deleteInput(componentId, userId, inputId);
            if (deleteInput.isRight()){
                ResponseFormat deleteResponseFormat = deleteInput.right().value();
                response = buildErrorResponse(deleteResponseFormat);
                return response;
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), deleteInput.left().value());
        } catch (Exception e){
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete input for service + " + componentId + " + with id: " + inputId);
            log.debug("Delete input failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    /**
     * Gets a specific data type associated with a component.
     * @param componentType the container type (service, resource, ...)
     * @param componentId the container ID
     * @param dataTypeName the data type name
     * @param request HttpServletRequest object
     * @return the data type info
     */
    @GET
    @Path("/{componentType}/{componentId}/dataType/{dataTypeName}")
    @ApiOperation(value = "Get data type in service", httpMethod = "GET", notes = "Get data type in service",
            response = DataTypeDefinition.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data type found"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Data type not found")})
    public Response getDataType(
            @PathParam("componentType") final String componentType,
            @PathParam("componentId") final String componentId,
            @PathParam("dataTypeName") final String dataTypeName,
            @Context final HttpServletRequest request
    ) {
        ServletContext context = request.getSession().getServletContext();
        ComponentsUtils componentsUtils = getComponentsUtils();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getDataType) Start handle request of {}", url);
        Response response;

        try {
            DataTypeBusinessLogic businessLogic = getDataTypeBL(context);
            Either<DataTypeDefinition, StorageOperationStatus> getResult = businessLogic.getPrivateDataType(componentId, dataTypeName);
            if (getResult.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(getResult.right().value());
                return buildErrorResponse(componentsUtils.getResponseFormat(actionStatus));
            }
            Object json = RepresentationUtils.toRepresentation(getResult.left().value());
            return buildOkResponse(componentsUtils.getResponseFormat(ActionStatus.OK), json);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get data type from service + " + componentId + " + with name: " + dataTypeName);
            log.debug("Get data type failed with exception", e);
            response = buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    /**
     * Gets a list of data types which a component has.
     * @param componentType the container type (service, resource, ...)
     * @param componentId the container ID
     * @param request HttpServletRequest object
     * @return the list of data types in the component
     */
    @GET
    @Path("/{componentType}/{componentId}/dataTypes")
    @ApiOperation(value = "Get data types that service has", httpMethod = "GET", notes = "Get data types in service",
            response = Resource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data type found"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Component not found")})
    public Response getDataTypes(
            @PathParam("componentType") final String componentType,
            @PathParam("componentId") final String componentId,
            @Context final HttpServletRequest request
    ) {
        ServletContext context = request.getSession().getServletContext();
        ComponentsUtils componentsUtils = getComponentsUtils();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(getDataType) Start handle request of {}", url);
        Response response;

        try {
            DataTypeBusinessLogic businessLogic = getDataTypeBL(context);
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
     * @param componentType the container type (service, resource, ...)
     * @param componentId the container ID
     * @param dataTypeName the data type name to be deleted
     * @param request HttpServletRequest object
     * @return operation result
     */
    @DELETE
    @Path("/{componentType}/{componentId}/dataType/{dataTypeName}")
    @ApiOperation(value = "Delete data type from service", httpMethod = "DELETE", notes = "Delete service input",
            response = Resource.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data type deleted"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 404, message = "Data type not found")})
    public Response deleteDataType(
            @PathParam("componentType") final String componentType,
            @PathParam("componentId") final String componentId,
            @PathParam("dataTypeName") final String dataTypeName,
            @Context final HttpServletRequest request
    ) {
        ServletContext context = request.getSession().getServletContext();
        ComponentsUtils componentsUtils = getComponentsUtils();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        Response response;

        try {
            DataTypeBusinessLogic businessLogic = getDataTypeBL(context);
            Either<DataTypeDefinition, StorageOperationStatus> deleteResult = businessLogic.deletePrivateDataType(componentId, dataTypeName);
            if (deleteResult.isRight()) {
                ActionStatus actionStatus = componentsUtils.convertFromStorageResponse(deleteResult.right().value());
                return buildErrorResponse(componentsUtils.getResponseFormat(actionStatus));
            }
            Object json = RepresentationUtils.toRepresentation(deleteResult.left().value());
            return buildOkResponse(componentsUtils.getResponseFormat(ActionStatus.OK), json);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete data type for service + " + componentId + " + with name: " + dataTypeName);
            log.debug("Delete data type failed with exception", e);
            response = buildErrorResponse(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }

    private DataTypeBusinessLogic getDataTypeBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(DataTypeBusinessLogic.class);
    }
}

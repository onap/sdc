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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.collections4.ListUtils;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.RelationshipTypeBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Types Fetch Servlet", description = "Types Fetch Servlet")
@Singleton
public class TypesFetchServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(TypesFetchServlet.class);
    private static final String FAILED_TO_GET_ALL_NON_ABSTRACT = "failed to get all non abstract {}";

    @GET
    @Path("dataTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get data types", httpMethod = "GET", notes = "Returns data types", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "datatypes"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Data types not found") })
    public Response getAllDataTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();

        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);

            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);

                PropertyBusinessLogic businessLogic = getPropertyBL(context);
                Either<Map<String, DataTypeDefinition>, ResponseFormat> allDataTypes = businessLogic.getAllDataTypes();

                if (allDataTypes.isRight()) {
                    log.info("Failed to get all dara types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);

                } else {

                    Map<String, DataTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
                    responseWrapper.setInnerElement(okResponse);

                }
            }

            return responseWrapper.getInnerElement();
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property");
            log.debug("get all data types failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("interfaceLifecycleTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get interface lifecycle types", httpMethod = "GET", notes = "Returns interface lifecycle types", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Interface lifecycle types"),
        @ApiResponse(code = 403, message = "Restricted operation"),
        @ApiResponse(code = 400, message = "Invalid content / Missing content"),
        @ApiResponse(code = 404, message = "Interface lifecycle types not found")
    })
    public Response getInterfaceLifecycleTypes(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();

        try {
            validateUserExist(responseWrapper, userWrapper, userId);

            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.info("Start handle request of {} | modifier id is {}", url, userId);

                InterfaceOperationBusinessLogic businessLogic = getInterfaceOperationBL(context);
                Either<Map<String, InterfaceDefinition>, ResponseFormat> allInterfaceLifecycleTypes =
                    businessLogic.getAllInterfaceLifecycleTypes();

                if (allInterfaceLifecycleTypes.isRight()) {
                    log.info("Failed to get all interface lifecycle types. Reason - {}",
                        allInterfaceLifecycleTypes.right().value());
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
    @ApiOperation(value = "Get capability types", httpMethod = "GET", notes = "Returns capability types", response =
            Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "capabilityTypes"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Capability types not found")})
    public Response getAllCapabilityTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value =
            Constants.USER_ID_HEADER) String userId) {

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();

        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);

            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);

                CapabilitiesBusinessLogic businessLogic = getCapabilitiesBL(context);
                Either<Map<String, CapabilityTypeDefinition>, ResponseFormat> allDataTypes =
                        businessLogic.getAllCapabilityTypes();

                if (allDataTypes.isRight()) {
                    log.info("Failed to get all capability types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);

                } else {

                    Map<String, CapabilityTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse =
                            buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
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
    @ApiOperation(value = "Get relationship types", httpMethod = "GET", notes = "Returns relationship types", response =
            Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "relationshipTypes"),
            @ApiResponse(code = 403, message = "Restricted operation"),
            @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Relationship types not found")})
    public Response getAllRelationshipTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value =
            Constants.USER_ID_HEADER) String userId) {

        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        ServletContext context = request.getSession().getServletContext();

        try {
            init();
            validateUserExist(responseWrapper, userWrapper, userId);

            if (responseWrapper.isEmpty()) {
                String url = request.getMethod() + " " + request.getRequestURI();
                log.debug("Start handle request of {} | modifier id is {}", url, userId);

                RelationshipTypeBusinessLogic businessLogic = getRelationshipTypeBL(context);
                Either<Map<String, RelationshipTypeDefinition>, ResponseFormat> allDataTypes =
                        businessLogic.getAllRelationshipTypes();

                if (allDataTypes.isRight()) {
                    log.info("Failed to get all relationship types. Reason - {}", allDataTypes.right().value());
                    Response errorResponse = buildErrorResponse(allDataTypes.right().value());
                    responseWrapper.setInnerElement(errorResponse);

                } else {

                    Map<String, RelationshipTypeDefinition> dataTypes = allDataTypes.left().value();
                    String dataTypeJson = gson.toJson(dataTypes);
                    Response okResponse =
                            buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), dataTypeJson);
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
    @ApiOperation(value = "Get node types", httpMethod = "GET", notes = "Returns node types", response = Response.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "nodeTypes"), @ApiResponse(code = 403, message =
            "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 404, message = "Node types not found")})
    public Response getAllNodeTypesServlet(@Context final HttpServletRequest request, @HeaderParam(value =
            Constants.USER_ID_HEADER) String userId) {

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

                ComponentBusinessLogic resourceBL = getComponentBL(ComponentTypeEnum.RESOURCE, context);
                response = getComponent(resourceBL, true, userId);
                if (response.isRight()) {
                    return response.right().value();
                }
                componentMap = new HashMap<>(response.left().value());

                response = getComponent(resourceBL, false, userId);
                if (response.isRight()) {
                    return response.right().value();
                }
                componentMap.putAll(response.left().value());

                String nodeTypesJson = gson.toJson(componentMap);
                Response okResponse =
                        buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), nodeTypesJson);
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

    private Either<Map<String, Component>, Response> getComponent(ComponentBusinessLogic resourceBL, boolean isAbstract,
                                                                  String userId) {
        Either<List<Component>, ResponseFormat> actionResponse;
        List<Component> componentList;

        actionResponse =
                resourceBL.getLatestVersionNotAbstractComponentsMetadata(isAbstract, HighestFilterEnum.HIGHEST_ONLY
                        , ComponentTypeEnum.RESOURCE, null, userId);
        if (actionResponse.isRight()) {
            log.debug(FAILED_TO_GET_ALL_NON_ABSTRACT, ComponentTypeEnum.RESOURCE.getValue());
            return Either.right(buildErrorResponse(actionResponse.right().value()));
        }

        componentList = actionResponse.left().value();

        return Either.left(ListUtils.emptyIfNull(componentList).stream()
                .filter(component -> ((ResourceMetadataDataDefinition) component
                        .getComponentMetadataDefinition().getMetadataDataDefinition()).getToscaResourceName() != null)
                .collect(Collectors.toMap(
                        component -> ((ResourceMetadataDataDefinition) component
                                .getComponentMetadataDefinition().getMetadataDataDefinition()).getToscaResourceName(),
                        component -> component, (component1, component2) -> component1)));
    }
}

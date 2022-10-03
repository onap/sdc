/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.servlets;

import static org.openecomp.sdc.be.tosca.InterfacesOperationsConverter.SELF;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.OperationInput;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.be.types.ServiceConsumptionSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Singleton
public class ServiceConsumptionServlet extends BeGenericServlet {

    private static final Logger log = LoggerFactory.getLogger(ServiceConsumptionServlet.class);
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;

    @Inject
    public ServiceConsumptionServlet(ComponentsUtils componentsUtils,
                                     InterfaceOperationBusinessLogic interfaceOperationBusinessLogic, ServiceBusinessLogic serviceBusinessLogic) {
        super(componentsUtils);
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    @POST
    @Path("/services/{serviceId}/consumption/{serviceInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @io.swagger.v3.oas.annotations.Operation(description = "Service consumption on operation", method = "POST", summary = "Returns consumption data", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Service property created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service property already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response addInputToServiceOperation(@PathParam("serviceId") final String serviceId,
                                               @PathParam("serviceInstanceId") final String serviceInstanceId,
                                               @Parameter(description = "Service Consumption Data", required = true) String data,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);
        User modifier = new User();
        modifier.setUserId(userId);
        try {
            Either<Map<String, List<ServiceConsumptionData>>, ResponseFormat> dataFromJson = getServiceConsumptionData(data, modifier);
            if (dataFromJson.isRight()) {
                return buildErrorResponse(dataFromJson.right().value());
            }
            Map<String, List<ServiceConsumptionData>> serviceConsumptionDataMap = dataFromJson.left().value();
            for (Entry<String, List<ServiceConsumptionData>> consumptionEntry : serviceConsumptionDataMap.entrySet()) {
                List<ServiceConsumptionData> consumptionList = consumptionEntry.getValue();
                Either<List<Operation>, ResponseFormat> operationEither = serviceBusinessLogic
                    .addServiceConsumptionData(serviceId, serviceInstanceId, consumptionEntry.getKey(), consumptionList, userId);
                if (operationEither.isRight()) {
                    return buildErrorResponse(operationEither.right().value());
                }
            }
            return buildOkResponse(serviceConsumptionDataMap);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Operation Inputs");
            log.debug("Create Operation Inputs failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    @GET
    @Path("/services/{serviceId}/consumption/{serviceInstanceId}/interfaces/{interfaceId}/operations/{operationId}/inputs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getInputsListOfOperation(@PathParam("serviceId") final String serviceId,
                                             @PathParam("serviceInstanceId") final String serviceInstanceId,
                                             @PathParam("interfaceId") final String interfaceId, @PathParam("operationId") final String operationId,
                                             @Context final HttpServletRequest request,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {}", url, userId);
        User user = new User();
        user.setUserId(userId);
        try {
            Either<List<OperationInputDefinition>, ResponseFormat> inputsEither = interfaceOperationBusinessLogic
                .getInputsListForOperation(serviceId, serviceInstanceId, interfaceId, operationId, user);
            if (inputsEither.isRight()) {
                return buildErrorResponse(inputsEither.right().value());
            }
            List<OperationInputDefinition> inputs = inputsEither.left().value();
            return buildOkResponse(updateOperationInputListForUi(inputs, interfaceOperationBusinessLogic));
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Operation Inputs");
            log.debug("Get Operation Inputs failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private List<OperationInput> updateOperationInputListForUi(List<OperationInputDefinition> inputsList,
                                                               InterfaceOperationBusinessLogic interfaceOperationBL) {
        List<OperationInput> operationInputs = new ArrayList<>();
        for (OperationInputDefinition input : inputsList) {
            String value = input.getValue();
            // Additional UI mapping needed for other sources
            if (StringUtils.isNotBlank(value) && !ServiceConsumptionSource.STATIC.getSource().equals(input.getSource())) {
                uiMappingForOtherSources(value, input);
            }
            // Add Constraint for UI
            OperationInput operationInput = new OperationInput(input);
            operationInput.setConstraints(interfaceOperationBL.setInputConstraint(input));
            operationInputs.add(operationInput);
        }
        return operationInputs;
    }

    private void uiMappingForOtherSources(String value, OperationInputDefinition input) {
        try {
            Map<String, Object> valueAsMap = (new Gson()).fromJson(value, Map.class);
            String toscaFunction = valueAsMap.keySet().iterator().next();
            Object consumptionValueName = valueAsMap.values().iterator().next();
            if (consumptionValueName instanceof List) {
                List<Object> toscaFunctionList = (List<Object>) consumptionValueName;
                String consumptionInputValue = null;
                if (ToscaFunctions.GET_PROPERTY.getFunctionName().equals(toscaFunction)) {
                    String propertyValue = toscaFunctionList.stream().map(Object::toString).filter(val -> !val.equals(SELF))
                        .collect(Collectors.joining("_"));
                    consumptionInputValue = String.valueOf(propertyValue);
                } else if (ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName().equals(toscaFunction)) {
                    //Return full output name
                    consumptionInputValue = toscaFunctionList.get(1) + "." + toscaFunctionList.get(2) + "." + toscaFunctionList.get(3);
                }
                input.setValue(consumptionInputValue);
            } else {
                input.setValue(String.valueOf(consumptionValueName));
            }
        } catch (JsonParseException ex) {
            log.info("This means it is static value for which no changes are needed");
        }
    }

    private Either<Map<String, List<ServiceConsumptionData>>, ResponseFormat> getServiceConsumptionData(String data, User user) {
        JSONParser parser = new JSONParser();
        Map<String, List<ServiceConsumptionData>> serviceConsumptionDataMap = new HashMap<>();
        try {
            JSONArray operationsArray = (JSONArray) parser.parse(data);
            Iterator iterator = operationsArray.iterator();
            while (iterator.hasNext()) {
                Map next = (Map) iterator.next();
                Entry consumptionEntry = (Entry) next.entrySet().iterator().next();
                String operationId = (String) consumptionEntry.getKey();
                Object value = consumptionEntry.getValue();
                JSONArray inputsArray = (JSONArray) parser.parse(value.toString());
                serviceConsumptionDataMap.putIfAbsent(operationId, new ArrayList<>());
                for (Object consumptionObject : inputsArray) {
                    Either<ServiceConsumptionData, ResponseFormat> serviceDataEither = getComponentsUtils()
                        .convertJsonToObjectUsingObjectMapper(consumptionObject.toString(), user, ServiceConsumptionData.class,
                            AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
                    if (serviceDataEither.isRight()) {
                        return Either.right(serviceDataEither.right().value());
                    }
                    serviceConsumptionDataMap.get(operationId).add(serviceDataEither.left().value());
                }
            }
        } catch (ParseException e) {
            log.info("Conetnt is invalid - {}", data);
            return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        return Either.left(serviceConsumptionDataMap);
    }
}

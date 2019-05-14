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

import static org.openecomp.sdc.be.tosca.utils.InterfacesOperationsToscaUtil.SELF;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.jcabi.aspects.Loggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.OperationInput;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.be.types.ServiceConsumptionSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
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

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Service Consumption Servlet", description = "Service Consumption Servlet")
@Singleton
public class ServiceConsumptionServlet extends BeGenericServlet {

  private static final Logger log = LoggerFactory.getLogger(ServiceConsumptionServlet.class);

  @POST
  @Path("/services/{serviceId}/consumption/{serviceInstanceId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Service consumption on operation", httpMethod = "POST",
      notes = "Returns consumption data", response = Response.class)
      @ApiResponses(value = { @ApiResponse(code = 201, message = "Service property created"),
      @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
      @ApiResponse(code = 409, message = "Service property already exist") })
  public Response addInputToServiceOperation(@PathParam("serviceId")final String serviceId,
                                             @PathParam("serviceInstanceId")final String serviceInstanceId,
                                             @ApiParam(value = "Service Consumption Data", required = true) String data,
                                             @Context final HttpServletRequest request,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);
    User modifier = new User();
    modifier.setUserId(userId);

    try {

      Either<Map<String, List<ServiceConsumptionData>>, ResponseFormat> dataFromJson =
          getServiceConsumptionData(data, modifier);
      if(dataFromJson.isRight()) {
        return buildErrorResponse(dataFromJson.right().value());
      }

      Map<String, List<ServiceConsumptionData>> serviceConsumptionDataMap = dataFromJson.left().value();
      ServiceBusinessLogic serviceBL = getServiceBL(context);

      for(Entry<String, List<ServiceConsumptionData>> consumptionEntry : serviceConsumptionDataMap.entrySet()) {
        List<ServiceConsumptionData> consumptionList = consumptionEntry.getValue();
        Either<List<Operation>, ResponseFormat> operationEither =
            serviceBL.addServiceConsumptionData(serviceId, serviceInstanceId,
                consumptionEntry.getKey(), consumptionList, userId);
        if (operationEither.isRight()) {
          return buildErrorResponse(operationEither.right().value());
        }
      }

      return buildOkResponse(serviceConsumptionDataMap);

    }
    catch (Exception e) {
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
  public Response getInputsListOfOperation(@PathParam("serviceId")final String serviceId,
                                           @PathParam("serviceInstanceId")final String serviceInstanceId,
                                           @PathParam("interfaceId")final String interfaceId,
                                           @PathParam("operationId")final String operationId,
                                           @Context final HttpServletRequest request,
                                           @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug("Start handle request of {} modifier id is {}", url, userId);
    User user = new User();
    user.setUserId(userId);

    try {
      InterfaceOperationBusinessLogic interfaceOperationBL = getInterfaceOperationBL(context);
      Either<List<OperationInputDefinition>, ResponseFormat> inputsEither =
          interfaceOperationBL.getInputsListForOperation(serviceId, serviceInstanceId, interfaceId, operationId, user);

      if(inputsEither.isRight()) {
        return buildErrorResponse(inputsEither.right().value());
      }

      List<OperationInputDefinition> inputs = inputsEither.left().value();
		return buildOkResponse(updateOperationInputListForUi(inputs, interfaceOperationBL));
    }
    catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Operation Inputs");
      log.debug("Get Operation Inputs failed with exception", e);
      ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
      return buildErrorResponse(responseFormat);
    }
  }

	private List<OperationInput> updateOperationInputListForUi(List<OperationInputDefinition> inputsList,
															   InterfaceOperationBusinessLogic interfaceOperationBL) {
		List<OperationInput> operationInputs = new ArrayList<>();
		for(OperationInputDefinition input : inputsList) {

			String value = input.getValue();

			// Additional UI mapping needed for other sources
			if (StringUtils.isNotBlank(value)
					&& !ServiceConsumptionSource.STATIC.getSource().equals(input.getSource())) {
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
			if(consumptionValueName instanceof List) {
				List<Object> toscaFunctionList = (List<Object>) consumptionValueName;
				String consumptionInputValue = null;
				if (ToscaFunctions.GET_PROPERTY.getFunctionName().equals(toscaFunction)) {
					String propertyValue = toscaFunctionList.stream()
							.map(Object::toString)
							.filter(val -> !val.equals(SELF))
							.collect(Collectors.joining("_"));
					consumptionInputValue = String.valueOf(propertyValue);
				} else if (ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName().equals(toscaFunction)) {
					//Return full output name
					consumptionInputValue =
							toscaFunctionList.get(1) + "." + toscaFunctionList.get(2) + "." +toscaFunctionList.get(3);
				}
				input.setValue(consumptionInputValue);
			} else {
				input.setValue(String.valueOf(consumptionValueName));
			}
		}
		catch(JsonParseException ex){
			log.info("This means it is static value for which no changes are needed");
		}
	}

  private Either<Map<String, List<ServiceConsumptionData>>, ResponseFormat> getServiceConsumptionData(String data,
                                                                                                      User user) {
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
        for(Object consumptionObject : inputsArray) {
          Either<ServiceConsumptionData, ResponseFormat> serviceDataEither =
              getComponentsUtils()
                  .convertJsonToObjectUsingObjectMapper(consumptionObject.toString(), user, ServiceConsumptionData
                      .class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
          if(serviceDataEither.isRight()) {
            return Either.right(serviceDataEither.right().value());
          }

          serviceConsumptionDataMap.get(operationId).add(serviceDataEither.left().value());
        }
      }
    }
    catch (ParseException e) {
      log.info("Conetnt is invalid - {}", data);
      return Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
    }
    return Either.left(serviceConsumptionDataMap);
  }
}

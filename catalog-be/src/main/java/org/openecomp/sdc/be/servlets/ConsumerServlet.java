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

import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.jcabi.aspects.Loggable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/consumers")
@Api(value = "Consumer Servlet", description = "Consumer Servlet")
@Singleton
public class ConsumerServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(ConsumerServlet.class.getName());

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Consumer credentials", httpMethod = "POST", notes = "Returns created ONAP consumer credentials", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Consumer credentials created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response createConsumer(@ApiParam(value = "Consumer Object to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		try {
			ConsumerBusinessLogic businessLogic = getConsumerBL(context);

			Either<ConsumerDefinition, ResponseFormat> convertionResponse = convertJsonToObject(data, modifier, AuditingActionEnum.ADD_ECOMP_USER_CREDENTIALS);

			if (convertionResponse.isRight()) {
				log.debug("failed to create Consumer");
				return buildErrorResponse(convertionResponse.right().value());
			}

			ConsumerDefinition consumer = convertionResponse.left().value();

			Either<ConsumerDefinition, ResponseFormat> actionResult = businessLogic.createConsumer(modifier, consumer);

			if (actionResult.isRight()) {
				log.debug("failed to create Consumer");
				return buildErrorResponse(actionResult.right().value());
			}

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResult.left().value());

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Create consumer");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create consumer");
			log.debug("create consumer failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	@GET
	@Path("/{consumerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve Consumer", httpMethod = "GET", notes = "Returns consumer according to ConsumerID", response = ConsumerDefinition.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Consumer found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Consumer not found") })
	public Response getConsumer(@PathParam("consumerId") final String consumerId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ConsumerBusinessLogic businessLogic = getConsumerBL(context);

			Either<ConsumerDefinition, ResponseFormat> actionResponse = businessLogic.getConsumer(consumerId, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to get consumer");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Consumer");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Consumer");
			log.debug("get consumer failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

		}
	}

	@DELETE
	@Path("/{consumerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Deletes Consumer", httpMethod = "DELETE", notes = "Returns deleted consumer according to ConsumerID", response = ConsumerDefinition.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Consumer deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Consumer not found") })
	public Response deleteConsumer(@PathParam("consumerId") final String consumerId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;
		try {
			ConsumerBusinessLogic businessLogic = getConsumerBL(context);

			Either<ConsumerDefinition, ResponseFormat> actionResponse = businessLogic.deleteConsumer(consumerId, modifier);

			if (actionResponse.isRight()) {
				log.debug("failed to delete consumer");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Consumer");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Consumer");
			log.debug("delete consumer failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

		}
	}

	private ConsumerBusinessLogic getConsumerBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		ConsumerBusinessLogic consumerBL = webApplicationContext.getBean(ConsumerBusinessLogic.class);

		return consumerBL;
	}

	public Either<ConsumerDefinition, ResponseFormat> convertJsonToObject(String data, User user, AuditingActionEnum actionEnum) {
		ConsumerDefinition consumer = null;
		Gson gson = new Gson();
		try {
			log.trace("convert json to object. json=\n {}", data);
			consumer = gson.fromJson(data, ConsumerDefinition.class);
			if (consumer == null) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
				BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
				log.debug("object is null after converting from json");
				ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(user, actionEnum);
				return Either.right(responseFormat);
			}
		} catch (Exception e) {
			// INVALID JSON
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
			log.debug("failed to convert from json {}", data, e);
			ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(user, actionEnum);
			return Either.right(responseFormat);
		}
		return Either.left(consumer);
	}

}

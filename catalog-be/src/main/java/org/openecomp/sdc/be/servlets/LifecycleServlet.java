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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoBase;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.UiComponentDataConverter;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.ui.model.UiComponentMetadata;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Lifecycle Actions Servlet", description = "Lifecycle Actions Servlet")
@Singleton
public class LifecycleServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(LifecycleServlet.class.getName());

	@POST
	@Path("/{componentCollection}/{componentId}/lifecycleState/{lifecycleOperation}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Change Resource lifecycle State", httpMethod = "POST", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Resource state changed"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 409, message = "Resource already exist") })
	public Response changeResourceState(@ApiParam(value = "LifecycleChangeInfo - relevant for checkin, failCertification, cancelCertification", required = false) String jsonChangeInfo,
			@ApiParam(value = "validValues: resources / services / products", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME + ","
					+ ComponentTypeEnum.PRODUCT_PARAM_NAME) @PathParam(value = "componentCollection") final String componentCollection,
			@ApiParam(allowableValues = "checkout, undoCheckout, checkin, certificationRequest, startCertification, failCertification,  cancelCertification, certify", required = true) @PathParam(value = "lifecycleOperation") final String lifecycleTransition,
			@ApiParam(value = "id of component to be changed") @PathParam(value = "componentId") final String componentId, @Context final HttpServletRequest request,
			@ApiParam(value = "id of user initiating the operation") @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		LifecycleBusinessLogic businessLogic = getLifecycleBL(context);

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		Response response = null;

		// get modifier from graph
		log.debug("get modifier properties");
		Either<User, ResponseFormat> eitherGetUser = getUser(request, userId);
		if (eitherGetUser.isRight()) {
			return buildErrorResponse(eitherGetUser.right().value());
		}
		User user = eitherGetUser.left().value();

		String resourceIdLower = componentId.toLowerCase();
		log.debug("perform {} operation to resource with id {} ", lifecycleTransition, resourceIdLower);
		Either<LifeCycleTransitionEnum, Response> validateEnum = validateTransitionEnum(lifecycleTransition, user);
		if (validateEnum.isRight()) {
			return validateEnum.right().value();
		}

		LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction();

		try {
			if (jsonChangeInfo != null && !jsonChangeInfo.isEmpty()) {
				// Either<LifecycleChangeInfo, ResponseFormat > changeInfoResult
				// =
				// getComponentsUtils().convertJsonToObjectUsingObjectMapper(jsonChangeInfo,
				// user, LifecycleChangeInfo.class,
				// AuditingActionEnum.CHECKOUT_RESOURCE, null);
				ObjectMapper mapper = new ObjectMapper();
				changeInfo = new LifecycleChangeInfoWithAction(mapper.readValue(jsonChangeInfo, LifecycleChangeInfoBase.class).getUserRemarks());
			}
		}

		catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
			BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
			log.debug("failed to convert from json {}", jsonChangeInfo, e);
			ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(user, AuditingActionEnum.CHECKOUT_RESOURCE);
			return buildErrorResponse(responseFormat);
		}

		try {
			LifeCycleTransitionEnum transitionEnum = validateEnum.left().value();
			ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(componentCollection);
			if (componentType != null) {
				Either<? extends Component, ResponseFormat> actionResponse = businessLogic.changeComponentState(componentType, componentId, user, transitionEnum, changeInfo, false, true);

				if (actionResponse.isRight()) {
					log.info("failed to change resource state");
					response = buildErrorResponse(actionResponse.right().value());
					return response;
				}

				log.debug("change state successful !!!");
				UiComponentMetadata componentMetatdata = UiComponentDataConverter.convertToUiComponentMetadata(actionResponse.left().value());
				Object value = RepresentationUtils.toRepresentation(componentMetatdata);
				response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), value);
				return response;
			} else {
				log.info("componentCollection \"{}\" is not valid. Supported componentCollection values are \"{}\", \"{}\" or \"{}\"", componentCollection, ComponentTypeEnum.RESOURCE_PARAM_NAME, ComponentTypeEnum.SERVICE_PARAM_NAME,
						ComponentTypeEnum.PRODUCT_PARAM_NAME);
				ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, AuditingActionEnum.CHECKOUT_RESOURCE);
				return buildErrorResponse(error);
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Change Lifecycle State");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Change Lifecycle State");
			log.debug("change lifecycle state failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	private Either<LifeCycleTransitionEnum, Response> validateTransitionEnum(final String lifecycleTransition, User user) {
		LifeCycleTransitionEnum transitionEnum = LifeCycleTransitionEnum.CHECKOUT;
		try {
			transitionEnum = LifeCycleTransitionEnum.getFromDisplayName(lifecycleTransition);
		} catch (IllegalArgumentException e) {
			log.info("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString());
			ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, AuditingActionEnum.CHECKOUT_RESOURCE);
			return Either.right(buildErrorResponse(error));
		}
		return Either.left(transitionEnum);
	}

//	private LifecycleBusinessLogic getLifecycleBL(ServletContext context) {
//		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
//		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
//		LifecycleBusinessLogic resourceBl = webApplicationContext.getBean(LifecycleBusinessLogic.class);
//		return resourceBl;
//	}
//
//	protected Either<User, Response> getUser(final HttpServletRequest request, String userId) {
//
//		Either<User, ActionStatus> eitherCreator = getUserAdminManager(request.getSession().getServletContext()).getUser(userId, false);
//		if (eitherCreator.isRight()) {
//			log.info("createResource method - user is not listed. userId= {}", userId);
//			ResponseFormat errorResponse = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_INFORMATION);
//			User user = new User("", "", userId, "", null, null);
//
//			getComponentsUtils().auditResource(errorResponse, user, null, "", "", AuditingActionEnum.CHECKOUT_RESOURCE, null);
//			return Either.right(buildErrorResponse(errorResponse));
//		}
//		return Either.left(eitherCreator.left().value());
//
//	}
}

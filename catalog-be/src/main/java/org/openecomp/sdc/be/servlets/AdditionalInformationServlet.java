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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.AdditionalInformationBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Additional Information Servlet", description = "Additional Information Servlet")
@Singleton
public class AdditionalInformationServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(AdditionalInformationServlet.class.getName());
	
	/**
	 * 
	 * @param resourceId
	 * @param data
	 * @param request
	 * @param userUserId
	 * @return
	 */
	@POST
	@Path("/resources/{resourceId}/additionalinfo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Additional Information Label and Value", httpMethod = "POST", notes = "Returns created Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Additional information created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response createResourceAdditionalInformationLabel(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "Additional information key value to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userUserId) {

		return createAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, request, userUserId, data);

	}
	
	/**
	 * 
	 * @param serviceId
	 * @param data
	 * @param request
	 * @param userUserId
	 * @return
	 */
	@POST
	@Path("/services/{serviceId}/additionalinfo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Additional Information Label and Value", httpMethod = "POST", notes = "Returns created Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Additional information created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response createServiceAdditionalInformationLabel(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
			@ApiParam(value = "Additional information key value to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userUserId) {

		return createAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, request, userUserId, data);

	}
	
	/**
	 * 
	 * @param resourceId
	 * @param labelId
	 * @param data
	 * @param request
	 * @param userId
	 * @return
	 */
	@PUT
	@Path("/resources/{resourceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Additional Information Label and Value", httpMethod = "PUT", notes = "Returns updated Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional information updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response updateResourceAdditionalInformationLabel(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @ApiParam(value = "Additional information key value to be created", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return updateAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId, data);

	}
	
	/**
	 * 
	 * @param serviceId
	 * @param labelId
	 * @param data
	 * @param request
	 * @param userId
	 * @return
	 */
	@PUT
	@Path("/services/{serviceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Additional Information Label and Value", httpMethod = "PUT", notes = "Returns updated Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional information updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response updateServiceAdditionalInformationLabel(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @ApiParam(value = "Additional information key value to be created", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return updateAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId, data);

	}
	
	/**
	 * 
	 * @param resourceId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	@DELETE
	@Path("/resources/{resourceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Additional Information Label and Value", httpMethod = "DELETE", notes = "Returns deleted Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional information deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response updateResourceAdditionalInformationLabel(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return deleteAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId);

	}
	
	/**
	 * 
	 * @param serviceId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	@DELETE
	@Path("/services/{serviceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Additional Information Label and Value", httpMethod = "DELETE", notes = "Returns deleted Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Additional information deleted"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response deleteServiceAdditionalInformationLabel(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return deleteAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId);

	}
	
	/**
	 * 
	 * @param resourceId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	@GET
	@Path("/resources/{resourceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Additional Information by id", httpMethod = "GET", notes = "Returns Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "fetched additional information"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response getResourceAdditionalInformationLabel(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return getAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId);

	}
	
	/**
	 * 
	 * @param serviceId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	@GET
	@Path("/services/{serviceId}/additionalinfo/{labelId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get Additional Information by id", httpMethod = "GET", notes = "Returns Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "fetched additional information"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response getServiceAdditionalInformationLabel(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
			@ApiParam(value = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return getAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId);

	}
	
	/**
	 * 
	 * @param resourceId
	 * @param request
	 * @param userId
	 * @return
	 */
	@GET
	@Path("/resources/{resourceId}/additionalinfo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all Additional Information under resource", httpMethod = "GET", notes = "Returns Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "list of additional information"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response getAllResourceAdditionalInformationLabel(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return getAllAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, request, userId);

	}
	
	/**
	 * 
	 * @param serviceId
	 * @param request
	 * @param userId
	 * @return
	 */
	@GET
	@Path("/services/{serviceId}/additionalinfo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get all Additional Information under service", httpMethod = "GET", notes = "Returns Additional Inforamtion property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "list of additional information"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Additional information key already exist") })
	public Response getAllServiceAdditionalInformationLabel(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		return getAllAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, request, userId);

	}

	/**
	 * 
	 * Create additional information property under given resource/service
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @param request
	 * @param userId
	 * @param data
	 * @return
	 */
	protected Response createAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, HttpServletRequest request, String userId, String data) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);
		log.debug("data is {}", data);

		try {
			// convert json to AdditionalInfoParameterInfo
			AdditionalInfoParameterInfo additionalInfoParameterInfo = gson.fromJson(data, AdditionalInfoParameterInfo.class);

			// create the new property
			AdditionalInformationBusinessLogic businessLogic = getBL(context);

			Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic.createAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, null, userId);

			if (either.isRight()) {
				ResponseFormat responseFormat = either.right().value();
				log.info("Failed to create additional information {}. Reason - {}", additionalInfoParameterInfo, responseFormat);
				return buildErrorResponse(responseFormat);
			}

			AdditionalInfoParameterInfo createdAI = either.left().value();

			log.debug("Additional information {}={} created successfully with id {}", createdAI.getKey(), createdAI.getValue(), createdAI.getUniqueId());

			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
			return buildOkResponse(responseFormat, createdAI);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Create Additional Information");
			log.debug("Create additional information failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);
		}

	}

	/**
	 * Update additional information property by id under given resource/service
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @param data
	 * @return
	 */
	protected Response updateAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId, HttpServletRequest request, String userId, String data) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);
		log.debug("data is {}", data);

		try {
			// convert json to AdditionalInfoParameterInfo
			AdditionalInfoParameterInfo additionalInfoParameterInfo = gson.fromJson(data, AdditionalInfoParameterInfo.class);

			// create the new property
			AdditionalInformationBusinessLogic businessLogic = getBL(context);

			additionalInfoParameterInfo.setUniqueId(labelId);

			Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic.updateAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, null, userId);

			if (either.isRight()) {
				ResponseFormat responseFormat = either.right().value();
				log.info("Failed to update additional information property. Reason - {}", responseFormat);
				return buildErrorResponse(responseFormat);
			}

			AdditionalInfoParameterInfo createdAI = either.left().value();

			log.debug("Additional information {}={} updated successfully with id {}", createdAI.getKey(), createdAI.getValue(), createdAI.getUniqueId());

			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, createdAI);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Update Additional Information");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Additional Information");
			log.debug("Update additional information failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);
		}

	}

	/**
	 * 
	 * Delete an additional information property by id under given resource/service
	 *
	 * @param nodeType
	 * @param uniqueId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	protected Response deleteAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId, HttpServletRequest request, String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);

		try {

			AdditionalInformationBusinessLogic businessLogic = getBL(context);

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
			additionalInfoParameterInfo.setUniqueId(labelId);

			Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic.deleteAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, null, userId);

			if (either.isRight()) {
				ResponseFormat responseFormat = either.right().value();
				log.info("Failed to update additional information property. Reason - {}", responseFormat);
				return buildErrorResponse(responseFormat);
			}

			AdditionalInfoParameterInfo createdAI = either.left().value();

			log.debug("Additional information {}={} deleted successfully with id {}", createdAI.getKey(), createdAI.getValue(), createdAI.getUniqueId());

			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, createdAI);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Delete Additional Information");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Additional Information");
			log.debug("Delete additional information failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);
		}

	}

	/**
	 * Get a specific additional information property by a given id under given resource/service
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @param labelId
	 * @param request
	 * @param userId
	 * @return
	 */
	protected Response getAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId, HttpServletRequest request, String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);

		try {

			// create the new property
			AdditionalInformationBusinessLogic businessLogic = getBL(context);

			AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
			additionalInfoParameterInfo.setUniqueId(labelId);

			Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic.getAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, null, userId);

			if (either.isRight()) {
				ResponseFormat responseFormat = either.right().value();
				log.info("Failed to update additional information property. Reason - {}", responseFormat);
				return buildErrorResponse(responseFormat);
			}

			AdditionalInfoParameterInfo createdAI = either.left().value();

			log.debug("Additional information {}={} fetched successfully with id {}", createdAI.getKey(), createdAI.getValue(), createdAI.getUniqueId());

			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, createdAI);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Additional Information");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Additional Information");

			log.debug("get additional information failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);
		}

	}

	/**
	 * Get all additional information properties under given resource/service
	 * 
	 * @param nodeType
	 * @param uniqueId
	 * @param request
	 * @param userId
	 * @return
	 */
	protected Response getAllAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, HttpServletRequest request, String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);

		try {

			AdditionalInformationBusinessLogic businessLogic = getBL(context);

			Either<AdditionalInformationDefinition, ResponseFormat> either = businessLogic.getAllAdditionalInformation(nodeType, uniqueId, null, userId);
			if (either.isRight()) {
				ResponseFormat responseFormat = either.right().value();
				log.info("Failed to update additional information property. Reason - {}", responseFormat);
				return buildErrorResponse(responseFormat);
			}

			AdditionalInformationDefinition additionalInformationDefinition = either.left().value();

			log.debug("All Additional information retrieved for component {} is {}", uniqueId, additionalInformationDefinition);

			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, additionalInformationDefinition);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get All Additional Information");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Additional Information");
			log.debug("Get all addiotanl information properties failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);
		}

	}

	private AdditionalInformationBusinessLogic getBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		AdditionalInformationBusinessLogic bl = webApplicationContext.getBean(AdditionalInformationBusinessLogic.class);
		return bl;
	}

}

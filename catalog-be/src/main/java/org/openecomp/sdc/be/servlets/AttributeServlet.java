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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.AttributeBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Web Servlet for actions on Attributes
 * 
 * @author mshitrit
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resource Attribute Servlet", description = "Resource Attribute Servlet")
@Singleton
public class AttributeServlet extends AbstractValidationsServlet {
	private static Logger log = LoggerFactory.getLogger(AttributeServlet.class.getName());

	/**
	 * Creates new Attribute on a resource with given resource ID
	 * 
	 * @param resourceId
	 * @param data
	 * @param request
	 * @param userId
	 * @return
	 */
	@POST
	@Path("resources/{resourceId}/attributes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource Attribute", httpMethod = "POST", notes = "Returns created resource attribute", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Resource property created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Resource attribute already exist") })
	public Response createAttribute(@ApiParam(value = "resource id to update with new attribute", required = true) @PathParam("resourceId") final String resourceId, @ApiParam(value = "Resource attribute to be created", required = true) String data,
			@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);

		try {
			Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
			Wrapper<PropertyDefinition> attributesWrapper = new Wrapper<>();
			// convert json to AttributeDefinition

			buildAttributeFromString(data, attributesWrapper, errorWrapper);
			if (errorWrapper.isEmpty()) {
				AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
				Either<PropertyDefinition, ResponseFormat> createAttribute = businessLogic.createAttribute(resourceId, attributesWrapper.getInnerElement(), userId);
				if (createAttribute.isRight()) {
					errorWrapper.setInnerElement(createAttribute.right().value());
				} else {
					attributesWrapper.setInnerElement(createAttribute.left().value());
				}
			}

			Response response;
			if (!errorWrapper.isEmpty()) {
				log.info("Failed to create Attribute. Reason - ", errorWrapper.getInnerElement());
				response = buildErrorResponse(errorWrapper.getInnerElement());
			} else {
				PropertyDefinition createdAttDef = attributesWrapper.getInnerElement();
				log.debug("Attribute {} created successfully with id {}", createdAttDef.getName(), createdAttDef.getUniqueId());
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
				response = buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(createdAttDef));
			}

			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Attribute");
			log.debug("create property failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	/**
	 * Updates existing Attribute with given attributeID on a resource with given resourceID
	 * 
	 * @param resourceId
	 * @param attributeId
	 * @param data
	 * @param request
	 * @param userId
	 * @return
	 */
	@PUT
	@Path("resources/{resourceId}/attributes/{attributeId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Resource Attribute", httpMethod = "PUT", notes = "Returns updated attribute", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Resource attribute updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateAttribute(@ApiParam(value = "resource id to update with new attribute", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "attribute id to update", required = true) @PathParam("attributeId") final String attributeId, @ApiParam(value = "Resource attribute to update", required = true) String data, @Context final HttpServletRequest request,
			@HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		// get modifier id
		User modifier = new User();
		modifier.setUserId(userId);
		log.debug("modifier id is {}", userId);

		try {
			// convert json to PropertyDefinition
			Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
			Wrapper<PropertyDefinition> attributesWrapper = new Wrapper<>();
			// convert json to AttributeDefinition

			buildAttributeFromString(data, attributesWrapper, errorWrapper);

			if (errorWrapper.isEmpty()) {
				AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
				Either<PropertyDefinition, ResponseFormat> eitherUpdateAttribute = businessLogic.updateAttribute(resourceId, attributeId, attributesWrapper.getInnerElement(), userId);
				// update property
				if (eitherUpdateAttribute.isRight()) {
					errorWrapper.setInnerElement(eitherUpdateAttribute.right().value());
				} else {
					attributesWrapper.setInnerElement(eitherUpdateAttribute.left().value());
				}
			}

			Response response;
			if (!errorWrapper.isEmpty()) {
				log.info("Failed to update Attribute. Reason - ", errorWrapper.getInnerElement());
				response = buildErrorResponse(errorWrapper.getInnerElement());
			} else {
				PropertyDefinition updatedAttribute = attributesWrapper.getInnerElement();
				log.debug("Attribute id {} updated successfully ", updatedAttribute.getUniqueId());
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				response = buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(updatedAttribute));
			}

			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Attribute");
			log.debug("update attribute failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	/**
	 * Deletes existing Attribute with given attributeID on a resource with given resourceID
	 * 
	 * @param resourceId
	 * @param attributeId
	 * @param request
	 * @param userId
	 * @return
	 */
	@DELETE
	@Path("resources/{resourceId}/attributes/{attributeId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource Attribute", httpMethod = "DELETE", notes = "Returns deleted attribute", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "deleted attribute"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 404, message = "Resource property not found") })
	public Response deleteAttribute(@ApiParam(value = "resource id of attribute", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "Attribute id to delete", required = true) @PathParam("attributeId") final String attributeId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);
		log.debug("modifier id is {}", userId);

		try {

			// delete the property
			AttributeBusinessLogic businessLogic = getClassFromWebAppContext(context, () -> AttributeBusinessLogic.class);
			Either<PropertyDefinition, ResponseFormat> eitherAttribute = businessLogic.deleteAttribute(resourceId, attributeId, userId);
			if (eitherAttribute.isRight()) {
				log.debug("Failed to delete Attribute. Reason - ", eitherAttribute.right().value());
				return buildErrorResponse(eitherAttribute.right().value());
			}
			PropertyDefinition attributeDefinition = eitherAttribute.left().value();
			String name = attributeDefinition.getName();

			log.debug("Attribute {} deleted successfully with id {}", name, attributeDefinition.getUniqueId());
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
			return buildOkResponse(responseFormat, RepresentationUtils.toRepresentation(attributeDefinition));

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Attribute");
			log.debug("delete attribute failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	private void buildAttributeFromString(String data, Wrapper<PropertyDefinition> attributesWrapper, Wrapper<ResponseFormat> errorWrapper) {

		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			final PropertyDefinition attribute = gson.fromJson(data, PropertyDefinition.class);
			if (attribute == null) {
				log.info("Attribute content is invalid - {}", data);
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
				errorWrapper.setInnerElement(responseFormat);
			} else {
				attributesWrapper.setInnerElement(attribute);
			}

		} catch (Exception e) {
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			errorWrapper.setInnerElement(responseFormat);
			log.debug("Attribute content is invalid - {}", data, e);
			log.info("Attribute content is invalid - {}", data);
		}
	}
}

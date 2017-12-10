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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintDeserialiser;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintSerialiser;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resource Property Servlet", description = "Resource Property Servlet")
@Singleton
public class PropertyServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(PropertyServlet.class.getName());

	@POST
	@Path("resources/{resourceId}/properties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource Property", httpMethod = "POST", notes = "Returns created resource property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Resource property created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Resource property already exist") })
	public Response createProperty(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId, @ApiParam(value = "Resource property to be created", required = true) String data,
			@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);

		try {
			// convert json to PropertyDefinition
			Either<Map<String, PropertyDefinition>, ActionStatus> either = getPropertyModel(resourceId, data);
			if (either.isRight()) {
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(either.right().value());
				return buildErrorResponse(responseFormat);
			}
			Map<String, PropertyDefinition> properties = either.left().value();
			if (properties == null || properties.size() != 1) {
				log.info("Property conetnt is invalid - {}", data);
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
				return buildErrorResponse(responseFormat);
			}
			Entry<String, PropertyDefinition> entry = properties.entrySet().iterator().next();
			String propertyName = entry.getKey();
			PropertyDefinition newPropertyDefinition = entry.getValue();

			// create the new property
			PropertyBusinessLogic businessLogic = getPropertyBL(context);
			Either<EntryData<String, PropertyDefinition>, ResponseFormat> status = businessLogic.createProperty(resourceId, propertyName, newPropertyDefinition, userId);
			if (status.isRight()) {
				log.info("Failed to create Property. Reason - ", status.right().value());
				return buildErrorResponse(status.right().value());
			}
			EntryData<String, PropertyDefinition> property = status.left().value();
			String name = property.getKey();
			PropertyDefinition propertyDefinition = property.getValue();

			log.debug("Property {} created successfully with id {}", name, propertyDefinition.getUniqueId());
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
			return buildOkResponse(responseFormat, propertyToJson(property));

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Create Property");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Property");
			log.debug("create property failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	@GET
	@Path("resources/{resourceId}/properties/{propertyId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource Property", httpMethod = "GET", notes = "Returns property of resource", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 404, message = "Resource property not found") })
	public Response getProperty(@ApiParam(value = "resource id of property", required = true) @PathParam("resourceId") final String resourceId, @ApiParam(value = "proerty id to get", required = true) @PathParam("propertyId") final String propertyId,
			@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}, modifier id is {}", url, userId);

		try {

			//
			PropertyBusinessLogic businessLogic = getPropertyBL(context);
			Either<Entry<String, PropertyDefinition>, ResponseFormat> status = businessLogic.getProperty(resourceId, propertyId, userId);

			if (status.isRight()) {
				log.info("Failed to get Property. Reason - ", status.right().value());
				return buildErrorResponse(status.right().value());
			}
			Entry<String, PropertyDefinition> property = status.left().value();
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, propertyToJson(property));
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Get Property");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Property");
			log.debug("get property failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	@DELETE
	@Path("resources/{resourceId}/properties/{propertyId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create Resource Property", httpMethod = "DELETE", notes = "Returns deleted property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 204, message = "deleted property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 404, message = "Resource property not found") })
	public Response deleteProperty(@ApiParam(value = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "Property id to delete", required = true) @PathParam("propertyId") final String propertyId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();

		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {} modifier id is {}", url, userId);

		try {

			// delete the property
			PropertyBusinessLogic businessLogic = getPropertyBL(context);
			Either<Entry<String, PropertyDefinition>, ResponseFormat> status = businessLogic.deleteProperty(resourceId, propertyId, userId);
			if (status.isRight()) {
				log.debug("Failed to delete Property. Reason - ", status.right().value());
				return buildErrorResponse(status.right().value());
			}
			Entry<String, PropertyDefinition> property = status.left().value();
			String name = property.getKey();
			PropertyDefinition propertyDefinition = property.getValue();

			log.debug("Property {} deleted successfully with id {}", name, propertyDefinition.getUniqueId());
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT);
			return buildOkResponse(responseFormat, propertyToJson(property));

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Property");
			log.debug("delete property failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	@PUT
	@Path("resources/{resourceId}/properties/{propertyId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Resource Property", httpMethod = "PUT", notes = "Returns updated property", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Resource property updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateProperty(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
			@ApiParam(value = "proerty id to update", required = true) @PathParam("propertyId") final String propertyId, @ApiParam(value = "Resource property to update", required = true) String data, @Context final HttpServletRequest request,
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
			Either<Map<String, PropertyDefinition>, ActionStatus> either = getPropertyModel(resourceId, data);
			if (either.isRight()) {
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(either.right().value());
				return buildErrorResponse(responseFormat);
			}
			Map<String, PropertyDefinition> properties = either.left().value();
			if (properties == null || properties.size() != 1) {
				log.info("Property conetnt is invalid - {}", data);
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
				return buildErrorResponse(responseFormat);
			}
			Entry<String, PropertyDefinition> entry = properties.entrySet().iterator().next();
			PropertyDefinition newPropertyDefinition = entry.getValue();

			// update property
			PropertyBusinessLogic businessLogic = getPropertyBL(context);
			Either<EntryData<String, PropertyDefinition>, ResponseFormat> status = businessLogic.updateProperty(resourceId, propertyId, newPropertyDefinition, userId);
			if (status.isRight()) {
				log.info("Failed to update Property. Reason - ", status.right().value());
				return buildErrorResponse(status.right().value());
			}
			EntryData<String, PropertyDefinition> property = status.left().value();
			PropertyDefinition propertyDefinition = property.getValue();

			log.debug("Property id {} updated successfully ", propertyDefinition.getUniqueId());
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			return buildOkResponse(responseFormat, propertyToJson(property));

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Update Property");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Property");
			log.debug("update property failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			return buildErrorResponse(responseFormat);

		}
	}

	private Either<Map<String, PropertyDefinition>, ActionStatus> getPropertyModel(String resourceId, String data) {
		JSONParser parser = new JSONParser();
		JSONObject root;
		try {
			Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
			root = (JSONObject) parser.parse(data);

			Set entrySet = root.entrySet();
			Iterator iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry next = (Entry) iterator.next();
				String propertyName = (String) next.getKey();
				JSONObject value = (JSONObject) next.getValue();
				String jsonString = value.toJSONString();
				Either<PropertyDefinition, ActionStatus> convertJsonToObject = convertJsonToObject(jsonString, PropertyDefinition.class);
				if (convertJsonToObject.isRight()) {
					return Either.right(convertJsonToObject.right().value());
				}
				PropertyDefinition propertyDefinition = convertJsonToObject.left().value();
				// PropertyDefinition propertyDefinition =
				// gson.fromJson(jsonString , PropertyDefinition.class);
				String uniqueId = UniqueIdBuilder.buildPropertyUniqueId(resourceId, (String) propertyName);
				propertyDefinition.setUniqueId(uniqueId);
				properties.put(propertyName, propertyDefinition);
			}

			// Set keySet = root.keySet();
			// for (Object propertyName : keySet){
			// JSONObject val = (JSONObject) root.get(propertyName);
			// String jsonString = val.toJSONString();
			// Either<PropertyDefinition,ActionStatus> convertJsonToObject =
			// convertJsonToObject(jsonString, PropertyDefinition.class);
			// if (convertJsonToObject.isRight()){
			// return Either.right(convertJsonToObject.right().value());
			// }
			// PropertyDefinition propertyDefinition =
			// convertJsonToObject.left().value();
			// //PropertyDefinition propertyDefinition =
			// gson.fromJson(jsonString , PropertyDefinition.class);
			// String uniqueId =
			// UniqueIdBuilder.buildPropertyUniqueId("resourceId",
			// (String)propertyName);
			// propertyDefinition.setUniqueId(uniqueId);
			// properties.put((String)propertyName,propertyDefinition);
			// }
			return Either.left(properties);
		} catch (ParseException e) {
			log.info("Property conetnt is invalid - {}", data);
			return Either.right(ActionStatus.INVALID_CONTENT);
		}
	}

	private String propertyToJson(Map.Entry<String, PropertyDefinition> property) {
		JSONObject root = new JSONObject();
		String propertyName = property.getKey();
		PropertyDefinition propertyDefinition = property.getValue();
		// String jsonPropertyDefinition = gson.toJson(propertyDefinition);
		// root.put(propertyName, jsonPropertyDefinition);
		JSONObject propertyDefinitionO = getPropertyDefinitionJSONObject(propertyDefinition);
		root.put(propertyName, propertyDefinitionO);
		propertyDefinition.getType();
		return root.toString();
	}

	private JSONObject getPropertyDefinitionJSONObject(PropertyDefinition propertyDefinition) {

		Either<String, ActionStatus> either = convertObjectToJson(propertyDefinition);
		if (either.isRight()) {
			return new JSONObject();
		}
		String value = either.left().value();
		try {
			JSONObject root = (JSONObject) new JSONParser().parse(value);
			return root;
		} catch (ParseException e) {
			log.info("failed to convert input to json");
			log.debug("failed to convert to json", e);
			return new JSONObject();
		}

	}

	private <T> Either<T, ActionStatus> convertJsonToObject(String data, Class<T> clazz) {
		T t = null;
		Type constraintType = new TypeToken<PropertyConstraint>() {
		}.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintDeserialiser()).create();
		try {
			log.trace("convert json to object. json=\n {}", data);
			t = gson.fromJson(data, clazz);
			if (t == null) {
				log.info("object is null after converting from json");
				return Either.right(ActionStatus.INVALID_CONTENT);
			}
		} catch (Exception e) {
			// INVALID JSON
			log.info("failed to convert from json");
			log.debug("failed to convert from json", e);
			return Either.right(ActionStatus.INVALID_CONTENT);
		}
		return Either.left(t);
	}

	private <T> Either<String, ActionStatus> convertObjectToJson(PropertyDefinition propertyDefinition) {
		Type constraintType = new TypeToken<PropertyConstraint>() {
		}.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType, new PropertyConstraintSerialiser()).create();
		try {
			log.trace("convert object to json. propertyDefinition= {}", propertyDefinition.toString());
			String json = gson.toJson(propertyDefinition);
			if (json == null) {
				log.info("object is null after converting to json");
				return Either.right(ActionStatus.INVALID_CONTENT);
			}
			return Either.left(json);
		} catch (Exception e) {
			// INVALID JSON
			log.info("failed to convert to json");
			log.debug("failed to convert fto json", e);
			return Either.right(ActionStatus.INVALID_CONTENT);
		}

	}

	private PropertyBusinessLogic getPropertyBL(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		PropertyBusinessLogic propertytBl = webApplicationContext.getBean(PropertyBusinessLogic.class);
		return propertytBl;
	}

	// private class UiProperty{
	// String type;
	// String source;
	// String name;
	// String description;
	// public String getType() {
	// return type;
	// }
	// public void setType(String type) {
	// this.type = type;
	// }
	// public String getSource() {
	// return source;
	// }
	// public void setSource(String source) {
	// this.source = source;
	// }
	// public String getName() {
	// return name;
	// }
	// public void setName(String name) {
	// this.name = name;
	// }
	// public String getDescription() {
	// return description;
	// }
	// public void setDescription(String description) {
	// this.description = description;
	// }
	//
	// }

}

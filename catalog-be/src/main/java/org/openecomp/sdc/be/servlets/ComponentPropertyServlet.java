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

import com.jcabi.aspects.Loggable;

import java.util.List;
import java.util.Map;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Component Property Servlet", description = "Property Servlet - used to create properties in Service and Resource")
@Singleton
public class ComponentPropertyServlet extends BeGenericServlet {

	@Autowired
	ApplicationDataTypeCache applicationDataTypeCache;

  private static final Logger log = LoggerFactory.getLogger(ComponentPropertyServlet.class);
  private static final String CREATE_PROPERTY = "Create Property";
  private static final String DEBUG_MESSAGE = "Start handle request of {} modifier id is {}";

  @POST
  @Path("services/{serviceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Create Service Property", httpMethod = "POST", notes = "Returns created service property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 201, message = "Service property created"),
      @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
      @ApiResponse(code = 409, message = "Service property already exist") })
  public Response createPropertyInService(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
                                 @ApiParam(value = "Service property to be created", required = true) String data,
                                 @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return createProperty(serviceId, data, request, userId);
  }

  @POST
  @Path("resources/{resourceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Create Resource Property", httpMethod = "POST", notes = "Returns created service property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource property created"),
          @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 409, message = "Resource property already exist") })
  public Response createPropertyInResource(@ApiParam(value = "Resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
                                           @ApiParam(value = "Resource property to be created", required = true) String data,
                                           @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return createProperty(resourceId, data, request, userId);
  }


  @GET
  @Path("services/{serviceId}/properties/{propertyId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get Service Property", httpMethod = "GET", notes = "Returns property of service", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 404, message = "Service property not found") })
  public Response getPropertyInService(@ApiParam(value = "service id of property", required = true)
                                       @PathParam("serviceId") final String serviceId, @ApiParam(value = "property id to get", required = true) @PathParam("propertyId") final String propertyId,
                                       @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return getProperty(serviceId, propertyId, request, userId);
  }

  @GET
  @Path("resources/{resourceId}/properties/{propertyId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get Resource Property", httpMethod = "GET", notes = "Returns property of resource", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 404, message = "Resource property not found") })
  public Response getPropertyInResource(@ApiParam(value = "resource id of property", required = true)
                                       @PathParam("resourceId") final String resourceId, @ApiParam(value = "property id to get", required = true) @PathParam("propertyId") final String propertyId,
                                       @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return getProperty(resourceId, propertyId, request, userId);
  }

  @GET
  @Path("services/{serviceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get Service Property", httpMethod = "GET", notes = "Returns property list of service", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
      @ApiResponse(code = 404, message = "Service property not found") })
  public Response getPropertyListInService(@ApiParam(value = "service id of property", required = true) @PathParam("serviceId") final String serviceId,
                                  @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return getPropertyList(serviceId, request, userId);
  }

  @GET
  @Path("resources/{resourceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get Resource Property", httpMethod = "GET", notes = "Returns property list of resource", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 404, message = "Resource property not found") })
  public Response getPropertyListInResource(@ApiParam(value = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
                                           @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return getPropertyList(resourceId, request, userId);
  }

  @DELETE
  @Path("services/{serviceId}/properties/{propertyId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Delete Service Property", httpMethod = "DELETE", notes = "Returns deleted property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 204, message = "deleted property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 404, message = "Service property not found") })
  public Response deletePropertyInService(@ApiParam(value = "service id of property", required = true) @PathParam("serviceId") final String serviceId,
                                          @ApiParam(value = "Property id to delete", required = true) @PathParam("propertyId") final String propertyId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return deleteProperty(serviceId, propertyId, request, userId);
  }

  @DELETE
  @Path("resources/{resourceId}/properties/{propertyId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Delete Resource Property", httpMethod = "DELETE", notes = "Returns deleted property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 204, message = "deleted property"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
          @ApiResponse(code = 404, message = "Resource property not found") })
  public Response deletePropertyInResource(@ApiParam(value = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
                                          @ApiParam(value = "Property id to delete", required = true) @PathParam("propertyId") final String propertyId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return deleteProperty(resourceId, propertyId, request, userId);
  }

  @PUT
  @Path("services/{serviceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Update Service Property", httpMethod = "PUT", notes = "Returns updated property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Service property updated"),
      @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
  public Response updatePropertyInService(@ApiParam(value = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
                                 @ApiParam(value = "Service property to update", required = true) String data, @Context final HttpServletRequest request,
                                 @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return updateProperty(serviceId, data, request, userId);
  }

  @PUT
  @Path("resources/{resourceId}/properties")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Update Resource Property", httpMethod = "PUT", notes = "Returns updated property", response = Response.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource property updated"),
          @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
  public Response updatePropertyInResource(@ApiParam(value = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
                                          @ApiParam(value = "Resource property to update", required = true) String data, @Context final HttpServletRequest request,
                                          @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

    return updateProperty(resourceId, data, request, userId);
  }

  private Response createProperty(String componentId, String data,  HttpServletRequest request,String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);

    try{
      Either<Map<String, PropertyDefinition>, ActionStatus> propertyDefinition =
              getPropertyModel(componentId, data);
      if (propertyDefinition.isRight()) {
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(propertyDefinition.right().value());
        return buildErrorResponse(responseFormat);
      }

      Map<String, PropertyDefinition> properties = propertyDefinition.left().value();
      if (properties == null || properties.size() != 1) {
        log.info("Property content is invalid - {}", data);
        ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
        return buildErrorResponse(responseFormat);
      }

      Map.Entry<String, PropertyDefinition> entry = properties.entrySet().iterator().next();
      PropertyDefinition newPropertyDefinition = entry.getValue();
      newPropertyDefinition.setParentUniqueId(componentId);
      String propertyName = newPropertyDefinition.getName();

      PropertyBusinessLogic propertyBL = getPropertyBL(context);
      Either<EntryData<String, PropertyDefinition>, ResponseFormat> addPropertyEither =
              propertyBL.addPropertyToComponent(componentId, propertyName, newPropertyDefinition, userId);

      if(addPropertyEither.isRight()) {
        return buildErrorResponse(addPropertyEither.right().value());
      }

      return buildOkResponse(newPropertyDefinition);

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
      log.debug("create property failed with exception", e);
      ResponseFormat responseFormat =
              getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
      return buildErrorResponse(responseFormat);
    }
  }


  private Response updateProperty(String componentId, String data, HttpServletRequest request, String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug("Start handle request of {}", url);

    // get modifier id
    User modifier = new User();
    modifier.setUserId(userId);
    log.debug("modifier id is {}", userId);
//
    try {
      // convert json to PropertyDefinition

      Either<Map<String, PropertyDefinition>, ActionStatus> propertiesListEither =
          getPropertiesListForUpdate(data);
      if (propertiesListEither.isRight()) {
        ResponseFormat responseFormat =
            getComponentsUtils().getResponseFormat(propertiesListEither.right().value());
        return buildErrorResponse(responseFormat);
      }
      Map<String, PropertyDefinition> properties = propertiesListEither.left().value();
      if (properties == null) {
        log.info("Property content is invalid - {}", data);
        ResponseFormat responseFormat =
            getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
        return buildErrorResponse(responseFormat);
      }

		//Validate value and Constraint of property
		Either<Boolean, ResponseFormat> constraintValidatorResponse =
				PropertyValueConstraintValidationUtil.getInstance().
						validatePropertyConstraints(properties.values(), applicationDataTypeCache);
		if (constraintValidatorResponse.isRight()) {
			log.error("Failed validation value and constraint of property: {}",
					constraintValidatorResponse.right().value());
			return buildErrorResponse(constraintValidatorResponse.right().value());
		}

      // update property

      PropertyBusinessLogic businessLogic = getPropertyBL(context);
      for(PropertyDefinition propertyDefinition : properties.values()) {
        Either<EntryData<String, PropertyDefinition>, ResponseFormat> status =
            businessLogic.updateComponentProperty(
                componentId, propertyDefinition.getUniqueId(), propertyDefinition, userId);
        if (status.isRight()) {
          log.info("Failed to update Property. Reason - ", status.right().value());
          return buildErrorResponse(status.right().value());
        }
        EntryData<String, PropertyDefinition> property = status.left().value();
        PropertyDefinition updatedPropertyDefinition = property.getValue();

        log.debug("Property id {} updated successfully ", updatedPropertyDefinition.getUniqueId());
      }

      ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
      return buildOkResponse(responseFormat, properties);

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Property");
      log.debug("update property failed with exception", e);
      ResponseFormat responseFormat =
          getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
      return buildErrorResponse(responseFormat);

    }
  }

  private Response getProperty(String componentId, String propertyId, HttpServletRequest request, String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug(DEBUG_MESSAGE, url, userId);

    try {
      PropertyBusinessLogic propertyBL = getPropertyBL(context);
      Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> retrievedPropertyEither =
              propertyBL.getComponentProperty(componentId, propertyId, userId);

      if(retrievedPropertyEither.isRight()) {
        return buildErrorResponse(retrievedPropertyEither.right().value());
      }

      return buildOkResponse(retrievedPropertyEither.left().value());

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
      log.debug("get property failed with exception", e);
      ResponseFormat responseFormat =
              getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
      return buildErrorResponse(responseFormat);
    }
  }
  private Response getPropertyList(String componentId, HttpServletRequest request, String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug(DEBUG_MESSAGE, url, userId);

    try {
      PropertyBusinessLogic propertyBL = getPropertyBL(context);
      Either<List<PropertyDefinition>, ResponseFormat> propertiesListEither =
              propertyBL.getPropertiesList(componentId, userId);

      if(propertiesListEither.isRight()) {
        return buildErrorResponse(propertiesListEither.right().value());
      }

      return buildOkResponse(propertiesListEither.left().value());

    } catch (Exception e) {
      BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
      log.debug("get property failed with exception", e);
      ResponseFormat responseFormat =
              getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
      return buildErrorResponse(responseFormat);
    }
  }
  private Response deleteProperty(String componentId, String propertyId, HttpServletRequest request, String userId) {
    ServletContext context = request.getSession().getServletContext();

    String url = request.getMethod() + " " + request.getRequestURI();
    log.debug(DEBUG_MESSAGE, url, userId);

    try {

      // delete the property
      PropertyBusinessLogic businessLogic = getPropertyBL(context);
      Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> status =
              businessLogic.deletePropertyFromComponent(componentId, propertyId, userId);
      if (status.isRight()) {
        log.debug("Failed to delete Property. Reason - ", status.right().value());
        return buildErrorResponse(status.right().value());
      }
      Map.Entry<String, PropertyDefinition> property = status.left().value();
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

}

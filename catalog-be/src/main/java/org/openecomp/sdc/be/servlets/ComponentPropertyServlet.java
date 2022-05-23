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
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
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
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.PropertyValueConstraintValidationUtil;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-2 APIs")
@Server(url = "/sdc2/rest")
@Singleton
public class ComponentPropertyServlet extends BeGenericServlet {

    private static final Logger log = LoggerFactory.getLogger(ComponentPropertyServlet.class);
    private static final String CREATE_PROPERTY = "Create Property";
    private static final String DEBUG_MESSAGE = "Start handle request of {} modifier id is {}";
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ComponentPropertyServlet.class.getName());
    private final PropertyBusinessLogic propertyBusinessLogic;
    private final ApplicationDataTypeCache applicationDataTypeCache;

    @Inject
    public ComponentPropertyServlet(UserBusinessLogic userBusinessLogic, ComponentsUtils componentsUtils,
                                    ApplicationDataTypeCache applicationDataTypeCache, PropertyBusinessLogic propertyBusinessLogic) {
        super(userBusinessLogic, componentsUtils);
        this.applicationDataTypeCache = applicationDataTypeCache;
        this.propertyBusinessLogic = propertyBusinessLogic;
    }

    @POST
    @Path("services/{serviceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Service Property", method = "POST", summary = "Returns created service property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Service property created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service property already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createPropertyInService(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "Service property to be created", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createProperty(serviceId, data, request, userId);
    }

    @POST
    @Path("resources/{resourceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource Property", method = "POST", summary = "Returns created service property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Resource property created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource property already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createPropertyInResource(
        @Parameter(description = "Resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Resource property to be created", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return createProperty(resourceId, data, request, userId);
    }

    @GET
    @Path("services/{serviceId}/properties/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Service Property", method = "GET", summary = "Returns property of service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "property"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPropertyInService(
        @Parameter(description = "service id of property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "property id to get", required = true) @PathParam("propertyId") final String propertyId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getProperty(serviceId, propertyId, request, userId);
    }

    @GET
    @Path("resources/{resourceId}/properties/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Resource Property", method = "GET", summary = "Returns property of resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "property"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPropertyInResource(
        @Parameter(description = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "property id to get", required = true) @PathParam("propertyId") final String propertyId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getProperty(resourceId, propertyId, request, userId);
    }

    @GET
    @Path("services/{serviceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Service Property", method = "GET", summary = "Returns property list of service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "property"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPropertyListInService(
        @Parameter(description = "service id of property", required = true) @PathParam("serviceId") final String serviceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getPropertyList(serviceId, request, userId);
    }

    @GET
    @Path("resources/{resourceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get Resource Property", method = "GET", summary = "Returns property list of resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "property"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getPropertyListInResource(
        @Parameter(description = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getPropertyList(resourceId, request, userId);
    }

    @DELETE
    @Path("services/{serviceId}/properties/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete Service Property", method = "DELETE", summary = "Returns deleted property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "deleted property"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deletePropertyInService(
        @Parameter(description = "service id of property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "Property id to delete", required = true) @PathParam("propertyId") final String propertyId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return deleteProperty(serviceId, propertyId, request, userId);
    }

    @DELETE
    @Path("resources/{resourceId}/properties/{propertyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Delete Resource Property", method = "DELETE", summary = "Returns deleted property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "204", description = "deleted property"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource property not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deletePropertyInResource(
        @Parameter(description = "resource id of property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Property id to delete", required = true) @PathParam("propertyId") final String propertyId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return deleteProperty(resourceId, propertyId, request, userId);
    }

    @PUT
    @Path("services/{serviceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Service Property", method = "PUT", summary = "Returns updated property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Service property updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updatePropertyInService(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "Service property to update", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return updateProperty(serviceId, data, request, userId);
    }

    @PUT
    @Path("resources/{resourceId}/properties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Resource Property", method = "PUT", summary = "Returns updated property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Resource property updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updatePropertyInResource(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Resource property to update", required = true) String data, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return updateProperty(resourceId, data, request, userId);
    }

    private Response createProperty(String componentId, String data, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {} modifier id is {} data is {}", url, userId, data);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_PROPERTIES, StatusCode.STARTED, "CREATE_PROPERTIES by user {} ", userId);
        try {
            Either<Map<String, PropertyDefinition>, ActionStatus> propertyDefinition = getPropertyModel(componentId, data);
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
            newPropertyDefinition.setUserCreated(true);
            Either<EntryData<String, PropertyDefinition>, ResponseFormat> addPropertyEither = propertyBusinessLogic
                .addPropertyToComponent(componentId, newPropertyDefinition, userId);
            if (addPropertyEither.isRight()) {
                return buildErrorResponse(addPropertyEither.right().value());
            }
            loggerSupportability.log(LoggerSupportabilityActions.CREATE_PROPERTIES, StatusCode.COMPLETE, "CREATE_PROPERTIES by user {} ", userId);
            return buildOkResponse(newPropertyDefinition);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
            log.debug("create property failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private Response updateProperty(String componentId, String data, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}", url);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.STARTED, "UPDATE_PROPERTIES by user {} ", userId);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
//
        try {
            // convert json to PropertyDefinition
            Either<Map<String, PropertyDefinition>, ActionStatus> propertiesListEither = getPropertiesListForUpdate(data);
            if (propertiesListEither.isRight()) {
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(propertiesListEither.right().value());
                return buildErrorResponse(responseFormat);
            }
            Map<String, PropertyDefinition> properties = propertiesListEither.left().value();
            if (properties == null) {
                log.info("Property content is invalid - {}", data);
                ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
                return buildErrorResponse(responseFormat);
            }
            //Validate value and Constraint of property and Fetch all data types from cache
            Either<Boolean, ResponseFormat> constraintValidatorResponse = new PropertyValueConstraintValidationUtil()
                .validatePropertyConstraints(properties.values(), applicationDataTypeCache,
                    propertyBusinessLogic.getComponentModelByComponentId(componentId));
            if (constraintValidatorResponse.isRight()) {
                log.error("Failed validation value and constraint of property: {}", constraintValidatorResponse.right().value());
                return buildErrorResponse(constraintValidatorResponse.right().value());
            }
            // update property
            for (PropertyDefinition propertyDefinition : properties.values()) {
                Either<EntryData<String, PropertyDefinition>, ResponseFormat> status = propertyBusinessLogic
                    .updateComponentProperty(componentId, propertyDefinition.getUniqueId(), propertyDefinition, userId);
                if (status.isRight()) {
                    log.info("Failed to update Property. Reason - {}", status.right().value());
                    return buildErrorResponse(status.right().value());
                }
                EntryData<String, PropertyDefinition> property = status.left().value();
                PropertyDefinition updatedPropertyDefinition = property.getValue();
                log.debug("Property id {} updated successfully ", updatedPropertyDefinition.getUniqueId());
            }
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            loggerSupportability.log(LoggerSupportabilityActions.UPDATE_PROPERTIES, StatusCode.COMPLETE, "UPDATE_PROPERTIES by user {} ", userId);
            return buildOkResponse(responseFormat, properties);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Property");
            log.debug("update property failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private Response getProperty(String componentId, String propertyId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(DEBUG_MESSAGE, url, userId);
        try {
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> retrievedPropertyEither = propertyBusinessLogic
                .getComponentProperty(componentId, propertyId, userId);
            if (retrievedPropertyEither.isRight()) {
                return buildErrorResponse(retrievedPropertyEither.right().value());
            }
            return buildOkResponse(retrievedPropertyEither.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
            log.debug("get property failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private Response getPropertyList(String componentId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(DEBUG_MESSAGE, url, userId);
        try {
            Either<List<PropertyDefinition>, ResponseFormat> propertiesListEither = propertyBusinessLogic.getPropertiesList(componentId, userId);
            if (propertiesListEither.isRight()) {
                return buildErrorResponse(propertiesListEither.right().value());
            }
            return buildOkResponse(propertiesListEither.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(CREATE_PROPERTY);
            log.debug("get property failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    private Response deleteProperty(String componentId, String propertyId, HttpServletRequest request, String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(DEBUG_MESSAGE, url, userId);
        try {
            // delete the property
            Either<Map.Entry<String, PropertyDefinition>, ResponseFormat> status = propertyBusinessLogic
                .deletePropertyFromComponent(componentId, propertyId, userId);
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

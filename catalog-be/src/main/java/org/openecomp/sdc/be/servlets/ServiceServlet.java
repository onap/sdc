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

import static org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.ServiceRelations;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.servlets.ServiceUploadServlet.ServiceAuthorityTypeEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Server(url = "/sdc2/rest")
@Controller
public class ServiceServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ServiceServlet.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ServiceServlet.class.getName());
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private final ElementBusinessLogic elementBusinessLogic;
    private final ServiceBusinessLogic serviceBusinessLogic;

    @Inject
    public ServiceServlet(UserBusinessLogic userBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBL, ComponentsUtils componentsUtils,
                          ServletUtils servletUtils, ResourceImportManager resourceImportManager, ServiceBusinessLogic serviceBusinessLogic,
                          ResourceBusinessLogic resourceBusinessLogic, ElementBusinessLogic elementBusinessLogic) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.elementBusinessLogic = elementBusinessLogic;
    }

    @POST
    @Path("/services")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Service", method = "POST", summary = "Returns created service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "201", description = "Service created"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createService(@Parameter(description = "Service object to be created", required = true) String data,
                                  @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_SERVICE, StatusCode.STARTED, "Starting to create a service by user {} ", userId);
        validateNotEmptyBody(data);
        Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
        if (convertResponse.isRight()) {
            throw new ByResponseFormatComponentException(convertResponse.right().value());
        }
        Service service = convertResponse.left().value();
        Either<Service, ResponseFormat> actionResponse = serviceBusinessLogic.createService(service, modifier);
        if (actionResponse.isRight()) {
            log.debug("Failed to create service");
            throw new ByResponseFormatComponentException(actionResponse.right().value());
        }
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_SERVICE, service.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
            "Service {} has been created by user {} ", service.getName(), userId);
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), actionResponse.left().value());
    }

    public Either<Service, ResponseFormat> parseToService(String serviceJson, User user) {
        return getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(serviceJson, user, Service.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.SERVICE);
    }

    @GET
    @Path("/services/validate-name/{serviceName}")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "validate service name", method = "GET", summary = "checks if the chosen service name is available ", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response validateServiceName(@PathParam("serviceName") final String serviceName, @Context final HttpServletRequest request,
                                        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Either<Map<String, Boolean>, ResponseFormat> actionResponse = serviceBusinessLogic.validateServiceNameExists(serviceName, userId);
            if (actionResponse.isRight()) {
                log.debug("failed to get validate service name");
                return buildErrorResponse(actionResponse.right().value());
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Service Name");
            log.debug("validate service name failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/audit-records/{componentType}/{componentUniqueId}")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "get component audit records", method = "GET", summary = "get audit records for a service or a resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getComponentAuditRecords(@PathParam("componentType") final String componentType,
                                             @PathParam("componentUniqueId") final String componentUniqueId,
                                             @Context final HttpServletRequest request,
                                             @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        init();
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<String> uuidWrapper = new Wrapper<>();
        Wrapper<String> versionWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        try {
            validateUserExist(responseWrapper, userWrapper, userId);
            if (responseWrapper.isEmpty()) {
                fillUUIDAndVersion(responseWrapper, uuidWrapper, versionWrapper, userWrapper.getInnerElement(), validateComponentType(componentType),
                    componentUniqueId, context);
            }
            if (responseWrapper.isEmpty()) {
                Either<List<Map<String, Object>>, ResponseFormat> eitherServiceAudit = serviceBusinessLogic
                    .getComponentAuditRecords(versionWrapper.getInnerElement(), uuidWrapper.getInnerElement(), userId);
                if (eitherServiceAudit.isRight()) {
                    Response errorResponse = buildErrorResponse(eitherServiceAudit.right().value());
                    responseWrapper.setInnerElement(errorResponse);
                } else {
                    List<Map<String, Object>> auditRecords = eitherServiceAudit.left().value();
                    Response okResponse = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), auditRecords);
                    responseWrapper.setInnerElement(okResponse);
                }
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Validate Service Name");
            log.debug("get Service Audit Records failed with exception", e);
            throw e;
        }
        return responseWrapper.getInnerElement();
    }

    private void fillUUIDAndVersion(Wrapper<Response> responseWrapper, Wrapper<String> uuidWrapper, Wrapper<String> versionWrapper, User user,
                                    final ComponentTypeEnum componentTypeEnum, final String componentUniqueId, ServletContext context) {
        if (componentTypeEnum == ComponentTypeEnum.RESOURCE) {
            Either<Resource, ResponseFormat> eitherResource = getResourceBL(context).getResource(componentUniqueId, user);
            if (eitherResource.isLeft()) {
                uuidWrapper.setInnerElement(eitherResource.left().value().getUUID());
                versionWrapper.setInnerElement(eitherResource.left().value().getVersion());
            } else {
                responseWrapper.setInnerElement(buildErrorResponse(eitherResource.right().value()));
            }
        } else {
            Either<Service, ResponseFormat> eitherService = getServiceBL(context).getService(componentUniqueId, user);
            if (eitherService.isLeft()) {
                uuidWrapper.setInnerElement(eitherService.left().value().getUUID());
                versionWrapper.setInnerElement(eitherService.left().value().getVersion());
            } else {
                responseWrapper.setInnerElement(buildErrorResponse(eitherService.right().value()));
            }
        }
    }

    @DELETE
    @Path("/services/{serviceId}")
    @Tag(name = "SDCE-2 APIs")
    @Operation(description = "Delete Service", method = "DELETE", summary = "Return no content", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "204", description = "Service deleted"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteService(@PathParam("serviceId") final String serviceId,
                                  @Parameter(description = "Optional parameter to determine the delete action: " +
                                      "DELETE, which will permanently delete theService from the system or " +
                                      "MARK_AS_DELETE, which will logically mark the service as deleted. Default action is to MARK_AS_DELETE")
                                  @QueryParam("deleteAction") final Action deleteAction,
                                  @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            String serviceIdLower = serviceId.toLowerCase();
            loggerSupportability
                .log(LoggerSupportabilityActions.DELETE_SERVICE, StatusCode.STARTED, "Starting to delete service {} by user {} ", serviceIdLower,
                    userId);
            ServiceBusinessLogic businessLogic = getServiceBL(context);
            ResponseFormat actionResponse;
            if (Action.DELETE.equals(deleteAction)) {
                businessLogic.deleteServiceAllVersions(serviceIdLower, modifier);
                actionResponse = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
            } else {
                actionResponse = businessLogic.markServiceForDeletion(serviceIdLower, modifier);
            }
            if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
                log.debug("failed to delete service");
                return buildErrorResponse(actionResponse);
            }
            loggerSupportability
                .log(LoggerSupportabilityActions.DELETE_SERVICE, StatusCode.COMPLETE, "Ended deleting service {} by user {}", serviceIdLower, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Service");
            log.debug("delete service failed with exception", e);
            throw e;
        }
    }

    @DELETE
    @Path("/services/{serviceName}/{version}")
    @Tag(name = "SDCE-2 APIs")
    @Operation(description = "Delete Service By Name And Version", method = "DELETE", summary = "Returns no content", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "204", description = "Service deleted"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteServiceByNameAndVersion(@PathParam("serviceName") final String serviceName, @PathParam("version") final String version,
                                                  @Context final HttpServletRequest request) {
        User modifier = getUser(request);
        try {
            ResponseFormat actionResponse = serviceBusinessLogic.deleteServiceByNameAndVersion(serviceName, version, modifier);
            if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
                log.debug("failed to delete service");
                return buildErrorResponse(actionResponse);
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Service");
            log.debug("delete service failed with exception", e);
            throw e;
        }
    }

    private User getUser(HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        return modifier;
    }

    @PUT
    @Path("/services/{serviceId}/metadata")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Service Metadata", method = "PUT", summary = "Returns updated service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "200", description = "Service Updated"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateServiceMetadata(@PathParam("serviceId") final String serviceId,
                                          @Parameter(description = "Service object to be Updated", required = true) String data,
                                          @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            String serviceIdLower = serviceId.toLowerCase();
            Either<Service, ResponseFormat> convertResponse = parseToService(data, modifier);
            if (convertResponse.isRight()) {
                log.debug("failed to parse service");
                return buildErrorResponse(convertResponse.right().value());
            }
            Service updatedService = convertResponse.left().value();
            Either<Service, ResponseFormat> actionResponse = serviceBusinessLogic.updateServiceMetadata(serviceIdLower, updatedService, modifier);
            if (actionResponse.isRight()) {
                log.debug("failed to update service");
                return buildErrorResponse(actionResponse.right().value());
            }
            Service service = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Service Metadata");
            log.debug("update service metadata failed with exception", e);
            throw e;
        }
    }

    /**
     * updates group instance property values Note, than in case of group instance updated successfully, related resourceInstance and containing
     * component modification time will be updated
     *
     * @param serviceId
     * @param componentInstanceId
     * @param groupInstanceId
     * @param data
     * @param request
     * @param userId
     * @return
     */
    @PUT
    @Path("/{containerComponentType}/{serviceId}/resourceInstance/{componentInstanceId}/groupInstance/{groupInstanceId}")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Group Instance Property Values", method = "PUT", summary = "Returns updated group instance", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "200", description = "Group Instance Property Values Updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateGroupInstancePropertyValues(@PathParam("serviceId") final String serviceId,
                                                      @PathParam("componentInstanceId") final String componentInstanceId,
                                                      @PathParam("groupInstanceId") final String groupInstanceId,
                                                      @Parameter(description = "Group instance object to be Updated", required = true) String data,
                                                      @Context final HttpServletRequest request,
                                                      @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws JsonProcessingException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Either<List<GroupInstanceProperty>, ResponseFormat> actionResponse = null;
        try {
            List<GroupInstanceProperty> updatedProperties;
            Type listType = new TypeToken<ArrayList<GroupInstanceProperty>>() {
            }.getType();
            ArrayList<GroupInstanceProperty> newProperties = gson.fromJson(data, listType);
            if (newProperties == null) {
                actionResponse = Either.right(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            }
            if (actionResponse == null) {
                log.debug("Start handle update group instance property values request. Received group instance is {}", groupInstanceId);
                actionResponse = serviceBusinessLogic
                    .updateGroupInstancePropertyValues(modifier, serviceId, componentInstanceId, groupInstanceId, newProperties);
                if (actionResponse.isRight()) {
                    actionResponse = Either.right(actionResponse.right().value());
                }
            }
            if (actionResponse.isLeft()) {
                updatedProperties = actionResponse.left().value();
                ObjectMapper mapper = new ObjectMapper();
                String result = mapper.writeValueAsString(updatedProperties);
                return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
            } else {
                return buildErrorResponse(actionResponse.right().value());
            }
        } catch (Exception e) {
            log.error(BUSINESS_PROCESS_ERROR, this.getClass().getName(), "Exception occured during update Group Instance property values.", e);
            throw e;
        }
    }

    @GET
    @Path("/services/{serviceId}")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Service", method = "GET", summary = "Returns service according to serviceId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getServiceById(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request,
                                   @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            String serviceIdLower = serviceId.toLowerCase();
            log.debug("get service with id {}", serviceId);
            Either<Service, ResponseFormat> actionResponse = serviceBusinessLogic.getService(serviceIdLower, modifier);
            if (actionResponse.isRight()) {
                log.debug("failed to get service");
                return buildErrorResponse(actionResponse.right().value());
            }
            Service service = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Service");
            log.debug("get service failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/services/serviceName/{serviceName}/serviceVersion/{serviceVersion}")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Service", method = "GET", summary = "Returns service according to name and version", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Service.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getServiceByNameAndVersion(@PathParam("serviceName") final String serviceName,
                                               @PathParam("serviceVersion") final String serviceVersion, @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        // get modifier id
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Either<Service, ResponseFormat> actionResponse = serviceBusinessLogic.getServiceByNameAndVersion(serviceName, serviceVersion, userId);
            if (actionResponse.isRight()) {
                return buildErrorResponse(actionResponse.right().value());
            }
            Service service = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Service by name and version");
            log.debug("get service failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/services/{serviceId}/distribution/{env}/activate")
    @Tag(name = "SDCE-5 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Activate distribution", method = "POST", summary = "activate distribution", responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "409", description = "Service cannot be distributed due to missing deployment artifacts"),
        @ApiResponse(responseCode = "404", description = "Requested service was not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error. Please try again later.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response activateDistribution(@PathParam("serviceId") final String serviceId, @PathParam("env") final String env,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Either<Service, ResponseFormat> distResponse = serviceBusinessLogic.activateDistribution(serviceId, env, modifier, request);
        if (distResponse.isRight()) {
            log.debug("failed to activate service distribution");
            return buildErrorResponse(distResponse.right().value());
        }
        Service service = distResponse.left().value();
        Object result = null;
        try {
            result = RepresentationUtils.toRepresentation(service);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Activate Distribution");
            log.debug("activate distribution failed with exception", e);
            throw e;
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
    }

    @POST
    @Path("/services/{serviceId}/distribution/{did}/markDeployed")
    @Tag(name = "SDCE-5 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Mark distribution as deployed", method = "POST", summary = "relevant audit record will be created", responses = {
        @ApiResponse(responseCode = "200", description = "Service was marked as deployed"),
        @ApiResponse(responseCode = "409", description = "Restricted operation"),
        @ApiResponse(responseCode = "403", description = "Service is not available"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Requested service was not found"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error. Please try again later.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response markDistributionAsDeployed(@PathParam("serviceId") final String serviceId, @PathParam("did") final String did,
                                               @Context final HttpServletRequest request,
                                               @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Either<Service, ResponseFormat> distResponse = serviceBusinessLogic.markDistributionAsDeployed(serviceId, did, modifier);
            if (distResponse.isRight()) {
                log.debug("failed to mark distribution as deployed");
                return buildErrorResponse(distResponse.right().value());
            }
            Service service = distResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(service);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Mark Distribution As Deployed");
            log.debug("mark distribution as deployed failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/services/{serviceId}/tempUrlToBeDeleted")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(responses = {@ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error. Please try again later.")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response tempUrlToBeDeleted(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request,
                                       @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Service service = (serviceBusinessLogic.getService(serviceId, modifier)).left().value();
            Either<Service, ResponseFormat> res = serviceBusinessLogic
                .updateDistributionStatusForActivation(service, modifier, DistributionStatusEnum.DISTRIBUTED);
            if (res.isRight()) {
                buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("tempUrlToBeDeleted");
            log.debug("failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/services/{serviceId}/linksMap")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Service component relations map", method = "GET", summary = "Returns service components relations", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceRelations.class)))),
        @ApiResponse(responseCode = "200", description = "Service found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Service not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getServiceComponentRelationMap(@PathParam("serviceId") final String serviceId, @Context final HttpServletRequest request,
                                                   @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User(userId);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            String serviceIdLower = serviceId.toLowerCase();
            log.debug("get service components relations with id {}", serviceId);
            Either<ServiceRelations, ResponseFormat> actionResponse = serviceBusinessLogic.getServiceComponentsRelations(serviceIdLower, modifier);
            if (actionResponse.isRight()) {
                log.debug("failed to get service relations data");
                return buildErrorResponse(actionResponse.right().value());
            }
            ServiceRelations serviceRelations = actionResponse.left().value();
            Object result = RepresentationUtils.toRepresentation(serviceRelations);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Service");
            log.debug("get service relations data failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/services/importService")
    @Tag(name = "SDCE-2 APIs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Import Service", method = "POST", summary = "Returns imported service", responses = {
        @ApiResponse(responseCode = "201", description = "Service created"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service already exist")})
    public Response importNsService(@Parameter(description = "Service object to be imported", required = true) String data,
                                    @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        initSpringFromContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            final Wrapper<Response> responseWrapper = performUIImport(data, request, userId, null);
            return responseWrapper.getInnerElement();
        } catch (IOException | ZipException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Import Service");
            log.debug("import service failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    private Wrapper<Response> performUIImport(String data, final HttpServletRequest request, String userId,
                                              String serviceUniqueId) throws FileNotFoundException, ZipException {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        Wrapper<User> userWrapper = new Wrapper<>();
        Wrapper<UploadServiceInfo> uploadServiceInfoWrapper = new Wrapper<>();
        Wrapper<String> yamlStringWrapper = new Wrapper<>();
        ServiceAuthorityTypeEnum serviceAuthorityTypeEnum = ServiceAuthorityTypeEnum.USER_TYPE_UI;
        commonServiceGeneralValidations(responseWrapper, userWrapper, uploadServiceInfoWrapper, serviceAuthorityTypeEnum, userId, data);
        specificServiceAuthorityValidations(responseWrapper, uploadServiceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request,
            data, serviceAuthorityTypeEnum);
        if (responseWrapper.isEmpty()) {
            handleImportService(responseWrapper, userWrapper.getInnerElement(), uploadServiceInfoWrapper.getInnerElement(), serviceUniqueId);
        }
        return responseWrapper;
    }

    /**
     * import ReplaceService
     *
     * @param userId
     * @param requestId
     * @param instanceIdHeader
     * @param accept
     * @param authorization
     * @param request
     * @param file
     * @param contentDispositionHeader
     * @param serviceInfoJsonString
     * @param uuid
     * @return
     */
    @POST
    @Path("/services/serviceUUID/{uuid}/importReplaceService")
    @Tag(name = "SDCE-2 APIs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Import Service", method = "POST", summary = "Returns imported service", responses = {
        @ApiResponse(responseCode = "201", description = "Service created"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Service already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response importReplaceService(
        @Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Context final HttpServletRequest request, @Parameter(description = "FileInputStream") @FormDataParam("serviceZip") File file,
        @Parameter(description = "ContentDisposition") @FormDataParam("serviceZip") FormDataContentDisposition contentDispositionHeader,
        @Parameter(description = "serviceMetadata") @FormDataParam("serviceZipMetadata") String serviceInfoJsonString,
        @Parameter(description = "The requested asset uuid", required = true) @PathParam("uuid") final String uuid) {
        initSpringFromContext();
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("importReplaceService,Start handle request of {}", url);
        // get modifier id
        User modifier = new User(userId);
        log.debug("importReplaceService,modifier id is {}", userId);
        log.debug("importReplaceService,get file:{},fileName:{}", file, file.getName());
        ResponseFormat responseFormat = null;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.Import_Replace_Service;
        String assetType = "services";
        Either<List<? extends Component>, ResponseFormat> assetTypeData = elementBusinessLogic
            .getCatalogComponentsByUuidAndAssetType(assetType, uuid);
        if (assetTypeData.isRight() || assetTypeData.left().value().size() != 1) {
            log.debug("getServiceAbstractStatus: Service Fetching Failed");
            throw new ByResponseFormatComponentException(assetTypeData.right().value());
        }
        log.debug("getServiceAbstractStatus: Service Fetching Success");
        Service oldService = (Service) assetTypeData.left().value().get(0);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentType.getValue());
        DistributionData distributionData = new DistributionData(instanceIdHeader, requestURI);
        // Mandatory
        if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
            log.debug("importReplaceService: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, null);
            return buildErrorResponse(responseFormat);
        }
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            // file import
            Wrapper<User> userWrapper = new Wrapper<>();
            Wrapper<UploadServiceInfo> uploadServiceInfoWrapper = new Wrapper<>();
            Wrapper<String> yamlStringWrapper = new Wrapper<>();
            ServiceUploadServlet.ServiceAuthorityTypeEnum serviceAuthorityEnum = ServiceUploadServlet.ServiceAuthorityTypeEnum.CSAR_TYPE_BE;
            // PayLoad Validations
            commonServiceGeneralValidations(responseWrapper, userWrapper, uploadServiceInfoWrapper, serviceAuthorityEnum, userId,
                serviceInfoJsonString);
            fillServicePayload(responseWrapper, uploadServiceInfoWrapper, yamlStringWrapper, modifier, serviceInfoJsonString, serviceAuthorityEnum,
                file);
            specificServiceAuthorityValidations(responseWrapper, uploadServiceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request,
                serviceInfoJsonString, serviceAuthorityEnum);
            log.debug("importReplaceService:get payload:{}", uploadServiceInfoWrapper.getInnerElement().getPayloadData());
            ServiceMetadataDataDefinition serviceMetadataDataDefinition = (ServiceMetadataDataDefinition) oldService.getComponentMetadataDefinition()
                .getMetadataDataDefinition();
            uploadServiceInfoWrapper.getInnerElement().setServiceVendorModelNumber(serviceMetadataDataDefinition.getServiceVendorModelNumber());
            uploadServiceInfoWrapper.getInnerElement().setDescription(oldService.getDescription());
            uploadServiceInfoWrapper.getInnerElement().setCategories(oldService.getCategories());
            uploadServiceInfoWrapper.getInnerElement().setIcon(oldService.getIcon());
            uploadServiceInfoWrapper.getInnerElement().setProjectCode(oldService.getProjectCode());
            if (responseWrapper.isEmpty()) {
                log.debug("importReplaceService:start handleImportService");
                handleImportService(responseWrapper, userWrapper.getInnerElement(), uploadServiceInfoWrapper.getInnerElement(), null);
            }
            return responseWrapper.getInnerElement();
        } catch (IOException | ZipException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Import Service");
            log.debug("import service failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    public enum Action {DELETE, MARK_AS_DELETE}
}

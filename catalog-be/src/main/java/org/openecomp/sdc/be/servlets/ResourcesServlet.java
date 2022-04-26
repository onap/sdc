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

import com.jcabi.aspects.Loggable;
import fj.data.Either;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.Servers;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.DeleteActionEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.elements.LoggerSupportability;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

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
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class ResourcesServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ResourcesServlet.class);
    private static final LoggerSupportability loggerSupportability = LoggerSupportability.getLogger(ResourcesServlet.class.getName());
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private final ResourceBusinessLogic resourceBusinessLogic;

    @Inject
    public ResourcesServlet(UserBusinessLogic userBusinessLogic, ComponentInstanceBusinessLogic componentInstanceBL,
                            ResourceBusinessLogic resourceBusinessLogic, ComponentsUtils componentsUtils, ServletUtils servletUtils,
                            ResourceImportManager resourceImportManager) {
        super(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.resourceBusinessLogic = resourceBusinessLogic;
    }

    @POST
    @Path("/resources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource", method = "POST", summary = "Returns created resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "201", description = "Resource created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createResource(@Parameter(description = "Resource object to be created", required = true) String data,
                                   @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException, ZipException {
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        loggerSupportability.log(LoggerSupportabilityActions.CREATE_RESOURCE, StatusCode.STARTED, "Starting to create Resource by user {}", userId);
        Response response;
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            // UI Import
            if (isUIImport(data)) {
                performUIImport(responseWrapper, data, request, userId, null);
            }
            // UI Create
            else {
                Either<Resource, ResponseFormat> convertResponse = parseToResource(data, modifier);
                if (convertResponse.isRight()) {
                    log.debug("failed to parse resource");
                    response = buildErrorResponse(convertResponse.right().value());
                    return response;
                }
                Resource resource = convertResponse.left().value();
                Resource createdResource = resourceBusinessLogic.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, modifier, null, null);
                Object representation = RepresentationUtils.toRepresentation(createdResource);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
                responseWrapper.setInnerElement(response);
                loggerSupportability
                    .log(LoggerSupportabilityActions.CREATE_RESOURCE, resource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                        "Resource successfully created user {}", userId);
            }
            return responseWrapper.getInnerElement();
        } catch (final IOException | ZipException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Resource");
            log.debug("create resource failed with exception", e);
            throw e;
        }
    }

    private boolean isUIImport(String data) {
        boolean isUIImport;
        try {
            JSONObject json = new JSONObject(data);
            String payloadName = json.getString(ImportUtils.Constants.UI_JSON_PAYLOAD_NAME);
            isUIImport = payloadName != null && !payloadName.isEmpty();
        } catch (JSONException e) {
            log.debug("failed to parse json sent from client, json:{}", data, e);
            isUIImport = false;
        }
        return isUIImport;
    }

    private void performUIImport(final Wrapper<Response> responseWrapper, final String data, final HttpServletRequest request, final String userId,
                                 final String resourceUniqueId) throws ZipException {
        Wrapper<User> userWrapper = new Wrapper<>();
        Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
        Wrapper<String> yamlStringWrapper = new Wrapper<>();
        ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.USER_TYPE_UI;
        commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, data);
        if (!CsarValidationUtils.isCsarPayloadName(uploadResourceInfoWrapper.getInnerElement().getPayloadName())) {
            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), data, resourceAuthorityEnum,
                null);
            // PayLoad Validations
            commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement());
        }
        specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request,
            data, resourceAuthorityEnum);
        if (responseWrapper.isEmpty()) {
            handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(),
                yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, true, resourceUniqueId);
        }
    }

    private Either<Resource, ResponseFormat> parseToResource(String resourceJson, User user) {
        return getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(resourceJson, user, Resource.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.RESOURCE);
    }

    private Either<Resource, ResponseFormat> parseToLightResource(String resourceJson, User user) {
        Either<Resource, ResponseFormat> ret = getComponentsUtils()
            .convertJsonToObjectUsingObjectMapper(resourceJson, user, Resource.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
                ComponentTypeEnum.RESOURCE);
        if (ret.isLeft()) {// drop unwanted data (sent from UI in update flow)
            ret.left().value().setRequirements(null);
            ret.left().value().setCapabilities(null);
        }
        return ret;
    }

    @DELETE
    @Path("/resources/{resourceId}")
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResource(@PathParam("resourceId") final String resourceId,
                                   @Parameter(description = "Optional parameter to determine the delete action: " +
                                           "DELETE, which will permanently delete the Resource from the system or " +
                                           "MARK_AS_DELETE, which will logically mark the Resource as deleted. Default action is to MARK_AS_DELETE")
                                   @QueryParam("deleteAction") final DeleteActionEnum deleteAction,
                                   @Context final HttpServletRequest request) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        loggerSupportability.log(LoggerSupportabilityActions.DELETE_RESOURCE, StatusCode.STARTED, "Starting to delete Resource by user {}", userId);
        try {
            String resourceIdLower = resourceId.toLowerCase();
            ResponseFormat actionResponse;
            if (DeleteActionEnum.DELETE.equals(deleteAction)) {
                resourceBusinessLogic.deleteResourceAllVersions(resourceId, modifier);
                actionResponse = componentsUtils.getResponseFormat(ActionStatus.NO_CONTENT);
            } else {
                actionResponse = resourceBusinessLogic.deleteResource(resourceIdLower, modifier);
            }
            if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
                log.debug("failed to delete resource");
                return buildErrorResponse(actionResponse);
            }

            loggerSupportability.log(LoggerSupportabilityActions.DELETE_RESOURCE, StatusCode.COMPLETE, "Ended delete Resource by user {}", userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource");
            log.debug("delete resource failed with exception ", e);
            throw e;
        }
    }

    @DELETE
    @Path("/resources/{resourceName}/{version}")
    @Operation(description = "Delete Resource By Name And Version", method = "DELETE", summary = "Returns no content", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "204", description = "Resource deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteResourceByNameAndVersion(@PathParam("resourceName") final String resourceName, @PathParam("version") final String version,
                                                   @Context final HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        ResourceBusinessLogic businessLogic = getResourceBL(context);
        ResponseFormat actionResponse = businessLogic.deleteResourceByNameAndVersion(resourceName, version, modifier);
        if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
            log.debug("failed to delete resource");
            response = buildErrorResponse(actionResponse);
            return response;
        }
        response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
        return response;
    }

    @GET
    @Path("/resources/{resourceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Resource", method = "GET", summary = "Returns resource according to resourceId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getResourceById(@PathParam("resourceId") final String resourceId, @Context final HttpServletRequest request,
                                    @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        try {
            String resourceIdLower = resourceId.toLowerCase();
            log.trace("get resource with id {}", resourceId);
            Either<Resource, ResponseFormat> actionResponse = resourceBusinessLogic.getResource(resourceIdLower, modifier);
            if (actionResponse.isRight()) {
                log.debug("failed to get resource");
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object resource = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource");
            log.debug("get resource failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/resources/resourceName/{resourceName}/resourceVersion/{resourceVersion}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Retrieve Resource by name and version", method = "GET", summary = "Returns resource according to resourceId", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource found"), @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "404", description = "Resource not found")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getResourceByNameAndVersion(@PathParam("resourceName") final String resourceName,
                                                @PathParam("resourceVersion") final String resourceVersion, @Context final HttpServletRequest request,
                                                @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        try {
            Either<Resource, ResponseFormat> actionResponse = resourceBusinessLogic
                .getResourceByNameAndVersion(resourceName, resourceVersion, userId);
            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object resource = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource by name and version");
            log.debug("get resource failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/resources/validate-name/{resourceName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "validate resource name", method = "GET", summary = "checks if the chosen resource name is available ", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource found"), @ApiResponse(responseCode = "403", description = "Restricted operation")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response validateResourceName(@PathParam("resourceName") final String resourceName, @QueryParam("subtype") String resourceType,
                                         @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        if (resourceType != null && !ResourceTypeEnum.containsName(resourceType)) {
            log.debug("invalid resource type received");
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            return response;
        }
        ResourceTypeEnum typeEnum = null;
        if (resourceType != null) {
            typeEnum = ResourceTypeEnum.valueOf(resourceType);
        }
        Either<Map<String, Boolean>, ResponseFormat> actionResponse = resourceBusinessLogic
            .validateResourceNameExists(resourceName, typeEnum, userId);
        if (actionResponse.isRight()) {
            log.debug("failed to validate resource name");
            response = buildErrorResponse(actionResponse.right().value());
            return response;
        }
        return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());
    }

    @GET
    @Path("/resources/certified/abstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCertifiedAbstractResources(@Context final HttpServletRequest request,
                                                  @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            List<Resource> resources = resourceBusinessLogic.getAllCertifiedResources(true, HighestFilterEnum.HIGHEST_ONLY, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(resources));
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Abstract Resources");
            log.debug("getCertifiedAbstractResources failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/resources/certified/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getCertifiedNotAbstractResources(@Context final HttpServletRequest request,
                                                     @HeaderParam(value = Constants.USER_ID_HEADER) String userId) throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}", url);
        try {
            List<Resource> resouces = resourceBusinessLogic.getAllCertifiedResources(false, HighestFilterEnum.ALL, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(resouces));
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Non Abstract Resources");
            log.debug("getCertifiedNotAbstractResources failed with exception", e);
            throw e;
        }
    }

    @PUT
    @Path("/resources/{resourceId}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Resource Metadata", method = "PUT", summary = "Returns updated resource metadata", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource metadata updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceMetadata(@PathParam("resourceId") final String resourceId,
                                           @Parameter(description = "Resource metadata to be updated", required = true) String data,
                                           @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId)
        throws IOException {
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        Response response;
        try {
            String resourceIdLower = resourceId.toLowerCase();
            Either<Resource, ResponseFormat> updateInfoResource = getComponentsUtils()
                .convertJsonToObjectUsingObjectMapper(data, modifier, Resource.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA,
                    ComponentTypeEnum.RESOURCE);
            if (updateInfoResource.isRight()) {
                log.debug("failed to parse resource metadata");
                response = buildErrorResponse(updateInfoResource.right().value());
                return response;
            }
            Resource updatedResource = resourceBusinessLogic
                .updateResourceMetadata(resourceIdLower, updateInfoResource.left().value(), null, modifier, false);
            Object resource = RepresentationUtils.toRepresentation(updatedResource);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource Metadata");
            log.debug("Update Resource Metadata failed with exception", e);
            throw e;
        }
    }

    @PUT
    @Path("/resources/{resourceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Update Resource", method = "PUT", summary = "Returns updated resource", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "200", description = "Resource updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResource(@Parameter(description = "Resource object to be updated", required = true) String data,
                                   @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                   @PathParam(value = "resourceId") String resourceId) throws IOException, ZipException {
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug(MODIFIER_ID_IS, userId);
        loggerSupportability.log(LoggerSupportabilityActions.UPDATE_RESOURCE, StatusCode.STARTED, "Starting to update a resource by user {}", userId);
        Response response;
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            // UI Import
            if (isUIImport(data)) {
                performUIImport(responseWrapper, data, request, userId, resourceId);
            } else {
                Either<Resource, ResponseFormat> convertResponse = parseToLightResource(data, modifier);
                if (convertResponse.isRight()) {
                    log.debug("failed to parse resource");
                    response = buildErrorResponse(convertResponse.right().value());
                    return response;
                }
                Resource updatedResource = resourceBusinessLogic
                    .validateAndUpdateResourceFromCsar(convertResponse.left().value(), modifier, null, null, resourceId);
                Object representation = RepresentationUtils.toRepresentation(updatedResource);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation);
                responseWrapper.setInnerElement(response);
                loggerSupportability
                    .log(LoggerSupportabilityActions.UPDATE_RESOURCE, updatedResource.getComponentMetadataForSupportLog(), StatusCode.COMPLETE,
                        "Ended update a resource by user {}", userId);
            }
            return responseWrapper.getInnerElement();
        } catch (final IOException | ZipException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource");
            log.debug("update resource failed with exception", e);
            throw e;
        }
    }

    @GET
    @Path("/resources/csar/{csaruuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Create Resource", method = "POST", summary = "Returns resource created from csar uuid", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Resource.class)))),
        @ApiResponse(responseCode = "201", description = "Resource retrieced"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getResourceFromCsar(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
                                        @PathParam(value = "csaruuid") String csarUUID) throws IOException {
        init();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        // retrieve user details
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        User user = new User();
        user.setUserId(userId);
        log.debug("user id is {}", userId);
        Response response;
        try {
            Either<Resource, ResponseFormat> eitherResource = resourceBusinessLogic
                .getLatestResourceFromCsarUuid(ValidationUtils.sanitizeInputString(csarUUID), user);
            // validate response
            if (eitherResource.isRight()) {
                log.debug("failed to get resource from csarUuid : {}", csarUUID);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT), eitherResource.right().value());
            } else {
                Object representation = RepresentationUtils.toRepresentation(eitherResource.left().value());
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation);
            }
            return response;
        } catch (IOException e) {
            log.debug("get resource by csar failed with exception", e);
            throw e;
        }
    }

    @POST
    @Path("/resources/importReplaceResource")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Import Resource", method = "POST", summary = "Returns imported resource", responses = {
        @ApiResponse(responseCode = "201", description = "Resource created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Resource already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response importReplaceResource(
        @Parameter(description = "The user id", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Context final HttpServletRequest request, @Parameter(description = "FileInputStream") @FormDataParam("resourceZip") File file,
        @Parameter(description = "ContentDisposition") @FormDataParam("resourceZip") FormDataContentDisposition contentDispositionHeader,
        @Parameter(description = "resourceMetadata") @FormDataParam("resourceZipMetadata") String resourceInfoJsonString) {
        init();
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug("importReplaceResource,Start handle request of {}", url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("importReplaceResource,modifier id is {}", userId);
        log.debug("importReplaceResource,get file:{},fileName:{}", file, file.getName());
        Response response;
        ResponseFormat responseFormat = null;
        AuditingActionEnum auditingActionEnum = AuditingActionEnum.Import_Replace_Resource;
        String assetType = "resources";
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentType.getValue());
        DistributionData distributionData = new DistributionData(instanceIdHeader, requestURI);
        // Mandatory
        if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
            log.debug("importReplaceResource: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, distributionData, resourceCommonInfo, requestId, null);
            return buildErrorResponse(responseFormat);
        }
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            // file import
            Wrapper<User> userWrapper = new Wrapper<>();
            Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
            Wrapper<String> yamlStringWrapper = new Wrapper<>();
            ResourceAuthorityTypeEnum serviceAuthorityEnum = ResourceAuthorityTypeEnum.CSAR_TYPE_BE;
            // PayLoad Validations
            commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, serviceAuthorityEnum, userId, resourceInfoJsonString);
            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, modifier, resourceInfoJsonString, serviceAuthorityEnum, file);
            specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(),
                request, resourceInfoJsonString, serviceAuthorityEnum);
            log.debug("importReplaceResource:get payload:{}", uploadResourceInfoWrapper.getInnerElement().getPayloadData());
            log.debug("importReplaceResource:get ResourceType:{}", uploadResourceInfoWrapper.getInnerElement().getResourceType());
            if (responseWrapper.isEmpty()) {
                log.debug("importReplaceService:start handleImport");
                handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(),
                    yamlStringWrapper.getInnerElement(), serviceAuthorityEnum, true, null);
            }
            return responseWrapper.getInnerElement();
        } catch (ZipException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Import Resource");
            log.debug("import resource failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }
}

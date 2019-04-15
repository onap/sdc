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
import io.swagger.annotations.*;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.sdc.be.components.impl.CsarValidationUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.HighestFilterEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resources Catalog", description = "Resources Servlet")
@Singleton
public class ResourcesServlet extends AbstractValidationsServlet {

    private static final Logger log = Logger.getLogger(ResourcesServlet.class);

    @POST
    @Path("/resources")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Resource", httpMethod = "POST", notes = "Returns created resource", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource created"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Resource already exist") })
    public Response createResource(@ApiParam(value = "Resource object to be created", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);

        Response response;
        try {

            Wrapper<Response> responseWrapper = new Wrapper<>();
            // UI Import
            if (isUIImport(data)) {
                performUIImport(responseWrapper, data, request, userId, null);
            }
            // UI Create
            else {
                ResourceBusinessLogic businessLogic = getResourceBL(context);

                Either<Resource, ResponseFormat> convertResponse = parseToResource(data, modifier);
                if (convertResponse.isRight()) {
                    log.debug("failed to parse resource");
                    response = buildErrorResponse(convertResponse.right().value());
                    return response;
                }

                Resource resource = convertResponse.left().value();
                Resource createdResource = businessLogic.createResource(resource, AuditingActionEnum.CREATE_RESOURCE, modifier, null, null);

                Object representation = RepresentationUtils.toRepresentation(createdResource);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
                responseWrapper.setInnerElement(response);
            }
            return responseWrapper.getInnerElement();
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Create Resource");
            log.debug("create resource failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
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

    private void performUIImport(Wrapper<Response> responseWrapper, String data, final HttpServletRequest request, String userId, String resourceUniqueId) throws FileNotFoundException {
        Wrapper<User> userWrapper = new Wrapper<>();
        Wrapper<UploadResourceInfo> uploadResourceInfoWrapper = new Wrapper<>();
        Wrapper<String> yamlStringWrapper = new Wrapper<>();

        ResourceAuthorityTypeEnum resourceAuthorityEnum = ResourceAuthorityTypeEnum.USER_TYPE_UI;

        commonGeneralValidations(responseWrapper, userWrapper, uploadResourceInfoWrapper, resourceAuthorityEnum, userId, data);

        if (!CsarValidationUtils.isCsarPayloadName(uploadResourceInfoWrapper.getInnerElement().getPayloadName())) {
            fillPayload(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), data, resourceAuthorityEnum, null);

            // PayLoad Validations
            commonPayloadValidations(responseWrapper, yamlStringWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement());
        }
        specificResourceAuthorityValidations(responseWrapper, uploadResourceInfoWrapper, yamlStringWrapper, userWrapper.getInnerElement(), request, data, resourceAuthorityEnum);

        if (responseWrapper.isEmpty()) {
            handleImport(responseWrapper, userWrapper.getInnerElement(), uploadResourceInfoWrapper.getInnerElement(), yamlStringWrapper.getInnerElement(), resourceAuthorityEnum, true, resourceUniqueId);
        }
    }

    private Either<Resource, ResponseFormat> parseToResource(String resourceJson, User user) {
        return getComponentsUtils().convertJsonToObjectUsingObjectMapper(resourceJson, user, Resource.class, AuditingActionEnum.CREATE_RESOURCE, ComponentTypeEnum.RESOURCE);
    }

    private Either<Resource, ResponseFormat> parseToLightResource(String resourceJson, User user) {
        Either<Resource, ResponseFormat> ret = getComponentsUtils().convertJsonToObjectUsingObjectMapper(resourceJson, user, Resource.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA, ComponentTypeEnum.RESOURCE);
        if (ret.isLeft()) {// drop unwanted data (sent from UI in update flow)
            ret.left().value().setRequirements(null);
            ret.left().value().setCapabilities(null);
        }
        return ret;
    }

    @DELETE
    @Path("/resources/{resourceId}")
    public Response deleteResource(@PathParam("resourceId") final String resourceId, @Context final HttpServletRequest request) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);

        Response response;

        try {
            String resourceIdLower = resourceId.toLowerCase();
            ResourceBusinessLogic businessLogic = getResourceBL(context);
            ResponseFormat actionResponse = businessLogic.deleteResource(resourceIdLower, modifier);

            if (actionResponse.getStatus() != HttpStatus.SC_NO_CONTENT) {
                log.debug("failed to delete resource");
                response = buildErrorResponse(actionResponse);
                return response;
            }
            response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.NO_CONTENT), null);
            return response;

        } catch (JSONException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Delete Resource");
            log.debug("delete resource failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @DELETE
    @Path("/resources/{resourceName}/{version}")
    public Response deleteResourceByNameAndVersion(@PathParam("resourceName") final String resourceName, @PathParam("version") final String version, @Context final HttpServletRequest request) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        String userId = request.getHeader(Constants.USER_ID_HEADER);
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);

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
    @ApiOperation(value = "Retrieve Resource", httpMethod = "GET", notes = "Returns resource according to resourceId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Resource not found") })
    public Response getResourceById(@PathParam("resourceId") final String resourceId, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);

        Response response;

        try {
            String resourceIdLower = resourceId.toLowerCase();
            ResourceBusinessLogic businessLogic = getResourceBL(context);
            log.trace("get resource with id {}", resourceId);
            Either<Resource, ResponseFormat> actionResponse = businessLogic.getResource(resourceIdLower, modifier);

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
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @GET
    @Path("/resources/resourceName/{resourceName}/resourceVersion/{resourceVersion}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve Resource by name and version", httpMethod = "GET", notes = "Returns resource according to resourceId", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Resource not found") })
    public Response getResourceByNameAndVersion(@PathParam("resourceName") final String resourceName, @PathParam("resourceVersion") final String resourceVersion, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);
        Response response;
        try {
            ResourceBusinessLogic businessLogic = getResourceBL(context);
            Either<Resource, ResponseFormat> actionResponse = businessLogic.getResourceByNameAndVersion(resourceName, resourceVersion, userId);
            if (actionResponse.isRight()) {
                response = buildErrorResponse(actionResponse.right().value());
                return response;
            }
            Object resource = RepresentationUtils.toRepresentation(actionResponse.left().value());
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);

        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Resource by name and version");
            log.debug("get resource failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));

        }
    }

    @GET
    @Path("/resources/validate-name/{resourceName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "validate resource name", httpMethod = "GET", notes = "checks if the chosen resource name is available ", response = Response.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource found"), @ApiResponse(code = 403, message = "Restricted operation") })
    public Response validateResourceName(@PathParam("resourceName") final String resourceName, @QueryParam("subtype") String resourceType, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}" , userId);
        Response response;
        ResourceBusinessLogic businessLogic = getResourceBL(context);

        if (resourceType != null && !ResourceTypeEnum.containsName(resourceType)) {
            log.debug("invalid resource type received");
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
            return response;

        }
        ResourceTypeEnum typeEnum = null;
        if (resourceType != null) {
            typeEnum = ResourceTypeEnum.valueOf(resourceType);
        }
        Either<Map<String, Boolean>, ResponseFormat> actionResponse = businessLogic.validateResourceNameExists(resourceName, typeEnum, userId);

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
    public Response getCertifiedAbstractResources(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}" , url);
        try {
            List<Resource> resources = getResourceBL(context)
                    .getAllCertifiedResources(true, HighestFilterEnum.HIGHEST_ONLY, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(resources));

        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Abstract Resources");
            log.debug("getCertifiedAbstractResources failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }
    }

    @GET
    @Path("/resources/certified/notabstract")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCertifiedNotAbstractResources(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("(get) Start handle request of {}" , url);
        try {
            ResourceBusinessLogic businessLogic = getResourceBL(context);
            List<Resource> resouces = businessLogic.getAllCertifiedResources(false, HighestFilterEnum.ALL, userId);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), RepresentationUtils.toRepresentation(resouces));

        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get Certified Non Abstract Resources");
            log.debug("getCertifiedNotAbstractResources failed with exception", e);
            return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
        }

    }

    @PUT
    @Path("/resources/{resourceId}/metadata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Resource Metadata", httpMethod = "PUT", notes = "Returns updated resource metadata", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource metadata updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content") })
    public Response updateResourceMetadata(@PathParam("resourceId") final String resourceId, @ApiParam(value = "Resource metadata to be updated", required = true) String data, @Context final HttpServletRequest request,
            @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        Response response;
        try {
            ResourceBusinessLogic businessLogic = getResourceBL(context);
            String resourceIdLower = resourceId.toLowerCase();
            Either<Resource, ResponseFormat> updateInfoResource = getComponentsUtils().convertJsonToObjectUsingObjectMapper(data, modifier, Resource.class, AuditingActionEnum.UPDATE_RESOURCE_METADATA, ComponentTypeEnum.RESOURCE);
            if (updateInfoResource.isRight()) {
                log.debug("failed to parse resource metadata");
                response = buildErrorResponse(updateInfoResource.right().value());
                return response;
            }
            Resource updatedResource = businessLogic.updateResourceMetadata(resourceIdLower, updateInfoResource.left().value(), null, modifier, false);
            Object resource = RepresentationUtils.toRepresentation(updatedResource);
            return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), resource);
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource Metadata");
            log.debug("Update Resource Metadata failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @PUT
    @Path("/resources/{resourceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update Resource", httpMethod = "PUT", notes = "Returns updated resource", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Resource updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
            @ApiResponse(code = 409, message = "Resource already exist") })
    public Response updateResource(@ApiParam(value = "Resource object to be updated", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId,
            @PathParam(value = "resourceId") String resourceId) {

        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        init();
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);
        // get modifier id
        User modifier = new User();
        modifier.setUserId(userId);
        log.debug("modifier id is {}", userId);
        Response response;
        try {
            Wrapper<Response> responseWrapper = new Wrapper<>();
            // UI Import
            if (isUIImport(data)) {
                performUIImport(responseWrapper, data, request, userId, resourceId);
            } else {
                ResourceBusinessLogic businessLogic = getResourceBL(context);
                Either<Resource, ResponseFormat> convertResponse = parseToLightResource(data, modifier);
                if (convertResponse.isRight()) {
                    log.debug("failed to parse resource");
                    response = buildErrorResponse(convertResponse.right().value());
                    return response;
                }
                Resource updatedResource = businessLogic.validateAndUpdateResourceFromCsar(convertResponse.left().value(), modifier, null, null, resourceId);
                Object representation = RepresentationUtils.toRepresentation(updatedResource);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation);
                responseWrapper.setInnerElement(response);
            }
            return responseWrapper.getInnerElement();
        } catch (IOException e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Resource");
            log.debug("update resource failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;

        }
    }

    @GET
    @Path("/resources/csar/{csaruuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create Resource", httpMethod = "POST", notes = "Returns resource created from csar uuid", response = Resource.class)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Resource retrieced"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
    public Response getResourceFromCsar(@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId, @PathParam(value = "csaruuid") String csarUUID) {

        init();

        ServletContext context = request.getSession().getServletContext();

        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug("Start handle request of {}" , url);

        // retrieve user details
        userId = (userId != null) ? userId : request.getHeader(Constants.USER_ID_HEADER);
        User user = new User();
        user.setUserId(userId);

        log.debug("user id is {}", userId);

        Response response;

        try {

            ResourceBusinessLogic businessLogic = getResourceBL(context);

            Either<Resource, ResponseFormat> eitherResource = businessLogic.getLatestResourceFromCsarUuid(csarUUID, user);

            // validate response
            if (eitherResource.isRight()) {
                log.debug("failed to get resource from csarUuid : {}", csarUUID);
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), eitherResource.right().value());
            } else {
                Object representation = RepresentationUtils.toRepresentation(eitherResource.left().value());
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation);
            }

            return response;

        } catch (IOException e) {
            log.debug("get resource by csar failed with exception", e);
            response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            return response;
        }
    }
}

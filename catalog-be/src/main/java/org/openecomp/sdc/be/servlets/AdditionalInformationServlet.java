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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.AdditionalInformationBusinessLogic;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tags({@Tag(name = "SDCE-2 APIs")})
@Servers({@Server(url = "/sdc2/rest")})
@Controller
public class AdditionalInformationServlet extends BeGenericServlet {

    private static final Logger log = Logger.getLogger(AdditionalInformationServlet.class);
    private static final String START_HANDLE_REQUEST_OF = "Start handle request of {}";
    private static final String MODIFIER_ID_IS = "modifier id is {}";
    private static final String FAILED_TO_UPDATE_ADDITIONAL_INFO_PROPERTY = "Failed to update additional information property. Reason - {}";
    private final AdditionalInformationBusinessLogic businessLogic;

    @Inject
    public AdditionalInformationServlet(ComponentsUtils componentsUtils,
                                        AdditionalInformationBusinessLogic businessLogic) {
        super(componentsUtils);
        this.businessLogic = businessLogic;
    }

    /**
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
    @Operation(description = "Create Additional Information Label and Value", method = "POST", summary = "Returns created Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Additional information created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createResourceAdditionalInformationLabel(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "Additional information key value to be created", required = true) String data,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userUserId) {
        return createAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, request, userUserId, data);
    }

    /**
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
    @Operation(description = "Create Additional Information Label and Value", method = "POST", summary = "Returns created Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "201", description = "Additional information created"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response createServiceAdditionalInformationLabel(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "Additional information key value to be created", required = true) String data,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userUserId) {
        return createAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, request, userUserId, data);
    }

    /**
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
    @Operation(description = "Update Additional Information Label and Value", method = "PUT", summary = "Returns updated Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Additional information updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceAdditionalInformationLabel(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId,
        @Parameter(description = "Additional information key value to be created", required = true) String data,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return updateAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId, data);
    }

    /**
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
    @Operation(description = "Update Additional Information Label and Value", method = "PUT", summary = "Returns updated Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Additional information updated"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateServiceAdditionalInformationLabel(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId,
        @Parameter(description = "Additional information key value to be created", required = true) String data,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return updateAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId, data);
    }

    /**
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
    @Operation(description = "Create Additional Information Label and Value", method = "DELETE", summary = "Returns deleted Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Additional information deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response updateResourceAdditionalInformationLabel(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return deleteAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId);
    }

    /**
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
    @Operation(description = "Create Additional Information Label and Value", method = "DELETE", summary = "Returns deleted Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Additional information deleted"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response deleteServiceAdditionalInformationLabel(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return deleteAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId);
    }

    /**
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
    @Operation(description = "Get Additional Information by id", method = "GET", summary = "Returns Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "fetched additional information"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getResourceAdditionalInformationLabel(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, labelId, request, userId);
    }

    /**
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
    @Operation(description = "Get Additional Information by id", method = "GET", summary = "Returns Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "fetched additional information"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getServiceAdditionalInformationLabel(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Parameter(description = "label id", required = true) @PathParam("labelId") final String labelId, @Context final HttpServletRequest request,
        @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, labelId, request, userId);
    }

    /**
     * @param resourceId
     * @param request
     * @param userId
     * @return
     */
    @GET
    @Path("/resources/{resourceId}/additionalinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get all Additional Information under resource", method = "GET", summary = "Returns Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "list of additional information"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllResourceAdditionalInformationLabel(
        @Parameter(description = "resource id to update with new property", required = true) @PathParam("resourceId") final String resourceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getAllAdditionalInformationLabelForComponent(NodeTypeEnum.Resource, resourceId, request, userId);
    }

    /**
     * @param serviceId
     * @param request
     * @param userId
     * @return
     */
    @GET
    @Path("/services/{serviceId}/additionalinfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "Get all Additional Information under service", method = "GET", summary = "Returns Additional Inforamtion property", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "list of additional information"),
        @ApiResponse(responseCode = "403", description = "Restricted operation"),
        @ApiResponse(responseCode = "400", description = "Invalid content / Missing content"),
        @ApiResponse(responseCode = "409", description = "Additional information key already exist")})
    @PermissionAllowed(AafPermission.PermNames.INTERNAL_ALL_VALUE)
    public Response getAllServiceAdditionalInformationLabel(
        @Parameter(description = "service id to update with new property", required = true) @PathParam("serviceId") final String serviceId,
        @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
        return getAllAdditionalInformationLabelForComponent(NodeTypeEnum.Service, serviceId, request, userId);
    }

    /**
     * Create additional information property under given resource/service
     *
     * @param nodeType
     * @param uniqueId
     * @param request
     * @param userId
     * @param data
     * @return
     */
    protected Response createAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, HttpServletRequest request, String userId,
                                                                    String data) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        log.debug("data is {}", data);
        try {
            // convert json to AdditionalInfoParameterInfo
            AdditionalInfoParameterInfo additionalInfoParameterInfo = gson.fromJson(data, AdditionalInfoParameterInfo.class);
            // create the new property
            Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic
                .createAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, userId);
            if (either.isRight()) {
                ResponseFormat responseFormat = either.right().value();
                log.info("Failed to create additional information {}. Reason - {}", additionalInfoParameterInfo, responseFormat);
                return buildErrorResponse(responseFormat);
            }
            AdditionalInfoParameterInfo createdAI = either.left().value();
            log.debug("Additional information {}={} created successfully with id {}", createdAI.getKey(), createdAI.getValue(),
                createdAI.getUniqueId());
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.CREATED);
            return buildOkResponse(responseFormat, createdAI);
        } catch (Exception e) {
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
    protected Response updateAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId,
                                                                    HttpServletRequest request, String userId, String data) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        log.debug("data is {}", data);
        try {
            // convert json to AdditionalInfoParameterInfo
            AdditionalInfoParameterInfo additionalInfoParameterInfo = gson.fromJson(data, AdditionalInfoParameterInfo.class);
            // create the new property
            additionalInfoParameterInfo.setUniqueId(labelId);
            Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic
                .updateAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, userId);
            if (either.isRight()) {
                ResponseFormat responseFormat = either.right().value();
                log.info(FAILED_TO_UPDATE_ADDITIONAL_INFO_PROPERTY, responseFormat);
                return buildErrorResponse(responseFormat);
            }
            AdditionalInfoParameterInfo createdAI = either.left().value();
            log.debug("Additional information {}={} updated successfully with id {}", createdAI.getKey(), createdAI.getValue(),
                createdAI.getUniqueId());
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            return buildOkResponse(responseFormat, createdAI);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Additional Information");
            log.debug("Update additional information failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }

    /**
     * Delete an additional information property by id under given resource/service
     *
     * @param nodeType
     * @param uniqueId
     * @param labelId
     * @param request
     * @param userId
     * @return
     */
    protected Response deleteAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId,
                                                                    HttpServletRequest request, String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
            additionalInfoParameterInfo.setUniqueId(labelId);
            Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic
                .deleteAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, userId);
            if (either.isRight()) {
                ResponseFormat responseFormat = either.right().value();
                log.info(FAILED_TO_UPDATE_ADDITIONAL_INFO_PROPERTY, responseFormat);
                return buildErrorResponse(responseFormat);
            }
            AdditionalInfoParameterInfo createdAI = either.left().value();
            log.debug("Additional information {}={} deleted successfully with id {}", createdAI.getKey(), createdAI.getValue(),
                createdAI.getUniqueId());
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            return buildOkResponse(responseFormat, createdAI);
        } catch (Exception e) {
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
    protected Response getAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, String labelId, HttpServletRequest request,
                                                                 String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            // create the new property
            AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
            additionalInfoParameterInfo.setUniqueId(labelId);
            Either<AdditionalInfoParameterInfo, ResponseFormat> either = businessLogic
                .getAdditionalInformation(nodeType, uniqueId, additionalInfoParameterInfo, userId);
            if (either.isRight()) {
                ResponseFormat responseFormat = either.right().value();
                log.info(FAILED_TO_UPDATE_ADDITIONAL_INFO_PROPERTY, responseFormat);
                return buildErrorResponse(responseFormat);
            }
            AdditionalInfoParameterInfo createdAI = either.left().value();
            log.debug("Additional information {}={} fetched successfully with id {}", createdAI.getKey(), createdAI.getValue(),
                createdAI.getUniqueId());
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            return buildOkResponse(responseFormat, createdAI);
        } catch (Exception e) {
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
    protected Response getAllAdditionalInformationLabelForComponent(NodeTypeEnum nodeType, String uniqueId, HttpServletRequest request,
                                                                    String userId) {
        ServletContext context = request.getSession().getServletContext();
        String url = request.getMethod() + " " + request.getRequestURI();
        log.debug(START_HANDLE_REQUEST_OF, url);
        log.debug(MODIFIER_ID_IS, userId);
        try {
            Either<AdditionalInformationDefinition, ResponseFormat> either = businessLogic.getAllAdditionalInformation(nodeType, uniqueId, userId);
            if (either.isRight()) {
                ResponseFormat responseFormat = either.right().value();
                log.info(FAILED_TO_UPDATE_ADDITIONAL_INFO_PROPERTY, responseFormat);
                return buildErrorResponse(responseFormat);
            }
            AdditionalInformationDefinition additionalInformationDefinition = either.left().value();
            log.debug("All Additional information retrieved for component {} is {}", uniqueId, additionalInformationDefinition);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            return buildOkResponse(responseFormat, additionalInformationDefinition);
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Additional Information");
            log.debug("Get all addiotanl information properties failed with exception", e);
            ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            return buildErrorResponse(responseFormat);
        }
    }
}

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
package org.openecomp.sdc.be.externalapi.servlet;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.aaf.AafPermission;
import org.openecomp.sdc.be.components.impl.aaf.PermissionAllowed;
import org.openecomp.sdc.be.components.impl.artifact.ArtifactOperationInfo;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Controller;

/**
 * This Servlet serves external users operations on artifacts.
 *
 * @author mshitrit
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Tag(name = "SDCE-7 APIs")
@Server(url = "/sdc")
@Controller
public class ArtifactExternalServlet extends AbstractValidationsServlet {

    private static final String FAILED_TO_UPDATE_ARTIFACT = "failed to update artifact";
    private static final String DOUBLE_CURLY_BRACKETS = "{} {}";
    private static final Logger log = Logger.getLogger(ArtifactExternalServlet.class);
    private static String startLog = "Start handle request of ";
    private final ArtifactsBusinessLogic artifactsBusinessLogic;
    @Context
    private HttpServletRequest request;

    @Inject
    public ArtifactExternalServlet(ComponentInstanceBusinessLogic componentInstanceBL,
                                   ComponentsUtils componentsUtils, ServletUtils servletUtils, ResourceImportManager resourceImportManager,
                                   ArtifactsBusinessLogic artifactsBusinessLogic) {
        super(componentInstanceBL, componentsUtils, servletUtils, resourceImportManager);
        this.artifactsBusinessLogic = artifactsBusinessLogic;
    }

    @POST
    @Path("/{assetType}/{uuid}/interfaces/{interfaceUUID}/operations/{operationUUID}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(parameters = {
        @Parameter(required = true, schema = @Schema(implementation = org.openecomp.sdc.be.model.ArtifactDefinition.class))}, description = "uploads of artifact to VF operation workflow", method = "POST", summary = "uploads of artifact to VF operation workflow", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact uploaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Artifact name given in input already exists in the context of the asset - SVC4125"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "400", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "400", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    public Response uploadInterfaceOperationArtifact(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType,
        @Parameter(description = "The value for this header must be the MD5 checksum over the whole json body", required = true) @HeaderParam(value = Constants.MD5_HEADER) String checksum,
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "Asset type") @PathParam("assetType") String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the interface", required = true) @PathParam("interfaceUUID") final String interfaceUUID,
        @Parameter(description = "The uuid of the operation", required = true) @PathParam("operationUUID") final String operationUUID,
        @Parameter(description = "The uuid of the artifact", required = true) @PathParam("artifactUUID") final String artifactUUID,
        @Parameter(hidden = true) String data) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(assetType);
        ArtifactDefinition artifactDefinition = null;
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("updateArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("updateArtifact: Missing USER_ID");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        try {
            if (responseWrapper.isEmpty()) {
                Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsBusinessLogic
                    .updateArtifactOnInterfaceOperationByResourceUUID(data, request, ComponentTypeEnum.findByParamName(assetType), uuid,
                        interfaceUUID, operationUUID, artifactUUID, resourceCommonInfo,
                        new ArtifactOperationInfo(true, false, ArtifactOperationEnum.UPDATE));
                if (uploadArtifactEither.isRight()) {
                    log.debug(FAILED_TO_UPDATE_ARTIFACT);
                    responseFormat = uploadArtifactEither.right().value();
                    responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
                } else {
                    artifactDefinition = uploadArtifactEither.left().value();
                    Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                    Map<String, String> headers = new HashMap<>();
                    headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                    responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                    responseWrapper
                        .setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
                }
            }
        } catch (Exception e) {
            final String message = "failed to update artifact on a resource or service";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API, resourceCommonInfo, request, artifactDefinition,
                    null);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * Uploads an artifact to resource or service
     */
    @POST
    @Path("/{assetType}/{uuid}/artifacts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(parameters = {
        @Parameter(required = true, schema = @Schema(implementation = org.openecomp.sdc.be.model.ArtifactDefinition.class))}, description = "uploads of artifact to a resource or service", method = "POST", summary = "uploads of artifact to a resource or service", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact uploaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Artifact name given in input already exists in the context of the asset - SVC4125"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "400", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "400", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed({AafPermission.PermNames.WRITE_VALUE})
    public Response uploadArtifact(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
        @Parameter(description = "The value for this header must be the MD5 checksum over the whole json body", required = true) @HeaderParam(value = Constants.MD5_HEADER) String checksum,
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(hidden = true) String data) {
        init();
        Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentTypeValue);
        if (componentType == null) {
            log.debug("uploadArtifact: assetType parameter {} is not valid", assetType);
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
        }
        if (responseWrapper.isEmpty()) {
            validateXECOMPInstanceIDHeader(instanceIdHeader, responseWrapper);
        }
        if (responseWrapper.isEmpty()) {
            validateHttpCspUserIdHeader(userId, responseWrapper);
        }
        Response response = null;
        ArtifactDefinition artifactDefinition = null;
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic.uploadArtifactToComponentByUUID(data, request, componentType, uuid, resourceCommonInfo,
                    new ArtifactOperationInfo(true, false, ArtifactOperationEnum.CREATE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.OK));
                response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers);
            }
        } catch (IOException e) {
            final String message = "failed to upload artifact to a resource or service";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
            response = buildErrorResponse(responseWrapper.getInnerElement());
        } catch (ComponentException e) {
            responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(e));
        } finally {
            if (response == null) {
                response = buildErrorResponse(responseWrapper.getInnerElement());
            }
            getComponentsUtils()
                .auditExternalCrudApi(responseWrapper.getInnerElement(), AuditingActionEnum.ARTIFACT_UPLOAD_BY_API, resourceCommonInfo, request,
                    artifactDefinition, null);
        }
        return response;
    }

    /**
     * Uploads an artifact to resource instance
     */
    @POST
    @Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(parameters = {
        @Parameter(required = true, schema = @Schema(implementation = org.openecomp.sdc.be.model.ArtifactDefinition.class), description = "json describe the artifact")}, description = "uploads an artifact to a resource instance", method = "POST", summary = "uploads an artifact to a resource instance", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact uploaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Artifact name given in input already exists in the context of the asset - SVC4125"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "400", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "400", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed(AafPermission.PermNames.WRITE_VALUE)
    public Response uploadArtifactToInstance(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
        @Parameter(description = "The value for this header must be the MD5 checksum over the whole json body", required = true) @HeaderParam(value = Constants.MD5_HEADER) String checksum,
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The component instance name (as publishedin the response of the detailed query)", required = true) @PathParam("resourceInstanceName") final String resourceInstanceName,
        @Parameter(hidden = true) String data) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(resourceInstanceName, componentTypeValue);
        ArtifactDefinition artifactDefinition = null;
        if (componentType == null) {
            log.debug("uploadArtifact: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("uploadArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("uploadArtifact: Missing USER_ID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic.uploadArtifactToRiByUUID(data, request, componentType, uuid, resourceInstanceName,
                    new ArtifactOperationInfo(true, false, ArtifactOperationEnum.CREATE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
            }
        } catch (IOException e) {
            final String message = "failed to upload artifact to a resource instance";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_UPLOAD_BY_API, resourceCommonInfo, request, artifactDefinition,
                    null);
        }
        return responseWrapper.getInnerElement();
    }

    @POST
    @Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(parameters = {
        @Parameter(required = true, schema = @Schema(implementation = org.openecomp.sdc.be.model.ArtifactDefinition.class), description = "json describe the artifact")}, description = "updates an artifact on a resource or service", method = "POST", summary = "uploads of artifact to a resource or service", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact updated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "403", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "409", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed(AafPermission.PermNames.WRITE_VALUE)
    public Response updateArtifact(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
        @Parameter(description = "The value for this header must be the MD5 checksum over the whole json body", required = true) @HeaderParam(value = Constants.MD5_HEADER) String checksum,
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID,
        @Parameter(hidden = true) String data) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentTypeValue);
        if (componentType == null) {
            log.debug("updateArtifact: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("updateArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("updateArtifact: Missing USER_ID");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        ArtifactDefinition artifactDefinition = null;
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic
                    .updateArtifactOnComponentByUUID(data, request, componentType, uuid, artifactUUID, resourceCommonInfo,
                        new ArtifactOperationInfo(true, false, ArtifactOperationEnum.UPDATE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
            }
        } catch (IOException e) {
            final String message = "failed to update artifact on a resource or service";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_UPDATE_BY_API, resourceCommonInfo, request, artifactDefinition,
                    artifactUUID);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * updates an artifact on a resource instance
     */
    @POST
    @Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(parameters = {
        @Parameter(required = true, schema = @Schema(implementation = org.openecomp.sdc.be.model.ArtifactDefinition.class), description = "json describe the artifact")}, description = "updates an artifact on a resource instance", method = "POST", summary = "uploads of artifact to a resource or service", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact updated", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "403", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "409", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed(AafPermission.PermNames.WRITE_VALUE)
    public Response updateArtifactOnResourceInstance(
        @Parameter(description = "Determines the format of the body of the request", required = true) @HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contentType,
        @Parameter(description = "The value for this header must be the MD5 checksum over the whole json body", required = true) @HeaderParam(value = Constants.MD5_HEADER) String checksum,
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID,
        @Parameter(description = "The component instance name (as publishedin the response of the detailed query)", required = true) @PathParam("resourceInstanceName") final String resourceInstanceName,
        @Parameter(hidden = true) String data) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(resourceInstanceName, componentTypeValue);
        if (componentType == null) {
            log.debug("updateArtifactOnResourceInstance: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("updateArtifactOnResourceInstance: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("updateArtifactOnResourceInstance: Missing USER_ID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        ArtifactDefinition artifactDefinition = null;
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic
                    .updateArtifactOnRiByUUID(data, request, componentType, uuid, resourceInstanceName, artifactUUID,
                        new ArtifactOperationInfo(true, false, ArtifactOperationEnum.UPDATE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                responseWrapper.setInnerElement(buildOkResponse(responseFormat, representation, headers));
            }
        } catch (IOException e) {
            final String message = "failed to update artifact on resource instance";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_UPDATE_BY_API, resourceCommonInfo, request, artifactDefinition,
                    artifactUUID);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * deletes an artifact of a resource or service
     */
    @DELETE
    @Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "deletes an artifact of a resource or service", method = "DELETE", summary = "deletes an artifact of a resource or service", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Artifact deleted", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "403", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "409", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed(AafPermission.PermNames.DELETE_VALUE)
    public Response deleteArtifact(
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentTypeValue);
        ArtifactDefinition artifactDefinition = null;
        if (componentType == null) {
            log.debug("deleteArtifact: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("deleteArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("deleteArtifact: Missing USER_ID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic
                    .deleteArtifactOnComponentByUUID(request, componentType, uuid, artifactUUID, resourceCommonInfo,
                        new ArtifactOperationInfo(true, false, ArtifactOperationEnum.DELETE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
            }
        } catch (IOException e) {
            final String message = "failed to delete an artifact of a resource or service";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_DELETE_BY_API, resourceCommonInfo, request, artifactDefinition,
                    artifactUUID);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * deletes an artifact of a resource instance
     */
    @DELETE
    @Path("{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "deletes an artifact of a resource insatnce", method = "DELETE", summary = "deletes an artifact of a resource insatnce", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Artifact deleted", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ArtifactDefinition.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "400", description = "Invalid artifactType was defined as input - SVC4122"),
        @ApiResponse(responseCode = "400", description = "Artifact type (mandatory field) is missing in request - SVC4124"),
        @ApiResponse(responseCode = "400", description = "Invalid MD5 header - SVC4127"),
        @ApiResponse(responseCode = "400", description = "Artifact name is missing in input - SVC4128"),
        @ApiResponse(responseCode = "403", description = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
        @ApiResponse(responseCode = "409", description = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
    @PermissionAllowed(AafPermission.PermNames.DELETE_VALUE)
    public Response deleteArtifactOnResourceInstance(
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID,
        @Parameter(description = "The component instance name (as publishedin the response of the detailed query)", required = true) @PathParam("resourceInstanceName") final String resourceInstanceName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(resourceInstanceName, componentTypeValue);
        if (componentType == null) {
            log.debug("deleteArtifactOnResourceInsatnce: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("deleteArtifactOnResourceInsatnce: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
            log.debug("deleteArtifactOnResourceInsatnce: Missing USER_ID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_USER_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        ArtifactDefinition artifactDefinition = null;
        try {
            if (responseWrapper.isEmpty()) {
                artifactDefinition = artifactsBusinessLogic.deleteArtifactOnRiByUUID(request, componentType, uuid, resourceInstanceName, artifactUUID,
                    new ArtifactOperationInfo(true, false, ArtifactOperationEnum.DELETE));
                Object representation = RepresentationUtils.toRepresentation(artifactDefinition);
                Map<String, String> headers = new HashMap<>();
                headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
                responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
                responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
            }
        } catch (IOException e) {
            final String message = "failed to delete an artifact of a resource instance";
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
            log.debug(message, e);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } finally {
            getComponentsUtils()
                .auditExternalCrudApi(responseFormat, AuditingActionEnum.ARTIFACT_DELETE_BY_API, resourceCommonInfo, request, artifactDefinition,
                    artifactUUID);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * downloads an artifact of a component (either a service or a resource) by artifactUUID
     */
    @GET
    @Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download component artifact", method = "GET", summary = "Returns downloaded artifact", responses = {
        @ApiResponse(responseCode = "200", description = "Artifact downloaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "404", description = "Artifact was not found - SVC4505")})
    @PermissionAllowed(AafPermission.PermNames.DELETE_VALUE)
    public Response downloadComponentArtifact(
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(schema = @Schema(allowableValues = {
            "resources,services"}), description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        if (componentType == null) {
            log.debug("downloadComponentArtifact: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("downloadComponentArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        ResourceCommonInfo resourceCommonInfo = new ResourceCommonInfo(componentTypeValue);
        if (!responseWrapper.isEmpty()) {
            getComponentsUtils().auditExternalDownloadArtifact(responseFormat,
                resourceCommonInfo, new DistributionData(instanceIdHeader, requestURI),
                requestId, artifactUUID, userId);
            return responseWrapper.getInnerElement();
        }
        byte[] value = artifactsBusinessLogic.downloadComponentArtifactByUUIDs(componentType, uuid, artifactUUID, resourceCommonInfo);
        try (InputStream is = new ByteArrayInputStream(value)) {
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByByteArray(value));
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } catch (IOException e) {
            log.debug("close ByteArrayInputStream error");
        } finally {
            getComponentsUtils()
                .auditExternalDownloadArtifact(responseFormat, resourceCommonInfo, new DistributionData(instanceIdHeader, requestURI), requestId,
                    artifactUUID, userId);
        }
        return responseWrapper.getInnerElement();
    }

    /**
     * downloads an artifact of a resource instance of a component (either a service or a resource) by artifactUUID
     */
    @GET
    @Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(description = "Download resource instance artifact", method = "GET", summary = "Returns downloaded artifact", responses = {
        @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))),
        @ApiResponse(responseCode = "200", description = "Artifact downloaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "400", description = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
        @ApiResponse(responseCode = "401", description = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
        @ApiResponse(responseCode = "403", description = "ECOMP component is not authorized - POL5003"),
        @ApiResponse(responseCode = "404", description = "Specified resource is not found - SVC4063"),
        @ApiResponse(responseCode = "405", description = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
        @ApiResponse(responseCode = "500", description = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
        @ApiResponse(responseCode = "404", description = "Artifact was not found - SVC4505")})
    @PermissionAllowed(AafPermission.PermNames.READ_VALUE)
    public Response downloadResourceInstanceArtifact(
        @Parameter(description = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true) @HeaderParam(value = Constants.USER_ID_HEADER) final String userId,
        @Parameter(description = "X-ECOMP-RequestID header", required = false) @HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
        @Parameter(description = "X-ECOMP-InstanceID header", required = true) @HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
        @Parameter(description = "Determines the format of the body of the response", required = false) @HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
        @Parameter(description = "The username and password", required = true) @HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
        @Parameter(description = "The requested asset type", required = true) @PathParam("assetType") final String assetType,
        @Parameter(description = "The uuid of the asset as published in the metadata", required = true) @PathParam("uuid") final String uuid,
        @Parameter(description = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true) @PathParam("artifactUUID") final String artifactUUID,
        @Parameter(description = "The component instance name (as publishedin the response of the detailed query)", required = true) @PathParam("resourceInstanceName") final String resourceInstanceName) {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        ResponseFormat responseFormat = null;
        String requestURI = request.getRequestURI();
        String url = request.getMethod() + " " + requestURI;
        log.debug(DOUBLE_CURLY_BRACKETS, startLog, url);
        ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
        String componentTypeValue = componentType == null ? null : componentType.getValue();
        if (componentType == null) {
            log.debug("downloadResourceInstanceArtifact: assetType parameter {} is not valid", assetType);
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
            log.debug("downloadResourceInstanceArtifact: Missing X-ECOMP-InstanceID header");
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
            responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
        }
        if (!responseWrapper.isEmpty()) {
            getComponentsUtils().auditExternalDownloadArtifact(responseFormat, new ResourceCommonInfo(resourceInstanceName, componentTypeValue),
                new DistributionData(instanceIdHeader, requestURI), requestId, artifactUUID, userId);
            return responseWrapper.getInnerElement();
        }
        byte[] value = artifactsBusinessLogic
            .downloadResourceInstanceArtifactByUUIDs(componentType, uuid, resourceInstanceName, artifactUUID);
        try (InputStream is = new ByteArrayInputStream(value)) {
            Map<String, String> headers = new HashMap<>();
            headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByByteArray(value));
            responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
            responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
        } catch (ComponentException e) {
            responseFormat = getComponentsUtils().getResponseFormat(e);
            throw e;
        } catch (IOException e) {
            log.debug("close ByteArrayInputStream error");
        } finally {
            getComponentsUtils().auditExternalDownloadArtifact(responseFormat, new ResourceCommonInfo(resourceInstanceName, componentTypeValue),
                new DistributionData(instanceIdHeader, requestURI), requestId, artifactUUID, userId);
        }
        return responseWrapper.getInnerElement();
    }
}

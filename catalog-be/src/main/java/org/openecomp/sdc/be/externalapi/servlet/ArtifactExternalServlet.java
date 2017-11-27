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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic.ArtifactOperationEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * This Servlet serves external users operations on artifacts.
 * 
 * @author mshitrit
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Artifact External Servlet", description = "Servlet serves external users operations on artifacts.")
@Singleton
public class ArtifactExternalServlet extends AbstractValidationsServlet {

	@Context
	private HttpServletRequest request;

	private static Logger log = LoggerFactory.getLogger(ArtifactExternalServlet.class.getName());
	
	private static String startLog = "Start handle request of ";

	/**
	 * Uploads an artifact to resource or service
	 * 
	 * @param contenType
	 * @param checksum
	 * @param userId
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param assetType
	 * @param uuid
	 * @param data
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/artifacts")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "uploads of artifact to a resource or service", httpMethod = "POST", notes = "uploads of artifact to a resource or service")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact uploaded", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Artifact name given in input already exists in the context of the asset - SVC4125"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 400, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 400, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.ArtifactDefinition", paramType = "body", value = "json describe the artifact")
	public Response uploadArtifact(
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "The value for this header must be the MD5 checksum over the whole json body", required = true)@HeaderParam(value = Constants.MD5_HEADER) String checksum,
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid, 
			String data) {
		
		init(log);
		
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		
		if (componentType == null) {
			log.debug("uploadArtifact: assetType parameter {} is not valid", assetType);
			responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
		}
		if (responseWrapper.isEmpty()) {
			validateXECOMPInstanceIDHeader(instanceIdHeader, responseWrapper);
		}
		if (responseWrapper.isEmpty() ) {
			validateHttpCspUserIdHeader(userId, responseWrapper);
		}
		Response response = null;
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.uploadArtifactToComponentByUUID(data, request, componentType, uuid,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Create));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to upload artifact");
					responseWrapper.setInnerElement(uploadArtifactEither.right().value());
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.OK));
					response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers);
				}
			} 
			if( response == null ){
				response = buildErrorResponse(responseWrapper.getInnerElement());
			}
			return response;
		} catch (Exception e) {
			final String message = "failed to upload artifact to a resource or service";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		} finally {
			getComponentsUtils().auditExternalUploadArtifact(responseWrapper.getInnerElement(), componentTypeValue, request, additionalParams);
		}
	}

	/**
	 * Uploads an artifact to resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "uploads an artifact to a resource instance", httpMethod = "POST", notes = "uploads an artifact to a resource instance")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact uploaded", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Artifact name given in input already exists in the context of the asset - SVC4125"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 400, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 400, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.ArtifactDefinition", paramType = "body", value = "json describe the artifact")
	public Response uploadArtifactToInstance(
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "The value for this header must be the MD5 checksum over the whole json body", required = true)@HeaderParam(value = Constants.MD5_HEADER) String checksum,
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid, 
			@ApiParam(value = "The component instance name (as publishedin the response of the detailed query)", required = true)@PathParam("resourceInstanceName") final String resourceInstanceName,
			String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
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
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.uploadArtifactToRiByUUID(data, request, componentType, uuid, resourceInstanceName,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Create));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to upload artifact");
					responseFormat = uploadArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} 
		}catch (Exception e) {
			final String message = "failed to upload artifact to a resource instance";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * 
	 * @param contenType
	 * @param checksum
	 * @param userId
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @param data
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "updates an artifact on a resource or service", httpMethod = "POST", notes = "uploads of artifact to a resource or service")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact updated", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 403, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 409, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.ArtifactDefinition", paramType = "body", value = "json describe the artifact")
	public Response updateArtifact(
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "The value for this header must be the MD5 checksum over the whole json body", required = true)@HeaderParam(value = Constants.MD5_HEADER) String checksum,
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID,
			String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.updateArtifactOnComponentByUUID(data, request, componentType, uuid, artifactUUID,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Update));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to update artifact");
					responseFormat = uploadArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			}
		} catch (Exception e) {
			final String message = "failed to update artifact on a resource or service";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * updates an artifact on a resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "updates an artifact on a resource instance", httpMethod = "POST", notes = "uploads of artifact to a resource or service")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact updated", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 403, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 409, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
			@ApiImplicitParam(required = true, dataType = "org.openecomp.sdc.be.model.ArtifactDefinition", paramType = "body", value = "json describe the artifact")
	public Response updateArtifactOnResourceInstance(
			@ApiParam(value = "Determines the format of the body of the request", required = true)@HeaderParam(value = Constants.CONTENT_TYPE_HEADER) String contenType,
			@ApiParam(value = "The value for this header must be the MD5 checksum over the whole json body", required = true)@HeaderParam(value = Constants.MD5_HEADER) String checksum,
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType,
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID,
			@ApiParam(value = "The component instance name (as publishedin the response of the detailed query)", required = true)@PathParam("resourceInstanceName") final String resourceInstanceName,
			String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.updateArtifactOnRiByUUID(data, request, componentType, uuid, resourceInstanceName, artifactUUID,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Update));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to update artifact");
					responseFormat = uploadArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			}
		} catch (Exception e) {
			final String message = "failed to update artifact on resource instance";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * deletes an artifact of a resource or service
	 * 
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @return
	 */
	@DELETE
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "deletes an artifact of a resource or service", httpMethod = "DELETE", notes = "deletes an artifact of a resource or service", response = Response.class)
	/*@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact Deleted"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })*/
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact deleted", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 403, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 409, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
	public Response deleteArtifact(
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.deleteArtifactOnComponentByUUID(request, componentType, uuid, artifactUUID,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Delete));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to delete artifact");
					responseFormat = uploadArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			}
		} catch (Exception e) {
			final String message = "failed to delete an artifact of a resource or service";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * deletes an artifact of a resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @return
	 */
	@DELETE
	@Path("{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "deletes an artifact of a resource insatnce", httpMethod = "DELETE", notes = "deletes an artifact of a resource insatnce", response = Response.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact deleted", response = ArtifactDefinition.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 400, message = "Invalid artifactType was defined as input - SVC4122"),
			@ApiResponse(code = 400, message = "Artifact type (mandatory field) is missing in request - SVC4124"),
			@ApiResponse(code = 400, message = "Invalid MD5 header - SVC4127"),
			@ApiResponse(code = 400, message = "Artifact name is missing in input - SVC4128"),
			@ApiResponse(code = 403, message = "Asset is being edited by different user. Only one user can checkout and edit an asset on given time. The asset will be available for checkout after the other user will checkin the asset - SVC4086"),
			@ApiResponse(code = 409, message = "Restricted Operation – the user provided does not have role of Designer or the asset is being used by another designer - SVC4301")})
	public Response deleteArtifactOnResourceInstance(
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID,
			@ApiParam(value = "The component instance name (as publishedin the response of the detailed query)", required = true)@PathParam("resourceInstanceName") final String resourceInstanceName) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.deleteArtifactOnRiByUUID(request, componentType, uuid, resourceInstanceName, artifactUUID,
						additionalParams, artifactsLogic.new ArtifactOperationInfo(true, false, ArtifactOperationEnum.Delete));
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to delete artifact");
					responseFormat = uploadArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} 
		} catch (Exception e) {
			final String message = "failed to delete an artifact of a resource instance";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * downloads an artifact of a component (either a service or a resource) by artifactUUID
	 * 
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download component artifact", httpMethod = "GET", notes = "Returns downloaded artifact")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact downloaded", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 404, message = "Artifact was not found - SVC4505")})
	public Response downloadComponentArtifact(
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<byte[], ResponseFormat> downloadComponentArtifactEither = artifactsLogic.downloadComponentArtifactByUUIDs(componentType, uuid, artifactUUID, additionalParams);
				if (downloadComponentArtifactEither.isRight()) {
					responseFormat = downloadComponentArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					byte[] value = downloadComponentArtifactEither.left().value();
					InputStream is = new ByteArrayInputStream(value);
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByByteArray(value));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
				}
			}
		} catch (Exception e) {
			final String message = "failed to download an artifact of a resource or service";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * downloads an artifact of a resource instance of a component (either a service or a resource) by artifactUUID
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(nickname = "downloadResourceInstanceArtifactByUUID",value = "Download resource instance artifact by artifact UUID", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Artifact downloaded", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified resource is not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000"),
			@ApiResponse(code = 404, message = "Artifact was not found - SVC4505")})
	public Response downloadResourceInstanceArtifact(
			@ApiParam(value = "The user ID of the DCAE Designer. This user must also have Designer role in SDC", required = true)@HeaderParam(value = Constants.USER_ID_HEADER) final String userId,			
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The uuid of the asset as published in the metadata", required = true)@PathParam("uuid") final String uuid,
			@ApiParam(value = "The uuid of the artifact as published in the asset detailed metadata or in the response of the upload / update operation", required = true)@PathParam("artifactUUID") final String artifactUUID,
			@ApiParam(value = "The component instance name (as publishedin the response of the detailed query)", required = true)@PathParam("resourceInstanceName") final String resourceInstanceName) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("{} {}", startLog, url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		String componentTypeValue = componentType == null ? null : componentType.getValue();
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resourceInstanceName);
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID, artifactUUID);
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
		try {
			if (responseWrapper.isEmpty()) {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<byte[], ResponseFormat> downloadResourceArtifactEither = artifactsLogic.downloadResourceInstanceArtifactByUUIDs(componentType, uuid, resourceInstanceName, artifactUUID, additionalParams);
				if (downloadResourceArtifactEither.isRight()) {
					responseFormat = downloadResourceArtifactEither.right().value();
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					byte[] value = downloadResourceArtifactEither.left().value();
					InputStream is = new ByteArrayInputStream(value);
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5Base64EncodedByByteArray(value));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
				}
			}
		} catch (Exception e) {
			final String message = "failed to download an artifact of a resource instance";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
		} finally {
			getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentTypeValue, request, additionalParams);
		}
		return responseWrapper.getInnerElement();
	}
}

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

package org.openecomp.sdc.be.distribution.servlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

import fj.data.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This Servlet serves external users to download artifacts.
 * 
 * @author tgitelman
 *
 */

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Distribution Catalog Servlet", description = "This Servlet serves external users to download artifacts.")
@Singleton
public class DistributionCatalogServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(DistributionCatalogServlet.class.getName());
	@Context
	private HttpServletRequest request;

	// *******************************************************
	// Download (GET) artifacts
	// **********************************************************/
	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param serviceName
	 * @param serviceVersion
	 * @param artifactName
	 * @return
	 */
	@GET
	@Path("/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download service artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "The artifact is found and streamed.", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified Service is not found - SVC4503"),
			@ApiResponse(code = 404, message = "Specified Service Version is  not  found - SVC4504"),
			@ApiResponse(code = 404, message = "Specified artifact is  not found - SVC4505"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response downloadServiceArtifact(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@PathParam("serviceName") final String serviceName, 
			@PathParam("serviceVersion") final String serviceVersion, 
			@PathParam("artifactName") final String artifactName) {
		
		Response response = null;
		String requestURI = request.getRequestURI();
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);

		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
			log.debug("Missing X-ECOMP-InstanceID header");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ServletContext context = request.getSession().getServletContext();
			ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
			Either<byte[], ResponseFormat> downloadRsrcArtifactEither = artifactsLogic.downloadServiceArtifactByNames(serviceName, serviceVersion, artifactName);
			if (downloadRsrcArtifactEither.isRight()) {
				ResponseFormat responseFormat = downloadRsrcArtifactEither.right().value();
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildErrorResponse(responseFormat);
			} else {
				byte[] value = downloadRsrcArtifactEither.left().value();
				InputStream is = new ByteArrayInputStream(value);

				Map<String, String> headers = new HashMap<>();
				headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildOkResponse(responseFormat, is, headers);
			}
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "download Murano package artifact for service - external API");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download Murano package artifact for service - external API");
			log.debug("download artifact failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}
	
	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param serviceName
	 * @param serviceVersion
	 * @param resourceName
	 * @param resourceVersion
	 * @param artifactName
	 * @return
	 */
	@GET
	@Path("/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download resource artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "The artifact is found and streamed.", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified Service is not found - SVC4503"),
			@ApiResponse(code = 404, message = "Specified Resource Instance  is not found - SVC4526"),
			@ApiResponse(code = 404, message = "Specified Service Version is  not  found - SVC4504"),
			@ApiResponse(code = 404, message = "Specified artifact is  not found - SVC4505"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response downloadResourceArtifact(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@PathParam("serviceName") final String serviceName, 
			@PathParam("serviceVersion") final String serviceVersion, 
			@PathParam("resourceName") final String resourceName,
			@PathParam("resourceVersion") final String resourceVersion, 
			@PathParam("artifactName") final String artifactName) {
		
		Response response = null;
		String requestURI = request.getRequestURI();
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);

		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
			log.debug("Missing X-ECOMP-InstanceID header");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ServletContext context = request.getSession().getServletContext();
			ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
			Either<byte[], ResponseFormat> downloadRsrcArtifactEither = artifactsLogic.downloadRsrcArtifactByNames(serviceName, serviceVersion, resourceName, resourceVersion, artifactName);
			if (downloadRsrcArtifactEither.isRight()) {
				ResponseFormat responseFormat = downloadRsrcArtifactEither.right().value();
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildErrorResponse(responseFormat);
			} else {
				byte[] value = downloadRsrcArtifactEither.left().value();
				// Returning 64-encoded as it was received during upload
				InputStream is = new ByteArrayInputStream(value);
				Map<String, String> headers = new HashMap<>();
				headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildOkResponse(responseFormat, is, headers);
			}
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "download interface artifact for resource - external API");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download interface artifact for resource - external API");
			log.debug("download artifact failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param serviceName
	 * @param serviceVersion
	 * @param resourceInstanceName
	 * @param artifactName
	 * @return
	 */
	@GET
	@Path("/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(nickname = "downloadResourceInstanceArtifactByName",value = "Download resource instance artifact by artifact name", httpMethod = "GET", notes = "Returns downloaded artifact", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "The artifact is found and streamed.", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic  Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Specified Service is not found - SVC4503"),
			@ApiResponse(code = 404, message = "Specified Resource Instance  is not found - SVC4526"),
			@ApiResponse(code = 404, message = "Specified Service Version is  not  found - SVC4504"),
			@ApiResponse(code = 404, message = "Specified artifact is  not found - SVC4505"),
			@ApiResponse(code = 405, message = "Method  Not Allowed: Invalid HTTP method type used (PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem or Cambria Service failure. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response downloadResourceInstanceArtifact(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@PathParam("serviceName") final String serviceName, 
			@PathParam("serviceVersion") final String serviceVersion, 
			@PathParam("resourceInstanceName") final String resourceInstanceName,
			@PathParam("artifactName") final String artifactName) {
		
		Response response = null;
		String requestURI = request.getRequestURI();
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);

		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
			log.debug("Missing X-ECOMP-InstanceID header");
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ServletContext context = request.getSession().getServletContext();
			ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
			Either<byte[], ResponseFormat> downloadRsrcArtifactEither = artifactsLogic.downloadRsrcInstArtifactByNames(serviceName, serviceVersion, resourceInstanceName, artifactName);
			if (downloadRsrcArtifactEither.isRight()) {
				ResponseFormat responseFormat = downloadRsrcArtifactEither.right().value();
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildErrorResponse(responseFormat);
			} else {
				byte[] value = downloadRsrcArtifactEither.left().value();
				// Returning 64-encoded as it was received during upload
				InputStream is = new ByteArrayInputStream(value);
				Map<String, String> headers = new HashMap<>();
				headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(artifactName));
				ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditDistributionDownload(responseFormat, auditingActionEnum, additionalParam);
				response = buildOkResponse(responseFormat, is, headers);
			}
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "download interface artifact for resource - external API");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("download interface artifact for resource - external API");
			log.debug("download artifact failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}
}

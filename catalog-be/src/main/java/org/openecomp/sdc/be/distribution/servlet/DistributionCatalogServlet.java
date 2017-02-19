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
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Singleton
public class DistributionCatalogServlet extends BeGenericServlet {

	private static Logger log = LoggerFactory.getLogger(DistributionCatalogServlet.class.getName());

	// *******************************************************
	// Download (GET) artifacts
	// **********************************************************/

	@GET
	@Path("/services/{serviceName}/{serviceVersion}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download service artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadServiceArtifact(@PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion, @PathParam("artifactName") final String artifactName,
			@Context final HttpServletRequest request) {
		Response response = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
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

	@GET
	@Path("/services/{serviceName}/{serviceVersion}/resources/{resourceName}/{resourceVersion}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download resource artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadResourceArtifact(@PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion, @PathParam("resourceName") final String resourceName,
			@PathParam("resourceVersion") final String resourceVersion, @PathParam("artifactName") final String artifactName, @Context final HttpServletRequest request) {
		Response response = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
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

	// --------------------------------

	@GET
	@Path("/services/{serviceName}/{serviceVersion}/resourceInstances/{resourceInstanceName}/artifacts/{artifactName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download resource artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadResourceInstanceArtifact(@PathParam("serviceName") final String serviceName, @PathParam("serviceVersion") final String serviceVersion, @PathParam("resourceInstanceName") final String resourceInstanceName,
			@PathParam("artifactName") final String artifactName, @Context final HttpServletRequest request) {
		Response response = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
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

	// --------------------------------
}

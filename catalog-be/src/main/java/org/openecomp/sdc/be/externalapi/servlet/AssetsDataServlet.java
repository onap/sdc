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
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Singleton
public class AssetsDataServlet extends BeGenericServlet {

	@Context
	private HttpServletRequest request;

	private AssetMetadataConverter assetMetadataUtils;
	private static Logger log = LoggerFactory.getLogger(AssetsDataServlet.class.getName());

	@GET
	@Path("/{assetType}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch list of assets", httpMethod = "GET", notes = "Returns list of assets", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Assets Fetched"), @ApiResponse(code = 400, message = "Invalid content / Missing content"), @ApiResponse(code = 401, message = "Authorization required"),
			@ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Asset not found") })
	public Response getAssetList(@PathParam("assetType") final String assetType, @QueryParam("category") String category, @QueryParam("subCategory") String subCategory, @QueryParam("distributionStatus") String distributionStatus) {

		Response response = null;
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String query = request.getQueryString();
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		String serverBaseURL = request.getRequestURL().toString();

		AuditingActionEnum auditingActionEnum = query == null ? AuditingActionEnum.GET_ASSET_LIST : AuditingActionEnum.GET_FILTERED_ASSET_LIST;

		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, query == null ? requestURI : requestURI + "?" + query);

		// Mandatory
		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
			log.debug("getAssetList: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ServletContext context = request.getSession().getServletContext();
			ElementBusinessLogic elementLogic = getElementBL(context);

			getAssetUtils(context);
			Map<FilterKeyEnum, String> filters = new HashMap<FilterKeyEnum, String>();

			if (category != null) {
				filters.put(FilterKeyEnum.CATEGORY, category);
			}
			if (subCategory != null) {
				filters.put(FilterKeyEnum.SUB_CATEGORY, subCategory);
			}
			if (distributionStatus != null) {
				filters.put(FilterKeyEnum.DISTRIBUTION_STATUS, distributionStatus);
			}

			Either<List<? extends Component>, ResponseFormat> assetTypeData = elementLogic.getFilteredCatalogComponents(assetType, filters, query);

			if (assetTypeData.isRight()) {
				log.debug("getAssetList: Asset Fetching Failed");
				responseFormat = assetTypeData.right().value();
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
				return buildErrorResponse(responseFormat);
			} else {
				log.debug("getAssetList: Asset Fetching Success");
				Either<List<? extends AssetMetadata>, ResponseFormat> resMetadata = assetMetadataUtils.convertToAssetMetadata(assetTypeData.left().value(), serverBaseURL, false);
				if (resMetadata.isRight()) {
					log.debug("getAssetList: Asset conversion Failed");
					responseFormat = resMetadata.right().value();
					getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
					return buildErrorResponse(responseFormat);
				}
				Object result = RepresentationUtils.toRepresentation(resMetadata.left().value());
				responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);

				response = buildOkResponse(responseFormat, result);
				return response;
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Fetch filtered list of assets");
			log.debug("getAssetList: Fetch list of assets failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	@GET
	@Path("/{assetType}/{uuid}/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch metadata of asset by uuid", httpMethod = "GET", notes = "Returns metadata of asset", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Assets Fetched"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response getAssetListByUuid(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @Context final HttpServletRequest request) {

		Response response = null;
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.GET_ASSET_METADATA;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		String serverBaseURL = request.getRequestURL().toString();

		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType.getValue());
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, uuid);

		// Mandatory
		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
			log.debug("getAssetList: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ServletContext context = request.getSession().getServletContext();
			ElementBusinessLogic elementLogic = getElementBL(context);
			getAssetUtils(context);

			Either<List<? extends Component>, ResponseFormat> assetTypeData = elementLogic.getCatalogComponentsByUuidAndAssetType(assetType, uuid);

			if (assetTypeData.isRight()) {
				log.debug("getAssetList: Asset Fetching Failed");
				responseFormat = assetTypeData.right().value();
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);

				return buildErrorResponse(responseFormat);
			} else {
				log.debug("getAssetList: Asset Fetching Success");
				additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, assetTypeData.left().value().iterator().next().getName());
				Either<List<? extends AssetMetadata>, ResponseFormat> resMetadata = assetMetadataUtils.convertToAssetMetadata(assetTypeData.left().value(), serverBaseURL, true);
				if (resMetadata.isRight()) {
					log.debug("getAssetList: Asset conversion Failed");
					responseFormat = resMetadata.right().value();
					getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
					return buildErrorResponse(responseFormat);
				}
				Object result = RepresentationUtils.toRepresentation(resMetadata.left().value().iterator().next());
				responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);

				response = buildOkResponse(responseFormat, result);
				return response;
			}
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Fetch filtered list of assets");
			log.debug("getAssetList: Fetch list of assets failed with exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
	}

	private void getAssetUtils(ServletContext context) {
		WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
		WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
		assetMetadataUtils = webApplicationContext.getBean(AssetMetadataConverter.class);
	}

	@GET
	@Path("/{assetType}/{uuid}/toscaModel")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Fetch asset csar", httpMethod = "GET", notes = "Returns asset csar", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Asset Model Fetched"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response getToscaModel(@PathParam("uuid") final String uuid,
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("assetType") final String assetType,
			@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization) {

		String url = request.getRequestURI();
		log.debug("Start handle request of {} {}", request.getMethod(), url);
		Response response = null;
		ResponseFormat responseFormat = null;
		ServletContext context = request.getSession().getServletContext();
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.GET_TOSCA_MODEL;
		String userId = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, userId);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, url);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType.getValue());
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, uuid);

		if (userId == null || userId.isEmpty()) {
			log.debug("getToscaModel: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
			return buildErrorResponse(responseFormat);
		}

		try {
			ComponentBusinessLogic componentBL = getComponentBL(componentType, context);

			Either<ImmutablePair<String, byte[]>, ResponseFormat> csarArtifact = componentBL.getToscaModelByComponentUuid(componentType, uuid, additionalParam);
			if (csarArtifact.isRight()) {
				responseFormat = csarArtifact.right().value();
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
				response = buildErrorResponse(responseFormat);
			} else {
				byte[] value = csarArtifact.left().value().getRight();
				InputStream is = new ByteArrayInputStream(value);
				String contenetMD5 = GeneralUtility.calculateMD5ByByteArray(value);
				Map<String, String> headers = new HashMap<>();
				headers.put(Constants.CONTENT_DISPOSITION_HEADER, getContentDispositionValue(csarArtifact.left().value().getLeft()));
				headers.put(Constants.MD5_HEADER, contenetMD5);
				responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
				response = buildOkResponse(responseFormat, is, headers);
			}
			return response;

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get asset tosca model");
			log.debug("falied to get asset tosca model", e);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			response = buildErrorResponse(responseFormat);
			getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
			return response;
		}
	}
}

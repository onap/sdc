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
import java.util.List;
import java.util.Map;

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
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
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
 * This Servlet serves external users for retrieving component metadata.
 * 
 * @author tgitelman
 *
 */

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Asset Metadata External Servlet", description = "This Servlet serves external users for retrieving component metadata.")
@Singleton
public class AssetsDataServlet extends AbstractValidationsServlet {

	@Context
	private HttpServletRequest request;

	private static Logger log = LoggerFactory.getLogger(AssetsDataServlet.class.getName());
	
	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param assetType
	 * @param category
	 * @param subCategory
	 * @param distributionStatus
	 * @param resourceType
	 * @return
	 */
	@GET
	@Path("/{assetType}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch list of assets", httpMethod = "GET", notes = "Returns list of assets")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ECOMP component is authenticated and list of Catalog Assets Metadata is returned", response = AssetMetadata.class, responseContainer="List"),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response getAssetListExternal(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The filter key (resourceType only for resources)", required = false)@QueryParam("category") String category, 
			@ApiParam(value = "The filter key (resourceType only for resources)", required = false)@QueryParam("subCategory") String subCategory, 
			@ApiParam(value = "The filter key (resourceType only for resources)", required = false)@QueryParam("distributionStatus") String distributionStatus,
			@ApiParam(value = "The filter key (resourceType only for resources)", required = false)@QueryParam("resourceType") String resourceType) {

		Response response = null;
		ResponseFormat responseFormat = null;
		String query = request.getQueryString();
		String requestURI = request.getRequestURI().endsWith("/")?
				removeDuplicateSlashSeparator(request.getRequestURI()): request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);

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

			AssetMetadataConverter assetMetadataUtils = getAssetUtils(context);
			Map<FilterKeyEnum, String> filters = new EnumMap<>(FilterKeyEnum.class);

			if (category != null) {
				filters.put(FilterKeyEnum.CATEGORY, category);
			}
			if (subCategory != null) {
				filters.put(FilterKeyEnum.SUB_CATEGORY, subCategory);
			}
			if (distributionStatus != null) {
				filters.put(FilterKeyEnum.DISTRIBUTION_STATUS, distributionStatus);
			}
			if (resourceType != null) {
				ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.getTypeIgnoreCase(resourceType);
				if (resourceTypeEnum == null) {
					log.debug("getAssetList: Asset Fetching Failed. Invalid resource type was received");
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
					getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
					return buildErrorResponse(responseFormat);
				}
				filters.put(FilterKeyEnum.RESOURCE_TYPE, resourceTypeEnum.name());
			}

			Either<List<? extends Component>, ResponseFormat> assetTypeData = elementLogic.getFilteredCatalogComponents(assetType, filters, query);

			if (assetTypeData.isRight()) {
				log.debug("getAssetList: Asset Fetching Failed");
				responseFormat = assetTypeData.right().value();
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);
				return buildErrorResponse(responseFormat);
			} else {
				log.debug("getAssetList: Asset Fetching Success");
				Either<List<? extends AssetMetadata>, ResponseFormat> resMetadata = assetMetadataUtils.convertToAssetMetadata(assetTypeData.left().value(), requestURI, false);
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
	
	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param assetType
	 * @param uuid
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Detailed metadata of asset by uuid", httpMethod = "GET", notes = "Returns detailed metadata of an asset by uuid")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ECOMP component is authenticated and list of Catalog Assets Metadata is returned", response = AssetMetadata.class, responseContainer="List"),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response getAssetSpecificMetadataByUuidExternal(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The requested asset uuid", required = true)@PathParam("uuid") final String uuid) {

		Response response = null;
		ResponseFormat responseFormat = null;
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.GET_ASSET_METADATA;
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);

		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<>(AuditingFieldsKeysEnum.class);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, requestURI);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, uuid);
		if (componentType == null) {
			log.debug("getMetaData: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			return buildErrorResponse(responseFormat);
		}
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType.getValue());

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
			AssetMetadataConverter assetMetadataUtils = getAssetUtils(context);

			Either<List<? extends Component>, ResponseFormat> assetTypeData = elementLogic.getCatalogComponentsByUuidAndAssetType(assetType, uuid);

			if (assetTypeData.isRight()) {
				log.debug("getAssetList: Asset Fetching Failed");
				responseFormat = assetTypeData.right().value();
				getComponentsUtils().auditExternalGetAsset(responseFormat, auditingActionEnum, additionalParam);

				return buildErrorResponse(responseFormat);
			} else {
				log.debug("getAssetList: Asset Fetching Success");
				additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, assetTypeData.left().value().iterator().next().getName());
				Either<List<? extends AssetMetadata>, ResponseFormat> resMetadata = assetMetadataUtils.convertToAssetMetadata(assetTypeData.left().value(), requestURI, true);
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
	
	/**
	 * 
	 * @param requestId
	 * @param instanceIdHeader
	 * @param accept
	 * @param authorization
	 * @param assetType
	 * @param uuid
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/toscaModel")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Fetch assets CSAR", httpMethod = "GET", notes = "Returns asset csar", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "ECOMP component is authenticated and list of Catalog Assets Metadata is returned", response = String.class),
			@ApiResponse(code = 400, message = "Missing  'X-ECOMP-InstanceID'  HTTP header - POL5001"),
			@ApiResponse(code = 401, message = "ECOMP component  should authenticate itself  and  to  re-send  again  HTTP  request  with its Basic Authentication credentials - POL5002"),
			@ApiResponse(code = 403, message = "ECOMP component is not authorized - POL5003"),
			@ApiResponse(code = 404, message = "Error: Requested '%1' (uuid) resource was not found - SVC4063"),
			@ApiResponse(code = 405, message = "Method  Not Allowed  :  Invalid HTTP method type used ( PUT,DELETE,POST will be rejected) - POL4050"),
			@ApiResponse(code = 500, message = "The GET request failed either due to internal SDC problem. ECOMP Component should continue the attempts to get the needed information - POL5000")})
	public Response getToscaModelExternal(
			@ApiParam(value = "X-ECOMP-RequestID header", required = false)@HeaderParam(value = Constants.X_ECOMP_REQUEST_ID_HEADER) String requestId,
			@ApiParam(value = "X-ECOMP-InstanceID header", required = true)@HeaderParam(value = Constants.X_ECOMP_INSTANCE_ID_HEADER) final String instanceIdHeader,
			@ApiParam(value = "Determines the format of the body of the response", required = false)@HeaderParam(value = Constants.ACCEPT_HEADER) String accept,
			@ApiParam(value = "The username and password", required = true)@HeaderParam(value = Constants.AUTHORIZATION_HEADER) String authorization,
			@ApiParam(value = "The requested asset type", required = true, allowableValues = "resources, services")@PathParam("assetType") final String assetType, 
			@ApiParam(value = "The requested asset uuid", required = true)@PathParam("uuid") final String uuid) {

		String url = request.getRequestURI();
		log.debug("Start handle request of {} {}", request.getMethod(), url);
		Response response = null;
		ResponseFormat responseFormat = null;
		ServletContext context = request.getSession().getServletContext();
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		AuditingActionEnum auditingActionEnum = AuditingActionEnum.GET_TOSCA_MODEL;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, instanceIdHeader);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL, url);
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, uuid);
		
		if (componentType == null) {
			log.debug("getToscaModel: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			return buildErrorResponse(responseFormat);
		}
		additionalParam.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE, componentType.getValue());


		if (instanceIdHeader == null || instanceIdHeader.isEmpty()) {
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
				String contenetMD5 = GeneralUtility.calculateMD5Base64EncodedByByteArray(value);
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


	private String removeDuplicateSlashSeparator(String requestUri) {
		return requestUri.substring(0, requestUri.length()-1);
	}


}

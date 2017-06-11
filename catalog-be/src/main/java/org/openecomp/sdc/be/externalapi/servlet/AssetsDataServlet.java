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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.Strings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoBase;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.api.CategoryTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.FilterKeyEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.be.utils.CommonBeUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Singleton
public class AssetsDataServlet extends AbstractValidationsServlet {

	@Context
	private HttpServletRequest request;

	private static Logger log = LoggerFactory.getLogger(AssetsDataServlet.class.getName());

	@GET
	@Path("/{assetType}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Fetch list of assets", httpMethod = "GET", notes = "Returns list of assets", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Assets Fetched"), @ApiResponse(code = 400, message = "Invalid content / Missing content"), @ApiResponse(code = 401, message = "Authorization required"),
			@ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Asset not found") })
	public Response getAssetList(@PathParam("assetType") final String assetType, @QueryParam("category") String category, @QueryParam("subCategory") String subCategory, @QueryParam("distributionStatus") String distributionStatus,
			@QueryParam("resourceType") String resourceType) {

		Response response = null;
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String query = request.getQueryString();
		String requestURI = request.getRequestURI();
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
			if (resourceType != null) {
				ResourceTypeEnum resourceTypeEnum = ResourceTypeEnum.getTypeIgnoreCase(resourceType);
				if( resourceTypeEnum == null ){
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
	
	/**
	 * Creates a new Resource
	 * 
	 * @param assetType
	 * @param data
	 * @return
	 */
	@POST
	@Path("/{assetType}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "creates a resource", httpMethod = "POST", notes = "creates a resource", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact uploaded"),
			@ApiResponse(code = 401, message = "Authorization required"),
			@ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 201, message = "Resource created"), @ApiResponse(code = 400, message = "Invalid content / Missing content"),
			@ApiResponse(code = 409, message = "Resource already exist") })
	public Response createResource(@PathParam("assetType") final String assetType, @ApiParam(value = "json describe the artifact", required = true) String data) {
		init(log);
		
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		String requestURI = request.getRequestURI();
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		Resource resource = null;
		User modifier = null;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		ServletContext context = request.getSession().getServletContext();
		ResourceBusinessLogic resourceBL = getResourceBL(context);
		try {
			// Validate X-ECOMP-InstanceID Header
			if (responseWrapper.isEmpty()) {
				validateXECOMPInstanceIDHeader(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER),
						responseWrapper);
			}
			// Validate USER_ID Header
			if (responseWrapper.isEmpty()) {
				validateHttpCspUserIdHeader(userId, responseWrapper);
			}
			// Validate assetType
			if (responseWrapper.isEmpty()) {
				if( !AssetTypeEnum.RESOURCES.getValue().equals(assetType) ){
					responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
				}
			}
			//Validate resource type
			if(responseWrapper.isEmpty()){
				JSONParser parser = new JSONParser();
				JSONObject jsonObj = (JSONObject) parser.parse(data);
				String resourceType = (String) jsonObj.get(FilterKeyEnum.RESOURCE_TYPE.getName());
				if( StringUtils.isEmpty(resourceType) || !ResourceTypeEnum.containsName(resourceType) ){
					additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, (String) jsonObj.get("name"));
					responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT));
				}
			}
			// Convert the user json to a resource
			if (responseWrapper.isEmpty()) {
				modifier = new User();
				modifier.setUserId(userId);
				Either<Resource, ResponseFormat> eitherResource = getComponentsUtils()
						.convertJsonToObjectUsingObjectMapper(data, modifier, Resource.class,
								null, ComponentTypeEnum.RESOURCE);
				if( eitherResource.isRight() ){
					responseWrapper.setInnerElement(eitherResource.right().value());
				}
				else{
					resource = eitherResource.left().value();
				}

			}
			//validate name exist
			if(responseWrapper.isEmpty()){
				if( Strings.isEmpty(resource.getName())){
					responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
							ActionStatus.MISSING_COMPONENT_NAME, ComponentTypeEnum.RESOURCE.getValue()));

				}
			}
			
			if(responseWrapper.isEmpty()){
				resource.setDerivedFrom(Arrays.asList("tosca.nodes.Root"));
				resource.setSystemName(ValidationUtils.convertToSystemName(resource.getName()));
				resource.setToscaResourceName(CommonBeUtils.generateToscaResourceName(ResourceTypeEnum.VFCMT.name(),
						resource.getSystemName()));
				handleCategories(context, data, resource, responseWrapper);
			}
			// Create the resource in the dataModel
			if (responseWrapper.isEmpty()) {
				Either<Resource, ResponseFormat> eitherCreateResponse = resourceBL.createResource(resource, null,
						modifier, null, null);
				if (eitherCreateResponse.isRight()) {
					responseWrapper.setInnerElement(eitherCreateResponse.right().value());
				} else {
					resource = eitherCreateResponse.left().value();
				}
			}
			Response response;
			//Build Response and store it in the response Wrapper
			if (responseWrapper.isEmpty()) {
				response = buildCreatedResourceResponse(resource, context, responseWrapper);
			}
			else{
				response = buildErrorResponse(responseWrapper.getInnerElement());
			}
			return response;

		} catch (Exception e) {
			final String message = "failed to create vfc monitoring template resource";
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
			log.debug(message, e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		finally{
			prepareAdditionalAudit(resource, additionalParams);
			
			getComponentsUtils().auditExternalCrudApi(responseWrapper.getInnerElement(),
					ComponentTypeEnum.RESOURCE.getValue(), AuditingActionEnum.CREATE_RESOURCE_BY_API.getName(), request,
					additionalParams);
		}
	}

	private void prepareAdditionalAudit(Resource resource, EnumMap<AuditingFieldsKeysEnum, Object> additionalParams) {
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, StringUtils.EMPTY);		
		additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, StringUtils.EMPTY);
		
		if( resource != null ){
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, ImportUtils.Constants.FIRST_NON_CERTIFIED_VERSION);
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.name());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, resource.getName());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, resource.getUUID());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, resource.getInvariantUUID());
		} else {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, StringUtils.EMPTY);
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE, StringUtils.EMPTY);
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, StringUtils.EMPTY);
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, StringUtils.EMPTY);
		}
	}

	private Response buildCreatedResourceResponse(Component resource, ServletContext context,
			Wrapper<ResponseFormat> responseWrapper) throws IOException, JsonGenerationException, JsonMappingException {
		ResponseFormat responseFormat;
		Response response;
		AssetMetadataConverter assetMetadataUtils = getAssetUtils(context);
		Either<? extends AssetMetadata, ResponseFormat> resMetadata = assetMetadataUtils
				.convertToSingleAssetMetadata(resource, request.getRequestURL().toString(),
						true);
		if (resMetadata.isRight()) {
			log.debug("Asset conversion Failed");
			responseFormat = resMetadata.right().value();
			responseWrapper.setInnerElement(responseFormat);
			response = buildErrorResponse(responseFormat);
		}
		else{
			final AssetMetadata assetData = resMetadata.left().value();
			assetData.setToscaModelURL(null);
			
			responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.CREATED));
			Object representation = RepresentationUtils.toRepresentation(assetData);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
			response = buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.CREATED), representation);
		}
		return response;
	}

	private void handleCategories(ServletContext context, String data, Resource resource,
			Wrapper<ResponseFormat> responseWrapper) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObj = (JSONObject) parser.parse(data);
			String category = (String) jsonObj.get(CategoryTypeEnum.CATEGORY.getValue());
			String subcategory = (String) jsonObj.get(CategoryTypeEnum.SUBCATEGORY.getValue());
			if (Strings.isEmpty(category)) {
				responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
						ActionStatus.COMPONENT_MISSING_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
			}
			else if (Strings.isEmpty(subcategory)) {
				responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
						ActionStatus.COMPONENT_MISSING_SUBCATEGORY));
			}
			if (responseWrapper.isEmpty()) {
				ElementBusinessLogic elementLogic = getElementBL(context);
				// get All Categories
				Either<List<CategoryDefinition>, ActionStatus> allResourceCategories = elementLogic
						.getAllResourceCategories();
				// Error fetching categories
				if (allResourceCategories.isRight()) {
					responseWrapper.setInnerElement(
							getComponentsUtils().getResponseFormat(allResourceCategories.right().value()));
				} else {
					addCategories(resource, category, subcategory, allResourceCategories, responseWrapper);
				}
			}
		} catch (Exception e) {
			log.debug("Exception occured in addCategories: {}", e.getMessage(), e);
			responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

	}

	private void addCategories(Resource resource, String category, String subcategory,
			Either<List<CategoryDefinition>, ActionStatus> allResourceCategories,
			Wrapper<ResponseFormat> responseWrapper) {
		Optional<CategoryDefinition> optionalCategory =
				// Stream of all the categories
				allResourceCategories.left().value().stream()
						// filter in only relevant category
						.filter(e -> e.getName().equals(category))
						// get the result
						.findAny();
		if (!optionalCategory.isPresent()) {
			responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
					ActionStatus.COMPONENT_INVALID_CATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
		} else {
			CategoryDefinition categoryDefinition = optionalCategory.get();

			List<SubCategoryDefinition> subCaregories =
					// Stream of all sub-categories of the relevant
					// category
					categoryDefinition.getSubcategories().stream()
							// filter in only relevant sub-category
							.filter(e -> e.getName().equals(subcategory))
							// get the result
							.collect(Collectors.toList());
			
			if( subCaregories.isEmpty() ){
				responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(
						ActionStatus.COMPONENT_INVALID_SUBCATEGORY, ComponentTypeEnum.RESOURCE.getValue()));
			}
			else{
				categoryDefinition.setSubcategories(subCaregories);
				resource.setCategories(Arrays.asList(categoryDefinition));
			}
			
		}
	}

	
	/**
	 * Changing the lifecycle of an asset
	 * @param jsonChangeInfo	The description - request body
	 * @param assetType The requested asset type.Valid values are: resources / services (for VFCMT – use "resources")
	 * @param uuid The uuid of the desired resource to be changed
	 * @param lifecycleTransition The lifecycle operation to be performed on the asset.Valid values are:Checkin / Checkout /  CERTIFICATION_REQUEST
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/lifecycleState/{lifecycleOperation}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Change Resource lifecycle State", httpMethod = "POST", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Resource state changed"), @ApiResponse(code = 403, message = "Asset is already checked-out by another user")})
	public Response changeResourceState(@ApiParam(value = "LifecycleChangeInfo - relevant for checkin", required = false) String jsonChangeInfo,
			@ApiParam(value = "validValues: resources / services ", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam(value = "assetType") final String assetType,
			@ApiParam(value = "id of component to be changed") @PathParam(value = "uuid") final String uuid,
			@ApiParam(allowableValues = "checkout, checkin", required = true) @PathParam(value = "lifecycleOperation") final String lifecycleTransition) {
		Response response = null;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);
		
		init(log);
		
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		
		//get the business logic
		ServletContext context = request.getSession().getServletContext();
		LifecycleBusinessLogic businessLogic = getLifecycleBL(context);		
		
		Wrapper<ResponseFormat> responseWrapper = runValidations(assetType);					
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		Component component = null;
		Component responseObject = null;
		User modifier = null;
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		
		try{
			if (responseWrapper.isEmpty()) {
				//get user
				Either<User, ResponseFormat> eitherGetUser = getUser(request, userId);
				if (eitherGetUser.isRight()) {
					ResponseFormat responseFormat = eitherGetUser.right().value();
					responseWrapper.setInnerElement(responseFormat);
					return buildErrorResponse(responseFormat);
				}
				modifier = eitherGetUser.left().value();
								
				//get the component id from the uuid
				Either<Component, ResponseFormat> latestVersion = businessLogic.getLatestComponentByUuid(componentType, uuid);		
				if (latestVersion.isRight()) {
					ResponseFormat responseFormat = latestVersion.right().value();
					responseWrapper.setInnerElement(responseFormat);
					return buildErrorResponse(responseFormat);
				}
				component = latestVersion.left().value();
				String componentId = component.getUniqueId();
								
				//validate the transition is valid
				Either<LifeCycleTransitionEnum, ResponseFormat> validateEnum = validateTransitionEnum(lifecycleTransition, modifier);
				if (validateEnum.isRight()) {
					ResponseFormat responseFormat = validateEnum.right().value();
					responseWrapper.setInnerElement(responseFormat);
					return buildErrorResponse(responseFormat);
				}
				LifeCycleTransitionEnum transitionEnum = validateEnum.left().value();
				
				//create changeInfo
				LifecycleChangeInfoWithAction changeInfo = new LifecycleChangeInfoWithAction();
				try {
					if (jsonChangeInfo != null && !jsonChangeInfo.isEmpty()) {
						ObjectMapper mapper = new ObjectMapper();
						changeInfo = new LifecycleChangeInfoWithAction(mapper.readValue(jsonChangeInfo, LifecycleChangeInfoBase.class).getUserRemarks());
					}
				}
				catch (Exception e) {
					BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeInvalidJsonInput, "convertJsonToObject");
					BeEcompErrorManager.getInstance().logBeInvalidJsonInput("convertJsonToObject");
					log.debug("failed to convert from json {}", jsonChangeInfo, e);
					ResponseFormat responseFormat = getComponentsUtils().getInvalidContentErrorAndAudit(modifier, AuditingActionEnum.CHECKOUT_RESOURCE);
					responseWrapper.setInnerElement(responseFormat);
					return buildErrorResponse(responseFormat);
				}
				
				//execute business logic
				Either<? extends Component, ResponseFormat> actionResponse = businessLogic.changeComponentState(componentType, componentId, modifier, transitionEnum, changeInfo, false, true);	
				if (actionResponse.isRight()) {
					log.info("failed to change resource state");
					ResponseFormat responseFormat = actionResponse.right().value();
					responseWrapper.setInnerElement(responseFormat);
					return buildErrorResponse(responseFormat);					
				}
	  
				log.debug("change state successful !!!");
				responseObject = actionResponse.left().value();
				response = buildCreatedResourceResponse(responseObject, context, responseWrapper);				
			} else {
				response = buildErrorResponse(responseWrapper.getInnerElement());
			}
			
			return response;
		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Change Lifecycle State");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Change Lifecycle State");
			log.debug("change lifecycle state failed with exception", e);
			ResponseFormat responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR);
			responseWrapper.setInnerElement(responseFormat);
			return buildErrorResponse(responseFormat);			
		} finally{
			auditChnageLifecycleAction(additionalParams, responseWrapper, componentType, component, responseObject, modifier, userId);
		}
	}

	private void auditChnageLifecycleAction(EnumMap<AuditingFieldsKeysEnum, Object> additionalParams,
			Wrapper<ResponseFormat> responseWrapper, ComponentTypeEnum componentType, Component component,
			Component responseObject, User modifier, String userId) {
		if (modifier!=null){
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, modifier.getFullName());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier.getUserId());
		} else {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, "");
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, userId);
		}
		
		if (component!=null){
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, component.getName());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, component.getVersion());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, component.getLifecycleState().name());
		} else {
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME, "");
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION, "");
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE, "");
		}
		
		if (responseObject!=null){
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, responseObject.getVersion());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, responseObject.getUUID());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, responseObject.getInvariantUUID());
			additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,responseObject.getLifecycleState().name());
		} else {
			if (component!=null){
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, component.getVersion());
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, component.getUUID());
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, component.getInvariantUUID());
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,component.getLifecycleState().name());
			} else {
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION, "");
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID, "");
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID, "");
				additionalParams.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,"");
			}
		}
		
		getComponentsUtils().auditExternalCrudApi(responseWrapper.getInnerElement(),
				componentType.getValue(), AuditingActionEnum.CHANGE_LIFECYCLE_BY_API.getName(), request,
				additionalParams);
	}

	private Wrapper<ResponseFormat> runValidations(final String assetType) {
		Wrapper<ResponseFormat> responseWrapper = new Wrapper<>();
		
		// Validate X-ECOMP-InstanceID Header
		if (responseWrapper.isEmpty()) {
			String instanceId = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
			validateXECOMPInstanceIDHeader(instanceId,responseWrapper);			
		}
		// Validate USER_ID Header
		if (responseWrapper.isEmpty()) {
			validateHttpCspUserIdHeader(request.getHeader(Constants.USER_ID_HEADER),responseWrapper);
		}
		// Validate assetType
		if (responseWrapper.isEmpty()) {
			if( !AssetTypeEnum.RESOURCES.getValue().equals(assetType) &&  !AssetTypeEnum.SERVICES.getValue().equals(assetType)){
				responseWrapper.setInnerElement(getComponentsUtils().getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
			}
		}
		
		return responseWrapper;
	}
	
	
	private Either<LifeCycleTransitionEnum, ResponseFormat> validateTransitionEnum(final String lifecycleTransition, User user) {
		LifeCycleTransitionEnum transitionEnum = LifeCycleTransitionEnum.CHECKOUT;
		try {
			transitionEnum = LifeCycleTransitionEnum.getFromDisplayName(lifecycleTransition);
		} catch (IllegalArgumentException e) {
			log.info("state operation is not valid. operations allowed are: {}", LifeCycleTransitionEnum.valuesAsString());
			ResponseFormat error = getComponentsUtils().getInvalidContentErrorAndAudit(user, AuditingActionEnum.CHECKOUT_RESOURCE);
			return Either.right(error);
		}
		return Either.left(transitionEnum);
	}
	
}

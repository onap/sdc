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

package org.openecomp.sdc.ci.tests.utils.rest;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.RelationshipInstData;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ResourceRestUtils extends BaseRestUtils {
	private static Logger logger = LoggerFactory.getLogger(ResourceRestUtils.class.getName());

	// ****** CREATE *******

	public static RestResponse createResource(ResourceReqDetails resourceDetails, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = sdncModifierDetails.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(userBodyJson);
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), calculateMD5);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse createResourceResponse = http.httpSendPost(url, userBodyJson, headersMap);
		if (createResourceResponse.getErrorCode() == STATUS_CODE_CREATED) {
			resourceDetails.setUUID(ResponseParser.getUuidFromResponse(createResourceResponse));
			resourceDetails.setVersion(ResponseParser.getVersionFromResponse(createResourceResponse));
			resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(createResourceResponse));
			String lastUpdaterUserId = ResponseParser.getValueFromJsonResponse(createResourceResponse.getResponse(),
					"lastUpdaterUserId");
			resourceDetails.setLastUpdaterUserId(lastUpdaterUserId);
			String lastUpdaterFullName = ResponseParser.getValueFromJsonResponse(createResourceResponse.getResponse(),
					"lastUpdaterFullName");
			resourceDetails.setLastUpdaterFullName(lastUpdaterFullName);
			// Creator details never change after component is created - Ella,
			// 12/1/2016
			resourceDetails.setCreatorUserId(userId);
			resourceDetails.setCreatorFullName(sdncModifierDetails.getFullName());
		}
		return createResourceResponse;

	}

	public static RestResponse createImportResource(ImportReqDetails importReqDetails, User sdncModifierDetails,
			Map<String, String> additionalHeaders) throws JSONException, IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());
		String userId = sdncModifierDetails.getUserId();

		Gson gson = new Gson();
		String resourceImportBodyJson = gson.toJson(importReqDetails);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(resourceImportBodyJson);

		Map<String, String> headersMap = prepareHeadersMap(userId);
		if (additionalHeaders != null) {
			headersMap.putAll(additionalHeaders);
		} else {
			headersMap.put(HttpHeaderEnum.Content_MD5.getValue(),
					ArtifactRestUtils.calculateMD5(resourceImportBodyJson));
		}

		RestResponse createResourceResponse = http.httpSendPost(url, resourceImportBodyJson, headersMap);
		if (createResourceResponse.getErrorCode() == STATUS_CODE_CREATED) {
			importReqDetails.setVersion(ResponseParser.getVersionFromResponse(createResourceResponse));
			importReqDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(createResourceResponse));
			// Creator details never change after component is created - Ella,
			// 12/1/2016
			importReqDetails.setCreatorUserId(userId);
			importReqDetails.setCreatorFullName(sdncModifierDetails.getFullName());
			importReqDetails
					.setToscaResourceName(ResponseParser.getToscaResourceNameFromResponse(createResourceResponse));
			importReqDetails.setDerivedList(ResponseParser.getDerivedListFromJson(createResourceResponse));
		}
		return createResourceResponse;

	}

	// ***** DELETE ****
	public static RestResponse deleteResource(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			String version) throws IOException {

		if (resourceDetails.getUniqueId() != null) {
			Config config = Utils.getConfig();
			String url = String.format(Urls.DELETE_RESOURCE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
					config.getCatalogBePort(), resourceDetails.getName(), version);
			return sendDelete(url, sdncModifierDetails.getUserId());
		} else {
			return null;
		}

	}

	public static RestResponse markResourceToDelete(String resourceId, String userId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId);
		RestResponse sendDelete = sendDelete(url, userId);

		return sendDelete;

	}

	public static RestResponse deleteResource(String resourceId, String userId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId);
		RestResponse sendDelete = sendDelete(url, userId);

		deleteMarkedResources(userId);

		return sendDelete;

	}

	public static void deleteMarkedResources(String userId) throws IOException {
		String url;
		Config config = Utils.getConfig();
		url = String.format(Urls.DELETE_MARKED_RESOURCES, config.getCatalogBeHost(), config.getCatalogBePort());
		sendDelete(url, userId);
	}

	public static RestResponse deleteResourceByNameAndVersion(User sdncModifierDetails, String resourceName,
			String resourceVersion) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_RESOURCE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), resourceName, resourceVersion);
		RestResponse sendDelete = sendDelete(url, sdncModifierDetails.getUserId());

		deleteMarkedResources(sdncModifierDetails.getUserId());

		return sendDelete;
	}

	public static Boolean deleteResourceByNameAndVersion(String resourceName, String resourceVersion)
			throws IOException {
		RestResponse deleteResponse = ResourceRestUtils.deleteResourceByNameAndVersion(
				ElementFactory.getDefaultUser(UserRoleEnum.ADMIN), resourceName, resourceVersion);
		return checkErrorCode(deleteResponse);
	}

	public static Boolean removeResource(String resourceId)
			throws FileNotFoundException, IOException, ClientProtocolException {
		RestResponse response = deleteResource(resourceId,
				ElementFactory.getDefaultUser(UserRoleEnum.ADMIN).getUserId());
		return checkErrorCode(response);
	}

	// ************** GET *************
	public static RestResponse getResource(User sdncModifierDetails, String uniqueId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), uniqueId);
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static RestResponse getModule(User sdncModifierDetails, String componentId, String moduleId)
			throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_MODULE_BY_ID, config.getCatalogBeHost(), config.getCatalogBePort(),
				componentId, moduleId);
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static RestResponse getLatestResourceFromCsarUuid(User sdncModifierDetails, String csarUuid)
			throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_RESOURCE_BY_CSAR_UUID, config.getCatalogBeHost(), config.getCatalogBePort(),
				csarUuid);
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static RestResponse getResource(ResourceReqDetails resourceDetails, User sdncModifierDetails)
			throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceDetails.getUniqueId());
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static RestResponse getResourceByNameAndVersion(String userId, String resourceName, String resourceVersion)
			throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_RESOURCE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), resourceName, resourceVersion);

		return sendGet(url, userId);
	}

	public static RestResponse getResourceList(User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FOLLWED_LIST, config.getCatalogBeHost(), config.getCatalogBePort());

		return sendGet(url, sdncModifierDetails.getUserId());

	}
	
	public static RestResponse getResourceListFilterByCategory(User sdncModifierDetails, String componentType, String category) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FILTERED_ASSET_LIST, config.getCatalogBeHost(), config.getCatalogBePort(), componentType, "category=" + category);
		
		Map<String, String> headersMap =  prepareHeadersMap(sdncModifierDetails.getUserId());
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		return sendGet(url, sdncModifierDetails.getUserId(), headersMap);

	}
	
	public static RestResponse getResourceListFilterBySubCategory(User sdncModifierDetails, String componentType, String subcategory) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FILTERED_ASSET_LIST, config.getCatalogBeHost(), config.getCatalogBePort(), componentType, "subCategory=" + subcategory);
		
		Map<String, String> headersMap =  prepareHeadersMap(sdncModifierDetails.getUserId());
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		return sendGet(url, sdncModifierDetails.getUserId(), headersMap);

	}
	
	public static RestResponse getResourceListFilterByCriteria(User sdncModifierDetails, String componentType, String criteria, String value) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FILTERED_ASSET_LIST, config.getCatalogBeHost(), config.getCatalogBePort(), componentType, criteria + "=" + value);
		
		Map<String, String> headersMap =  prepareHeadersMap(sdncModifierDetails.getUserId());
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		return sendGet(url, sdncModifierDetails.getUserId(), headersMap);

	}

	public static RestResponse getResource(String resourceId) throws ClientProtocolException, IOException {
		return getResource(ElementFactory.getDefaultUser(UserRoleEnum.ADMIN), resourceId);
	}

	public static RestResponse getLatestResourceFromCsarUuid(String csarUuid)
			throws ClientProtocolException, IOException {
		return getLatestResourceFromCsarUuid(ElementFactory.getDefaultUser(UserRoleEnum.ADMIN), csarUuid);
	}

	public static RestResponse getResourceLatestVersionList(User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_RESOURCE_lATEST_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort());

		return sendGet(url, sdncModifierDetails.getUserId());

	}

	public static RestResponse putAllCategoriesTowardsCatalogFeWithUuidNotAllowed(String uuid) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES_FE, config.getCatalogFeHost(), config.getCatalogFePort(),
				BaseRestUtils.RESOURCE_COMPONENT_TYPE);

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(), uuid);
		HttpRequest http = new HttpRequest();

		logger.debug("Send PUT request to get all categories (should be 405): {}", url);
		return http.httpSendByMethod(url, "PUT", null, headersMap);
	}

	public static RestResponse getAllTagsTowardsCatalogBe() throws IOException {

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ALL_TAGS, config.getCatalogBeHost(), config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		
		return http.httpSendGet(url, headersMap);

	}

	public static RestResponse getAllPropertyScopesTowardsCatalogBe() throws IOException {

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_PROPERTY_SCOPES_LIST, config.getCatalogBeHost(), config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), "cs0008");

		return http.httpSendGet(url, headersMap);

	}

	public static RestResponse getAllArtifactTypesTowardsCatalogBe() throws IOException {

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ALL_ARTIFACTS, config.getCatalogBeHost(), config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<String, String>();

		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), "cs0008");

		return http.httpSendGet(url, headersMap);

	}

	public static RestResponse getConfigurationTowardsCatalogBe() throws IOException {

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_CONFIGURATION, config.getCatalogBeHost(), config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), "cs0008");

		return http.httpSendGet(url, headersMap);

	}

	public static RestResponse sendOptionsTowardsCatalogFeWithUuid() throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_ALL_CATEGORIES_FE, config.getCatalogFeHost(), config.getCatalogFePort(),
				BaseRestUtils.RESOURCE_COMPONENT_TYPE);

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		HttpRequest http = new HttpRequest();

		logger.debug("Send OPTIONS request for categories: {}", url);
		return http.httpSendByMethod(url, "OPTIONS", null, headersMap);
	}

	// ********** UPDATE *************
	public static RestResponse updateResourceMetadata(ResourceReqDetails updatedResourceDetails,
			User sdncModifierDetails, String uniqueId, String encoding) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				uniqueId);

		String ContentTypeString = String.format("%s;%s", contentTypeHeaderData, encoding);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(updatedResourceDetails);
		String userId = sdncModifierDetails.getUserId();

		RestResponse updateResourceResponse = sendPut(url, userBodyJson, userId, ContentTypeString);

		updatedResourceDetails.setVersion(ResponseParser.getVersionFromResponse(updateResourceResponse));
		updatedResourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(updateResourceResponse));

		return updateResourceResponse;
	}

	public static RestResponse updateResourceTEST(Resource resource, User sdncModifierDetails, String uniqueId,
			String encoding) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				uniqueId);

		String ContentTypeString = String.format("%s;%s", contentTypeHeaderData, encoding);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resource);
		String userId = sdncModifierDetails.getUserId();

		RestResponse updateResourceResponse = sendPut(url, userBodyJson, userId, ContentTypeString);

		// String resourceUniqueId =
		// ResponseParser.getValueFromJsonResponse(updateResourceResponse.getResponse(),
		// "uniqueId");
		// updatedResourceDetails.setUniqueId(resourceUniqueId);
		// String resourceVersion =
		// ResponseParser.getValueFromJsonResponse(updateResourceResponse.getResponse(),
		// "version");
		// updatedResourceDetails.setUniqueId(resourceVersion);

		return updateResourceResponse;
	}

	public static RestResponse updateResourceMetadata(ResourceReqDetails updatedResourceDetails,
			User sdncModifierDetails, String uniqueId) throws Exception {
		return updateResourceMetadata(updatedResourceDetails, sdncModifierDetails, uniqueId, "");
	}

	public static RestResponse updateResourceMetadata(String json, User sdncModifierDetails, String resourceId)
			throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId);
		String userId = sdncModifierDetails.getUserId();

		RestResponse updateResourceResponse = sendPut(url, json, userId, contentTypeHeaderData);

		return updateResourceResponse;
	}

	public static RestResponse updateResource(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			String resourceId) throws IOException {

		String userId = sdncModifierDetails.getUserId();
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId);

		Map<String, String> headersMap = prepareHeadersMap(userId);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(userBodyJson);
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), calculateMD5);
		HttpRequest http = new HttpRequest();
		RestResponse updateResourceResponse = http.httpSendPut(url, userBodyJson, headersMap);
		if (updateResourceResponse.getErrorCode() == STATUS_CODE_UPDATE_SUCCESS) {
			resourceDetails.setUUID(ResponseParser.getUuidFromResponse(updateResourceResponse));
			resourceDetails.setVersion(ResponseParser.getVersionFromResponse(updateResourceResponse));
			resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(updateResourceResponse));
			String lastUpdaterUserId = ResponseParser.getValueFromJsonResponse(updateResourceResponse.getResponse(),
					"lastUpdaterUserId");
			resourceDetails.setLastUpdaterUserId(lastUpdaterUserId);
			String lastUpdaterFullName = ResponseParser.getValueFromJsonResponse(updateResourceResponse.getResponse(),
					"lastUpdaterFullName");
			resourceDetails.setLastUpdaterFullName(lastUpdaterFullName);
			resourceDetails.setCreatorUserId(userId);
			resourceDetails.setCreatorFullName(sdncModifierDetails.getFullName());
		}
		return updateResourceResponse;
	}

	public static RestResponse createResourceInstance(ResourceReqDetails resourceDetails, User modifier,
			String vfResourceUniqueId) throws Exception {
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, modifier, vfResourceUniqueId, ComponentTypeEnum.RESOURCE);
		ResourceRestUtils.checkCreateResponse(createResourceInstanceResponse);
		return createResourceInstanceResponse;
	}

	public static RestResponse associateResourceInstances(JSONObject body, User sdncModifierDetails,
			Component component) throws IOException {

		Config config = Utils.getConfig();
		Gson gson = new Gson();
		String bodyJson = gson.toJson(body);
		component.getComponentType();
		String componentType = ComponentTypeEnum.findParamByType(component.getComponentType());
		String url = String.format(Urls.ASSOCIATE__RESOURCE_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), componentType, component.getUniqueId());
		return sendPost(url, bodyJson, sdncModifierDetails.getUserId(), null);

	}

	public static RestResponse getFollowedList(User sdncModifierDetails) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_FOLLWED_LIST, config.getCatalogBeHost(), config.getCatalogBePort());
		return sendGet(url, sdncModifierDetails.getUserId());
	}

	public static List<Resource> restResponseToResourceObjectList(String restResponse) {
		JsonElement jelement = new JsonParser().parse(restResponse);
		JsonArray jsonArray = jelement.getAsJsonArray();
		List<Resource> restResponseArray = new ArrayList<>();
		Resource resource = null;
		for (int i = 0; i < jsonArray.size(); i++) {
			String resourceString = (String) jsonArray.get(i).toString();
			resource = ResponseParser.convertResourceResponseToJavaObject(resourceString);
			restResponseArray.add(resource);
		}

		return restResponseArray;

	}

	public static Resource getResourceObjectFromResourceListByUid(List<Resource> resourceList, String uid) {
		if (resourceList != null && resourceList.size() > 0) {
			for (Resource resource : resourceList) {
				if (resource.getUniqueId().equals(uid))
					return resource;
			}
		} else
			return null;
		return null;
	}

	// =======================================resource
	// associate==================================================
	public static RestResponse associate2ResourceInstances(Component container, ComponentInstance fromNode,
			ComponentInstance toNode, String assocType, User sdncUserDetails) throws IOException {
		return associate2ResourceInstances(container, fromNode.getUniqueId(), toNode.getUniqueId(), assocType,
				sdncUserDetails);
	}

	public static RestResponse associate2ResourceInstances(Component component, String fromNode, String toNode,
			String assocType, User sdncUserDetails) throws IOException {

		RelationshipInstData relationshipInstData = new RelationshipInstData();
		Map<String, List<CapabilityDefinition>> capabilitiesMap = component.getCapabilities();
		Map<String, List<RequirementDefinition>> requirementMap = component.getRequirements();
		List<CapabilityDefinition> capabilitiesList = capabilitiesMap.get(assocType);
		List<RequirementDefinition> requirementList = requirementMap.get(assocType);

		RequirementDefinition requirementDefinitionFrom = getRequirementDefinitionByOwnerId(requirementList, fromNode);
		CapabilityDefinition capabilityDefinitionTo = getCapabilityDefinitionByOwnerId(capabilitiesList, toNode);
		relationshipInstData.setCapabilityOwnerId(capabilityDefinitionTo.getOwnerId());
		relationshipInstData.setCapabiltyId(capabilityDefinitionTo.getUniqueId());
		relationshipInstData.setRequirementOwnerId(requirementDefinitionFrom.getOwnerId());
		relationshipInstData.setRequirementId(requirementDefinitionFrom.getUniqueId());

		JSONObject assocBody = assocBuilder(relationshipInstData, capabilityDefinitionTo, requirementDefinitionFrom,
				toNode, fromNode);
		return ResourceRestUtils.associateResourceInstances(assocBody, sdncUserDetails, component);

	}

	private static JSONObject assocBuilder(RelationshipInstData relationshipInstData,
			CapabilityDefinition capabilityDefinitionTo, RequirementDefinition requirementDefinitionFrom, String toNode,
			String fromNode) {

		String type = capabilityDefinitionTo.getType();
		String requirement = requirementDefinitionFrom.getName();
		String capability = requirementDefinitionFrom.getName();

		JSONObject wrapper = new JSONObject();
		JSONArray relationshipsArray = new JSONArray();
		JSONObject relationship = new JSONObject();
		JSONObject simpleObject = new JSONObject();

		relationship.put("type", type);
		simpleObject.put("relationship", relationship);
		simpleObject.put("requirement", requirement);
		simpleObject.put("capability", capability);
		simpleObject.put("capabilityUid", relationshipInstData.getCapabiltyId());
		simpleObject.put("capabilityOwnerId", relationshipInstData.getCapabilityOwnerId());
		simpleObject.put("requirementOwnerId", relationshipInstData.getRequirementOwnerId());
		simpleObject.put("requirementUid", relationshipInstData.getRequirementId());
		relationshipsArray.add(simpleObject);

		ArrayList<Object> relationships = new ArrayList<Object>(relationshipsArray);
		wrapper.put("fromNode", fromNode);
		wrapper.put("toNode", toNode);
		wrapper.put("relationships", relationships);
		return wrapper;

	}

	private static CapabilityDefinition getCapabilityDefinitionByOwnerId(
			List<CapabilityDefinition> capabilityDefinitionList, String ownerId) {

		for (CapabilityDefinition capabilityDefinition : capabilityDefinitionList) {
			if (capabilityDefinition.getOwnerId().equals(ownerId)) {
				return capabilityDefinition;
			}
		}
		return null;
	}

	private static RequirementDefinition getRequirementDefinitionByOwnerId(
			List<RequirementDefinition> requirementDefinitionList, String ownerId) {

		for (RequirementDefinition requirementDefinition : requirementDefinitionList) {
			if (requirementDefinition.getOwnerId().equals(ownerId)) {
				return requirementDefinition;
			}
		}
		return null;
	}

	public static String getRiUniqueIdByRiName(Component component, String resourceInstanceName) {

		List<ComponentInstance> componentInstances = component.getComponentInstances();
		String name = null;
		for (ComponentInstance componentInstance : componentInstances) {
			if (componentInstance.getName().equals(resourceInstanceName)) {
				name = componentInstance.getUniqueId();
				break;
			}
		}
		return name;
	}

	public static Resource convertResourceGetResponseToJavaObject(ResourceReqDetails resourceDetails)
			throws IOException {
		RestResponse response = ResourceRestUtils.getResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertEquals("Check response code after get resource", 200, response.getErrorCode().intValue());
		return ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
	}

	public static RestResponse changeResourceInstanceVersion(String containerUniqueId, String instanceToReplaceUniqueId,
			String newResourceUniqueId, User sdncModifierDetails, ComponentTypeEnum componentType) throws IOException {
		return ProductRestUtils.changeServiceInstanceVersion(containerUniqueId, instanceToReplaceUniqueId,
				newResourceUniqueId, sdncModifierDetails, componentType);
	}

}

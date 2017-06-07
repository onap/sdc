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

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

import com.google.gson.Gson;

public class ComponentInstanceRestUtils extends BaseRestUtils {
	public static String acceptHeaderDate = "application/json";
	static Config config = Config.instance();
	public static Gson gson = new Gson();

	// 'componentType' can be 'services' or 'resources'

	public static RestResponse createComponentInstance(ComponentInstanceReqDetails componentInstanceReqDetails,
			User sdncModifierDetails, Component component) throws Exception {
		return createComponentInstance(componentInstanceReqDetails, sdncModifierDetails, component.getUniqueId(),
				component.getComponentType());
	}

	public static RestResponse createComponentInstance(ComponentInstanceReqDetails componentInstanceReqDetails,
			User sdncModifierDetails, String componentId, ComponentTypeEnum componentType) throws Exception {

		return createComponentInstance(componentInstanceReqDetails, sdncModifierDetails, componentId,
				ComponentTypeEnum.findParamByType(componentType));
	}

	public static RestResponse createComponentInstance(ComponentInstanceReqDetails componentInstanceReqDetails,
			User sdncModifierDetails, String componentId, String componentType) throws Exception {
		Config config = Utils.getConfig();
		String userId = sdncModifierDetails.getUserId();
		String serviceBodyJson = gson.toJson(componentInstanceReqDetails);
		String url = String.format(Urls.CREATE_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				componentType, componentId);
		RestResponse createResourceInstance = sendPost(url, serviceBodyJson, userId, acceptHeaderData);
		if (createResourceInstance.getErrorCode().equals(BaseRestUtils.STATUS_CODE_CREATED)) {
			String uniqueId = ResponseParser.getValueFromJsonResponse(createResourceInstance.getResponse(), "uniqueId");
			componentInstanceReqDetails.setUniqueId(uniqueId);
			// Gson gson = new Gson();
			// ResourceInstanceReqDetails fromJson =
			// gson.fromJson(createResourceInstance.getResponse(),
			// ResourceInstanceReqDetails.class);
			// componentInstanceReqDetails.setUniqueId(fromJson.getUniqueId());
		}
		return createResourceInstance;
	}

	public static RestResponse getComponentInstances(ComponentTypeEnum type, String componentId, User user)
			throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), user.getUserId());

		String url = String.format(Urls.GET_COMPONENT_INSTANCES, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(type), componentId);

		RestResponse sendGetServerRequest = sendGet(url, user.getUserId(), headersMap);

		return sendGetServerRequest;

	}
	
	public static RestResponse getComponentInstancePropertiesByID(ComponentTypeEnum type, String componentId, String resourceInstanceId, User user)
			throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), user.getUserId());

		String url = String.format(Urls.GET_COMPONENT_INSTANCE_PROPERTIES_BY_ID, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(type), componentId, resourceInstanceId);

		return sendGet(url, user.getUserId(), headersMap);

	}
	
	public static RestResponse deleteComponentInstance(User sdncModifierDetails, String componentId,
			String resourceInstanceId, ComponentTypeEnum componentType) throws Exception {

		return deleteComponentInstance(sdncModifierDetails, componentId, resourceInstanceId,
				ComponentTypeEnum.findParamByType(componentType));
	}

	public static RestResponse deleteComponentInstance(User sdncModifierDetails, String componentId,
			String resourceInstanceId, String componentTypeString) throws Exception {
		Config config = Utils.getConfig();
		String userId = sdncModifierDetails.getUserId();
		String url = String.format(Urls.DELETE_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				componentTypeString, componentId, resourceInstanceId);
		RestResponse sendCreateUserRequest = sendDelete(url, userId);
		return sendCreateUserRequest;
	}

	public static RestResponse updateComponentInstance(ComponentInstanceReqDetails componentInstanceReqDetails,
			User sdncModifierDetails, String componentId, ComponentTypeEnum componentType) throws IOException {

		Config config = Utils.getConfig();
		String userId = sdncModifierDetails.getUserId();
		String serviceBodyJson = gson.toJson(componentInstanceReqDetails);
		String url = String.format(Urls.UPDATE_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(componentType), componentId,
				componentInstanceReqDetails.getUniqueId());
		RestResponse updateResourceInstance = sendPost(url, serviceBodyJson, userId, acceptHeaderData);
		return updateResourceInstance;
	}
	public static RestResponse updateComponentInstance(ComponentInstance componentInstance,
			User sdncModifierDetails, String componentId, ComponentTypeEnum componentType) throws IOException {

		Config config = Utils.getConfig();
		String userId = sdncModifierDetails.getUserId();
		String serviceBodyJson = gson.toJson(componentInstance);
		String url = String.format(Urls.UPDATE_COMPONENT_INSTANCE, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(componentType), componentId,
				componentInstance.getUniqueId());
		RestResponse updateResourceInstance = sendPost(url, serviceBodyJson, userId, acceptHeaderData);
		return updateResourceInstance;
	}

	public static RestResponse updateMultipleComponentInstance(
			List<ComponentInstanceReqDetails> componentInstanceReqDetailsList, User sdncModifierDetails,
			String componentId, ComponentTypeEnum componentType) throws IOException {
		Config config = Utils.getConfig();
		String userId = sdncModifierDetails.getUserId();
		String serviceBodyJson = gson.toJson(componentInstanceReqDetailsList.toArray());
		String url = String.format(Urls.UPDATE_MULTIPLE_COMPONENT_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentType), componentId);
		RestResponse updateResourceInstance = sendPost(url, serviceBodyJson, userId, acceptHeaderData);
		return updateResourceInstance;
	}

	public static RestResponse associateInstances(RequirementCapabilityRelDef relation, User sdncModifierDetails,
			String componentId, ComponentTypeEnum componentTypeEnum) throws IOException {

		Config config = Utils.getConfig();

		String componentType = "";
		switch (componentTypeEnum) {
		case RESOURCE:
			componentType = ComponentTypeEnum.RESOURCE_PARAM_NAME;
			break;
		case SERVICE:
			componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
			break;
		default:
			break;
		}
		String serviceBodyJson = gson.toJson(relation);
		String url = String.format(Urls.ASSOCIATE_RESOURCE_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), componentType, componentId);

		RestResponse associateInstance = sendPost(url, serviceBodyJson, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		return associateInstance;

	}

	public static RestResponse dissociateInstances(RequirementCapabilityRelDef relation, User sdncModifierDetails,
			String componentId, ComponentTypeEnum componentTypeEnum) throws IOException {

		Config config = Utils.getConfig();

		String componentType = "";
		switch (componentTypeEnum) {
		case RESOURCE:
			componentType = ComponentTypeEnum.RESOURCE_PARAM_NAME;
			break;
		case SERVICE:
			componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
			break;
		default:
			break;
		}
		String serviceBodyJson = gson.toJson(relation);
		String url = String.format(Urls.DISSOCIATE_RESOURCE_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), componentType, componentId);

		RestResponse associateInstance = sendPut(url, serviceBodyJson, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		return associateInstance;

	}

	public static void checkComponentInstanceType(RestResponse response, String expectedComponentType) {
		String actualComponentType = ResponseParser.getComponentTypeFromResponse(response);
		assertTrue(expectedComponentType.equals(actualComponentType),
				"Failed. expected: " + expectedComponentType + ", actual: " + actualComponentType + "/");
	}

	public static RestResponse updatePropertyValueOnResourceInstance(Component component, ComponentInstance instDetails,
			User user, ComponentInstanceProperty updatedInstanceProperty) throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), user.getUserId());

		String url = String.format(Urls.UPDATE_PROPERTY_TO_RESOURCE_INSTANCE, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(component.getComponentType()),
				component.getUniqueId(), instDetails.getUniqueId());
		String body = gson.toJson(updatedInstanceProperty);

		RestResponse sendGetServerRequest = sendPost(url, body, user.getUserId(), acceptHeaderData);
		return sendGetServerRequest;

	}

	public static RestResponse changeComponentInstanceVersion(Component container,
			ComponentInstance componentInstanceToReplace, Component newInstance, User sdncModifierDetails)
			throws Exception {

		return changeComponentInstanceVersion(container.getUniqueId(), componentInstanceToReplace, newInstance,
				sdncModifierDetails, container.getComponentType());
	}

	public static RestResponse changeComponentInstanceVersion(String containerUID,
			ComponentInstance componentInstanceToReplace, Component component, User sdncModifierDetails,
			ComponentTypeEnum componentType) throws IOException {

		Config config = Utils.getConfig();
		String resourceUid = ("{\"componentUid\":\"" + component.getUniqueId() + "\"}");
		String url = String.format(Urls.CHANGE_RESOURCE_INSTANCE_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentType), containerUID,
				componentInstanceToReplace.getUniqueId());
		RestResponse changeResourceInstanceVersion = sendPost(url, resourceUid, sdncModifierDetails.getUserId(),
				acceptHeaderData);

		if (changeResourceInstanceVersion.getErrorCode() == 200
				|| changeResourceInstanceVersion.getErrorCode() == 201) {
			Gson gson = new Gson();
			// ResourceInstanceReqDetails
			// convertResourceInstanceResponseToJavaObject =
			// ResponseParser.convertResourceInstanceResponseToJavaObject(createResourceInstance.getResponse());
			ComponentInstanceReqDetails fromJson = gson.fromJson(changeResourceInstanceVersion.getResponse(),
					ComponentInstanceReqDetails.class);

			componentInstanceToReplace.setUniqueId(fromJson.getUniqueId());

		}

		return changeResourceInstanceVersion;

	}

	public static RestResponse changeComponentInstanceVersion(String componentUniqueId,
			String serviceInstanceToReplaceUniqueId, String serviceUniqueId, User sdncModifierDetails,
			ComponentTypeEnum componentType) throws IOException {
		Config config = Utils.getConfig();
		String resourceUid = ("{\"componentUid\":\"" + serviceUniqueId + "\"}");
		String url = String.format(Urls.CHANGE_RESOURCE_INSTANCE_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentType), componentUniqueId,
				serviceInstanceToReplaceUniqueId);
		RestResponse changeResourceInstanceVersion = sendPost(url, resourceUid, sdncModifierDetails.getUserId(),
				acceptHeaderData);
		return changeResourceInstanceVersion;

	}

}

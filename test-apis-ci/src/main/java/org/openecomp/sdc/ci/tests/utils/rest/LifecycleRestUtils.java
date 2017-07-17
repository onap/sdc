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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleRestUtils extends BaseRestUtils {
	private static Logger logger = LoggerFactory.getLogger(LifecycleRestUtils.class.getName());
	public static final String COMMENT = "comment";
	
	// External APIs
	public static RestResponse checkInResource(String resourceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Chekin resource: " + resourceUUID;
		return changeLifeCycleOfAsset(resourceUUID, AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CHECKIN, sdncModifierDetails, comment);
	}
	
	public static RestResponse checkInService(String serviceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Chekin service: " + serviceUUID;
		return changeLifeCycleOfAsset(serviceUUID, AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CHECKIN, sdncModifierDetails, comment);
	}
	
	public static RestResponse checkOutResource(String resourceUUID, User sdncModifierDetails) throws IOException {
		String comment = "CheckOut resource: " + resourceUUID;
		return changeLifeCycleOfAsset(resourceUUID, AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CHECKOUT, sdncModifierDetails, comment);
	}
	
	public static RestResponse checkOutService(String serviceUUID, User sdncModifierDetails) throws IOException {
		String comment = "CheckOut service: " + serviceUUID;
		return changeLifeCycleOfAsset(serviceUUID, AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CHECKOUT, sdncModifierDetails, comment);
	}
	
	public static RestResponse certificationRequestService(String serviceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Certification request service: " + serviceUUID;
		return changeLifeCycleOfAsset(serviceUUID, AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CERTIFICATIONREQUEST, sdncModifierDetails, comment);
	}
	
	public static RestResponse certificationRequestResource(String resourceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Certification request resource: " + resourceUUID;
		return changeLifeCycleOfAsset(resourceUUID, AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CERTIFICATIONREQUEST, sdncModifierDetails, comment);
	}
	
	public static RestResponse startTestingService(String serviceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Start testing request service: " + serviceUUID;
		return changeLifeCycleOfAsset(serviceUUID, AssetTypeEnum.SERVICES, LifeCycleStatesEnum.STARTCERTIFICATION, sdncModifierDetails, comment);
	}
	
	public static RestResponse startTestingResource(String resourceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Start testing request resource: " + resourceUUID;
		return changeLifeCycleOfAsset(resourceUUID, AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.STARTCERTIFICATION, sdncModifierDetails, comment);
	}
	
	public static RestResponse certifyService(String serviceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Certify request service: " + serviceUUID;
		return changeLifeCycleOfAsset(serviceUUID, AssetTypeEnum.SERVICES, LifeCycleStatesEnum.CERTIFY, sdncModifierDetails, comment);
	}
	
	public static RestResponse certifyResource(String resourceUUID, User sdncModifierDetails) throws IOException {
		String comment = "Certify request resource: " + resourceUUID;
		return changeLifeCycleOfAsset(resourceUUID, AssetTypeEnum.RESOURCES, LifeCycleStatesEnum.CERTIFY, sdncModifierDetails, comment);
	}
	
	
	
	
	
	private static RestResponse changeLifeCycleOfAsset(String assetUUID, AssetTypeEnum assetTypeEnum, LifeCycleStatesEnum lifeCycleStatesEnum, User sdncModifierDetails, String comment) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.POST_EXTERNAL_API_CHANGE_LIFE_CYCLE_OF_ASSET, config.getCatalogBeHost(), config.getCatalogBePort(), assetTypeEnum.getValue(), assetUUID, lifeCycleStatesEnum.getState());
		
		Map<String, String> additionalHeaders = new HashMap<String, String>();
		
		additionalHeaders.put(HttpHeaderEnum.AUTHORIZATION.getValue(), authorizationHeader);
		additionalHeaders.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");

		String jsonBody = "{\"userRemarks\": \"" + comment + "\"}";

		RestResponse res = sendPost(url, jsonBody, sdncModifierDetails.getUserId(), acceptHeaderData, additionalHeaders);

		return res;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static RestResponse changeResourceState(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			String version, LifeCycleStatesEnum LifeCycleStatesEnum) throws IOException {
		return changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum,
				createLifecycleCommentJson(COMMENT));
	}

	public static RestResponse changeResourceState(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			String version, LifeCycleStatesEnum LifeCycleStatesEnum, String LifecycleChangeInfo) throws IOException {

		return changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum, LifecycleChangeInfo);

	}

	public static RestResponse changeResourceState(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws IOException {

		return changeResourceState(resourceDetails, sdncModifierDetails, LifeCycleStatesEnum,
				createLifecycleCommentJson(COMMENT));

	}

	public static RestResponse changeResourceState(ResourceReqDetails resourceDetails, String modifierUserId,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws IOException {
		User user = new User();
		user.setUserId(modifierUserId);
		return changeResourceState(resourceDetails, user, LifeCycleStatesEnum, createLifecycleCommentJson(COMMENT));
	}

	private static RestResponse changeResourceState(ResourceReqDetails resourceDetails, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum, String LifecycleChangeInfo) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CHANGE_RESOURCE_LIFECYCLE_STATE, config.getCatalogBeHost(),
				config.getCatalogBePort(), resourceDetails.getUniqueId(), LifeCycleStatesEnum);
		// System.out.println("url: " + url);

		RestResponse LifeCycleStatesEnumResourceResponse = sendPost(url, LifecycleChangeInfo,
				sdncModifierDetails.getUserId(), acceptHeaderData);
		if (LifeCycleStatesEnumResourceResponse.getErrorCode() == STATUS_CODE_SUCCESS) {
			String stateFromJsonResponse = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumResourceResponse.getResponse(), "lifecycleState");
			resourceDetails.setVersion(ResponseParser.getVersionFromResponse(LifeCycleStatesEnumResourceResponse));
			resourceDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(LifeCycleStatesEnumResourceResponse));
			if (stateFromJsonResponse != null) {
				resourceDetails.setLifecycleState(LifecycleStateEnum.valueOf(stateFromJsonResponse));
			}
		}
		return LifeCycleStatesEnumResourceResponse;
	}

	public static RestResponse changeServiceState(ServiceReqDetails serviceDetails, User sdncModifierDetails,
			String version, LifeCycleStatesEnum LifeCycleStatesEnum) throws Exception {
		{
			return changeServiceState(serviceDetails, sdncModifierDetails, version, LifeCycleStatesEnum,
					createLifecycleCommentJson(COMMENT));
		}
	}

	public static RestResponse changeServiceState(ServiceReqDetails serviceDetails, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws Exception {
		{
			return changeServiceState(serviceDetails, sdncModifierDetails, null, LifeCycleStatesEnum,
					createLifecycleCommentJson(COMMENT));
		}
	}

	public static RestResponse changeServiceState(ServiceReqDetails serviceDetails, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum, String lifecycleChangeInfo) throws Exception {
		{
			return changeServiceState(serviceDetails, sdncModifierDetails, null, LifeCycleStatesEnum,
					lifecycleChangeInfo);
		}
	}

	public static RestResponse changeServiceState(ServiceReqDetails serviceDetails, User sdncModifierDetails,
			String version, LifeCycleStatesEnum LifeCycleStatesEnum, String lifecycleChangeInfo) throws Exception {

		Config config = Utils.getConfig();
		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CHANGE_SERVICE_LIFECYCLE_STATE, config.getCatalogBeHost(),
				config.getCatalogBePort(), serviceDetails.getUniqueId(), LifeCycleStatesEnum);
		// System.out.println("url: " + url);
		RestResponse LifeCycleStatesEnumServiceResponse = http.httpSendPost(url, lifecycleChangeInfo, headersMap);
		if (LifeCycleStatesEnumServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS) {
			String serviceUniqueId = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "uniqueId");
			serviceDetails.setUniqueId(serviceUniqueId);
			String serviceVersion = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "version");
			serviceDetails.setVersion(serviceVersion);
			String stateFromJsonResponse = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "lifecycleState");
			serviceDetails.setLifecycleState(LifecycleStateEnum.valueOf(stateFromJsonResponse));
		}
		return LifeCycleStatesEnumServiceResponse;
	}

	public static RestResponse changeProductState(Product product, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum, String lifecycleChangeInfo) throws Exception {
		{
			return _changeProductState(product, sdncModifierDetails, LifeCycleStatesEnum, lifecycleChangeInfo);
		}
	}

	public static RestResponse changeProductState(Product product, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws Exception {
		{
			return _changeProductState(product, sdncModifierDetails, LifeCycleStatesEnum, COMMENT);
		}
	}

	public static RestResponse changeProductState(ProductReqDetails productDetails, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CHANGE_PRODUCT_LIFECYCLE_STATE, config.getCatalogBeHost(),
				config.getCatalogBePort(), productDetails.getUniqueId(), LifeCycleStatesEnum);
		RestResponse LifeCycleStatesEnumServiceResponse = sendPost(url, createLifecycleCommentJson(COMMENT),
				sdncModifierDetails.getUserId(), acceptHeaderData);
		if (LifeCycleStatesEnumServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS) {
			String productUniqueId = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "uniqueId");
			productDetails.setUniqueId(productUniqueId);
			String productVersion = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "version");
			productDetails.setVersion(productVersion);
			String newLifecycleState = ResponseParser
					.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "lifecycleState");
			productDetails.setLifecycleState(LifecycleStateEnum.valueOf(newLifecycleState));
		}
		return LifeCycleStatesEnumServiceResponse;

	}

	public static RestResponse changeComponentState(Component component, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.CHANGE_COMPONENT_LIFECYCLE_STATE, config.getCatalogBeHost(),
				config.getCatalogBePort(), ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId(), LifeCycleStatesEnum);
		RestResponse LifeCycleStatesEnumServiceResponse = sendPost(url, createLifecycleCommentJson(COMMENT), sdncModifierDetails.getUserId(), acceptHeaderData);
		if (LifeCycleStatesEnumServiceResponse.getErrorCode() == STATUS_CODE_SUCCESS) {
			String productUniqueId = ResponseParser.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "uniqueId");
			component.setUniqueId(productUniqueId);
			String productVersion = ResponseParser.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "version");
			component.setVersion(productVersion);
			String newLifecycleState = ResponseParser.getValueFromJsonResponse(LifeCycleStatesEnumServiceResponse.getResponse(), "lifecycleState");
			component.setLifecycleState(LifecycleStateEnum.valueOf(newLifecycleState));
		}
		return LifeCycleStatesEnumServiceResponse;

	}

	public static RestResponse certifyResource(ResourceReqDetails resourceDetails) throws Exception {
		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		// if (restResponseResource.getErrorCode() == 200){
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		// }else
		// return restResponseResource;
		User testerDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, testerDetails,
					LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponseResource;
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, testerDetails,
					LifeCycleStatesEnum.CERTIFY);
			if (restResponseResource.getErrorCode() == 200) {
				String newVersion = ResponseParser.getVersionFromResponse(restResponseResource);
				resourceDetails.setVersion(newVersion);
				resourceDetails.setLifecycleState(LifecycleStateEnum.CERTIFIED);
				resourceDetails.setLastUpdaterUserId(testerDetails.getUserId());
				resourceDetails.setLastUpdaterFullName(testerDetails.getFullName());
				String uniqueIdFromRresponse = ResponseParser.getUniqueIdFromResponse(restResponseResource);
				resourceDetails.setUniqueId(uniqueIdFromRresponse);
			}
		}
		return restResponseResource;
	}

	public static RestResponse certifyService(ServiceReqDetails serviceDetails) throws Exception {
		RestResponse restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		// if (restResponseService.getErrorCode() == 200){
		restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		// }else
		// return restResponseService;
		if (restResponseService.getErrorCode() == 200) {
			restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails,
					ElementFactory.getDefaultUser(UserRoleEnum.TESTER), LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponseService;
		if (restResponseService.getErrorCode() == 200) {
			User testerDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
			restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails, testerDetails,
					LifeCycleStatesEnum.CERTIFY);
			if (restResponseService.getErrorCode() == 200) {
				String newVersion = ResponseParser.getVersionFromResponse(restResponseService);
				serviceDetails.setVersion(newVersion);
				serviceDetails.setLifecycleState(LifecycleStateEnum.CERTIFIED);
				serviceDetails.setLastUpdaterUserId(testerDetails.getUserId());
				serviceDetails.setLastUpdaterFullName(testerDetails.getFullName());
				String uniqueIdFromRresponse = ResponseParser.getUniqueIdFromResponse(restResponseService);
				serviceDetails.setUniqueId(uniqueIdFromRresponse);
			}
		}
		return restResponseService;
	}

	private static RestResponse _changeProductState(Product product, User sdncModifierDetails,
			LifeCycleStatesEnum LifeCycleStatesEnum, String lifecycleChangeInfo) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CHANGE_PRODUCT_LIFECYCLE_STATE, config.getCatalogBeHost(),
				config.getCatalogBePort(), product.getUniqueId(), LifeCycleStatesEnum);
		RestResponse LifeCycleStatesEnumServiceResponse = sendPost(url, createLifecycleCommentJson(lifecycleChangeInfo),
				sdncModifierDetails.getUserId(), acceptHeaderData);

		return LifeCycleStatesEnumServiceResponse;
	}

	public static String createLifecycleCommentJson(String commentContent) {
		String res = null;
		if (commentContent != null) {
			res = "{\"userRemarks\": \"" + commentContent + "\"}";
		}
		return res;
	}

	public static void checkLCS_Response(RestResponse response) {
		checkStatusCode(response, "change lifecycle request failed", false, STATUS_CODE_SUCCESS);
	}

	private static Map<String, String> prepareHeadersMap(User sdncModifierDetails) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		return headersMap;
	}

	public static RestResponse changeDistributionStatus(ServiceReqDetails serviceDetails, String version, User user,
			String userRemarks, DistributionStatusEnum reqDistributionStatus) throws Exception {
		String uniqueId = serviceDetails.getUniqueId();
		Config config = Utils.getConfig();
		String environmentName = "PROD-Andreys-Only";
//		String environmentName = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getEnvironments().get(0);
		DistributionStatusEnum distributionStatusEnum = DistributionStatusEnum.findState(reqDistributionStatus.getValue());
		switch(distributionStatusEnum){
			case DISTRIBUTION_APPROVED:
				return sendApproveDistribution(user, uniqueId, userRemarks);
			case DISTRIBUTED:
				String url = String.format(Urls.ACTIVATE_DISTRIBUTION, config.getCatalogBeHost(), config.getCatalogBePort(), uniqueId, environmentName);
				return sendDistrState(user, userRemarks, url);
			case DISTRIBUTION_REJECTED:
				return rejectDistribution(user, userRemarks, uniqueId);
			default:
				return null;	
			
		}
		
//		if (reqDistributionStatus == DistributionStatusEnum.DISTRIBUTION_APPROVED) {
//			return sendApproveDistribution(user, uniqueId, userRemarks);
//		} else if (reqDistributionStatus == DistributionStatusEnum.DISTRIBUTION_REJECTED) {
//			return rejectDistribution(user, userRemarks, uniqueId);
//		} else if (reqDistributionStatus == DistributionStatusEnum.DISTRIBUTED) {
//			Config config = Utils.getConfig();
//			// String url =
//			// String.format("http://%s:%s/sdc2/rest/v1/catalog/services/%s/tempUrlToBeDeleted",
//			// config.getCatalogBeHost(), config.getCatalogBePort(), uniqueId);
//			String url = String.format(Urls.ACTIVATE_DISTRIBUTION, config.getCatalogBeHost(), config.getCatalogBePort(),
//					uniqueId, "PROD");
//			return sendDistrState(user, userRemarks, url);
//		} else
//			return null;

	}

	public static RestResponse sendApproveDistribution(User sdncModifierDetails, String uniqueId, String userRemarks)
			throws FileNotFoundException, IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.APPROVE_DISTRIBUTION, config.getCatalogBeHost(), config.getCatalogBePort(),
				uniqueId);
		return sendDistrState(sdncModifierDetails, userRemarks, url);
	}

	public static RestResponse rejectDistribution(ServiceReqDetails serviceDetails, String version, User user,
			String userRemarks) throws Exception {
		return rejectDistribution(user, userRemarks, serviceDetails.getUniqueId());
	}

	public static RestResponse rejectDistribution(User user, String userRemarks, String uniqueId)
			throws FileNotFoundException, IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.REJECT_DISTRIBUTION, config.getCatalogBeHost(), config.getCatalogBePort(),
				uniqueId);
		return sendDistrState(user, userRemarks, url);
	}

	private static RestResponse sendDistrState(User user, String userRemarks, String url) throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(user);
		Map<String, String> userRemarksMap = new HashMap<String, String>();
		userRemarksMap.put("userRemarks", userRemarks);

		String serviceBodyJson = new JSONObject().toJSONString(userRemarksMap);

		HttpRequest httpRequest = new HttpRequest();
		logger.debug(url);
		logger.debug("Send POST request to create service: {}", url);
		logger.debug("Service body: {}", serviceBodyJson);
		logger.debug("Service headers: {}", headersMap);
		RestResponse rejectDistributionResponse = httpRequest.httpSendPost(url, serviceBodyJson, headersMap);

		return rejectDistributionResponse;
	}

}

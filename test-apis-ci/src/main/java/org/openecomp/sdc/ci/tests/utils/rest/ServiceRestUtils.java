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

import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServiceRestUtils extends BaseRestUtils {
	private static Logger logger = LoggerFactory.getLogger(ServiceRestUtils.class.getName());
	// ****** CREATE *******

	private static Gson gson = new Gson();

	public static RestResponse deleteService(String serviceName, String version, User sdncModifierDetails)
			throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_SERVICE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), serviceName, version);

		String userId = sdncModifierDetails.getUserId();
		RestResponse sendDelete = sendDelete(url, userId);
		deleteMarkedServices(userId);
		return sendDelete;
	}
	
	public static RestResponse markServiceToDelete(String resourceId, String userId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId);
		RestResponse sendDelete = sendDelete(url, userId);

		return sendDelete;

	}

	public static RestResponse deleteServiceById(String serviceId, String userId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId);
		RestResponse sendDelete = sendDelete(url, userId);
		deleteMarkedServices(userId);
		return sendDelete;
	}

	public static void deleteMarkedServices(String userId) throws IOException {
		String url;
		Config config = Utils.getConfig();
		url = String.format(Urls.DELETE_MARKED_SERVICES, config.getCatalogBeHost(), config.getCatalogBePort());
		sendDelete(url, userId);
	}

	public static RestResponse createService(ServiceReqDetails service, User user) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort());
		String serviceBodyJson = gson.toJson(service);

		logger.debug("Send POST request to create service: {}", url);
		logger.debug("Service body: {}", serviceBodyJson);

		RestResponse res = sendPost(url, serviceBodyJson, user.getUserId(), acceptHeaderData);
		if (res.getErrorCode() == STATUS_CODE_CREATED) {
			service.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
            service.setName(ResponseParser.getNameFromResponse(res));
			service.setVersion(ResponseParser.getVersionFromResponse(res));
			service.setUUID(ResponseParser.getUuidFromResponse(res));
			// Creator details never change after component is created - Ella,
			// 12/1/2016
			service.setCreatorUserId(user.getUserId());
			service.setCreatorFullName(user.getFullName());
		}

		return res;
	}

	public static RestResponse updateService(ServiceReqDetails service, User user) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_SERVICE_METADATA, config.getCatalogBeHost(), config.getCatalogBePort(),
				service.getUniqueId());
		String serviceBodyJson = gson.toJson(service);

		logger.debug("Send PUT request to create service: {}", url);
		logger.debug("Service body: {}", serviceBodyJson);

		RestResponse res = sendPut(url, serviceBodyJson, user.getUserId(), acceptHeaderData);
		if (res.getErrorCode() == STATUS_CODE_CREATED) {
			service.setUniqueId(ResponseParser.getUniqueIdFromResponse(res));
			service.setVersion(ResponseParser.getVersionFromResponse(res));
		}

		return res;
	}

	public static RestResponse getService(String serviceId) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId);
		return getServiceFromUrl(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), false);
	}

	public static RestResponse getService(ServiceReqDetails serviceReqDetails, User sdncModifierDetails)
			throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(),
				serviceReqDetails.getUniqueId());
		return getServiceFromUrl(url, sdncModifierDetails, false);
	}

	public static RestResponse getService(String serviceId, User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId);
		return getServiceFromUrl(url, sdncModifierDetails, false);
	}

	public static RestResponse getServiceByNameAndVersion(User sdncModifierDetails, String serviceName,
			String serviceVersion) throws IOException {
		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SERVICE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), serviceName, serviceVersion);
		return getServiceFromUrl(url, sdncModifierDetails, false);
	}

	public static RestResponse getServiceFromUrl(String url, User sdncModifierDetails, boolean isCached)
			throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails, isCached);
		HttpRequest http = new HttpRequest();
		logger.debug("Send GET request to create service: {}", url);
		logger.debug("Service headers: {}", headersMap);
		RestResponse sendGetServerRequest = http.httpSendGet(url, headersMap);

		return sendGetServerRequest;
	}

	public static Map<String, String> prepareHeadersMap(User sdncModifierDetails, boolean isCached) {
		Map<String, String> headersMap = new HashMap<>();
		if (isCached)
			headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), BaseRestUtils.cacheControlHeader);

		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), BaseRestUtils.contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), BaseRestUtils.acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		return headersMap;
	}

	public static RestResponse approveServiceDistribution(String serviceId, String userId) throws Exception {
		return changeServiceDistributionState(serviceId, userId, Urls.APPROVE_DISTRIBUTION);
	}

	public static RestResponse rejectServiceDistribution(String serviceId, String userId) throws Exception {
		return changeServiceDistributionState(serviceId, userId, Urls.REJECT_DISTRIBUTION);
	}

	// Benny
	public static RestResponse rejectServiceDistribution(String serviceId, String userId, String comment)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.REJECT_DISTRIBUTION, config.getCatalogBeHost(), config.getCatalogBePort(),
				serviceId);
		String userBodyJson = gson.toJson(comment);
		return sendPost(url, userBodyJson, userId, BaseRestUtils.acceptHeaderData);

	}

	private static RestResponse changeServiceDistributionState(String serviceId, String userId, String distributionUrl)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(distributionUrl, config.getCatalogBeHost(), config.getCatalogBePort(), serviceId);
		String defComment = "{ userRemarks : \"this is an test\" }";
		String userBodyJson = gson.toJson(defComment);
		return sendPost(url, userBodyJson, userId, acceptHeaderData);

	}

	public static RestResponse getServiceLatestVersionList(User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_SERVICE_lATEST_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort());

		return sendGet(url, sdncModifierDetails.getUserId());

	}

	public static RestResponse createServiceByHttpMethod(ServiceReqDetails serviceDetails, User sdncModifierDetails,
			String method, String urls) throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails, true);

		Config config = Utils.getConfig();
		String serviceBodyJson = gson.toJson(serviceDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(urls, config.getCatalogBeHost(), config.getCatalogBePort());
		// TODO: ADD AUTHENTICATION IN REQUEST
		logger.debug(url);
		logger.debug("Send {} request to create user: {}",method,url);
		logger.debug("User body: {}", serviceBodyJson);
		logger.debug("User headers: {}", headersMap);
		RestResponse sendCreateUserRequest = http.httpSendByMethod(url, method, serviceBodyJson, headersMap);

		return sendCreateUserRequest;

	}

	public static RestResponse deleteServiceByNameAndVersion(User sdncModifierDetails, String serviceName,
			String serviceVersion) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = prepareHeadersMap(sdncModifierDetails, true);

		HttpRequest http = new HttpRequest();

		String url = String.format(Urls.DELETE_SERVICE_BY_NAME_AND_VERSION, config.getCatalogBeHost(),
				config.getCatalogBePort(), serviceName, serviceVersion);
		RestResponse deleteResponse = http.httpSendDelete(url, headersMap);

		deleteMarkedServices(sdncModifierDetails.getUserId());
		return deleteResponse;
	}

	public static RestResponse getFollowed(User user) throws Exception {
		Config config = Utils.getConfig();

		HttpRequest httpRequest = new HttpRequest();

		String url = String.format(Urls.GET_FOLLWED_LIST, config.getCatalogBeHost(), config.getCatalogBePort());

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), BaseRestUtils.contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), BaseRestUtils.acceptHeaderData);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), user.getUserId());

		RestResponse getResourceNotAbstarctResponse = httpRequest.httpSendGet(url, headersMap);

		return getResourceNotAbstarctResponse;
	}

	public static JSONArray getListArrayFromRestResponse(RestResponse restResponse) {
		String json = restResponse.getResponse();
		JSONObject jsonResp = (JSONObject) JSONValue.parse(json);
		JSONArray servicesArray = (JSONArray) jsonResp.get("services");

		logger.debug("services= {}", servicesArray);

		return servicesArray;
	}
	
	public static RestResponse getDistributionServiceList(Service service, User user) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DISTRIBUTION_SERVICE_LIST, config.getCatalogBeHost(), config.getCatalogBePort(), service.getUUID());
		return getServiceFromUrl(url, user, false);
	}

}

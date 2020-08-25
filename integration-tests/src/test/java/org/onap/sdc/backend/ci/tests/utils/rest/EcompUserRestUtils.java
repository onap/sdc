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

package org.onap.sdc.backend.ci.tests.utils.rest;

import com.google.gson.Gson;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpRequest;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcompUserRestUtils extends BaseRestUtils {

	static Gson gson = new Gson();

	static Logger logger = LoggerFactory.getLogger(EcompUserRestUtils.class.getName());
	static String ecompUsername = "12345";
	static String ecompPassword = "12345";

	public EcompUserRestUtils() {
		super();
	}

	public static RestResponse pushUser(EcompUser ecompUser) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String userBodyJson = gson.toJson(ecompUser);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_PUSH_USER, config.getCatalogBeHost(), config.getCatalogBePort());

		logger.debug("Send POST request to create user: {}", url);
		logger.debug("User body: {}", userBodyJson);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendPushUserResponse = http.httpSendPost(url, userBodyJson, headersMap);

		return sendPushUserResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse editUser(String loginId, EcompUser ecompUser) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String userBodyJson = gson.toJson(ecompUser);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_EDIT_USER, config.getCatalogBeHost(), config.getCatalogBePort(), loginId);

		logger.debug("Send POST request to edit user: {}", url);
		logger.debug("User body: {}", userBodyJson);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendEditUserResponse = http.httpSendPost(url, userBodyJson, headersMap);

		return sendEditUserResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse getUser(String loginId) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_USER, config.getCatalogBeHost(), config.getCatalogBePort(), loginId);

		logger.debug("Send GET request to get user: {}", url);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);

		return sendGetUserRequest;
	}

	public static RestResponse getAllUsers() throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_ALL_USERS, config.getCatalogBeHost(), config.getCatalogBePort());

		logger.debug("Send POST request to get all users: {}", url);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendGetAllUsersRequest = http.httpSendGet(url, headersMap);

		return sendGetAllUsersRequest;
	}

	public static RestResponse getAllAvailableRoles() throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_ALL_AVAILABLE_ROLES, config.getCatalogBeHost(),
				config.getCatalogBePort());

		logger.debug("Send GET request to get all available roles: {}", url);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendUpdateUserRequest = http.httpSendGet(url, headersMap);

		return sendUpdateUserRequest;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse pushUserRoles(String loginId, List<EcompRole> roles) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String roleBodyJson = gson.toJson(roles);
		if(roleBodyJson.equals("[{}]")) {
			roleBodyJson = "[]";
		}

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_PUSH_USER_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(),
				loginId);

		logger.debug("Send POST request to push user role: {}", url);
		logger.debug("Roles body: {}", roleBodyJson);
		logger.debug("Request headers: {}", headersMap);

		RestResponse sendpushUserRolesResponse = http.httpSendPost(url, roleBodyJson, headersMap);

		return sendpushUserRolesResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse getUserRoles(String loginId) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderData);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_USER_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(),
				loginId);

		logger.debug("Send GET request to get user roles: {}", url);
		logger.debug("User headers: {}", headersMap);

		RestResponse sendGetUserRolesRequest = http.httpSendGet(url, headersMap);

		return sendGetUserRolesRequest;
	}
}

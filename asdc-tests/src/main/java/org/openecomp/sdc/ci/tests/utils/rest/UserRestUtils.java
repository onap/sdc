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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.run.StartTest;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class UserRestUtils extends BaseRestUtils {

	static Gson gson = new Gson();

	static Logger logger = LoggerFactory.getLogger(UserRestUtils.class.getName());
	static String contentTypeHeaderData = "application/json";
	static String acceptHeaderDate = "application/json";

	public UserRestUtils() {
		super();

		StartTest.enableLogger();
	}

	public static RestResponse createUser(User sdncUserDetails, User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		String userBodyJson = gson.toJson(sdncUserDetails);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_USER, config.getCatalogBeHost(), config.getCatalogBePort());

		logger.debug("Send POST request to create user: {}",url);
		logger.debug("User body: {}",userBodyJson);
		logger.debug("User headers: {}",headersMap);
		RestResponse sendCreateUserRequest = http.httpSendPost(url, userBodyJson, headersMap);

		return sendCreateUserRequest;

	}

	public static RestResponse deactivateUser(User sdncUserDetails, User sdncModifierDetails) throws IOException {
		return deleteUser(sdncUserDetails, sdncModifierDetails, true);
	}

	public static RestResponse deActivateUser(User sdncUserDetails, User sdncModifierDetails) throws IOException {
		return deleteUser(sdncUserDetails, sdncModifierDetails, false);
	}

	public static RestResponse deleteUser(User sdncUserDetails, User sdncModifierDetails, boolean isForceDelete)
			throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		if (isForceDelete) {
			headersMap.put(User.FORCE_DELETE_HEADER_FLAG, User.FORCE_DELETE_HEADER_FLAG);
		}

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.DELETE_USER, config.getCatalogBeHost(), config.getCatalogBePort(),
				sdncUserDetails.getUserId());
		RestResponse sendDeleteUserRequest = http.httpSendDelete(url, headersMap);
		return sendDeleteUserRequest;

	}

	public static RestResponse updateUser(User sdncUserDetails, User sdncModifierDetails)
			throws IOException, CloneNotSupportedException {

		Config config = Utils.getConfig();
		User user = new User(sdncModifierDetails);

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		user.setUserId(StringUtils.EMPTY);
		user.setRole(StringUtils.EMPTY);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(user);
		logger.debug("userBodyJson: {}",userBodyJson);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_USER, config.getCatalogBeHost(), config.getCatalogBePort(),
				sdncModifierDetails.getUserId());
		RestResponse sendUpdateUserRequest = http.httpSendPost(url, userBodyJson, headersMap);

		return sendUpdateUserRequest;
	}

	/// Benny
	public static RestResponse updateUserRole(User sdncUserDetails, User sdncModifierDetails, String userIdToUpdate)
			throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(sdncUserDetails);
		logger.debug("userBodyJson: {}",userBodyJson);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.UPDATE_USER_ROLE, config.getCatalogBeHost(), config.getCatalogBePort(),
				userIdToUpdate);
		RestResponse sendUpdateUserRequest = http.httpSendPost(url, userBodyJson, headersMap);

		return sendUpdateUserRequest;

	}

	public static RestResponse getUser(User sdncUserDetails, User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_USER, config.getCatalogBeHost(), config.getCatalogBePort(),
				sdncUserDetails.getUserId());
		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);
		return sendGetUserRequest;

	}

	public static RestResponse getAllAdminUsers(User sdncModifierDetails) throws IOException {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		// Gson gson = new Gson();
		// String userBodyJson = gson.toJson(sdncModifierDetails);
		// System.out.println(userBodyJson);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_ALL_ADMIN_USERS, config.getCatalogBeHost(), config.getCatalogBePort());
		logger.debug("Send following url: {} and headers: {}",url,headersMap.toString());
		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);

		return sendGetUserRequest;

	}

	// US571255
	public static RestResponse getUsersByRoles(User sdncModifierDetails, String roles) throws IOException {

		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		HttpRequest http = new HttpRequest();
		String url;
		if (roles == "/") {
			url = String.format(Urls.GET_ALL_USERS, config.getCatalogBeHost(), config.getCatalogBePort());
		} else {
			url = String.format(Urls.GET_USERS_BY_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(), roles);

		}
		logger.debug("Send following url: {} and headers: {}",url,headersMap.toString());
		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);
		return sendGetUserRequest;
	}

	public static RestResponse getUsersByRolesHttpCspAtuUidIsMissing(User sdncModifierDetails, String roles)
			throws Exception {

		Config config = Utils.getConfig();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());
		headersMap.remove("USER_ID");
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.GET_USERS_BY_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(),
				roles);
		logger.debug(
				"Send following url without USER_ID header : " + url + "  headers: " + headersMap.toString());

		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);
		return sendGetUserRequest;
	}

	public static RestResponse authorizedUserTowardsCatalogBe(User sdncUserDetails) throws IOException {

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		if (sdncUserDetails.getFirstName() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_FIRSTNAME.getValue(), sdncUserDetails.getFirstName());
		}
		if (sdncUserDetails.getLastName() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_LASTNAME.getValue(), sdncUserDetails.getLastName());
		}
		if (sdncUserDetails.getEmail() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_EMAIL.getValue(), sdncUserDetails.getEmail());
		}

		logger.debug("headersMap: {}",headersMap.toString());

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.AUTHORIZE_USER, config.getCatalogBeHost(), config.getCatalogBePort());
		logger.debug("Send GET request to login as seal user : {}",url);
		return http.httpSendGet(url, headersMap);
	}

	public static RestResponse authorizedUserTowardsCatalogBeQA(User sdncUserDetails) throws IOException {

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncUserDetails.getUserId());
		if (sdncUserDetails.getFirstName() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_FIRSTNAME.getValue(), sdncUserDetails.getFirstName());
		}
		if (sdncUserDetails.getLastName() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_LASTNAME.getValue(), sdncUserDetails.getLastName());
		}
		if (sdncUserDetails.getEmail() != null) {
			headersMap.put(HttpHeaderEnum.HTTP_CSP_EMAIL.getValue(), sdncUserDetails.getEmail());
		}

		logger.debug("headersMap: {}",headersMap.toString());

		Config config = Utils.getConfig();
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.AUTHORIZE_USER, config.getCatalogBeHost(), config.getCatalogBePort());
		logger.debug("Send GET request to login as seal user : {}",url);
		return http.httpSendGet(url, headersMap);
	}

}

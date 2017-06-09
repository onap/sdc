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
import java.util.List;
import java.util.Map;

import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.portalsdk.core.restful.domain.EcompUser;
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

public class EcompUserRestUtils extends BaseRestUtils {

	static Gson gson = new Gson();
	private static Logger logger = LoggerFactory.getLogger(UserRestUtils.class.getName());

	static String contentTypeHeaderData = "application/json";
	static String acceptHeaderDate = "application/json";
	static String ecompUsername = "12345";
	static String ecompPassword = "12345";

	public EcompUserRestUtils() {
		super();

		StartTest.enableLogger();
	}

	public static RestResponse pushUser(EcompUser ecompUser) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String userBodyJson = gson.toJson(ecompUser);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_PUSH_USER, config.getCatalogBeHost(), config.getCatalogBePort());

		logger.debug("Send POST request to create user: {}",url);
		logger.debug("User body: {}",userBodyJson);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendPushUserResponse = http.httpSendPost(url, userBodyJson, headersMap);

		return sendPushUserResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse editUser(String loginId, EcompUser ecompUser) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String userBodyJson = gson.toJson(ecompUser);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_EDIT_USER, config.getCatalogBeHost(), config.getCatalogBePort(), loginId);

		logger.debug("Send POST request to edit user: {}",url);
		logger.debug("User body: {}",userBodyJson);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendEditUserResponse = http.httpSendPost(url, userBodyJson, headersMap);

		return sendEditUserResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse getUser(String loginId) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_USER, config.getCatalogBeHost(), config.getCatalogBePort(), loginId);

		logger.debug("Send GET request to get user: {}",url);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendGetUserRequest = http.httpSendGet(url, headersMap);

		return sendGetUserRequest;
	}

	public static RestResponse getAllUsers() throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_ALL_USERS, config.getCatalogBeHost(), config.getCatalogBePort());

		logger.debug("Send POST request to get all users: {}",url);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendGetAllUsersRequest = http.httpSendGet(url, headersMap);

		return sendGetAllUsersRequest;
	}

	public static RestResponse getAllAvailableRoles() throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_ALL_AVAILABLE_ROLES, config.getCatalogBeHost(),
				config.getCatalogBePort());

		logger.debug("Send GET request to get all available roles: {}",url);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendUpdateUserRequest = http.httpSendGet(url, headersMap);

		return sendUpdateUserRequest;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse pushUserRoles(String loginId, List<EcompRole> roles) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		String roleBodyJson = gson.toJson(roles);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_PUSH_USER_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(),
				loginId);

		logger.debug("Send POST request to push user role: {}",url);
		logger.debug("Roles body: {}",roleBodyJson);
		logger.debug("Request headers: {}",headersMap);

		RestResponse sendpushUserRolesResponse = http.httpSendPost(url, roleBodyJson, headersMap);

		return sendpushUserRolesResponse;
	}

	/*
	 * loginId - equals to userId
	 */
	public static RestResponse getUserRoles(String loginId) throws IOException {
		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.ECOMP_USERNAME.getValue(), ecompUsername);
		headersMap.put(HttpHeaderEnum.ECOMP_PASSWORD.getValue(), ecompPassword);

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.ECOMP_GET_USER_ROLES, config.getCatalogBeHost(), config.getCatalogBePort(),
				loginId);

		logger.debug("Send GET request to get user roles: {}",url);
		logger.debug("User headers: {}",headersMap);

		RestResponse sendGetUserRolesRequest = http.httpSendGet(url, headersMap);

		return sendGetUserRolesRequest;
	}

	// TODO !!!!!!!!!!!!!!
	/*
	 * Ask Eli if implementation of users is needed DELETE ECOMP USER
	 */

	/*
	 * public static void main(String[] args) { EcompUser ecompUser = new
	 * EcompUser(); ecompUser.setFirstName("Test");
	 * ecompUser.setLastName("Testovich");
	 * ecompUser.setEmail("ttes@intl.sdc.com"); ecompUser.setLoginId("tt0004");
	 * ecompUser.setActive(true);
	 * 
	 * EcompRole roleToUpdate = new EcompRole(); roleToUpdate.setId(new
	 * Long(6)); roleToUpdate.setName("PRODUCT_STRATEGIST"); List<EcompRole>
	 * listOfRoles = new LinkedList<>(); listOfRoles.add(roleToUpdate);
	 * 
	 * try {
	 * System.out.println("\n-----------------------------\n Testing pushUser");
	 * System.out.println(pushUser(ecompUser));
	 * System.out.println("\n-----------------------------\n Testing editUser");
	 * System.out.println("\n-----------------------------\n Testing getUser");
	 * // System.out.println(getUser(ecompUser.getLoginId())); System.out.
	 * println("\n-----------------------------\n Testing getAllUsers"); //
	 * System.out.println(getAllUsers()); System.out.
	 * println("\n-----------------------------\n Testing getAllAvailableRoles"
	 * ); // System.out.println(getAllAvailableRoles().toString()); System.out.
	 * println("\n-----------------------------\n Testing pushUserRoles"); //
	 * println("\n-----------------------------\n Testing getUserRoles"); //
	 * TODO Auto-generated catch block e.printStackTrace(); } }
	 */
}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRestUtils extends BaseValidationUtils {
	public static final String contentTypeHeaderData = "application/json";
	public static final String acceptHeaderData = "application/json";
	public static final String acceptJsonHeader = "application/json";
	public static final String acceptOctetHeader = "application/octet-stream";
	public static final String authorizationHeader = "Basic " + Base64.encodeBase64String("ci:123456".getBytes());
	public static final String acceptOctetStream = "application/octet-stream";
	public static final String ecomp = "ecomp";
	public static final String authorizationPrefixString = "Basic ";

	public static final String RESOURCE_COMPONENT_TYPE = "resources";
	public static final String PRODUCT_COMPONENT_TYPE = "products";
	public static final String SERVICE_COMPONENT_TYPE = "services";

	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_CREATED = 201;
	public static final int STATUS_CODE_DELETE = 204;
	public static final int STATUS_CODE_NOT_FOUND = 404;
	public static final int STATUS_CODE_SUCCESS_NO_CONTENT = 204;
	public static final int STATUS_CODE_SUCCESS_DELETE = 204;
	public static final int STATUS_CODE_INVALID_CONTENT = 400;
	public static final int STATUS_CODE_MISSING_DATA = 400;
	public static final int STATUS_CODE_MISSING_INFORMATION = 403;
	public static final int STATUS_CODE_RESTRICTED_ACCESS = 403;
	public static final int STATUS_CODE_ALREADY_EXISTS = 409;
	public static final int STATUS_CODE_RESTRICTED_OPERATION = 409;
	public static final int STATUS_CODE_COMPONENT_NAME_EXCEEDS_LIMIT = 400;
	public static final int STATUS_CODE_MISSING_COMPONENT_NAME = 400;
	public static final int STATUS_CODE_UNSUPPORTED_ERROR = 400;
	public static final int STATUS_CODE_IMPORT_SUCCESS = 201;
	public static final int STATUS_CODE_UPDATE_SUCCESS = 200;
	public static final int RESTRICTED_OPERATION = 409;
	public static final int STATUS_CODE_GET_SUCCESS = 200;

	public static final String SUCCESS_MESSAGE = "OK";
	private static Logger logger = LoggerFactory.getLogger(BaseRestUtils.class.getName());

	private static byte[] encodeBase64;

	// ************* PRIVATE METHODS ************************

	protected static Map<String, String> prepareHeadersMap(String userId) {
		return prepareHeadersMap(userId, acceptHeaderData);
	}

	protected static Map<String, String> prepareHeadersMap(String userId, String accept) {
		Map<String, String> headersMap = new HashMap<String, String>();
		if (contentTypeHeaderData != null) {
			headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		}
		if (accept != null) {
			headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), accept);
		}
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}

		return headersMap;
	}

	// send request
	// GET
	protected static RestResponse sendGet(String url, String userId) throws IOException {
		return sendGet(url, userId, null);
	}

	protected static RestResponse sendGet(String url, String userId, Map<String, String> additionalHeaders)
			throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(userId);
		if (additionalHeaders != null) {
			headersMap.putAll(additionalHeaders);
		}

		HttpRequest http = new HttpRequest();
		RestResponse getResourceResponse = http.httpSendGet(url, headersMap);
		return getResourceResponse;
	}

	public static RestResponse sendGetAndRemoveHeaders(String url, String userId, List<String> headersToRemove)
			throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(userId);
		if (headersToRemove != null) {
			for (String header : headersToRemove) {
				headersMap.remove(header);
			}
		}

		HttpRequest http = new HttpRequest();
		RestResponse getResourceResponse = http.httpSendGet(url, headersMap);
		return getResourceResponse;
	}

	// PUT
	protected static RestResponse sendPut(String url, String userBodyJson, String userId, String cont)
			throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(userId, cont);

		HttpRequest http = new HttpRequest();
		RestResponse updateResourceResponse = http.httpSendByMethod(url, "PUT", userBodyJson, headersMap);
		return updateResourceResponse;
	}

	// POST
	public static RestResponse sendPost(String url, String userBodyJson, String userId, String accept)
			throws IOException {
		return sendPost(url, userBodyJson, userId, accept, null);
	}

	protected static RestResponse sendPost(String url, String userBodyJson, String userId, String accept,
			Map<String, String> additionalHeaders) throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(userId, accept);
		if (additionalHeaders != null) {
			headersMap.putAll(additionalHeaders);
		}
		HttpRequest http = new HttpRequest();
		RestResponse postResourceResponse = http.httpSendPost(url, userBodyJson, headersMap);
		return postResourceResponse;
	}

	// used form complex requests like import categories..
	protected static RestResponse sendPost(String url, HttpEntity entity, String userId, String accept)
			throws IOException {
		RestResponse postResponse = new RestResponse();
		CloseableHttpResponse response = null;
		CloseableHttpClient client = null;
		try {
			client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);

			httpPost.addHeader("USER_ID", userId);
			httpPost.setEntity(entity);
			response = client.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();

			postResponse.setErrorCode(statusCode);
			StringBuffer sb = new StringBuffer();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
				}
				in.close();
			} catch (Exception e) {
				logger.debug("response body is null");
			}
			postResponse.setResponse(sb.toString());
		} finally {
			try {
				if (response != null) {
					response.close();
				}

			} catch (IOException e) {
				logger.debug("failed to close client or response: ", e);
			}
			try {
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				logger.debug("failed to close client or response: ", e);
			}
		}
		return postResponse;
	}

	// DELETE
	protected static RestResponse sendDelete(String url, String userId) throws IOException {
//		Map<String, String> headersMap = prepareHeadersMap(userId);
		
		return sendDelete(url, userId, null);
	}
	
	protected static RestResponse sendDelete(String url, String userId, Map<String, String> additionalHeaders) throws IOException {
		Map<String, String> headersMap = prepareHeadersMap(userId);
		if (additionalHeaders != null) {
			headersMap.putAll(additionalHeaders);
		}
		
		HttpRequest http = new HttpRequest();
		RestResponse deleteResourceResponse = http.httpSendDelete(url, headersMap);
		return deleteResourceResponse;
	}

	/*
	 * // ------ protected static Boolean checkErrorCode(RestResponse
	 * deleteResponse) { if (deleteResponse.getErrorCode() ==
	 * STATUS_CODE_SUCCESS || deleteResponse.getErrorCode() ==
	 * STATUS_CODE_DELETE) { return true; } return false; }
	 * 
	 * // *** STATUS CODE VALIDATION UTIITIES **** public static void
	 * checkStatusCode(RestResponse response, String assertMessage, boolean AND,
	 * int... statuses) { int statusCode = response.getErrorCode(); for (int
	 * status : statuses) { if (AND && statusCode != status) {
	 * Assert.fail(assertMessage + " status: " + statusCode); } else if
	 * (statusCode == status) { return; } } if (!AND) {
	 * Assert.fail(assertMessage + " status: " + statusCode); } }
	 * 
	 * public static void checkDeleteResponse(RestResponse response) {
	 * checkStatusCode(response,"delete request failed",false,STATUS_CODE_DELETE
	 * ,STATUS_CODE_NOT_FOUND, STATUS_CODE_SUCCESS); // STATUS_CODE_SUCCESS for
	 * deActivate user }
	 * 
	 * public static void checkCreateResponse(RestResponse response) {
	 * checkStatusCode(response, "create request failed", false,
	 * STATUS_CODE_CREATED); }
	 */
	public static String encodeUrlForDownload(String url) {
		return url.replaceAll(" ", "%20");
	}

	public static Map<String, String> addAuthorizeHeader(String userName, String password) {
		String userCredentials = userName + ":" + password;
		encodeBase64 = Base64.encodeBase64(userCredentials.getBytes());
		String encodedUserCredentials = authorizationPrefixString + new String(encodeBase64);
		Map<String, String> authorizationHeader = new HashMap<String, String>();
		authorizationHeader.put(HttpHeaderEnum.AUTHORIZATION.getValue(), encodedUserCredentials);
		return authorizationHeader;
	}

}

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

import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;

import com.google.gson.Gson;

public class ConsumerRestUtils extends BaseRestUtils {

	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_CREATED = 201;
	public static final int STATUS_CODE_DELETE = 204;
	public static final int STATUS_CODE_NOT_FOUND = 404;
	Utils utils = new Utils();
	private static Long expectedsLastupdatedtime;
	private static Long expectedLastAuthenticationTime;

	public static RestResponse createConsumer(ConsumerDataDefinition consumerDataDefinition, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = sdncModifierDetails.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(consumerDataDefinition);

		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse createConsumerResponse = http.httpSendPost(url, userBodyJson, headersMap);
		if (createConsumerResponse.getErrorCode() == STATUS_CODE_CREATED) {
			ConsumerDataDefinition getConsumerDataObject = parseComsumerResp(createConsumerResponse);
			consumerDataDefinition
					.setConsumerDetailsLastupdatedtime(getConsumerDataObject.getConsumerDetailsLastupdatedtime());
			consumerDataDefinition
					.setConsumerLastAuthenticationTime(getConsumerDataObject.getConsumerLastAuthenticationTime());
			consumerDataDefinition.setLastModfierAtuid(getConsumerDataObject.getLastModfierAtuid());
		}
		return createConsumerResponse;
	}

	public static RestResponse createConsumerHttpCspAtuUidIsMissing(ConsumerDataDefinition consumerDataDefinition,
			User sdncModifierDetails) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.CREATE_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort());

		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		headersMap.remove("USER_ID");
		Gson gson = new Gson();
		String userBodyJson = gson.toJson(consumerDataDefinition);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse createConsumerResponse = http.httpSendPost(url, userBodyJson, headersMap);
		if (createConsumerResponse.getErrorCode() == STATUS_CODE_CREATED) {
			ConsumerDataDefinition getConsumerDataObject = parseComsumerResp(createConsumerResponse);
			consumerDataDefinition
					.setConsumerDetailsLastupdatedtime(getConsumerDataObject.getConsumerDetailsLastupdatedtime());
			consumerDataDefinition
					.setConsumerLastAuthenticationTime(getConsumerDataObject.getConsumerLastAuthenticationTime());
			consumerDataDefinition.setLastModfierAtuid(getConsumerDataObject.getLastModfierAtuid());
		}
		return createConsumerResponse;
	}

	public static RestResponse deleteConsumerHttpCspAtuUidIsMissing(ConsumerDataDefinition consumerDataDefinition,
			User sdncModifierDetails) throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort(),
				consumerDataDefinition.getConsumerName());

		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		headersMap.remove("USER_ID");
		Gson gson = new Gson();
		String userBodyJson = gson.toJson(consumerDataDefinition);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse deleteConsumerResponse = http.httpSendDelete(url, headersMap);
		return deleteConsumerResponse;
	}

	public static ConsumerDataDefinition parseComsumerResp(RestResponse restResponse) throws Exception {

		String bodyToParse = restResponse.getResponse();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ConsumerDataDefinition component = mapper.readValue(bodyToParse, ConsumerDataDefinition.class);
			return component;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static RestResponse deleteConsumer(ConsumerDataDefinition consumerDataDefinition, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.DELETE_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort(),
				consumerDataDefinition.getConsumerName());

		String userId = sdncModifierDetails.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		RestResponse deleteConsumerResponse = http.httpSendDelete(url, headersMap);
		return deleteConsumerResponse;
	}

	public static RestResponse getConsumer(ConsumerDataDefinition consumerDataDefinition, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_CONSUMER, config.getCatalogBeHost(), config.getCatalogBePort(),
				consumerDataDefinition.getConsumerName());

		String userId = sdncModifierDetails.getUserId();

		Map<String, String> headersMap = prepareHeadersMap(userId);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		RestResponse getConsumerResponse = http.httpSendGet(url, headersMap);
		return getConsumerResponse;
	}

	public static void validateConsumerReqVsResp(ConsumerDataDefinition consumerDefinition,
			ConsumerDataDefinition getConsumerDataObject) {

		String expected;

		expected = consumerDefinition.getConsumerName();
		assertEquals("consumer name - ", expected, getConsumerDataObject.getConsumerName());

		expected = consumerDefinition.getConsumerPassword().toLowerCase();
		assertEquals("consumer password  - ", expected, getConsumerDataObject.getConsumerPassword());

		expected = consumerDefinition.getLastModfierAtuid();
		assertEquals("consumer Last Modfier Atuid - ", expected, getConsumerDataObject.getLastModfierAtuid());

		expected = consumerDefinition.getConsumerSalt();
		assertEquals("consumer Salt - ", expected, getConsumerDataObject.getConsumerSalt());

		expectedsLastupdatedtime = consumerDefinition.getConsumerDetailsLastupdatedtime();
		assertEquals("consumer Last updated time - ", expectedsLastupdatedtime,
				getConsumerDataObject.getConsumerDetailsLastupdatedtime());

		expectedLastAuthenticationTime = consumerDefinition.getConsumerLastAuthenticationTime();
		assertEquals("consumer Last authentication time - ", expectedLastAuthenticationTime,
				getConsumerDataObject.getConsumerLastAuthenticationTime());
	}

	///// New
	public enum EcompConsumerAuditJsonKeysEnum {
		ACTION("ACTION"), MODIFIER("MODIFIER"), ECOMP_USER("ECOMP_USER"), STATUS("STATUS"), DESC("DESCRIPTION");
		private String auditJsonKeyName;

		private EcompConsumerAuditJsonKeysEnum(String auditJsonKeyName) {
			this.auditJsonKeyName = auditJsonKeyName;
		}

		public String getAuditJsonKeyName() {
			return auditJsonKeyName.toLowerCase();
		}
	}

	/*
	 * protected void resourceArtifatAuditSuccess(String action,
	 * ArtifactReqDetails artifact, ResourceReqDetails resourceDetails , User
	 * user) throws Exception { ExpectedResourceAuditJavaObject
	 * expectedResourceAuditJavaObject =
	 * Convertor.constructFieldsForAuditValidation(resourceDetails,
	 * resourceDetails.getVersion(), user); String auditAction = action;
	 * expectedResourceAuditJavaObject.setAction(auditAction);
	 * expectedResourceAuditJavaObject.setPrevState("");
	 * expectedResourceAuditJavaObject.setPrevVersion("");
	 * expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.
	 * NOT_CERTIFIED_CHECKOUT).toString());
	 * expectedResourceAuditJavaObject.setStatus("200");
	 * expectedResourceAuditJavaObject.setDesc("OK");
	 * expectedResourceAuditJavaObject.setArtifactName(artifact.getArtifactName(
	 * )); AuditUtils.validateAudit(expectedResourceAuditJavaObject,
	 * auditAction, null); }
	 */

	/*
	 * protected void resourceArtifatValidateAuditWithErrorMessage(String
	 * actionStatus, ResourceReqDetails resourceDetails, String auditAction,
	 * String setCurrState, Object ... variables)throws Exception { ErrorInfo
	 * errorInfo = utils.parseYaml(actionStatus);
	 * ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	 * Convertor.constructFieldsForAuditValidation(resourceDetails,
	 * resourceDetails.getVersion(), sdncUserDetails);
	 * expectedResourceAuditJavaObject.setAction(auditAction);
	 * expectedResourceAuditJavaObject.setPrevState("");
	 * expectedResourceAuditJavaObject.setPrevVersion("");
	 * expectedResourceAuditJavaObject.setCurrState(setCurrState);
	 * expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString())
	 * ;
	 * expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc(variables)
	 * ); expectedResourceAuditJavaObject.setArtifactName("");
	 * AuditUtils.validateAudit(expectedResourceAuditJavaObject, auditAction,
	 * null); }
	 */
}

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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.ExceptionEnumType;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class ErrorValidationUtils {

	static Logger logger = LoggerFactory.getLogger(Utils.class.getName());

	public static void checkBodyResponseOnError(String errorType, List<String> variables, String actualResponse)
			throws FileNotFoundException, JSONException {

		ErrorInfo errorInfo = parseErrorConfigYaml(errorType);
		JSONObject expectedResponseBody = null;
		if (errorInfo.getMessageId() != null) {
			if (errorInfo.getMessageId().contains("SVC")) {
				expectedResponseBody = restExceptionFormatBuilder(errorInfo.getMessageId(), errorInfo.getMessage(),
						variables, ExceptionEnumType.SERVICE_EXCEPTION.getValue());
			} else {
				expectedResponseBody = restExceptionFormatBuilder(errorInfo.getMessageId(), errorInfo.getMessage(),
						variables, ExceptionEnumType.POLICY_EXCPTION.getValue());
			}
		}
		actualResponse = actualResponse.replaceAll("\\n", "");
		logger.debug("actualResponse - {}",actualResponse);
		logger.debug("expectedResponseBody - {}",expectedResponseBody);
		assertEquals(expectedResponseBody, new JSONObject(actualResponse));
	}
	
	public static String checkUIResponseOnError(String errorType)
			throws FileNotFoundException, JSONException {

		ErrorInfo errorInfo = parseErrorConfigYaml(errorType);
		String messageId = errorInfo.getMessageId();
		
		return messageId;
	}

	public static JSONObject restExceptionFormatBuilder(String messageId, String text, List<String> variables,
			String type) {

		JSONObject simpleElements = new JSONObject();
		JSONObject exceptionType = new JSONObject();
		JSONObject requestError = new JSONObject();

		try {
			simpleElements.put("messageId", messageId);
			simpleElements.put("text", text);
			simpleElements.put("variables", variables);
			exceptionType.put(type, simpleElements);
			requestError.put("requestError", exceptionType);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return requestError;

	}

	public static ErrorInfo parseErrorConfigYaml(String error) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		ErrorInfo errInfo = null;
		Config config = Utils.getConfig();
		String errorConfigurationFile = config.getErrorConfigurationFile();
		File file = new File(errorConfigurationFile);
		// File file = new
		// File("../catalog-be/src/main/resources/config/error-configuration.yaml");
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
		// System.out.println(map.get("errors"));
		@SuppressWarnings("unchecked")
		Map<String, ErrorInfo> errorMap = (Map<String, ErrorInfo>) map.get("errors");
		@SuppressWarnings("unchecked")
		Map<String, Object> errorInfo = (Map<String, Object>) errorMap.get(error);

		String message = (String) errorInfo.get("message");
		String messageId = (String) errorInfo.get("messageId");
		int code = (Integer) errorInfo.get("code");
		errInfo = new ErrorInfo(code, message, messageId);

		return errInfo;
	}

}

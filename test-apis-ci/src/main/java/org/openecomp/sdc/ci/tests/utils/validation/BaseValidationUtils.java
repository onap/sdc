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

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.exception.ResponseFormat;
import org.testng.Assert;

public class BaseValidationUtils {

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
	public static final int STATUS_CODE_RESTRICTED_OPERATION = 409;
	public static final int STATUS_CODE_ALREADY_EXISTS = 409;

	// ------
	protected static Boolean checkErrorCode(RestResponse deleteResponse) {
		if (deleteResponse.getErrorCode() == STATUS_CODE_SUCCESS
				|| deleteResponse.getErrorCode() == STATUS_CODE_DELETE) {
			return true;
		}
		return false;
	}

	// *** STATUS CODE VALIDATION UTIITIES ****
	public static void checkStatusCode(RestResponse response, String assertMessage, boolean AND, int... statuses) {
		int statusCode = response.getErrorCode();
		for (int status : statuses) {
			if (AND && statusCode != status) {
				Assert.fail(assertMessage + " status: " + statusCode);
			} else if (statusCode == status) {
				return;
			}
		}
		if (!AND) {
			Assert.fail(assertMessage + " status: " + statusCode);
		}
	}

	public static void checkDeleteResponse(RestResponse response) {
		checkStatusCode(response, "delete request failed", false, STATUS_CODE_DELETE, STATUS_CODE_NOT_FOUND,
				STATUS_CODE_SUCCESS); // STATUS_CODE_SUCCESS for deActivate user
	}

	public static void checkCreateResponse(RestResponse response) {
		checkStatusCode(response, "create request failed", false, STATUS_CODE_CREATED);
	}

	public static void checkSuccess(RestResponse response) {
		checkStatusCode(response, "request failed", false, STATUS_CODE_SUCCESS);
	}

	public static void checkErrorResponse(RestResponse errorResponse, ActionStatus actionStatus,
			String... expectedVariables) throws FileNotFoundException {
		// Expected error
		ErrorInfo expectedError = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		String expectedMessage = expectedError.getMessage();

		// Actual error
		ResponseFormat responseFormat = ResponseParser.parseToObjectUsingMapper(errorResponse.getResponse(),
				ResponseFormat.class);
		String actualMessage = responseFormat.getText();
		String[] actualVariables = responseFormat.getVariables();

		assertEquals("Unexpected error message", expectedMessage, actualMessage);
		assertEquals("Unexpected error variables", Arrays.asList(expectedVariables), Arrays.asList(actualVariables));
	}

	public static void checkErrorMessageResponse(RestResponse errorResponse, ActionStatus actionStatus)
			throws FileNotFoundException {
		// Expected error
		ErrorInfo expectedError = ErrorValidationUtils.parseErrorConfigYaml(actionStatus.name());
		String expectedMessage = expectedError.getMessage();

		// Actual error
		ResponseFormat responseFormat = ResponseParser.parseToObjectUsingMapper(errorResponse.getResponse(),
				ResponseFormat.class);
		String actualMessage = responseFormat.getText();

		assertEquals("Unexpected error message", expectedMessage, actualMessage);
	}
}

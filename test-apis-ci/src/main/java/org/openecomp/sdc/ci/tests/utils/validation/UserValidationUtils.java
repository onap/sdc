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
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.users.AddUserAuditMessageInfo;
import org.openecomp.sdc.ci.tests.users.UserAuditJavaObject;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UserValidationUtils {
	public static void compareExpectedAndActualUsers(User expected, User actual) {

		String actualFirstName = actual.getFirstName();
		String expectedFirstName = expected.getFirstName();
		assertEquals("check user first name - ", expectedFirstName, actualFirstName);

		String actualLastName = actual.getLastName();
		String expectedLastName = expected.getLastName();
		assertEquals("check user last name - ", expectedLastName, actualLastName);

		String actualUserId = actual.getUserId();
		String expectedUserId = expected.getUserId();
		assertEquals("check user userId - ", expectedUserId, actualUserId);

		String actualEmail = actual.getEmail();
		String expectedEmail = expected.getEmail();
		assertEquals("check user email - ", expectedEmail, actualEmail);

		Long actualLastLoginTime = actual.getLastLoginTime();
		Long expectedLastLoginTime = expected.getLastLoginTime();
		assertEquals("check user last login time - ", expectedLastLoginTime, actualLastLoginTime);

		String actualRole = actual.getRole();
		if (expected.getRole() == null) {
			String expectedRole = UserRoleEnum.DESIGNER.name();
			assertEquals("check user role - ", expectedRole, actualRole);
		} else {
			String expectedRole = expected.getRole();
			assertEquals("check user role - ", expectedRole, actualRole);
		}

		UserStatusEnum actualStatus = expected.getStatus();
		UserStatusEnum expectedStatus = expected.getStatus();
		assertEquals("check user status - ", expectedStatus, actualStatus);
	}

	public static void validateDeleteUserAuditMessage(User sdncUserDetails, User sdncModifierDetails,
			String responseCode, String responseMessage, AddUserAuditMessageInfo addUserAuditMessageInfo) {
		String action = "DeleteUser";
		validateUserAuditMessage(sdncUserDetails, sdncModifierDetails, responseCode, responseMessage,
				addUserAuditMessageInfo, action);

	}

	private static void validateUserAuditMessage(User sdncUserDetails, User sdncModifierDetails, String responseCode,
			String responseMessage, AddUserAuditMessageInfo addUserAuditMessageInfo, String expectedAction) {

		assertEquals("check audit action - ", expectedAction, addUserAuditMessageInfo.getACTION());

		// String expectedModifierFirstLastName =
		// sdncModifierDetails.getFirstName() + " " +
		// sdncModifierDetails.getLastName();
		// assertEquals("check audit modifier name - ",
		// expectedModifierFirstLastName,
		// addUserAuditMessageInfo.getMODIFIER_NAME());
		String fullName = sdncModifierDetails.getFullName();
		if (sdncModifierDetails.getFullName().equals(" ")) {
			fullName = "";
		}
		String expectedModifierId = fullName + "(" + sdncModifierDetails.getUserId() + ")";
		assertEquals("check audit modifier uid - ", expectedModifierId, addUserAuditMessageInfo.getMODIFIER());

		String expectedUserFirstLastName = sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName();
		if (expectedUserFirstLastName.equals("null null")) {
			expectedUserFirstLastName = "";
		}

		String email = (sdncUserDetails.getEmail() == null) ? "" : sdncUserDetails.getEmail();
		String role = (sdncUserDetails.getRole() == null) ? "DESIGNER" : sdncUserDetails.getRole();

		String formatedUser = String.format("%s,%s,%s,%s", sdncUserDetails.getUserId(), expectedUserFirstLastName,
				email, role);

		//
		// String expectedUserFirstLastName = sdncUserDetails.getFirstName() + "
		// " + sdncUserDetails.getLastName();
		// if (expectedUserFirstLastName.equals("null null")) {
		// expectedUserFirstLastName = "";
		// }
		//
		// String expectedUserFirstLastName = "";
		// expectedUserFirstLastName += sdncUserDetails.getFirstName() == null ?
		// "" : sdncUserDetails.getFirstName();
		// String lastName = sdncUserDetails.getLastName() == null ? "" :
		// sdncUserDetails.getLastName();
		// if (expectedUserFirstLastName.isEmpty()) {
		// expectedUserFirstLastName = lastName;
		// } else {
		// expectedUserFirstLastName += " " + lastName;
		// }

		assertEquals("check audit user name - ", formatedUser, addUserAuditMessageInfo.getUSER());

		// String expectedUserUid = sdncUserDetails.getUserId();
		// assertEquals("check audit user uid - ", expectedUserUid,
		// addUserAuditMessageInfo.getUSER_UID());
		//
		// String expectedUserEmail = sdncUserDetails.getEmail() == null ? "" :
		// sdncUserDetails.getEmail();
		// //TODO: esofer check with Andrey. Audit return "" but in user we have
		// null
		// assertEquals("check audit user email - ", expectedUserEmail,
		// addUserAuditMessageInfo.getUSER_EMAIL());
		//
		// String expectedUserRole = sdncUserDetails.getRole();
		// if (expectedUserRole ==null){
		// expectedUserRole = "DESIGNER";
		// assertEquals("check audit user role - ", expectedUserRole,
		// addUserAuditMessageInfo.getUSER_ROLE());
		// }else{
		// assertEquals("check audit user role - ", expectedUserRole,
		// addUserAuditMessageInfo.getUSER_ROLE());
		// }

		String expectedUserResponseCode = responseCode;
		assertEquals("check audit user response code - ", expectedUserResponseCode,
				addUserAuditMessageInfo.getSTATUS());

		String expectedUserResponseMessage = responseMessage;
		assertEquals("check audit user response message - ", expectedUserResponseMessage,
				addUserAuditMessageInfo.getDESC());

	}

	public static void validateDataAgainstAuditDB_access(User sdncUserDetails, UserAuditJavaObject auditJavaObject,
			RestResponse restResponse, ErrorInfo errorInfo, List<String> variables) {

		validateAuditDataAgainstAuditDbInAccess(sdncUserDetails, auditJavaObject, restResponse, errorInfo, variables);

	}

	public static void validateAuditDataAgainstAuditDbInAccess(User sdncUserDetails,
			UserAuditJavaObject auditJavaObject, RestResponse restResponse, ErrorInfo errorInfo,
			List<String> variables) {

		String expected;

		expected = "Access";
		assertEquals("ACTION- ", expected, auditJavaObject.getACTION());

		if (sdncUserDetails.getFirstName() != StringUtils.EMPTY && sdncUserDetails.getLastName() != StringUtils.EMPTY) {
			expected = sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName();
		} else {
			expected = StringUtils.EMPTY;
		}
		String formatedUser = String.format("%s(%s)", expected, sdncUserDetails.getUserId());
		assertTrue(
				"check audit user: expected start with - " + formatedUser + " ,actual - " + auditJavaObject.getUSER(),
				auditJavaObject.getUSER().startsWith(formatedUser));

		expected = restResponse.getErrorCode().toString();
		assertEquals("check audit user status code - ", expected, auditJavaObject.getSTATUS());

		if (restResponse.getErrorCode() == 200 || restResponse.getErrorCode() == 201) {
			expected = errorInfo.getMessage();
		} else {
			expected = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		}

		assertEquals("check audit user desc - ", expected, auditJavaObject.getDESC());

		// expected = sdncUserDetails.getUserId();
		// assertEquals(expected, auditJavaObject.getUSER());

	}

	public static void validateUserDetailsOnResponse(User sdncUserDetails, String userDetailsOnResponse) {

		String actualFirstName = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "firstName");
		String expectedFirstName = sdncUserDetails.getFirstName();
		assertEquals("check user first name - ", expectedFirstName, actualFirstName);

		String actualLastName = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "lastName");
		String expectedLastName = sdncUserDetails.getLastName();
		assertEquals("check user last name - ", expectedLastName, actualLastName);

		String actualUserId = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "userId");
		String expectedUserId = sdncUserDetails.getUserId();
		assertEquals("check user userId - ", expectedUserId, actualUserId);

		String actualEmail = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "email");
		String expectedEmail = sdncUserDetails.getEmail();
		assertEquals("check user email - ", expectedEmail, actualEmail);

		String actualRole = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "role");
		if (sdncUserDetails.getRole() == null) {
			String expectedRole = UserRoleEnum.DESIGNER.name();
			assertEquals("check user role - ", expectedRole, actualRole);
		} else {
			String expectedRole = sdncUserDetails.getRole();
			assertEquals("check user role - ", expectedRole, actualRole);
		}

		String actualStatus = Utils.getJsonObjectValueByKey(userDetailsOnResponse, "status");
		String expectedStatus = sdncUserDetails.getStatus().name();
		assertEquals("check user status - ", expectedStatus, actualStatus);

	}

	public static AddUserAuditMessageInfo getAddUserAuditMessage(String action) throws Exception {

		Gson gson = new Gson();
		String index = "auditingevents*";
		String type = "useradminevent";
		String pattern = "/_search?q=ACTION:\"" + action + "\"";
		String auditingMessage = DbUtils.retrieveAuditMessagesByPattern(pattern);
		// String auditingMessage = retrieveAuditMessageByIndexType(index, type,
		// pattern);
		JsonElement jElement = new JsonParser().parse(auditingMessage);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonObject hitsObject = (JsonObject) jObject.get("hits");
		JsonArray hitsArray = (JsonArray) hitsObject.get("hits");
		// for (int i = 0; i < hitsArray.size();){
		if (hitsArray != null) {
			JsonObject jHitObject = (JsonObject) hitsArray.get(0);
			JsonObject jSourceObject = (JsonObject) jHitObject.get("_source");
			AddUserAuditMessageInfo addUserAuditMessageInfo = new AddUserAuditMessageInfo();
			addUserAuditMessageInfo = gson.fromJson(jSourceObject, AddUserAuditMessageInfo.class);
			return addUserAuditMessageInfo;
		}
		return null;

	}

	public static void validateAddUserAuditMessage(User sdncUserDetails, User sdncModifierDetails, String responseCode,
			String responseMessage, AddUserAuditMessageInfo addUserAuditMessageInfo) {

		String action = "AddUser";
		validateUserAuditMessage(sdncUserDetails, sdncModifierDetails, responseCode, responseMessage,
				addUserAuditMessageInfo, action);

	}

}

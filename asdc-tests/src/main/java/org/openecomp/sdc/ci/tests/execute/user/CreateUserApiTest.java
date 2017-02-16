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

package org.openecomp.sdc.ci.tests.execute.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedUserCRUDAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.users.UserResponseMessageEnum;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.UserValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CreateUserApiTest extends ComponentBaseTest {

	protected User sdncAdminUser;
	protected User sdncDesignerUser;
	protected User sdncGovernorUser;
	protected User sdncTesterUser;

	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_SUCSESS_CREATED = 201;
	public static final int STATUS_CODE_SUCCESS_DELETE_GET = 200;
	public static final int STATUS_CODE_INVALID_CONTENT = 400;
	public static final int STATUS_CODE_MISSING_DATA = 400;
	public static final int STATUS_CODE_MISSING_INFORMATION = 403;
	public static final int STATUS_CODE_RESTRICTED_ACCESS = 403;
	public static final int STATUS_CODE_NOT_FOUND = 404;
	public static final int STATUS_CODE_RESTRICTED_OPERATION = 409;
	public static final int USER_ALREADY_EXIST = 409;
	public static final int INVALID_ROLE = 400;

	@Rule
	public static TestName name = new TestName();

	public CreateUserApiTest() {
		super(name, CreateUserApiTest.class.getName());
	}

	@BeforeMethod
	public void init() {
		sdncAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncDesignerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncGovernorUser = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
		sdncTesterUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);

	}

	// Story : REST API to provision new user (POST) - US429379
	// must parameters: UserId and Email

	// **********************************************************201***************************************************
	// create user with full parameter set(UserID, First Name, Last Name, Email,
	// Role = "DESIGNER", Creator details)
	// expected 201 Created
	@Test
	public void createUser() throws Exception {

		// user initialization
		String httpCspUserId = "km2000";
		String userFirstName = "Kot";
		String userLastName = "Matroskin";
		String email = "km2000@intl.sdc.com";
		String role = "ADMIN";
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		String addUser = "AddUser";
		UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		DbUtils.cleanAllAudits();
		RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);

		AssertJUnit.assertNotNull("check response object is not null after create user", createUserResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create user",
				createUserResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create user", 201,
				createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());

		ExpectedUserCRUDAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(addUser,
				sdncAdminUser, ActionStatus.CREATED, sdncUserDetails, null);
		AuditValidationUtils.validateAddUserAudit(constructFieldsForAuditValidation, addUser);
		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());
	}

	protected static final String ADD_USER = "AddUser";

	private User mechIdUser = new User();
	private User emptyUser = new User();
	private static final User adminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

	@AfterMethod
	public void setup() throws IOException {
		UserRestUtils.deactivateUser(mechIdUser, adminUser);
	}

	// create default user(UserID, Email, Creator details)
	// expected: role = DESIGNER, first and last name = null, 201 Created
	@Test
	public void createDefaultUser() throws Exception {
		// user initialization
		String httpCspUserId = "km2000";
		String userFirstName = null;
		String userLastName = null;
		String email = null;
		String role = null;
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		deleteUserAndAudit(sdncUserDetails);
		RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);

		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());
		UserValidationUtils.validateAddUserAuditMessage(sdncUserDetails, sdncAdminUser, "201",
				UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());

	}

	// create user with one optional parameter first name (UserID, Email, First
	// Name, Creator details)
	// expected: role = DESIGNER, last name = null, 201 Created
	@Test
	public void createUserFirstName() throws Exception {
		// user initialization
		String httpCspUserId = "km2000";
		String userFirstName = "Kot";
		String userLastName = null;
		String email = null;
		String role = null;
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		deleteUserAndAudit(sdncUserDetails);
		RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);

		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());
		UserValidationUtils.validateAddUserAuditMessage(sdncUserDetails, sdncAdminUser, "201",
				UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());
	}

	@Test
	public void createDeleteOpsUser() throws Exception {

		String httpCspUserId = "oo2000";
		String userFirstName = "ops";
		String userLastName = "opsLast";
		String email = "ops@intl.sdc.com";
		String role = "OPS";
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		deleteUserAndAudit(sdncUserDetails);
		RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);

		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());
		UserValidationUtils.validateAddUserAuditMessage(sdncUserDetails, sdncAdminUser, "201",
				UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());

		UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		RestResponse getDeletedUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		assertEquals("Check response code after delete user", 404, getDeletedUserResponse.getErrorCode().intValue());

	}

	@Test
	public void createDeleteGOVERNORUser() throws Exception {

		String httpCspUserId = "gg2000";
		String userFirstName = "gov";
		String userLastName = "govLast";
		String email = "gov@intl.sdc.com";
		String role = "GOVERNOR";
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		deleteUserAndAudit(sdncUserDetails);
		RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());
		UserValidationUtils.validateAddUserAuditMessage(sdncUserDetails, sdncAdminUser, "201",
				UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());

		UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		RestResponse getDeletedUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		assertEquals("Check response code after delete user", 404, getDeletedUserResponse.getErrorCode().intValue());

	}

	// Benny
	// Admin Create OPS user
	@Test
	public void createOpsUser() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "aa1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		User expectedOpsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedOpsUser);
		RestResponse createUserResponse = UserRestUtils.createUser(expectedOpsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedOpsUser, createUserResponse.getResponse());
		deleteAndCheckUserResponse(expectedOpsUser, 200);

	}

	// Admin Create GOVERNOR user
	@Test
	public void createGovernorUser() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "aa1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		User expectedUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedUser);
		RestResponse createUserResponse = UserRestUtils.createUser(expectedUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedUser, createUserResponse.getResponse());
		RestResponse getUserResponse = UserRestUtils.getUser(expectedUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedUser, getUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(expectedUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from OPS to GOVERNOR
	@Test
	public void updateOpsUserRole() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ab1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());

		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(governerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(governerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Designer Create OPS user -409 Response Restricted operation
	@Test
	public void createOpsUserByDesigner() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "aa1122";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		User expectedOpsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedOpsUser);
		RestResponse createUserResponse = UserRestUtils.createUser(expectedOpsUser, sdncDesignerUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 409, createUserResponse.getErrorCode().intValue());
		assertEquals("Check response code after create user", "Conflict", createUserResponse.getResponseMessage());
	}

	// Tester Create OPS user -409 Response Restricted operation
	@Test
	public void createOpsUserByTester() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "aa1122";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		User expectedOpsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedOpsUser);
		RestResponse createUserResponse = UserRestUtils.createUser(expectedOpsUser, sdncTesterUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 409, createUserResponse.getErrorCode().intValue());
		assertEquals("Check response code after create user", "Conflict", createUserResponse.getResponseMessage());
	}

	// Designer Try Update OPS user role to GOVERNOR - Response 409
	@Test
	public void updateOpsUserRolebyDesigner() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "bt751e";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// Admin create user with OPS role
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// Designer user try to update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncDesignerUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 409, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Conflict",
				updateUserRoleResponse.getResponseMessage());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting  user", deleteOpsUser);
		assertEquals("Check response code after deleting  user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Tester Try Update OPS user role to GOVERNOR - Response 409
	@Test
	public void updateOpsUserRolebyTester() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "bt751w";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// Admin create user with OPS role
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// Designer user try to update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncTesterUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 409, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Conflict",
				updateUserRoleResponse.getResponseMessage());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from OPS to Designer
	@Test
	public void updateOpsUserRoleFromOpsToDesigner() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ab1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "DESIGNER";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User designerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());

		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(designerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(designerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from OPS to TESTER
	@Test
	public void updateOpsUserRoleFromOpsToTester() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ac1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "TESTER";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User testerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());

		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(testerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(testerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Tester try to Update user role from OPS to GOVERNOR - Response 409
	// Conflict
	@Test
	public void updateOpsUserRoleByTester() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ad1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		// Create user by Admin
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR by Tester
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncTesterUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 409, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Conflict",
				updateUserRoleResponse.getResponseMessage());

		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Designer try to Update user role from OPS to GOVERNOR - Response 409
	// Conflict
	@Test
	public void updateOpsUserRoleByDesigner() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ad1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		// User governerUser = new User(userFirstName,
		// userLastName,httpCspUserId, email, updatedRole);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		// Create user by Admin
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR by Tester
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncDesignerUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 409, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Conflict",
				updateUserRoleResponse.getResponseMessage());

		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Create OPS user - user already exist
	@Test
	public void createOpsUserAlreadyExist() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "af1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "OPS";
		User expectedOpsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedOpsUser);
		RestResponse createUserResponse = UserRestUtils.createUser(expectedOpsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedOpsUser, createUserResponse.getResponse());
		// Create user that already exists
		RestResponse createUserAgainResponse = UserRestUtils.createUser(expectedOpsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserAgainResponse);
		assertNotNull("check error code exists in response after create user", createUserAgainResponse.getErrorCode());
		assertEquals("Check response code after create user", 409, createUserAgainResponse.getErrorCode().intValue());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(expectedOpsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting  user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from OPS to GOVERNOR - user already has GOVERNOR
	// role
	@Test
	public void updateRoleToSameRole() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ag1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "GOVERNOR";
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		RestResponse createUserResponse = UserRestUtils.createUser(opsUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from GOVERNOR to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				opsUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(governerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(governerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from Tester to GOVERNOR - 200 response

	// Admin Update user role from Designer to GOVERNOR - 200 response
	@Test
	public void updateUserRoleDesignerToGovernor() throws Exception {
		DbUtils.cleanAllAudits();
		String httpCspUserId = "ah1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "DESIGNER";
		String updatedRole = "GOVERNOR";
		User designerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(designerUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(designerUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from TESTER to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				designerUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());
		// Update user role
		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(governerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(governerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(designerUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update deactivated user role - response 404 (user not found)
	@Test
	public void updateRoleToDeactivatedUser() throws Exception {
		DbUtils.cleanAllAudits();
		String httpCspUserId = "aj1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "DESIGNER";
		String updatedRole = "GOVERNOR";
		User designerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User governerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(designerUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(designerUser, createUserResponse.getResponse());
		deleteAndCheckUserResponse(designerUser, 200);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role - user deActivted
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				designerUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after create user", 404, updateUserRoleResponse.getErrorCode().intValue());
	}

	// Admin Update user role, user does not exist in DB - response 404 (user
	// not found)
	@Test
	public void updateRoleForNonExistingUser() throws Exception {
		DbUtils.cleanAllAudits();
		String httpCspUserId = "aj1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "DESIGNER";
		String updatedRole = "GOVERNOR";
		User designerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		// User governerUser = new User(userFirstName,
		// userLastName,httpCspUserId, email, updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role - user deActivted
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				designerUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 404, updateUserRoleResponse.getErrorCode().intValue());

	}

	// Admin Update user role from GOVERNOR to TESTER
	@Test
	public void updateRoleFromGovernorToTester() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ak1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "TESTER";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User testerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());

		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(testerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(testerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(governorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from GOVERNOR to DESIGNER
	@Test
	public void updateRoleFromGovernorToDesigner() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ak1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "DESIGNER";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User designerUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());

		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(designerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(designerUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(governorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role from GOVERNOR to OPS
	@Test
	public void updateRoleFromGovernorToOps() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ak1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "OPS";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User opsUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(opsUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(opsUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		deleteAndCheckUserResponse(governorUser, 200);

	}

	private void deleteAndCheckUserResponse(User userDetailes, int expectedResponseCode) throws IOException {
		RestResponse deleteUser = UserRestUtils.deleteUser(sdncGovernorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteUser);
		assertEquals("Check response code after deleting user", expectedResponseCode,
				deleteUser.getErrorCode().intValue());
	}

	// Admin Update user role from GOVERNOR to ADMIN
	@Test
	public void updateRoleFromGovernorToAdmin() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ak1000";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "ADMIN";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User adminUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 200, updateUserRoleResponse.getErrorCode().intValue());

		RestResponse getUpdatedRoleUserResponse = UserRestUtils.getUser(adminUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(adminUser, getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(governorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role to non existing role - Response 400 Bad Request
	@Test
	public void updateRoleToNonExistingRole() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "al1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "VVVVVVV";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User newUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role from OPS to GOVERNOR
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 400, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Bad Request",
				updateUserRoleResponse.getResponseMessage());

		// RestResponse getUpdatedRoleUserResponse =
		// UserRestUtils.getUser(adminUser,sdncAdminUser);
		// UserValidationUtils.validateUserDetailsOnResponse(adminUser,getUpdatedRoleUserResponse.getResponse());
		// Delete OPS user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(governorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}

	// Admin Update user role to null - Response 400 Bad Request
	@Test
	public void updateRoleToNull() throws Exception {
		DbUtils.cleanAllAudits();

		String httpCspUserId = "ax1001";
		String userFirstName = "Benny";
		String userLastName = "Tal";
		String email = "optBenny@intl.sdc.com";
		String role = "GOVERNOR";
		String updatedRole = "";
		User governorUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		User newUser = new User(userFirstName, userLastName, httpCspUserId, email, updatedRole, null);
		// UserRestUtils.deleteUser(opsUser, sdncAdminUser, true);
		// UserRestUtils.deleteUser(UserUpdateRole, sdncAdminUser);
		RestResponse createUserResponse = UserRestUtils.createUser(governorUser, sdncAdminUser);
		assertNotNull("check response object is not null after create user", createUserResponse);
		assertNotNull("check error code exists in response after create user", createUserResponse.getErrorCode());
		assertEquals("Check response code after create user", 201, createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(governorUser, createUserResponse.getResponse());
		// opsUser.setRole(updatedRole);
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// update user role
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				governorUser.getUserId());
		assertNotNull("check response object is not null after updating user", updateUserRoleResponse);
		assertNotNull("check error code exists in response after updating user", updateUserRoleResponse.getErrorCode());
		assertEquals("Check response code after updating user", 400, updateUserRoleResponse.getErrorCode().intValue());
		assertEquals("Check response code after updating user", "Bad Request",
				updateUserRoleResponse.getResponseMessage());
		// Delete user
		RestResponse deleteOpsUser = UserRestUtils.deleteUser(governorUser, sdncAdminUser, true);
		assertNotNull("check response object is not null after deleting user", deleteOpsUser);
		assertEquals("Check response code after deleting user", 200, deleteOpsUser.getErrorCode().intValue());

	}
	
	@Test
	public void createProductManagerUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Man";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_MANAGER";
		User expectedProductManagerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser, true);
		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, createUserResponse.getResponse());
		// Audit validation
		UserValidationUtils.validateAddUserAuditMessage(expectedProductManagerUser, sdncAdminUser,
				Integer.toString(STATUS_CODE_SUCSESS_CREATED), UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductManagerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test
	public void createProductStrategistUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodStr@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		UserRestUtils.deleteUser(expectedProductStrategistUser, sdncAdminUser, true);
		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductStrategistUser,
				createUserResponse.getResponse());
		// Audit validation
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, sdncAdminUser,
				Integer.toString(STATUS_CODE_SUCSESS_CREATED), UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductStrategistUser, getUserResponse.getResponse());
		// Delete ProductStrategist user
		RestResponse deleteProductStrategistUser = UserRestUtils.deleteUser(expectedProductStrategistUser,
				sdncAdminUser, true);
		assertNotNull("Check response object is not null after deleting OPS user", deleteProductStrategistUser);
		assertEquals("Check response code after deleting OPS user", 200,
				deleteProductStrategistUser.getErrorCode().intValue());
	}

	@Test
	public void createProductStrategistUserByNonAdminUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodStr@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		UserRestUtils.deleteUser(expectedProductStrategistUser, sdncAdminUser, true);
		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, sdncDesignerUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_RESTRICTED_OPERATION,
				createUserResponse.getErrorCode().intValue());
		// Audit validation
		expectedProductStrategistUser.setUserId("");
		expectedProductStrategistUser.setFirstName(null);
		expectedProductStrategistUser.setLastName(null);
		expectedProductStrategistUser.setEmail("");
		expectedProductStrategistUser.setRole("");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, sdncDesignerUser,
				Integer.toString(STATUS_CODE_RESTRICTED_OPERATION), errorInfo.getAuditDesc(""),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// Try to get user - user is not created
		expectedProductStrategistUser.setUserId("pm1000");
		expectedProductStrategistUser.setFirstName("Prod");
		expectedProductStrategistUser.setLastName("Strategist");
		expectedProductStrategistUser.setEmail("prodStr@intl.sdc.com");
		expectedProductStrategistUser.setRole("PRODUCT_STRATEGIST");
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, getUserResponse.getErrorCode().intValue());
	}

	@Test
	public void createProductManagerUserByNonAdminUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Man";
		String email = "prodStr@intl.sdc.com";
		String role = "PRODUCT_MANAGER";
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		UserRestUtils.deleteUser(expectedProductStrategistUser, sdncAdminUser, true);
		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, sdncDesignerUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_RESTRICTED_OPERATION,
				createUserResponse.getErrorCode().intValue());
		// Audit validation
		expectedProductStrategistUser.setUserId("");
		expectedProductStrategistUser.setFirstName(null);
		expectedProductStrategistUser.setLastName(null);
		expectedProductStrategistUser.setEmail("");
		expectedProductStrategistUser.setRole("");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, sdncDesignerUser,
				Integer.toString(STATUS_CODE_RESTRICTED_OPERATION), errorInfo.getAuditDesc(""),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// Try to get user - user is not created
		expectedProductStrategistUser.setUserId("pm1000");
		expectedProductStrategistUser.setFirstName("Prod");
		expectedProductStrategistUser.setLastName("Strategist");
		expectedProductStrategistUser.setEmail("prodStr@intl.sdc.com");
		expectedProductStrategistUser.setRole("PRODUCT_MANAGER");
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, getUserResponse.getErrorCode().intValue());
	}

	@Test
	public void createProductStrategistUserByNonExistingUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodStr@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		User noSdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		noSdncUserDetails.setRole("blabla");
		noSdncUserDetails.setUserId("bt750h");
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, noSdncUserDetails);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_NOT_FOUND,
				createUserResponse.getErrorCode().intValue());
		// Audit validation
		expectedProductStrategistUser.setUserId("");
		expectedProductStrategistUser.setFirstName(null);
		expectedProductStrategistUser.setLastName(null);
		expectedProductStrategistUser.setEmail("");
		expectedProductStrategistUser.setRole("");
		noSdncUserDetails.setFirstName("");
		noSdncUserDetails.setLastName("");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_NOT_FOUND.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, noSdncUserDetails,
				Integer.toString(STATUS_CODE_NOT_FOUND), errorInfo.getAuditDesc(noSdncUserDetails.getUserId()),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// Try to get user - user is not created
		expectedProductStrategistUser.setUserId("pm1000");
		expectedProductStrategistUser.setFirstName("Prod");
		expectedProductStrategistUser.setLastName("Strategist");
		expectedProductStrategistUser.setEmail("prodStr@intl.sdc.com");
		expectedProductStrategistUser.setRole("PRODUCT_STRATEGIST");
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, getUserResponse.getErrorCode().intValue());
	}

	@Test
	public void createProductManagerUserByNonExistingUser() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Man";
		String email = "prodStr@intl.sdc.com";
		String role = "PRODUCT_MANAGER";
		User noSdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		noSdncUserDetails.setRole("blabla");
		noSdncUserDetails.setUserId("bt750h");
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		DbUtils.deleteFromEsDbByPattern("_all");
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, noSdncUserDetails);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_NOT_FOUND,
				createUserResponse.getErrorCode().intValue());
		// Audit validation
		expectedProductStrategistUser.setUserId("");
		expectedProductStrategistUser.setFirstName(null);
		expectedProductStrategistUser.setLastName(null);
		expectedProductStrategistUser.setEmail("");
		expectedProductStrategistUser.setRole("");
		noSdncUserDetails.setFirstName("");
		noSdncUserDetails.setLastName("");
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_NOT_FOUND.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, noSdncUserDetails,
				Integer.toString(STATUS_CODE_NOT_FOUND), errorInfo.getAuditDesc(noSdncUserDetails.getUserId()),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// Try to get user - user is not created
		expectedProductStrategistUser.setUserId("pm1000");
		expectedProductStrategistUser.setFirstName("Prod");
		expectedProductStrategistUser.setLastName("Strategist");
		expectedProductStrategistUser.setEmail("prodStr@intl.sdc.com");
		expectedProductStrategistUser.setRole("PRODUCT_MANAGER");
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code ", STATUS_CODE_NOT_FOUND, getUserResponse.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void updateProjectManagerRole() throws Exception {
		// Update user role from PRODUCT_STRATEGIST to PRODUCT_MANAGER
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Man";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_MANAGER";
		String updatedRole = "GOVERNOR";
		User expectedProductManagerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductManagerUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, createUserResponse.getResponse());
		// Update user role
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// Update user role from PRODUCT_STRATEGIST to PRODUCT_MANAGER
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				expectedProductManagerUser.getUserId());
		assertEquals("Check response code after create user", STATUS_CODE_SUCCESS,
				updateUserRoleResponse.getErrorCode().intValue());
		expectedProductManagerUser.setRole(updatedRole);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser,
				updateUserRoleResponse.getResponse());
		// Audit validation
		UserValidationUtils.validateAddUserAuditMessage(expectedProductManagerUser, sdncAdminUser,
				Integer.toString(STATUS_CODE_SUCCESS), UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("UpdateUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductManagerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void updateProductStrategistRole() throws Exception {
		// Update user role from PRODUCT_STRATEGIST to PRODUCT_MANAGER
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		String updatedRole = "TESTER";
		User expectedProductManagerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductManagerUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, createUserResponse.getResponse());
		// Update user role
		User newRoleUser = new User();
		newRoleUser.setRole(updatedRole);
		// Update user role from PRODUCT_STRATEGIST to PRODUCT_MANAGER
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				expectedProductManagerUser.getUserId());
		assertEquals("Check response code after create user", STATUS_CODE_SUCCESS,
				updateUserRoleResponse.getErrorCode().intValue());
		expectedProductManagerUser.setRole(updatedRole);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser,
				updateUserRoleResponse.getResponse());
		// Audit validation
		UserValidationUtils.validateAddUserAuditMessage(expectedProductManagerUser, sdncAdminUser,
				Integer.toString(STATUS_CODE_SUCCESS), UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
				UserValidationUtils.getAddUserAuditMessage("UpdateUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductManagerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test
	public void createProductManagerUserAlreadyExit() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Man";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_MANAGER";
		User expectedProductManagerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductManagerUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, createUserResponse.getResponse());
		// create same user again
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse createUserAgainResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", USER_ALREADY_EXIST,
				createUserAgainResponse.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_ALREADY_EXIST.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductManagerUser, sdncAdminUser,
				Integer.toString(USER_ALREADY_EXIST), errorInfo.getAuditDesc(expectedProductManagerUser.getUserId()),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductManagerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test
	public void createProductStrategistUserAlreadyExit() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		User expectedProductManagerUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductManagerUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, createUserResponse.getResponse());
		// create same user again
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse createUserAgainResponse = UserRestUtils.createUser(expectedProductManagerUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", USER_ALREADY_EXIST,
				createUserAgainResponse.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_ALREADY_EXIST.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductManagerUser, sdncAdminUser,
				Integer.toString(USER_ALREADY_EXIST), errorInfo.getAuditDesc(expectedProductManagerUser.getUserId()),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductManagerUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductManagerUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductManagerUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void UpdateProductStrategistToNonExistingRole() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodMan@intl.sdc.com";
		String role = "PRODUCT_STRATEGIST";
		String nonExistingRole = "BLABLA";
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductStrategistUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", STATUS_CODE_SUCSESS_CREATED,
				createUserResponse.getErrorCode().intValue());
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductStrategistUser,
				createUserResponse.getResponse());
		// Update user Role to non Existing role
		User newRoleUser = new User();
		newRoleUser.setRole(nonExistingRole);
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse updateUserRoleResponse = UserRestUtils.updateUserRole(newRoleUser, sdncAdminUser,
				expectedProductStrategistUser.getUserId());
		assertEquals("Check response code after updating user role", INVALID_ROLE,
				updateUserRoleResponse.getErrorCode().intValue());

		// Audit validation
		/*
		 * expectedProductStrategistUser.setUserId("");
		 * expectedProductStrategistUser.setFirstName(null);
		 * expectedProductStrategistUser.setLastName(null);
		 * expectedProductStrategistUser.setEmail("");
		 * expectedProductStrategistUser.setRole("");
		 */
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_ROLE.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, sdncAdminUser,
				Integer.toString(INVALID_ROLE), errorInfo.getAuditDesc(nonExistingRole),
				UserValidationUtils.getAddUserAuditMessage("UpdateUser"));
		// get user and compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(expectedProductStrategistUser, getUserResponse.getResponse());
		// Delete ProductManager user
		RestResponse deleteProductManagerUser = UserRestUtils.deleteUser(expectedProductStrategistUser, sdncAdminUser,
				true);
		assertEquals("Check response code after deleting OPS user", STATUS_CODE_SUCCESS,
				deleteProductManagerUser.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void createUserWithNonExistingRole() throws Exception {
		String httpCspUserId = "pm1000";
		String userFirstName = "Prod";
		String userLastName = "Strategist";
		String email = "prodMan@intl.sdc.com";
		String role = "BLABLA";
		User expectedProductStrategistUser = new User(userFirstName, userLastName, httpCspUserId, email, role, null);
		deleteUserAndAudit(expectedProductStrategistUser);
		// create user
		RestResponse createUserResponse = UserRestUtils.createUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check response code after create Product-Manager user", INVALID_ROLE,
				createUserResponse.getErrorCode().intValue());

		// Audit validation
		/*
		 * expectedProductStrategistUser.setUserId("");
		 * expectedProductStrategistUser.setFirstName(null);
		 * expectedProductStrategistUser.setLastName(null);
		 * expectedProductStrategistUser.setEmail("");
		 * expectedProductStrategistUser.setRole("");
		 */
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_ROLE.name());
		UserValidationUtils.validateAddUserAuditMessage(expectedProductStrategistUser, sdncAdminUser,
				Integer.toString(INVALID_ROLE), errorInfo.getAuditDesc(role),
				UserValidationUtils.getAddUserAuditMessage("AddUser"));
		// get user - verify user is not createdand compare with expected
		RestResponse getUserResponse = UserRestUtils.getUser(expectedProductStrategistUser, sdncAdminUser);
		assertEquals("Check user not created", STATUS_CODE_NOT_FOUND, getUserResponse.getErrorCode().intValue());

	}

	private void deleteUserAndAudit(User sdncUserDetails) throws IOException {
		UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		DbUtils.cleanAllAudits();
	}

}

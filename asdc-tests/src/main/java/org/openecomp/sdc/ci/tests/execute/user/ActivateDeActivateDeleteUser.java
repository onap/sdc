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
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedUserCRUDAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.users.UserResponseMessageEnum;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.UserValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * @author alitvinsky
 *
 */
public class ActivateDeActivateDeleteUser extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(ActivateDeActivateDeleteUser.class.getName());
	protected Gson gson = new Gson();
	protected User sdncAdminUser;

	@BeforeMethod
	public void init() {
		sdncAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

	}

	@Rule
	public static TestName name = new TestName();

	public ActivateDeActivateDeleteUser() {
		super(name, ActivateDeActivateDeleteUser.class.getName());
	}

	@Test
	public void authorizeDeActivatedUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			AssertJUnit.assertEquals("Check response code after deActive user", errorInfo.getCode(),
					getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

			// clean audit before authorization test
			DbUtils.cleanAllAudits();

			// Perform login from WebSeal
			User sealUserDetails = sdncUserDetails;
			RestResponse authorizedUserResponse = UserRestUtils.authorizedUserTowardsCatalogBeQA(sealUserDetails);

			// validate response

			ErrorInfo errorInfo2 = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_ACCESS.name());

			AssertJUnit.assertNotNull("check response object is not null after user login", authorizedUserResponse);
			AssertJUnit.assertNotNull("check error code exists in response after user login",
					authorizedUserResponse.getErrorCode());
			AssertJUnit.assertEquals("Check response code after deActive user", errorInfo2.getCode(),
					authorizedUserResponse.getErrorCode());

			List<String> variables2 = Arrays.asList();
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_ACCESS.name(), variables2,
					authorizedUserResponse.getResponse());

			// validate against ES DB

			UserValidationUtils.validateDataAgainstAuditDB_access(sealUserDetails,
					DbUtils.parseAuditRespByAction("Access"), authorizedUserResponse, errorInfo2, variables2);

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	public User getDefaultUserDetails() {

		String httpCspUserId = "km2000";
		String userFirstName = "Kot";
		String userLastName = "May";
		String email = "km2000@intl.sdc.com";
		String role = UserRoleEnum.ADMIN.name();
		User sdncUserDetails = new User(userFirstName, userLastName, httpCspUserId, email, role, null);

		return sdncUserDetails;
	}

	public void validateSuccessCreateUserResponse(User sdncUserDetails, RestResponse createUserResponse)
			throws Exception {

		AssertJUnit.assertNotNull("check response object is not null after create user", createUserResponse);
		AssertJUnit.assertNotNull("check error code exists in response after create user",
				createUserResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after create user", HttpStatus.SC_CREATED,
				createUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, createUserResponse.getResponse());
		// UserRestUtils.validateAddUserAuditMessage(sdncUserDetails,
		// sdncAdminUser, String.valueOf(HttpStatus.SC_CREATED),
		// UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
		// UserRestUtils.getAddUserAuditMessage("AddUser"));
		String addUser = "AddUser";
		ExpectedUserCRUDAudit constructFieldsForAuditValidation = Convertor.constructFieldsForAuditValidation(addUser,
				sdncAdminUser, ActionStatus.CREATED, sdncUserDetails, null);
		AuditValidationUtils.validateAddUserAudit(constructFieldsForAuditValidation, addUser);

		RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);
		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, getUserResponse.getResponse());

	}

	public void validateSuccessDeActivateUserResponse(User sdncUserDetails, RestResponse deActivateUserResponse)
			throws Exception {

		AssertJUnit.assertNotNull("check response object is not null after deActive user", deActivateUserResponse);
		AssertJUnit.assertNotNull("check error code exists in response after deActive user",
				deActivateUserResponse.getErrorCode());
		AssertJUnit.assertEquals("Check response code after deActive user", 200,
				deActivateUserResponse.getErrorCode().intValue());

		UserValidationUtils.validateUserDetailsOnResponse(sdncUserDetails, deActivateUserResponse.getResponse());

		String deleteUser = "DeleteUser";
		ExpectedUserCRUDAudit constructFieldsForAuditValidation = Convertor
				.constructFieldsForAuditValidation(deleteUser, sdncAdminUser, ActionStatus.OK, null, sdncUserDetails);
		AuditValidationUtils.validateAddUserAudit(constructFieldsForAuditValidation, deleteUser);

	}

	// US498322 - Add Status Field to USER

	@Test
	public void createNewUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();
		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	@Test
	public void createDefaultUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();
		sdncUserDetails.setFirstName(null);
		sdncUserDetails.setLastName(null);
		sdncUserDetails.setEmail(null);
		sdncUserDetails.setRole(null);

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	@Test
	public void createTesterUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();
		sdncUserDetails.setLastName(null);
		sdncUserDetails.setRole(UserRoleEnum.TESTER.name());

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	@Test
	public void deActivateCreatedAdminUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());

			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			assertEquals("Check response code after get user", errorInfo.getCode(), getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	@Test
	public void deActivateTheSameUserTwice() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());

			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			assertEquals("Check response code after deActive user", errorInfo.getCode(),
					getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

			// deActivate the same user once time more
			RestResponse deActivateUserResponse2 = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			ErrorInfo errorInfo2 = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			assertEquals("Check response code after deActive user", errorInfo2.getCode(),
					deActivateUserResponse2.getErrorCode());

			List<String> variables2 = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables2,
					deActivateUserResponse2.getResponse());

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	@Test
	public void createAgainDeActivatedUser() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();

		try {

			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			assertEquals("Check response code after deActive user", errorInfo.getCode(),
					getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

			// create the user with the same UserId(details) as deActivated user
			DbUtils.cleanAllAudits();

			RestResponse createUserResponse2 = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			ErrorInfo errorInfo2 = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			assertEquals("Check response code after deActive user", errorInfo2.getCode(),
					createUserResponse2.getErrorCode());

			List<String> variables2 = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables2,
					createUserResponse2.getResponse());

		} finally {
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	// very not recommend to run this test, resources/services may be zombie
	// @Test
	public void deActivateLastAdminUser() throws Exception {

		try {

			// send get all ADMIN user request toward BE
			RestResponse getAllAdminUsers = UserRestUtils.getAllAdminUsers(sdncAdminUser);

			assertNotNull("check response object is not null after create user", getAllAdminUsers);
			assertNotNull("check error code exists in response after create user", getAllAdminUsers.getErrorCode());
			assertEquals("Check response code after create user", 200, getAllAdminUsers.getErrorCode().intValue());

			TypeToken<List<User>> typeToken = new TypeToken<List<User>>() {
			};
			List<User> listOfUsersOnResponse = gson.fromJson(getAllAdminUsers.getResponse(), typeToken.getType());
			logger.debug("listOfUsers: {}", listOfUsersOnResponse);

			// build map of all Admin users from listOfUsersOnResponse from
			// response
			Map<String, User> mapAllUsersOnResponse = new HashMap<String, User>();
			for (User sdncUser : listOfUsersOnResponse) {
				mapAllUsersOnResponse.put(sdncUser.getUserId(), sdncUser);
			}

			// remove from mapAllUsersOnResponse map one of admin users
			mapAllUsersOnResponse.remove(sdncAdminUser.getUserId());
			logger.debug("map Of all Admin users exclude one : {}", mapAllUsersOnResponse);

			// deActivate all Admin users from the userIdAllAdminList list
			for (Entry<String, User> entry : mapAllUsersOnResponse.entrySet()) {
				UserRestUtils.deActivateUser(entry.getValue(), sdncAdminUser);
			}

			// deActivate last Admin user user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncAdminUser, sdncAdminUser);

			ErrorInfo errorInfo = ErrorValidationUtils
					.parseErrorConfigYaml(ActionStatus.DELETE_USER_ADMIN_CONFLICT.name());

			assertEquals("Check response code after deActive user", errorInfo.getCode(),
					deActivateUserResponse.getErrorCode());

			List<String> variables = Arrays.asList();
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.DELETE_USER_ADMIN_CONFLICT.name(), variables,
					deActivateUserResponse.getResponse());

		} finally {
			// UserRestUtils.deleteUser(UserRestUtils.getAdminDetails2(),
			// sdncAdminUser);
			// UserRestUtils.deleteUser(UserRestUtils.getAdminDetails3(),
			// sdncAdminUser);
			// UserRestUtils.createUser(UserRestUtils.getAdminDetails2(),
			// sdncAdminUser);
			// UserRestUtils.createUser(UserRestUtils.getAdminDetails3(),
			// sdncAdminUser);
		}

	}

	// test check the resource accessibility via catalog view, resource was
	// created by user which was deActivated

	@Test
	public void resourceAccessibility() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource("tosca.nodes.newresource4test4",
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, "jh0003");
		String resourceBaseVersion = "0.1";

		try {
			// Delete resource
			// resourceUtils.deleteResource_allVersions(resourceDetails,
			// sdncAdminUser);
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);

			DbUtils.cleanAllAudits();
			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// ------------------------Start create
			// resource---------------------------------------------------------------------------------

			// create resource
			RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
			assertEquals("Check response code after create", 201, createResponse.getErrorCode().intValue());

			Resource createdResource = ResponseParser.convertResourceResponseToJavaObject(createResponse.getResponse());

			RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncUserDetails,
					createdResource.getUniqueId());
			assertEquals("Check response code after get", 200, resourceGetResponse.getErrorCode().intValue());

			// validate get response
			ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
					resourceBaseVersion);
			resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
			resourceRespJavaObject.setAbstractt("false");
			resourceRespJavaObject.setCreatorUserId(sdncUserDetails.getUserId());
			resourceRespJavaObject.setLastUpdaterUserId(sdncUserDetails.getUserId());

			resourceRespJavaObject
					.setCreatorFullName(sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName());
			resourceRespJavaObject
					.setLastUpdaterFullName(sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName());

			ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);

			// ------------------------End create
			// resource---------------------------------------------------------------------------------

			// clean audit before authorization test
			DbUtils.cleanAllAudits();

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			UserValidationUtils.validateDeleteUserAuditMessage(sdncUserDetails, sdncAdminUser, "200",
					UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
					UserValidationUtils.getAddUserAuditMessage("DeleteUser"));

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			assertEquals("Check response code after deActive user", errorInfo.getCode(),
					getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

			// checking if created resource is accessible
			DbUtils.cleanAllAudits();

			RestResponse getCatalogDataResponse = CatalogRestUtils.getCatalog(sdncAdminUser.getUserId());

			// validate response

			assertNotNull("check response object is not null after user login", getCatalogDataResponse);
			assertNotNull("check error code exists in response after user login",
					getCatalogDataResponse.getErrorCode());
			assertEquals("Check response code after deActive user", 200,
					getCatalogDataResponse.getErrorCode().intValue());

			// expected resource list
			List<String> resourceExpectedUniqIdList = new ArrayList<String>();
			resourceExpectedUniqIdList.add(resourceDetails.getUniqueId());
			logger.debug("resourceExpectedUniqIdList: {}", resourceExpectedUniqIdList);

			compareResourceUniqIdList(getCatalogDataResponse.getResponse(), resourceExpectedUniqIdList, true);

		} finally {
			// resourceUtils.deleteResource_allVersions(resourceDetails,
			// sdncAdminUser);
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	// test check the resource accessibility via catalog view, resource was
	// created by user which was deActivated

	@Test
	public void resourceAccessibilityOnImport() throws Exception {

		User sdncUserDetails = getDefaultUserDetails();
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource("importResource4test",
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, "jh0003");
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(),
				ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
		// String resourceBaseVersion = "1.0";

		try {
			// Delete resource
			// resourceUtils.deleteResource_allVersions(resourceDetails,
			// sdncAdminUser);
			RestResponse deleteUserResponse = UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
			assertTrue("delete user request failed",
					deleteUserResponse.getErrorCode() == 200 || deleteUserResponse.getErrorCode() == 404);
			DbUtils.cleanAllAudits();
			RestResponse createUserResponse = UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
			validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);

			// ------------------------Start import
			// resource---------------------------------------------------------------------------------

			// import new resource with CERTIFIED state
			User importer = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN4);
			RestResponse importResponse = ImportRestUtils.importResourceByName(resourceDetails, importer);

			assertNotNull("check response object is not null after create user", importResponse);
			assertNotNull("check error code exists in response after create user", importResponse.getErrorCode());
			assertEquals("Check response code after create user", 201, importResponse.getErrorCode().intValue());

			// ------------------------End import
			// resource---------------------------------------------------------------------------------

			// clean audit before authorization test
			DbUtils.cleanAllAudits();

			// deActivate created user
			RestResponse deActivateUserResponse = UserRestUtils.deActivateUser(sdncUserDetails, sdncAdminUser);
			sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
			validateSuccessDeActivateUserResponse(sdncUserDetails, deActivateUserResponse);

			UserValidationUtils.validateDeleteUserAuditMessage(sdncUserDetails, sdncAdminUser, "200",
					UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
					UserValidationUtils.getAddUserAuditMessage("DeleteUser"));

			ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
			RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails, sdncAdminUser);

			assertEquals("Check response code after deActive user", errorInfo.getCode(),
					getUserResponse.getErrorCode());

			List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
			ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(), variables,
					getUserResponse.getResponse());

			// checking if created resource is accessible
			DbUtils.cleanAllAudits();

			RestResponse getCatalogDataResponse = CatalogRestUtils.getCatalog(sdncAdminUser.getUserId());

			// validate response

			assertNotNull("check response object is not null after user login", getCatalogDataResponse);
			assertNotNull("check error code exists in response after user login",
					getCatalogDataResponse.getErrorCode());
			assertEquals("Check response code after deActive user", 200,
					getCatalogDataResponse.getErrorCode().intValue());

			// expected resource list
			List<String> resourceExpectedUniqIdList = new ArrayList<String>();
			resourceExpectedUniqIdList.add(resourceDetails.getUniqueId());
			logger.debug("resourceExpectedUniqIdList: {}", resourceExpectedUniqIdList);

			compareResourceUniqIdList(getCatalogDataResponse.getResponse(), resourceExpectedUniqIdList, true);

		} finally {
			// resourceUtils.deleteResource_allVersions(resourceDetails,
			// sdncAdminUser);
			UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
		}

	}

	// test check the service accessibility via catalog view, service was
	// created by user which was deActivated

	// @Test
	// public void serviceAccessibility() throws Exception{
	//
	// User sdncUserDetails = getDefaultUserDetails();
	//// fill new service details
	// ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
	// String serviceBaseVersion = "0.1";
	//
	// try{
	// //Delete service
	//// ServiceRestUtils.deleteService_allVersions(serviceDetails,
	// sdncAdminUser);
	// UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
	//
	// DbUtils.cleanAllAudits();
	// RestResponse createUserResponse =
	// UserRestUtils.createUser(sdncUserDetails, sdncAdminUser);
	// validateSuccessCreateUserResponse(sdncUserDetails, createUserResponse);
	//
	//// ------------------------Start create
	// service---------------------------------------------------------------------------------
	// RestResponse restResponse =
	// ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
	//
	// assertNotNull("check response object is not null after create service",
	// restResponse);
	// assertNotNull("check error code exists in response after create service",
	// restResponse.getErrorCode());
	// assertEquals("Check response code after create service", 201,
	// restResponse.getErrorCode().intValue());
	//
	//// validate create service response vs actual
	//
	// Service service =
	// ServiceRestUtils.convertServiceResponseToJavaObject(restResponse.getResponse());
	// UserValidationUtils.validateServiceResponseMetaData(serviceDetails,service,sdncUserDetails,
	// (LifecycleStateEnum)null);
	//
	//// validate get service response vs actual
	// restResponse = ServiceRestUtils.getService(serviceDetails.getUniqueId(),
	// sdncUserDetails);
	// service =
	// ServiceRestUtils.convertServiceResponseToJavaObject(restResponse.getResponse());
	// UserValidationUtils.validateServiceResponseMetaData(serviceDetails,service,sdncUserDetails,
	// (LifecycleStateEnum)null);
	//
	// //validate audit
	//
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// ServiceRestUtils.constructFieldsForAuditValidation(serviceDetails,
	// serviceBaseVersion, sdncUserDetails);
	//
	// String auditAction="Create";
	// expectedResourceAuditJavaObject.setAction(auditAction);
	// expectedResourceAuditJavaObject.setPrevState("");
	// expectedResourceAuditJavaObject.setPrevVersion("");
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setStatus("201");
	// expectedResourceAuditJavaObject.setDesc("OK");
	//
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	//
	//// ------------------------End create
	// service---------------------------------------------------------------------------------
	//
	//// clean audit before authorization test
	// DbUtils.cleanAllAudits();
	//
	//// deActivate created user
	// RestResponse deActivateUserResponse =
	// UserRestUtils.deActivateUser(sdncUserDetails,sdncAdminUser);
	// sdncUserDetails.setStatus(UserStatusEnum.INACTIVE);
	// validateSuccessDeActivateUserResponse(sdncUserDetails,
	// deActivateUserResponse);
	//
	// UserValidationUtils.validateDeleteUserAuditMessage(sdncUserDetails,
	// sdncAdminUser, "200", UserResponseMessageEnum.SUCCESS_MESSAGE.getValue(),
	// UserValidationUtils.getAddUserAuditMessage("DeleteUser"));
	//
	// ErrorInfo errorInfo =
	// ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.USER_INACTIVE.name());
	// RestResponse getUserResponse = UserRestUtils.getUser(sdncUserDetails,
	// sdncAdminUser);
	//
	// assertEquals("Check response code after deActive user",
	// errorInfo.getCode(), getUserResponse.getErrorCode());
	//
	// List<String> variables = Arrays.asList(sdncUserDetails.getUserId());
	// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.USER_INACTIVE.name(),
	// variables, getUserResponse.getResponse());
	//
	// //checking if created service is accessible
	// DbUtils.cleanAllAudits();
	//
	// RestResponse getCatalogDataResponse =
	// CatalogRestUtils.getCatalog(sdncAdminUser.getUserId());
	//
	// //validate response
	//
	// assertNotNull("check response object is not null after user login",
	// getCatalogDataResponse);
	// assertNotNull("check error code exists in response after user login",
	// getCatalogDataResponse.getErrorCode());
	// assertEquals("Check response code after deActive user", 200,
	// getCatalogDataResponse.getErrorCode().intValue());
	//
	//// expected service list
	// List<String> serviceExpectedUniqIdList= new ArrayList<String>();
	// serviceExpectedUniqIdList.add(serviceDetails.getUniqueId());
	// logger.debug("serviceExpectedUniqIdList: {}", serviceExpectedUniqIdList);
	//
	// compareServiceUniqIdList(getCatalogDataResponse.getResponse(),
	// serviceExpectedUniqIdList, true);
	//
	//
	// }finally{
	//// ServiceRestUtils.deleteService_allVersions(serviceDetails,
	// sdncAdminUser);
	// UserRestUtils.deleteUser(sdncUserDetails, sdncAdminUser, true);
	// }
	//
	// }

	public void compareServiceUniqIdList(String response, List<String> expectedList, boolean flag) {

		JsonElement jelement = new JsonParser().parse(response);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray serviceArray = (JsonArray) jobject.get("services");
		logger.debug("{}", serviceArray);
		assertTrue("expected service count: " + expectedList.size() + " or more" + ", actual: " + serviceArray.size(),
				serviceArray.size() >= expectedList.size());

		// build service list from response
		List<ServiceReqDetails> serviceReqDetailsListOnResponse = new ArrayList<ServiceReqDetails>();
		for (int i = 0; i < serviceArray.size(); i++) {
			ServiceReqDetails json = gson.fromJson(serviceArray.get(i), ServiceReqDetails.class);
			serviceReqDetailsListOnResponse.add(json);
		}
	}

	public void compareResourceUniqIdList(String response, List<String> expectedList, boolean flag) {

		JsonElement jelement = new JsonParser().parse(response);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray resourceArray = (JsonArray) jobject.get("resources");
		logger.debug("{}", resourceArray);
		assertTrue("expected resource count: " + expectedList.size() + " or more" + ", actual: " + resourceArray.size(),
				resourceArray.size() >= expectedList.size());

		// build resource list from response
		List<ResourceReqDetails> resourceReqDetailsListOnResponse = new ArrayList<ResourceReqDetails>();
		for (int i = 0; i < resourceArray.size(); i++) {
			ResourceReqDetails json = gson.fromJson(resourceArray.get(i), ResourceReqDetails.class);
			resourceReqDetailsListOnResponse.add(json);
		}

		logger.debug("ResourceReqDetails list on response: {}", resourceReqDetailsListOnResponse);

		List<String> resourceActualUniqIdList = new ArrayList<String>();
		for (ResourceReqDetails resource : resourceReqDetailsListOnResponse) {
			resourceActualUniqIdList.add(resource.getUniqueId());
		}
		logger.debug("resourceActualUniqIdList on response: {}", resourceActualUniqIdList);
		logger.debug("resourceExpectedUniqIdList on response: {}", expectedList);

		if (flag) {
			assertTrue("actual list does not contain expected list",
					resourceActualUniqIdList.containsAll(expectedList));
		} else {
			assertFalse("actual list contains non expected list elements",
					resourceActualUniqIdList.containsAll(expectedList));
		}
	}

}

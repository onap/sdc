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

package org.openecomp.sdc.ci.tests.execute.general;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedEcomConsumerAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ManageEcompConsumerCredentials extends ComponentBaseTest {

	protected static final String ADD_ECOMP_USER_CREDENTIALS = "AddECOMPUserCredentials";
	protected static final String DELETE_ECOMP_USER_CREDENTIALS = "DeleteECOMPUserCredentials";
	protected static final String GET_ECOMP_USER_CREDENTIALS = "GetECOMPUserCredentials";

	public static final String contentTypeHeaderData = "application/json";
	public static final String acceptHeaderData = "application/json";

	public static final int STATUS_CODE_SUCCESS = 200;
	public static final int STATUS_CODE_SUCSESS_CREATED = 201;
	public static final int STATUS_CODE_SUCCESS_DELETE_GET = 200;
	public static final int STATUS_CODE_INVALID_CONTENT = 400;
	public static final int STATUS_CODE_MISSING_DATA = 400;
	public static final int STATUS_CODE_MISSING_INFORMATION = 403;
	public static final int STATUS_CODE_RESTRICTED_ACCESS = 403;

	public static final int STATUS_CODE_NOT_FOUND = 404;
	public static final int STATUS_CODE_RESTRICTED_OPERATION = 409;

	protected static Gson gson = new Gson();
	protected ConsumerDataDefinition consumerDataDefinition;
	protected User sdncAdminUserDetails;
	protected User sdncDesignerUserDetails;
	protected User sdncTesterUserDetails;
	protected User sdncGovernorUserDetails;
	protected User sdncOpsUserDetails;

	public ManageEcompConsumerCredentials() {
		super(name, ManageEcompConsumerCredentials.class.getName());
	}

	@Rule
	public static TestName name = new TestName();

	protected String salt = "123456789012345678901234567890ab";
	protected String password = "123456789012345678901234567890ab123456789012345678901234567890ab";
	protected String ecompUser = "benny";

	protected Long consumerDetailsLastupdatedtime;

	@BeforeMethod
	public void init() throws Exception {
		sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncDesignerUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncTesterUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncGovernorUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
		sdncOpsUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.OPS);

		consumerDataDefinition = new ConsumerDataDefinition();
		consumerDataDefinition.setConsumerName(ecompUser);
		consumerDataDefinition.setConsumerPassword(password);
		consumerDataDefinition.setConsumerSalt(salt);
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);

	}

	// US563681 manage ECOMP consumer credentials - DELETE/GET
	@Test
	public void deleteEcompCredentialsMethodDelete() throws Exception {
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		AssertJUnit.assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		AssertJUnit.assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		// DbUtils.deleteFromEsDbByPattern("_all");
		DbUtils.cleanAllAudits();
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		AssertJUnit.assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS_DELETE_GET,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCCESS_DELETE_GET);
		// Get Consumer to verify that consumer user does not exist
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		AssertJUnit.assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
	}

	//// US561728 CREATE ECOMP consumer credentials
	@Test
	public void createEcompCredentialsMethodPost() throws Exception {
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		// Validate actual consumerData to returned from response
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);

		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
	}

	@Test(enabled = false)
	public void createEcompCredentialsUserAlreayExist() throws Exception {
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);

		// Create consumer which already exists with different password and Salt
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerPassword("zxcvb");
		consumerDataDefinition.setConsumerSalt("1234567890qwertyuiop1234567890as");
		createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer with new data
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsByDesigner() throws Exception { // HttpCspUserId
																		// header
																		// contains
																		// Designer
																		// UserId
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncDesignerUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncDesignerUserDetails, ActionStatus.RESTRICTED_OPERATION);
	}

	@Test
	public void createEcompCredentialsByTester() throws Exception { // HttpCspUserId
																	// header
																	// contains
																	// Tester
																	// UserId
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncTesterUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncTesterUserDetails, ActionStatus.RESTRICTED_OPERATION);
	}

	@Test
	public void createEcompCredentialsByOps() throws Exception { // HttpCspUserId
																	// header
																	// contains
																	// OPS
																	// UserId
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition, sdncOpsUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncOpsUserDetails, ActionStatus.RESTRICTED_OPERATION);
	}

	@Test
	public void createEcompCredentialsByGovernor() throws Exception { // HttpCspUserId
																		// header
																		// contains
																		// Governor
																		// UserId
																		// Create
																		// Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncGovernorUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncGovernorUserDetails, ActionStatus.RESTRICTED_OPERATION);
	}

	@Test
	public void createEcompCredentialsByNoExistingIUser() throws Exception {
		User noSdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		noSdncUserDetails.setRole("blabla");
		noSdncUserDetails.setUserId("bt750h");
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition, noSdncUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_ACCESS.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(
				consumerDataDefinition.getConsumerName() + "," + consumerDataDefinition.getConsumerSalt().toLowerCase()
						+ "," + consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc(""));
		expectedEcomConsumerAuditJavaObject.setModifier("(" + noSdncUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	// user name
	@Test
	public void createEcompCredentialsUserNameIsNull() throws Exception {
		consumerDataDefinition.setConsumerName(null); // SVC4528
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_DATA,
				createConsumerRest.getErrorCode().intValue());
		// verify taht consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerSalt().toLowerCase() + ","
				+ consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer name"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsUserNameIsEmpty() throws Exception {
		consumerDataDefinition.setConsumerName("");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerSalt().toLowerCase() + ","
				+ consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer name"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsUserNameIsNotUTF8() throws Exception {
		consumerDataDefinition.setConsumerName("בני"); // SVC4528
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void createEcompCredentialsUserNameMaxLength() throws Exception {
		consumerDataDefinition.setConsumerName(
				"_ABCD-.abcdqwertyuiopasdfghjklzxcvbnmqw1234567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890pf34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfgghjklqwertyuiopzaiutrewasdfghjklqwertyuiopzasxcdferf123456.-"); // SVC4528
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Validate actual consumerData to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameExceedMaxLength() throws Exception {
		consumerDataDefinition.setConsumerName(
				"_ABCD-.abcdqwertyuiopasdfghjklzxcvbnmqw1234567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890pf34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfgghjklqwertyuiopzaiutrewasdfghjklqwertyuiopzasxcdferf123456.--"); // SVC4528
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.EXCEEDS_LIMIT, "Consumer name", "255");
	}

	@Test
	public void createEcompCredentialsUserNameLastCharIsDash() throws Exception { // allowed
		consumerDataDefinition.setConsumerName("ABCD34567890pf34567890poiutrew-");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject , Validate actual consumerData
		// to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameLastCharIsPeriod() throws Exception {
		consumerDataDefinition.setConsumerName("ABCD34567890pf34567890poiutrew.");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject , Validate actual consumerData
		// to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameLastCharIsUnderscore() throws Exception {
		consumerDataDefinition.setConsumerName("ABCD34567890pf34567890poiutrew_");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject , Validate actual consumerData
		// to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameFirstCharIsUnderscore() throws Exception {
		consumerDataDefinition.setConsumerName("_ABCD34567890pf34567890poiutre");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject , Validate actual consumerData
		// to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameFirstCharIsPeriod() throws Exception {
		consumerDataDefinition.setConsumerName(".ABCD34567890pf34567890poiutre");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.INVALID_CONTENT_PARAM, "Consumer name");
	}

	@Test
	public void createEcompCredentialsUserNameFirstCharIsDash() throws Exception { // Not
																					// allowed
		consumerDataDefinition.setConsumerName("-ABCD34567890pf34567890poiutre");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.INVALID_CONTENT_PARAM, "Consumer name");
	}

	/// Password
	@Test
	public void createEcompCredentialsPasswordIsNull() throws Exception {
		consumerDataDefinition.setConsumerPassword(null);
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_DATA,
				createConsumerRest.getErrorCode().intValue());
		// verify taht consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer password"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsPasswordIsEmpty() throws Exception {
		consumerDataDefinition.setConsumerPassword("");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_DATA,
				createConsumerRest.getErrorCode().intValue());
		// verify taht consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer password"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsPasswordMaxLength() throws Exception { // password
																				// must
																				// be
																				// 64
																				// chars
		consumerDataDefinition.setConsumerPassword("123456789012345678901234567890ab123456789012345678901234567890ab");
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		// Validate actual consumerData to returned from response
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
		// Delete Consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsPasswordExceeedMaxLength() throws Exception { // password
																					// must
																					// be
																					// 64
																					// chars
		consumerDataDefinition.setConsumerPassword("123456789012345678901234567890ab123456789012345678901234567890ab1");
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.INVALID_LENGTH, "Consumer password", "64");
	}

	@Test
	public void createEcompCredentiaPasswordValid() throws Exception {
		// Password Contains lowercase/uppercase characters and numbers -
		// convert upper case letter to lower
		consumerDataDefinition.setConsumerPassword("ABCabc1234567890POImnb12345678901234567890POIUzxcvbNMASDFGhjkl12");
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);

	}

	//// Salt
	@Test
	public void createEcompCredentialsSaltIsNull() throws Exception {
		// Length must be 32 characters
		consumerDataDefinition.setConsumerSalt(null);
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_DATA,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerPassword().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);

	}

	@Test
	public void createEcompCredentialsSaltIsEmpty() throws Exception {
		consumerDataDefinition.setConsumerSalt("");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_DATA,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerPassword().toLowerCase());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsSaltLengthLessThan32() throws Exception {
		consumerDataDefinition.setConsumerSalt("123456789012345678901234567890a");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.INVALID_LENGTH, "Consumer salt");

	}

	// Bug
	@Test
	public void createEcompCredentialsSaltLengthMoreThan32() throws Exception { // Length
																				// must
																				// be
																				// 32
																				// characters
																				// -
																				// SVC4529
																				// "Error:
																				// Invalid
																				// Content.
																				// %1
																				// exceeds
																				// limit
																				// of
																				// %2
																				// characters."
		consumerDataDefinition.setConsumerSalt("123456789012345678901234567890abc");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.INVALID_LENGTH, "Consumer salt");

	}

	@Test
	public void createEcompCredentialsSaltUppercaseCharacters() throws Exception {
		// Contains uppercase characters– exception invalid content
		consumerDataDefinition.setConsumerSalt("123456789012345678901234567890AB");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
				createConsumerRest.getErrorCode().intValue());
		// verify that consumer didn't created
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT_PARAM.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt() + "," + consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject
				.setModifier(sdncAdminUserDetails.getFullName() + "(" + sdncAdminUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	// UserId (UserId is taken from USER_ID header)

	@Test
	public void createEcompCredentialsHttpCspUserIdIsEmpty() throws Exception {
		// UserId is taken from USER_ID header
		sdncAdminUserDetails.setUserId("");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt() + "," + consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject.setModifier("");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsHttpCspUserIdIsNull() throws Exception { // UserId
																				// is
																				// taken
																				// from
																				// USER_ID
																				// header
		sdncAdminUserDetails.setUserId(null);
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt() + "," + consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject.setModifier("");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void createEcompCredentialsHttpCspUserIdHeaderIsMissing() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumerHttpCspAtuUidIsMissing(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName() + ","
				+ consumerDataDefinition.getConsumerSalt() + "," + consumerDataDefinition.getConsumerPassword());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc("Consumer salt"));
		expectedEcomConsumerAuditJavaObject.setModifier("");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				ADD_ECOMP_USER_CREDENTIALS);
	}

	// add UserId in json body
	@Test
	public void createEcompCredentiaJsonBodyContainLastModfierAtuid() throws Exception {
		// Add UserId (not admin) to json - we will ignore and create the user
		HashMap<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put("consumerName", "benny");
		jsonMap.put("consumerPassword", "123456789012345678901234567890ab123456789012345678901234567890ab");
		jsonMap.put("consumerSalt", "123456789012345678901234567890ab");
		jsonMap.put("lastModfierAtuid", "cs0008"); // designer
		Gson gson = new Gson();
		ConsumerDataDefinition consumer = gson.fromJson(jsonMap.toString(), ConsumerDataDefinition.class);

		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumer, sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Validate actual consumerData to returned from response
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumer, getConsumerDataObject);
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumer, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumer, sdncAdminUserDetails,
				STATUS_CODE_SUCSESS_CREATED);
		// Delete consumer
		ConsumerRestUtils.deleteConsumer(consumer, sdncAdminUserDetails);
	}

	@Test
	public void createEcompCredentialsUserNameNotAllowedCharacters() throws Exception {
		char invalidChars[] = { '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '=', '<', '>', '?', '/',
				'"', ':', '}', ']', '[', '{', '|', '\\', ' ', '\t', '\n' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			consumerDataDefinition.setConsumerName(invalidChars[i] + "ABCdef123");
			RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
					sdncAdminUserDetails);
			assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
					createConsumerRest.getErrorCode().intValue());
			// Audit validation
			AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
					sdncAdminUserDetails, ActionStatus.INVALID_CONTENT_PARAM, "Consumer name");
		}
	}

	@Test
	public void createEcompCredentialsPasswordIsInvalid() throws Exception {
		char invalidChars[] = { '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '=', '<', '>', '?', '/',
				'"', ':', '}', ']', '[', '{', '|', '\\', ' ', '\t', '\n' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			consumerDataDefinition.setConsumerPassword(
					"ABC" + invalidChars[i] + "ABCabc1234567890POImnb12345678901234567890POIUzxcvbNMASDFGhj");
			RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
					sdncAdminUserDetails);
			assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
					createConsumerRest.getErrorCode().intValue());
			// Audit validation
			AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
					sdncAdminUserDetails, ActionStatus.INVALID_CONTENT_PARAM, "Consumer password");
		}
	}

	@Test
	public void createEcompCredentialsSaltNotAllowedCharacters() throws Exception { // Salt
																					// must
																					// be
																					// 32
																					// chars
		char invalidChars[] = { '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '=', '<', '>', '?', '/',
				'"', ':', '}', ']', '[', '{', '|', '\\', ' ', '\t', '\n' };
		for (int i = 0; i < invalidChars.length; i++) {
			DbUtils.deleteFromEsDbByPattern("_all");
			consumerDataDefinition.setConsumerSalt(invalidChars[i] + "1234567890123456789012345678901");
			RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
					sdncAdminUserDetails);
			assertEquals("Check response code after create Consumer", STATUS_CODE_INVALID_CONTENT,
					createConsumerRest.getErrorCode().intValue());
			// Audit validation
			AuditValidationUtils.createEcompConsumerAuditFailure(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
					sdncAdminUserDetails, ActionStatus.INVALID_CONTENT_PARAM, "Consumer salt");
		}
	}

	@Test
	public void createEcompCredentialsPasswordEncoded() throws Exception {
		consumerDataDefinition.setConsumerPassword("0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		// Validate actual consumerData to returned from response
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);

		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(ADD_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCSESS_CREATED);
	}

	//

	@Test
	public void deleteEcompUserAlreayDeleted() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete ECOMP consumer
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS_DELETE_GET,
				deleteConsumerRest.getErrorCode().intValue());
		// Try to delete ECOMP consumer already deleted
		DbUtils.deleteFromEsDbByPattern("_all");
		deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void deleteEcompUserByTester() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncTesterUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncTesterUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Verify that consumer is not deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void deleteEcompUserByOps() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncOpsUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncOpsUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Verify that consumer is not deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void deleteEcompUserByGovernor() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncGovernorUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncGovernorUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Verify that consumer is not deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void deleteEcompUserByDesigner() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncDesignerUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncDesignerUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Verify that consumer is not deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void deleteEcompUserByNoExistingIUser() throws Exception {
		User noSdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		noSdncUserDetails.setRole("blabla");
		noSdncUserDetails.setUserId("bt750h");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, noSdncUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_ACCESS,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_ACCESS.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(ADD_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc(""));
		expectedEcomConsumerAuditJavaObject.setModifier("(" + noSdncUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				DELETE_ECOMP_USER_CREDENTIALS);
		// Verify that consumer is not deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void deleteEcompCredentialsUserDoesNotExist() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());

	}

	@Test
	public void deleteEcompCredentialsUserNameIsNull() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerName(null);
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void deleteEcompCredentialsUserNameMaxLength() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerName(
				"_BCD-.abcdqwertyuiopasdfghjklzxcvbnmqw1234567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890pf34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfgghjklqwertyuiopzaiutrewasdfghjklqwertyuiopzasxcdferf123456.--"); // SVC4528
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void deleteEcompCredentialsUserNameExceedMaxLength() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerName(
				"_XXXBCD-.abcdqwertyuiopasdfghjklzxcvbnmqw1234567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890pf34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfgghjklqwertyuiopzaiutrewasdfghjklqwertyuiopzasxcdferf123456.--"); // SVC4528
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void deleteEcompCredentialsHttpCspUserIdHeaderIsMissing() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse createConsumerRest = ConsumerRestUtils.deleteConsumerHttpCspAtuUidIsMissing(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_MISSING_INFORMATION,
				createConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(DELETE_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc());
		expectedEcomConsumerAuditJavaObject.setModifier("");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				DELETE_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void deleteEcompCredentialsNameIsUpperCase() throws Exception {
		consumerDataDefinition.setConsumerName("benny");
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerName("BENNY");
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				deleteConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(DELETE_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
		// Get Consumer to verify that consumer user was not deleted
		consumerDataDefinition.setConsumerName("benny");
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
	}

	@Test
	public void getEcompCredentialsMethodGet() throws Exception {
		// Create Consumer
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		// Validate actual consumerData to returned from response
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		DbUtils.deleteFromEsDbByPattern("_all");
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCCESS_DELETE_GET);
		// Delete consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void getEcompUserAlreayDeleted() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		// Delete ECOMP consumer
		RestResponse deleteConsumerRest = ConsumerRestUtils.deleteConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS_DELETE_GET,
				deleteConsumerRest.getErrorCode().intValue());
		DbUtils.deleteFromEsDbByPattern("_all");
		// Try to get ECOMP consumer already deleted
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void getEcompUserByTester() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer by Tester user
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncTesterUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncTesterUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Get Consumer by Admin
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void getEcompUserByOps() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer by Ops user
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncOpsUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncOpsUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Get Consumer by Admin
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void getEcompUserByGovernor() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer by Ops user
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncGovernorUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncGovernorUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Get Consumer by Admin
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void getEcompUserByDesigner() throws Exception {
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// Get Consumer by Designer user
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncDesignerUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_OPERATION,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncDesignerUserDetails, ActionStatus.RESTRICTED_OPERATION);
		// Get Consumer by Admin
		getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}

	@Test
	public void getEcompUserByNoExistingIUser() throws Exception {
		User noSdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		noSdncUserDetails.setRole("blabla");
		noSdncUserDetails.setUserId("bt750h");
		// Get Consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, noSdncUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_RESTRICTED_ACCESS,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_ACCESS.name());
		ExpectedEcomConsumerAudit expectedEcomConsumerAuditJavaObject = new ExpectedEcomConsumerAudit();
		expectedEcomConsumerAuditJavaObject.setAction(GET_ECOMP_USER_CREDENTIALS);
		expectedEcomConsumerAuditJavaObject.setEcomUser(consumerDataDefinition.getConsumerName());
		expectedEcomConsumerAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedEcomConsumerAuditJavaObject.setDesc(errorInfo.getAuditDesc(""));
		expectedEcomConsumerAuditJavaObject.setModifier("(" + noSdncUserDetails.getUserId() + ")");
		AuditValidationUtils.validateEcompConsumerAudit(expectedEcomConsumerAuditJavaObject,
				GET_ECOMP_USER_CREDENTIALS);
	}

	@Test
	public void getEcompCredentialsUserDoesNotExist() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());

	}

	@Test
	public void getEcompCredentialsUserNameIsNull() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		consumerDataDefinition.setConsumerName(null);
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_NOT_FOUND,
				getConsumerRest.getErrorCode().intValue());
		// Audit validation
		AuditValidationUtils.deleteEcompConsumerAuditFailure(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, ActionStatus.ECOMP_USER_NOT_FOUND, consumerDataDefinition.getConsumerName());
	}

	@Test
	public void getEcompCredentialsUserNameMaxLength() throws Exception {
		consumerDataDefinition.setConsumerName(
				"_ABCD-.abcdqwertyuiopasdfghjklzxcvbnmqw1234567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfg34567890pf34567890poiutrewasdfghjklqwertyuiopzaiutrewasdfgghjklqwertyuiopzaiutrewasdfghjklqwertyuiopzasxcdferf123456.-"); // SVC4528
		RestResponse createConsumerRest = ConsumerRestUtils.createConsumer(consumerDataDefinition,
				sdncAdminUserDetails);
		assertEquals("Check response code after create Consumer", STATUS_CODE_SUCSESS_CREATED,
				createConsumerRest.getErrorCode().intValue());
		// parse updated response to javaObject
		ConsumerDataDefinition getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(createConsumerRest);
		// Validate actual consumerData to returned from response
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Get Consumer
		DbUtils.deleteFromEsDbByPattern("_all");
		RestResponse getConsumerRest = ConsumerRestUtils.getConsumer(consumerDataDefinition, sdncAdminUserDetails);
		assertEquals("Check response code after get Consumer", STATUS_CODE_SUCCESS,
				getConsumerRest.getErrorCode().intValue());
		getConsumerDataObject = ConsumerRestUtils.parseComsumerResp(getConsumerRest);
		ConsumerRestUtils.validateConsumerReqVsResp(consumerDataDefinition, getConsumerDataObject);
		// Audit validation
		AuditValidationUtils.ecompConsumerAuditSuccess(GET_ECOMP_USER_CREDENTIALS, consumerDataDefinition,
				sdncAdminUserDetails, STATUS_CODE_SUCCESS_DELETE_GET);
		// Delete consumer
		ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
	}
}

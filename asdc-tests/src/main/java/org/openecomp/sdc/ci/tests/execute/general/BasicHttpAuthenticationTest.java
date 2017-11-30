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

//US505653
package org.openecomp.sdc.ci.tests.execute.general;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedAuthenticationAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BasicHttpAuthenticationTest extends ComponentBaseTest {

	protected static final String AUTH_FAILED_INVALID_AUTHENTICATION_HEADER = "AUTH_FAILED_INVALID_AUTHENTICATION_HEADER";

	protected static final String AUTH_SUCCESS = "AUTH_SUCCESS";

	protected static final String AUTH_FAILED_INVALID_PASSWORD = "AUTH_FAILED_INVALID_PASSWORD";

	protected static final String AUTH_FAILED_USER_NOT_FOUND = "AUTH_FAILED_USER_NOT_FOUND";

	protected static final String AUTH_REQUIRED = "AUTH_REQUIRED";

	protected static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	// user ci password 123456
	// protected final String authorizationHeader = "Basic Y2k6MTIzNDU2";
	// user ci password 123456
	protected final String USER = "ci";

	protected final String PASSWORD = "123456";

	protected final String SALT = "2a1f887d607d4515d4066fe0f5452a50";

	protected final String HASHED_PASSWORD = "0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b";

	protected User sdncAdminUserDetails;

	protected ConsumerDataDefinition consumerDataDefinition;
	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected User sdncUserDetails;

	protected ArtifactReqDetails deploymentArtifact;

	protected ExpectedAuthenticationAudit expectedAuthenticationAudit;

	protected final String auditAction = "HttpAuthentication";

	protected String expectedDownloadServiceUrl;
	protected String expectedDownloadResourceUrl;
	protected ComponentInstanceReqDetails componentInstanceReqDetails;

	@Rule
	public static TestName name = new TestName();

	public BasicHttpAuthenticationTest() {
		super(name, BasicHttpAuthenticationTest.class.getName());
	}

	@BeforeMethod
	public void init() throws Exception {

		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		Resource resourceObject = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails = new ResourceReqDetails(resourceObject);
		Service serviceObject = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		serviceDetails = new ServiceReqDetails(serviceObject);

		deploymentArtifact = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		RestResponse response = ArtifactRestUtils.addInformationalArtifactToResource(deploymentArtifact,
				sdncUserDetails, resourceDetails.getUniqueId());
		AssertJUnit.assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		componentInstanceReqDetails = ElementFactory.getDefaultComponentInstance();
		// certified resource
		response = LifecycleRestUtils.certifyResource(resourceDetails);
		AssertJUnit.assertTrue("certify resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		// add resource instance with HEAT deployment artifact to the service
		componentInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails, sdncUserDetails,
				serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		AssertJUnit.assertTrue("response code is not 201, returned: " + response.getErrorCode(),
				response.getErrorCode() == 201);
		expectedAuthenticationAudit = new ExpectedAuthenticationAudit();

		// RestResponse addDeploymentArtifactResponse =
		// ArtifactRestUtils.addInformationalArtifactToService(deploymentArtifact,
		// sdncUserDetails, serviceDetails.getUniqueId());
		// assertEquals("didn't succeed to upload deployment artifact", 200,
		// addDeploymentArtifactResponse.getErrorCode().intValue());
		//
		// downloadUrl =
		// String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL,
		// ValidationUtils.convertToSystemName(serviceDetails.getServiceName()),
		// serviceDetails.getVersion(),
		// ValidationUtils.normalizeFileName(deploymentArtifact.getArtifactName()));

		expectedDownloadResourceUrl = String.format(Urls.DISTRIB_DOWNLOAD_RESOURCE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceDetails.getVersion(),
				ValidationUtils.convertToSystemName(resourceDetails.getName()), resourceDetails.getVersion(),
				ValidationUtils.normalizeFileName(deploymentArtifact.getArtifactName()));
		expectedDownloadResourceUrl = expectedDownloadResourceUrl.substring("/sdc/".length(),
				expectedDownloadResourceUrl.length());

		expectedDownloadServiceUrl = String.format(Urls.DISTRIB_DOWNLOAD_SERVICE_ARTIFACT_RELATIVE_URL,
				ValidationUtils.convertToSystemName(serviceDetails.getName()), serviceDetails.getVersion(),
				ValidationUtils.normalizeFileName(deploymentArtifact.getArtifactName()));
		expectedDownloadServiceUrl = expectedDownloadServiceUrl.substring("/sdc/".length(),
				expectedDownloadServiceUrl.length());

		sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		consumerDataDefinition = createConsumer();
		RestResponse deleteResponse = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);
		;

		RestResponse createResponse = ConsumerRestUtils.createConsumer(consumerDataDefinition, sdncAdminUserDetails);
		BaseRestUtils.checkCreateResponse(createResponse);

	}

	@AfterMethod
	public void tearDown() throws Exception {
		RestResponse deleteResponse = ConsumerRestUtils.deleteConsumer(consumerDataDefinition, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);
		;
	}

	@Test
	public void sendAuthenticatedRequestTest_success() throws Exception, Exception {
		DbUtils.cleanAllAudits();
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(USER, PASSWORD);
		// RestResponse restResponse =
		// ArtifactRestUtils.downloadServiceArtifact(serviceDetails,
		// deploymentArtifact, sdncUserDetails, authorizationHeader);
		RestResponse restResponse = ArtifactRestUtils.downloadResourceArtifact(serviceDetails, resourceDetails,
				deploymentArtifact, sdncUserDetails, authorizationHeader);
		AssertJUnit.assertEquals("Check response code after download artifact", 200,
				restResponse.getErrorCode().intValue());
		AssertJUnit.assertFalse(restResponse.getHeaderFields().containsKey(HttpHeaderEnum.WWW_AUTHENTICATE.getValue()));

		validateAuditAuthentication(USER, AUTH_SUCCESS, ComponentTypeEnum.RESOURCE);

	}

	protected void validateAuditAuthentication(String userName, String AuthStatus, ComponentTypeEnum compType)
			throws Exception {
		if (compType.equals(ComponentTypeEnum.RESOURCE)) {
			expectedAuthenticationAudit = new ExpectedAuthenticationAudit(expectedDownloadResourceUrl, userName,
					auditAction, AuthStatus);
		} else {
			expectedAuthenticationAudit = new ExpectedAuthenticationAudit(expectedDownloadServiceUrl, userName,
					auditAction, AuthStatus);
		}
		AuditValidationUtils.validateAuthenticationAudit(expectedAuthenticationAudit);
	}

	protected ConsumerDataDefinition createConsumer() {
		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName(USER);
		consumer.setConsumerSalt(SALT);
		consumer.setConsumerPassword(HASHED_PASSWORD);
		return consumer;

	}

	@Test
	public void sendAuthenticatedRequestWithoutHeadersTest() throws Exception, Exception {
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, new HashMap<String, String>());
		assertEquals("Check response code after download artifact", 401, restResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.AUTH_REQUIRED.name(), new ArrayList<String>(),
				restResponse.getResponse());
		assertTrue(restResponse.getHeaderFields().containsKey(WWW_AUTHENTICATE));
		List<String> getAuthenticateHeader = restResponse.getHeaderFields().get(WWW_AUTHENTICATE);
		assertEquals("www-authenticate header contains more then one value", 1, getAuthenticateHeader.size());
		assertTrue(getAuthenticateHeader.get(0).equals("Basic realm=" + "\"ASDC\""));

		validateAuditAuthentication("", AUTH_REQUIRED, ComponentTypeEnum.SERVICE);
	}

	@Test
	public void sendAuthenticatedRequestTest_userIsNotProvsioned() throws Exception, Exception {
		String userName = "shay";
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(userName, "123456");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 403, restResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.AUTH_FAILED.name(), new ArrayList<String>(),
				restResponse.getResponse());
		assertFalse(restResponse.getHeaderFields().containsKey(WWW_AUTHENTICATE));

		validateAuditAuthentication(userName, AUTH_FAILED_USER_NOT_FOUND, ComponentTypeEnum.SERVICE);
	}

	@Test
	public void sendAuthenticatedRequestTest_userIsNull() throws Exception, Exception {
		String userName = "";
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(userName, "123456");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 403, restResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.AUTH_FAILED.name(), new ArrayList<String>(),
				restResponse.getResponse());
		assertFalse(restResponse.getHeaderFields().containsKey(WWW_AUTHENTICATE));

		validateAuditAuthentication(userName, AUTH_FAILED_USER_NOT_FOUND, ComponentTypeEnum.SERVICE);
	}

	@Test
	public void sendAuthenticatedRequestTest_passwordIsNull() throws Exception, Exception {
		String userName = "ci";
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(userName, "");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 403, restResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.AUTH_FAILED.name(), new ArrayList<String>(),
				restResponse.getResponse());
		assertFalse(restResponse.getHeaderFields().containsKey(WWW_AUTHENTICATE));

		validateAuditAuthentication(userName, AUTH_FAILED_INVALID_PASSWORD, ComponentTypeEnum.SERVICE);
	}

	@Test
	public void sendAuthenticatedRequestTest_passowrdIsNotValidated() throws Exception, Exception {
		String userCi = "ci";
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(userCi, "98765");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 403, restResponse.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.AUTH_FAILED.name(), new ArrayList<String>(),
				restResponse.getResponse());
		assertFalse(restResponse.getHeaderFields().containsKey(HttpHeaderEnum.WWW_AUTHENTICATE.getValue()));

		validateAuditAuthentication(userCi, AUTH_FAILED_INVALID_PASSWORD, ComponentTypeEnum.SERVICE);
	}

	@Test
	public void sendAuthenticatedRequestTest_InvalidHeader() throws Exception, Exception {
		String userCredentials = USER + ":" + PASSWORD;
		byte[] encodeBase64 = Base64.encodeBase64(userCredentials.getBytes());
		String encodedUserCredentials = new String(encodeBase64);
		Map<String, String> authorizationHeader = new HashMap<String, String>();
		authorizationHeader.put(HttpHeaderEnum.AUTHORIZATION.getValue(), encodedUserCredentials);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 400, restResponse.getErrorCode().intValue());
		assertFalse(restResponse.getHeaderFields().containsKey(HttpHeaderEnum.WWW_AUTHENTICATE.getValue()));

		validateAuditAuthentication("", AUTH_FAILED_INVALID_AUTHENTICATION_HEADER, ComponentTypeEnum.SERVICE);
	}

	@Test(enabled = false)
	public void sendTwoAuthenticatedRequestsTest() throws Exception, Exception {
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(USER, PASSWORD);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());

		RestResponse secondRestResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after second download artifact", 200,
				secondRestResponse.getErrorCode().intValue());
	}

	@Test(enabled = false)
	public void sendAuthenticatedRequestTest_userValidation_1() throws Exception, Exception {

		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName("cI2468");
		consumer.setConsumerPassword(HASHED_PASSWORD);
		consumer.setConsumerSalt(SALT);
		RestResponse deleteResponse = ConsumerRestUtils.deleteConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);

		RestResponse createResponse = ConsumerRestUtils.createConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkCreateResponse(createResponse);

		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(consumer.getConsumerName(),
				PASSWORD);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());

		deleteResponse = ConsumerRestUtils.deleteConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);
	}

	// ECOMP Consumer Name - UTF-8 string up to 255 characters containing the
	// following characters : ( maybe to limit 4-64 chars ? )
	// Lowercase characters {a-z}
	// Uppercase characters {A-Z}
	// Numbers {0-9}
	// Dash {-}; this character is not supported as the first character in the
	// user name
	// Period {.}; this character is not supported as the first character in the
	// user name
	// Underscore {_}
	// @Ignore("add manually user:password 24-!68:123456 to
	// users-configuration.yaml in runtime")
	@Test(enabled = false)
	public void sendAuthenticatedRequestTest_userValidation_2() throws Exception, Exception {
		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName("24-!68");
		consumer.setConsumerPassword(HASHED_PASSWORD);
		consumer.setConsumerSalt(SALT);
		RestResponse deleteResponse = ConsumerRestUtils.deleteConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);

		RestResponse createResponse = ConsumerRestUtils.createConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkCreateResponse(createResponse);
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader(consumer.getConsumerName(),
				PASSWORD);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());

		deleteResponse = ConsumerRestUtils.deleteConsumer(consumer, sdncAdminUserDetails);
		BaseRestUtils.checkStatusCode(deleteResponse, "delete operation filed", false, 404, 200);
	}

	// this is invalide becouse we do not use the : any more
	// @Ignore("can't exectue, yaml file does not allow to enter more then one
	// colon continuously (\":\") ")
	@Test(enabled = false)
	public void sendAuthenticatedRequestTest_userValidation_3() throws Exception, Exception {
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader("a:", "123456");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());
	}

	//
	// * ECOMP Consumer Password - expected to be SHA-2 256 encrypted value (
	// SALT + "real" password ) => maximal length 256 bytes = 32 characters
	// Before storing/comparing please convert upper case letter to lower.
	// The "normalized" encrypted password should match the following format :
	// [a-z0-9]
	// @Ignore("add manually user:password 2468:123:456 to
	// users-configuration.yaml in runtime")
	@Test(enabled = false)
	public void sendAuthenticatedRequestTest_passwordValidation_1() throws Exception, Exception {
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader("A1", "123:456");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());
	}

	// * ECOMP Consumer Password - expected to be SHA-2 256 encrypted value (
	// SALT + "real" password ) => maximal length 256 bytes = 32 characters
	// Before storing/comparing please convert upper case letter to lower.
	// The "normalized" encrypted password should match the following format :
	// [a-z0-9]
	@Test(enabled = false)
	// @Ignore("add manually user:password 2468:Sq123a456B to
	// users-configuration.yaml in runtime")
	public void sendAuthenticatedRequestTest_passwordValidation_2() throws Exception, Exception {
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader("B2", "Sq123a456B");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());
	}

	// * ECOMP Consumer Password - expected to be SHA-2 256 encrypted value (
	// SALT + "real" password ) => maximal length 256 bytes = 32 characters
	// Before storing/comparing please convert upper case letter to lower.
	// The "normalized" encrypted password should match the following format :
	// [a-z0-9]
	@Test
	// @Ignore("add C3:111T-0-*# to file")
	public void sendAuthenticatedRequestTest_passwordValidation_3() throws Exception, Exception {
		Map<String, String> authorizationHeader = BaseRestUtils.addAuthorizeHeader("C3", "111T-0-*#");
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, deploymentArtifact,
				sdncUserDetails, authorizationHeader);
		assertEquals("Check response code after download artifact", 200, restResponse.getErrorCode().intValue());
	}

}

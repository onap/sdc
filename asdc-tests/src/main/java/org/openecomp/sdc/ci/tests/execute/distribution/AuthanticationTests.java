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

package org.openecomp.sdc.ci.tests.execute.distribution;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedAuthenticationAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AuthanticationTests extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();
	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected User sdncUserDetails;

	protected static final String AUTH_SUCCESS = "AUTH_SUCCESS";

	protected static final String AUTH_REQUIRED = "AUTH_REQUIRED";

	// user ci password 123456
	// protected final String authorizationHeader = "Basic Y2k6MTIzNDU2";
	// user ci password 123456
	protected final String USER = "ci";
	protected final String PASSWORD = "123456";
	protected final String SALT = "2a1f887d607d4515d4066fe0f5452a50";
	protected final String HASHED_PASSWORD = "0a0dc557c3bf594b1a48030e3e99227580168b21f44e285c69740b8d5b13e33b";
	protected User sdncAdminUserDetails;
	protected ConsumerDataDefinition consumerDataDefinition;

	public AuthanticationTests() {
		super(name, AuthanticationTests.class.getName());
	}

	@DataProvider
	private final Object[][] getServiceDepArtType() throws IOException, Exception {
		return new Object[][] { { ArtifactTypeEnum.YANG_XML.getType() }, { ArtifactTypeEnum.OTHER.getType() } };
	}

	@BeforeMethod
	public void setup() throws Exception {
		resourceDetails = ElementFactory.getDefaultResource();
		serviceDetails = ElementFactory.getDefaultService();
		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		createComponents();
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

	protected ConsumerDataDefinition createConsumer() {
		ConsumerDataDefinition consumer = new ConsumerDataDefinition();
		consumer.setConsumerName(USER);
		consumer.setConsumerSalt(SALT);
		consumer.setConsumerPassword(HASHED_PASSWORD);
		return consumer;

	}

	protected void createComponents() throws Exception {
		RestResponse response = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		ServiceRestUtils.checkCreateResponse(response);
	}

	@Test(dataProvider = "getServiceDepArtType", description = "mumu")
	public void downloadServiceArtifactSuccessWithAutantication(String serviceDepArtType) throws Exception {
		String serviceUniqueId = serviceDetails.getUniqueId();

		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(serviceDepArtType);

		RestResponse addArtifactResponse = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails,
				sdncUserDetails, serviceUniqueId, ArtifactRestUtils.calculateChecksum(artifactDetails));
		AssertJUnit.assertEquals("Check response code after adding interface artifact", 200,
				addArtifactResponse.getErrorCode().intValue());

		String artifactName = ValidationUtils.normalizeFileName(artifactDetails.getArtifactName());
		// Thread.sleep(5000);
		Map<String, String> authorizationHeaders = BaseRestUtils.addAuthorizeHeader(USER, PASSWORD);
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, artifactDetails,
				sdncUserDetails, authorizationHeaders);
		AssertJUnit.assertEquals("Check response code after download resource", 200,
				restResponse.getErrorCode().intValue());

		List<String> contDispHeaderList = restResponse.getHeaderFields().get(Constants.CONTENT_DISPOSITION_HEADER);
		AssertJUnit.assertNotNull(contDispHeaderList);
		AssertJUnit.assertEquals("Check content disposition header",
				new StringBuilder().append("attachment; filename=\"").append(artifactName).append("\"").toString(),
				contDispHeaderList.get(0));

		String downloadUrl = ArtifactRestUtils
				.getPartialUrlByArtifactName(serviceDetails, serviceDetails.getVersion(), artifactName).substring(6);

		ExpectedAuthenticationAudit expectedAuthenticationAudit = new ExpectedAuthenticationAudit(downloadUrl, USER,
				AuditingActionEnum.AUTH_REQUEST.getName(), AUTH_SUCCESS);
		AuditValidationUtils.validateAuthenticationAudit(expectedAuthenticationAudit);
	}

	@Test(dataProvider = "getServiceDepArtType")
	public void downloadServiceArtifactWithOutAutantication(String serviceDepArtType) throws Exception {
		String serviceUniqueId = serviceDetails.getUniqueId();

		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(serviceDepArtType);

		RestResponse addArtifactResponse = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails,
				sdncUserDetails, serviceUniqueId, ArtifactRestUtils.calculateChecksum(artifactDetails));
		assertEquals("Check response code after adding interface artifact", 200,
				addArtifactResponse.getErrorCode().intValue());

		Map<String, String> authorizationHeaders = new HashMap<String, String>();
		RestResponse restResponse = ArtifactRestUtils.downloadServiceArtifact(serviceDetails, artifactDetails,
				sdncUserDetails, authorizationHeaders);
		assertEquals("Check response code after download resource failure", 401,
				restResponse.getErrorCode().intValue());

		String downloadUrl = ArtifactRestUtils.getPartialUrlByArtifactName(serviceDetails, serviceDetails.getVersion(),
				artifactDetails.getArtifactName()).substring(6);
		ExpectedAuthenticationAudit expectedAuthenticationAudit = new ExpectedAuthenticationAudit(downloadUrl, "",
				AuditingActionEnum.AUTH_REQUEST.getName(), AUTH_REQUIRED);
		AuditValidationUtils.validateAuthenticationAudit(expectedAuthenticationAudit);

	}

}

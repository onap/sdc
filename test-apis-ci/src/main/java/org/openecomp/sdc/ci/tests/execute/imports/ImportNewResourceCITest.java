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

package org.openecomp.sdc.ci.tests.execute.imports;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.ImportTestTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ImportRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class ImportNewResourceCITest extends ComponentBaseTest {

	// public static UserUtils userUtils = new UserUtils();
	// public ResourceUtils resourceUtils = new ResourceUtils();
	// public AuditValidationUtils AuditValidationUtils = new
	// AuditValidationUtils();
	// protected ArtifactUtils artifactUtils = new ArtifactUtils();

	protected String resourceVersion = null;
	protected String auditAction = null;
	public User sdncModifierDetails = new User();
	protected String artifactName1 = "data_artifact1.sh";
	protected String artifactName2 = "data_artifact2.sh";
	protected String interfaze = "standard";
	protected String interfaceArtifactName = "data_interface1.sh";

	private String SPECIAL_CHARACTERS = "~!#@~$%^*()[];:'\"|\\/";

	public ResourceReqDetails resourceDetails = new ResourceReqDetails();

	public Gson gson = new Gson();

	@Rule
	public static TestName name = new TestName();

	public ImportNewResourceCITest() {
		super(name, ImportNewResourceCITest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {

		// init user
		sdncModifierDetails.setUserId(UserRoleEnum.ADMIN.getUserId());
		// init resource details
		resourceDetails = ElementFactory.getDefaultResource("importResource4test", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, "jh0003");
	}

	@Test
	public void importAllTestResources_toValidateNewAPI() throws Exception {

		for (ImportTestTypesEnum currResource : ImportTestTypesEnum.values()) {
			// clean audit
			DbUtils.cleanAllAudits();

			// import testResources trough newResource API
			RestResponse importResponse = ImportRestUtils.importNewResourceByName(currResource.getFolderName(),
					UserRoleEnum.ADMIN);
			System.err.println("import Resource " + "<" + currResource.getFolderName() + ">" + "response: "
					+ importResponse.getErrorCode());

			// validate response
			ImportRestUtils.validateImportTestTypesResp(currResource, importResponse);
			if (currResource.getvalidateAudit() == true) {
				// validate audit
				// String baseVersion="0.1";
				String baseVersion = "";
				ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(currResource.getActionStatus().name());
				ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();
				String auditAction = "ResourceImport";
				expectedResourceAuditJavaObject.setAction(auditAction);
				expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.ADMIN.getUserId());
				expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.ADMIN.getUserName());
				expectedResourceAuditJavaObject.setResourceName(currResource.getNormativeName());
				expectedResourceAuditJavaObject.setResourceType("Resource");
				expectedResourceAuditJavaObject.setPrevVersion("");
				expectedResourceAuditJavaObject.setCurrVersion(baseVersion);
				expectedResourceAuditJavaObject.setPrevState("");
				// expectedResourceAuditJavaObject.setCurrState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT.toString());
				expectedResourceAuditJavaObject.setCurrState("");
				expectedResourceAuditJavaObject.setComment(null);
				expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
				List<String> variables = (currResource.getErrorParams() != null ? currResource.getErrorParams()
						: new ArrayList<String>());
				String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
				expectedResourceAuditJavaObject.setDesc(auditDesc);
				AuditValidationUtils.validateAuditImport(expectedResourceAuditJavaObject, auditAction);
			}
		}
	}

	protected RestResponse importNewResource(UserRoleEnum userRoleEnum) throws Exception {

		// init user
		sdncModifierDetails.setUserId(userRoleEnum.getUserId());
		// init resource details
		resourceDetails = ElementFactory.getDefaultResource("importResource4test", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, "jh0003");
		// clean ES DB
		DbUtils.cleanAllAudits();
		// import new resource (expected checkOut state)
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4test", userRoleEnum);
		return importResponse;
	}

	@Test(enabled = false)
	public void importUIResource() throws IOException {
		String payload = "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n"
				+ "  org.openecomp.resource.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n"
				+ "    description: someDesc";

		String encodedPayload = new String(Base64.encodeBase64(payload.getBytes()));

		String json = "{\r\n" + "  \"resourceName\": \"importResource4test\",\r\n"
				+ "  \"payloadName\": \"importResource4test.yml\",\r\n"
				+ "  \"categories\": [{\"name\": \"Application L4+\",\"normalizedName\": \"application l4+\",\"uniqueId\": \"resourceNewCategory.application l4+\",\"subcategories\": [{\"name\": \"Web Server\"}]}],\r\n"
				+ "  \"description\": \"ResourceDescription\",\r\n" + "  \"vendorName\": \"VendorName\",\r\n"
				+ "  \"vendorRelease\": \"VendorRelease\",\r\n" + "  \"contactId\": \"AT1234\",\r\n"
				+ "  \"icon\": \"router\",\r\n" + "  \"tags\": [\r\n" + "    \"importResource4test\"\r\n" + "  ],\r\n"
				+ "  \"payloadData\": \"" + encodedPayload + "\"\r\n" + "}";

		String md5 = GeneralUtility.calculateMD5Base64EncodedByString(json);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Constants.MD5_HEADER, md5);
		headers.put(Constants.USER_ID_HEADER, UserRoleEnum.ADMIN.getUserId());
		headers.put(Constants.CONTENT_TYPE_HEADER, "application/json");

		String url = String.format(Urls.CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());

		HttpRequest httpUtil = new HttpRequest();
		RestResponse httpSendPost = httpUtil.httpSendPost(url, json, headers);
		Integer errorCode = httpSendPost.getErrorCode();
		assertTrue(errorCode == HttpStatus.SC_CREATED);

	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_suc() throws Exception {

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		// validate response

		resourceVersion = "0.1";

		// ResourceRespJavaObject resourceRespJavaObject =
		// Convertor.constructFieldsForRespValidation(resourceDetails,
		// resourceVersion);
		// resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		// ResourceValidationUtils.validateResp(importResponse,
		// resourceRespJavaObject);
		//
		// //validate get response
		//
		// RestResponse resourceGetResponse =
		// ResourceRestUtils.getResource(sdncModifierDetails, resourceVersion);
		// ResourceValidationUtils.validateResp(resourceGetResponse,
		// resourceRespJavaObject);
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromImport);

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceRespJavaObject.getUniqueId());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// validate
		ResourceValidationUtils.validateModelObjects(resourceFromImport, resourceFromGet);

		// validate audit
		resourceDetails.setVersion(resourceDetails.getVersion());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);

		auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void importNewResource_byTester_failed() throws Exception {

		RestResponse importResponse = importNewResource(UserRoleEnum.TESTER);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 409, importResponse.getErrorCode().intValue());

	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existInCheckout_updateVendorName_updateCategory() throws Exception {

		// import new resource
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4testUpdateVendorNameAndCategory",
				UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 200, importResponse.getErrorCode().intValue());

		// validate response
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromImport);

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceRespJavaObject.getUniqueId());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// validate
		ResourceValidationUtils.validateModelObjects(resourceFromImport, resourceFromGet);

		// validate audit
		resourceDetails.setVersion(resourceDetails.getVersion());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails);

		auditAction = "ResourceImport";
		resourceVersion = "0.1";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void importNewResource_perfromByAdmin_ownedBy_diffrentUser() throws Exception {

		RestResponse importResponse = importNewResource(UserRoleEnum.DESIGNER);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		// clean audit
		DbUtils.cleanAllAudits();

		importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CHECKOUT_STATE.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), importResponse.getErrorCode());

		String[] split = resourceFromImport.getLastUpdaterFullName().split(" ");
		String firstName = split[0];
		String lastName = split[1];
		List<String> variables = Arrays.asList(resourceFromImport.getName(), "resource", firstName, lastName,
				resourceFromImport.getLastUpdaterUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_IN_CHECKOUT_STATE.name(), variables,
				importResponse.getResponse());

	}

	@Test
	public void importNewResource_perfromByDesigner_ownedBy_diffrentUser() throws Exception {

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		// clean audit
		DbUtils.cleanAllAudits();

		importResponse = importNewResource(UserRoleEnum.DESIGNER);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CHECKOUT_STATE.name());
		assertEquals("Check response code after adding artifact", errorInfo.getCode(), importResponse.getErrorCode());

		String[] split = resourceFromImport.getLastUpdaterFullName().split(" ");
		String firstName = split[0];
		String lastName = split[1];
		List<String> variables = Arrays.asList(resourceFromImport.getName(), "resource", firstName, lastName,
				resourceFromImport.getLastUpdaterUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_IN_CHECKOUT_STATE.name(), variables,
				importResponse.getResponse());

	}

	@Test(enabled = false)
	public void importNewResource_nameSpace_vf() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4testVF",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertTrue(resourceRespJavaObject.getResourceType().equals(ResourceTypeEnum.VF));

	}

	@Test
	public void importNewResource_nameSpace_vfc() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4testVFC",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertTrue(resourceRespJavaObject.getResourceType().equals(ResourceTypeEnum.VFC));
	}

	@Test
	public void importNewResource_nameSpace_vl() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4testVL",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertTrue(resourceRespJavaObject.getResourceType().equals(ResourceTypeEnum.VL));

	}

	@Test
	public void importNewResource_nameSpace_cp() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4testCP",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertTrue(resourceRespJavaObject.getResourceType().equals(ResourceTypeEnum.CP));
	}

	@Test
	public void importNewResource_nameSpace_unknown() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4test",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		Resource resourceRespJavaObject = ResponseParser
				.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertTrue(resourceRespJavaObject.getResourceType().equals(ResourceTypeEnum.VFC));

	}

	@Test
	public void importNewResource_MissingNameSpace() throws Exception {
		RestResponse importResponse = ImportRestUtils.importNewResourceByName("importResource4testMissingNameSpace",
				UserRoleEnum.DESIGNER);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 400, importResponse.getErrorCode().intValue());

	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existInCheckOut() throws Exception {

		// import new resource

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in CHECKOUT state

		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 200, importResponse.getErrorCode().intValue());

		// validate response
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromImport);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceRespJavaObject.getUniqueId());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// validate
		ResourceValidationUtils.validateModelObjects(resourceFromImport, resourceFromGet);

		// validate audit
		resourceDetails.setVersion(resourceDetails.getVersion());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails);

		auditAction = "ResourceImport";
		resourceVersion = "0.1";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existIn_CheckIn_state() throws Exception {

		// import new resource

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		// checkIn resource

		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);

		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in CHECKIN state

		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 200, importResponse.getErrorCode().intValue());

		// validate response
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromImport);

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceRespJavaObject.getUniqueId());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// validate
		ResourceValidationUtils.validateModelObjects(resourceFromImport, resourceFromGet);

		// validate audit
		resourceDetails.setVersion(resourceDetails.getVersion());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails);

		resourceVersion = "0.2";
		auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void importNewResource_existIn_Ready4cert_state_performByTester() throws Exception {
		// import new resource

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		// add mandatory artifacts
		// // resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);

		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(checkInResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(checkInResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in CHECKIN state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.TESTER);

		// validate response
		resourceVersion = resourceDetails.getVersion();
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		assertNotNull("check response object is not null after create resouce", importResponse);
		assertNotNull("check error code exists in response after create resource", importResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), importResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				importResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);

		String auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setResourceName("");
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.TESTER.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.TESTER.getUserName());
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existIn_Ready4cert_state_performByDesigner() throws Exception {
		// import new resource

		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.DESIGNER);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION.name());
		assertNotNull("check response object is not null after create resouce", importResponse);
		assertNotNull("check error code exists in response after create resource", importResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), importResponse.getErrorCode());
		String[] split = resourceFromGet.getLastUpdaterFullName().split(" ");
		String firstName = split[0];
		String lastName = split[1];
		List<String> variables = Arrays.asList(resourceFromGet.getName(), "resource", firstName, lastName,
				resourceFromGet.getLastUpdaterUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION.name(), variables,
				importResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.DESIGNER.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.DESIGNER.getUserName());
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.READY_FOR_CERTIFICATION).toString());
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existIn_Ready4cert_state_performByAdmin() throws Exception {

		// import new resource
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.ADMIN);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 200, importResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());
		resourceVersion = resourceDetails.getVersion();
		// resourceVersion="0.2";

		// validate response
		Resource resourceFromImport = ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
		assertNotNull(resourceFromImport);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());

		// validate get response
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceRespJavaObject.getUniqueId());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// validate
		ResourceValidationUtils.validateModelObjects(resourceFromImport, resourceFromGet);

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void importNewResource_existIn_CerInProgress_state_performByTester() throws Exception {

		// import new resource
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// startCert
		RestResponse startCert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertNotNull("check response object is not null after resource request start certification", startCert);
		assertEquals("Check response code after checkout resource", 200, startCert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(startCert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(startCert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.TESTER);

		// validate response
		resourceVersion = resourceDetails.getVersion();
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
		assertNotNull("check response object is not null after create resouce", importResponse);
		assertNotNull("check error code exists in response after create resource", importResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), importResponse.getErrorCode());
		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				importResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setResourceName("");
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.TESTER.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.TESTER.getUserName());
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existIn_CerInProgress_state_performByDesigner() throws Exception {

		User sdncAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// import new resource
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// startCert
		RestResponse startCert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertNotNull("check response object is not null after resource request start certification", startCert);
		assertEquals("Check response code after checkout resource", 200, startCert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(startCert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(startCert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());
		resourceVersion = resourceDetails.getVersion();

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.DESIGNER);
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
		assertNotNull("check response object is not null after create resouce", importResponse);
		assertNotNull("check error code exists in response after create resource", importResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), importResponse.getErrorCode());
		List<String> variables = Arrays.asList(resourceDetails.getName(), "resource", sdncAdminUser.getFirstName(),
				sdncAdminUser.getLastName(), sdncAdminUser.getUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name(),
				variables, importResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.DESIGNER.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.DESIGNER.getUserName());
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.CERTIFICATION_IN_PROGRESS).toString());
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	// TODO DE171337
	@Test(enabled = false)
	public void importNewResource_existIn_CerInProgress_state_performByAdmin() throws Exception {

		User sdncAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// import new resource
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);
		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);

		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// checkIn resource
		resourceVersion = resourceDetails.getVersion();
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";
		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		// startCert
		RestResponse startCert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertNotNull("check response object is not null after resource request start certification", startCert);
		assertEquals("Check response code after checkout resource", 200, startCert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(startCert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(startCert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());
		resourceVersion = resourceDetails.getVersion();

		// clean audit
		DbUtils.cleanAllAudits();

		// import new resource while resource already exist in other state
		importResponse = ImportRestUtils.importNewResourceByName("importResource4test", UserRoleEnum.ADMIN);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
		assertNotNull("check response object is not null after create resouce", importResponse);
		assertNotNull("check error code exists in response after create resource", importResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), importResponse.getErrorCode());
		List<String> variables = Arrays.asList(resourceDetails.getName(), "resource", sdncAdminUser.getFirstName(),
				sdncAdminUser.getLastName(), sdncAdminUser.getUserId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name(),
				variables, importResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "ResourceImport";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setModifierUid(UserRoleEnum.ADMIN.getUserId());
		expectedResourceAuditJavaObject.setModifierName(UserRoleEnum.ADMIN.getUserName());
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.CERTIFICATION_IN_PROGRESS).toString());
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	// TODO DE171337
	// @Test(enabled = false)
	// public void
	// importNewResource_existIn_Certified_state_chnage_reqAndCap_byDesigner()
	// throws Exception{
	//
	// // Andrey - set default artifact details
	// ArtifactDefinition artifactDefinition =
	// artifactUtils.constructDefaultArtifactInfo();
	//
	// // import new resource
	// RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);
	// assertNotNull("check response object is not null after import resource",
	// importResponse);
	// assertNotNull("check error code exists in response after import
	// resource", importResponse.getErrorCode());
	// assertEquals("Check response code after import resource", 201,
	// importResponse.getErrorCode().intValue());
	// String resourceId =
	// ResponseParser.getUniqueIdFromResponse(importResponse);
	// resourceDetails =
	// ResponseParser.parseToObject(importResponse.getResponse(),
	// ResourceReqDetails.class);
	// resourceVersion = resourceDetails.getVersion();
	// RestResponse resourceGetResponse =
	// ResourceRestUtils.getResource(sdncModifierDetails,
	// resourceDetails.getUniqueId());
	// assertEquals("Check response code after get resource", 200,
	// resourceGetResponse.getErrorCode().intValue());
	// Resource resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	// assertNotNull(resourceFromGet);
	//
	// // add mandatory artifacts
	// // resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
	// resourceGetResponse);
	// resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
	// resourceDetails.getUniqueId());
	// assertEquals("Check response code after get resource", 200,
	// resourceGetResponse.getErrorCode().intValue());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	// assertNotNull(resourceFromGet);
	// resourceDetails =
	// ResponseParser.parseToObject(importResponse.getResponse(),
	// ResourceReqDetails.class);
	// resourceDetails.setVersion(resourceFromGet.getVersion());
	//
	// // add artifact
	// artifactDefinition.setArtifactName(artifactName1);
	// ArtifactRestUtils.addInformationalArtifactToResource(resourceDetails,
	// sdncModifierDetails, resourceVersion , artifactDefinition);
	//
	// // add artifact
	// artifactDefinition.setArtifactName(artifactName2);
	// resourceUtils.add_artifact(resourceDetails, sdncModifierDetails,
	// resourceVersion , artifactDefinition);
	//
	// // add interface
	// artifactDefinition.setArtifactName(interfaceArtifactName);
	// ResourceRestUtils.add_interface(resourceDetails, sdncModifierDetails,
	// resourceVersion , artifactDefinition);
	//
	// //construct fields for validation
	// resourceVersion="1.0";
	//
	// ResourceRespJavaObject resourceRespJavaObject =
	// Convertor.constructFieldsForRespValidation(resourceDetails,
	// resourceVersion);
	// ArrayList<String> artifacts = new ArrayList<String>();
	//
	// artifacts.add(resourceId+":"+artifactName1);
	// artifacts.add(resourceId+":"+artifactName2);
	// resourceRespJavaObject.setArtifacts(artifacts);
	// ArrayList<String> interfaces = new ArrayList<String>();
	//
	// interfaces.add(interfaze);
	// resourceRespJavaObject.setInterfaces(interfaces);
	//
	// // checkIn resource
	// resourceVersion = resourceDetails.getVersion();
	// String checkinComment = "good checkin";
	// String checkinComentJson = "{\"userRemarks\": \""+checkinComment+"\"}";
	// RestResponse checkInResponse =
	// LifecycleRestUtils.changeResourceState(resourceDetails,
	// sdncModifierDetails, resourceVersion, LifeCycleStatesEnum.CHECKIN,
	// checkinComentJson);
	// assertNotNull("check response object is not null after import resource",
	// checkInResponse);
	// assertEquals("Check response code after checkout resource", 200,
	// checkInResponse.getErrorCode().intValue());
	//
	// // req4cert resource
	// RestResponse request4cert =
	// LifecycleRestUtils.changeResourceState(resourceDetails,
	// sdncModifierDetails, resourceVersion,
	// LifeCycleStatesEnum.CERTIFICATIONREQUEST);
	// assertNotNull("check response object is not null after resource request
	// for certification", request4cert);
	// assertEquals("Check response code after checkout resource", 200,
	// request4cert.getErrorCode().intValue());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
	// assertNotNull(resourceFromGet);
	// resourceDetails =
	// ResponseParser.parseToObject(request4cert.getResponse(),
	// ResourceReqDetails.class);
	// resourceDetails.setVersion(resourceFromGet.getVersion());
	//
	// // startCert
	// RestResponse startCert =
	// LifecycleRestUtils.changeResourceState(resourceDetails,
	// sdncModifierDetails, resourceVersion,
	// LifeCycleStatesEnum.STARTCERTIFICATION);
	// assertNotNull("check response object is not null after resource request
	// start certification", startCert);
	// assertEquals("Check response code after checkout resource", 200,
	// startCert.getErrorCode().intValue());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(startCert.getResponse());
	// assertNotNull(resourceFromGet);
	// resourceDetails = ResponseParser.parseToObject(startCert.getResponse(),
	// ResourceReqDetails.class);
	// resourceDetails.setVersion(resourceFromGet.getVersion());
	//
	// // certify
	// RestResponse certify =
	// LifecycleRestUtils.changeResourceState(resourceDetails,
	// sdncModifierDetails, resourceVersion, LifeCycleStatesEnum.CERTIFY);
	// assertNotNull("check response object is not null after resource request
	// certify", certify);
	// assertEquals("Check response code after certify resource", 200,
	// certify.getErrorCode().intValue());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(certify.getResponse());
	// assertNotNull(resourceFromGet);
	// resourceDetails = ResponseParser.parseToObject(certify.getResponse(),
	// ResourceReqDetails.class);
	// resourceDetails.setVersion(resourceFromGet.getVersion());
	//
	// // clean audit
	// DbUtils.cleanAllAudits();
	//
	// // change resource details
	//
	// // import new resource while resource already exist in other state
	// importResponse =
	// ImportRestUtils.importNewResourceByName("importResource4testUpdateWithoutReqCap",
	// UserRoleEnum.ADMIN);
	// assertNotNull("check response object is not null after import resource",
	// importResponse);
	// assertNotNull("check error code exists in response after import
	// resource", importResponse.getErrorCode());
	// assertEquals("Check response code after import resource", 200,
	// importResponse.getErrorCode().intValue());
	// resourceDetails =
	// ResponseParser.parseToObject(importResponse.getResponse(),
	// ResourceReqDetails.class);
	// resourceVersion = resourceDetails.getVersion();
	// resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
	// resourceDetails.getUniqueId());
	// assertEquals("Check response code after get resource", 200,
	// resourceGetResponse.getErrorCode().intValue());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	// assertNotNull(resourceFromGet);
	//
	// // validate response
	// Resource resourceFromImport =
	// ResponseParser.convertResourceResponseToJavaObject(importResponse.getResponse());
	// assertNotNull(resourceFromImport);
	//
	// resourceDetails =
	// ResponseParser.parseToObject(importResponse.getResponse(),
	// ResourceReqDetails.class);
	// resourceRespJavaObject =
	// Convertor.constructFieldsForRespValidation(resourceDetails);
	// resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	//
	// // validate get response
	// resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
	// resourceRespJavaObject.getUniqueId());
	// resourceFromGet =
	// ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
	// assertNotNull(resourceFromGet);
	//
	// // validate
	// ResourceValidationUtils.validateModelObjects(resourceFromImport,
	// resourceFromGet);
	//
	// // validate audit
	// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
	// Convertor.constructFieldsForAuditValidation(resourceDetails,
	// resourceVersion);
	// auditAction="ResourceImport";
	// expectedResourceAuditJavaObject.setAction(auditAction);
	// expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
	// expectedResourceAuditJavaObject.setPrevVersion(resourceVersion);
	// expectedResourceAuditJavaObject.setStatus("200");
	// expectedResourceAuditJavaObject.setDesc("OK");
	// expectedResourceAuditJavaObject.setToscaNodeType(resourceFromGet.getToscaResourceName());
	// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
	// auditAction, null, false);
	// }

	@Test
	public void importNewResource_uuidTest() throws Exception {
		RestResponse importResponse = importNewResource(UserRoleEnum.ADMIN);

		assertNotNull("check response object is not null after import resource", importResponse);
		assertNotNull("check error code exists in response after import resource", importResponse.getErrorCode());
		assertEquals("Check response code after import resource", 201, importResponse.getErrorCode().intValue());
		String oldUuid = ResponseParser.getValueFromJsonResponse(importResponse.getResponse(), "uuid");

		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceVersion = resourceDetails.getVersion();
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		Resource resourceFromGet = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		// add mandatory artifacts
		// resourceUtils.addResourceMandatoryArtifacts(sdncModifierDetails,
		// resourceGetResponse);
		resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails, resourceDetails.getUniqueId());
		assertEquals("Check response code after get resource", 200, resourceGetResponse.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(importResponse.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		RestResponse checkInResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				"0.1", LifeCycleStatesEnum.CHECKIN);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		String newUuid = ResponseParser.getValueFromJsonResponse(checkInResponse.getResponse(), "uuid");
		assertTrue(ResourceValidationUtils.validateUuidAfterChangingStatus(oldUuid, newUuid));

		// req4cert resource
		RestResponse request4cert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertNotNull("check response object is not null after resource request for certification", request4cert);
		assertEquals("Check response code after checkout resource", 200, request4cert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(request4cert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(request4cert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		String newUuid2 = ResponseParser.getValueFromJsonResponse(request4cert.getResponse(), "uuid");
		assertTrue(ResourceValidationUtils.validateUuidAfterChangingStatus(oldUuid, newUuid2));

		// startCert
		RestResponse startCert = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				resourceVersion, LifeCycleStatesEnum.STARTCERTIFICATION);
		assertNotNull("check response object is not null after resource request start certification", startCert);
		assertEquals("Check response code after checkout resource", 200, startCert.getErrorCode().intValue());
		resourceFromGet = ResponseParser.convertResourceResponseToJavaObject(startCert.getResponse());
		assertNotNull(resourceFromGet);
		resourceDetails = ResponseParser.parseToObject(startCert.getResponse(), ResourceReqDetails.class);
		resourceDetails.setVersion(resourceFromGet.getVersion());

		String newUuid3 = ResponseParser.getValueFromJsonResponse(startCert.getResponse(), "uuid");
		assertTrue(ResourceValidationUtils.validateUuidAfterChangingStatus(oldUuid, newUuid3));

		RestResponse certify = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails, "0.1",
				LifeCycleStatesEnum.CERTIFY);
		assertNotNull("check response object is not null after import resource", certify);
		assertEquals("Check response code after checkout resource", 200, certify.getErrorCode().intValue());

		String newUuid4 = ResponseParser.getValueFromJsonResponse(certify.getResponse(), "uuid");
		assertTrue(ResourceValidationUtils.validateUuidAfterChangingStatus(oldUuid, newUuid4));

		RestResponse checkoutResponse = LifecycleRestUtils.changeResourceState(resourceDetails, sdncModifierDetails,
				"1.0", LifeCycleStatesEnum.CHECKOUT);
		assertNotNull("check response object is not null after import resource", checkInResponse);
		assertEquals("Check response code after checkout resource", 200, checkInResponse.getErrorCode().intValue());

		String newUuid5 = ResponseParser.getValueFromJsonResponse(checkoutResponse.getResponse(), "uuid");
		assertFalse(ResourceValidationUtils.validateUuidAfterChangingStatus(oldUuid, newUuid5));
	}

	@Test
	public void importNewResource_propertiesMapInternalUrlCredential() throws Exception {
		String folderName = "validateProporties_typeMap_valueUrlCredential";
		RestResponse importResponse = ImportRestUtils.importNewResourceByName(folderName, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.parseToObjectUsingMapper(importResponse.getResponse(), Resource.class);

		List<PropertyDefinition> properties = resource.getProperties();
		assertEquals("check properties size", 3, properties.size());

		PropertyDefinition propertyDefinition = properties.stream().filter(p -> p.getName().equals("validation_test"))
				.findFirst().get();
		String defaultValue = propertyDefinition.getDefaultValue();

		Map mapValue = gson.fromJson(defaultValue, Map.class);
		assertEquals("check Map value size", 2, mapValue.size());
		checkMapValues(mapValue, "key", 1, null);
		checkMapValues(mapValue, "key", 2, null);

		System.err.println("import Resource " + "<" + folderName + ">" + "response: " + importResponse.getErrorCode());

	}

	@Test
	public void importNewResource_propertiesListInternalUrlCredential() throws Exception {
		String folderName = "validateProporties_typeList_valueUrlCredential";
		RestResponse importResponse = ImportRestUtils.importNewResourceByName(folderName, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.parseToObjectUsingMapper(importResponse.getResponse(), Resource.class);

		List<PropertyDefinition> properties = resource.getProperties();
		assertEquals("check properties size", 3, properties.size());

		PropertyDefinition propertyDefinition = properties.stream().filter(p -> p.getName().equals("validation_test"))
				.findFirst().get();
		String defaultValue = propertyDefinition.getDefaultValue();

		List listValue = gson.fromJson(defaultValue, List.class);
		assertEquals("check List value size", 2, listValue.size());
		checkListValues(listValue.get(0), 1, SPECIAL_CHARACTERS);
		checkListValues(listValue.get(1), 2, SPECIAL_CHARACTERS);

		// Verify attributes
		List<PropertyDefinition> attributes = resource.getAttributes();

		assertEquals("check properties size", 2, attributes.size());

		// Verify attribute from type map
		PropertyDefinition attributeMapDefinition = attributes.stream()
				.filter(p -> p.getName().equals("validation_test_map")).findFirst().get();
		String defaultMapValue = attributeMapDefinition.getDefaultValue();
		Map attributeMapValue = gson.fromJson(defaultMapValue, Map.class);
		assertEquals("check Map value size", 2, attributeMapValue.size());
		checkMapValues(attributeMapValue, "key", 1, SPECIAL_CHARACTERS);
		checkMapValues(attributeMapValue, "key", 2, SPECIAL_CHARACTERS);

		// Verify attribute from type list
		PropertyDefinition attributeListDefinition = attributes.stream()
				.filter(p -> p.getName().equals("validation_test_list")).findFirst().get();
		String defaultListValue = attributeListDefinition.getDefaultValue();

		List attributeListValue = gson.fromJson(defaultListValue, List.class);
		assertEquals("check List value size", 2, attributeListValue.size());
		checkListValues(attributeListValue.get(0), 1, SPECIAL_CHARACTERS);
		checkListValues(attributeListValue.get(1), 2, SPECIAL_CHARACTERS);

		System.err.println("import Resource " + "<" + folderName + ">" + "response: " + importResponse.getErrorCode());

	}

	private void checkListValues(Object object, int index, String suffix) {

		Map map = (Map) object;
		assertEquals("check Map protocol value", "protocol" + index + (suffix == null ? "" : suffix),
				map.get("protocol"));
		assertEquals("check Map token value", "token" + index, map.get("token"));
	}

	// @Test
	public void importNewResource_validateProporties_typeTestDataType() throws Exception {
		String folderName = "validateProporties_typeTestDataType";
		RestResponse importResponse = ImportRestUtils.importNewResourceByName(folderName, UserRoleEnum.DESIGNER);

		Resource resource = ResponseParser.parseToObjectUsingMapper(importResponse.getResponse(), Resource.class);

	}

	private void checkMapValues(Map mapValue, String key, int index, String suffix) {

		Map map1 = (Map) mapValue.get(key + index);
		assertEquals("check Map protocol value", "protocol" + index + (suffix == null ? "" : suffix),
				map1.get("protocol"));
		assertEquals("check Map token value", "token" + index, map1.get("token"));

	}
}

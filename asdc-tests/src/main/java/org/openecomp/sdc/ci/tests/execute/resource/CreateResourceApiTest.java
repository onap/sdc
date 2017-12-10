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

package org.openecomp.sdc.ci.tests.execute.resource;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_SUCCESS;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.Convertor;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ResourceValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.google.gson.Gson;

/**
 * @author yshlosberg
 * 
 */
public class CreateResourceApiTest extends ComponentBaseTest {

	private static Logger log = LoggerFactory.getLogger(CreateResourceApiTest.class.getName());

	String contentTypeHeaderData = "application/json";
	String acceptHeaderDate = "application/json";
	String resourceVersion = "0.1";

	@Rule
	public static TestName name = new TestName();

	public CreateResourceApiTest() {
		super(name, CreateResourceApiTest.class.getName());
	}

	@Test
	public void createResourceTest() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// ResourceReqDetails resourceDetails = new
		// ResourceReqDetails(resourceName, description, resourceTags, category,
		// derivedFrom, vendorName, vendorRelease, contactId, icon);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		String resourceName = resourceDetails.getName();
		resourceDetails.setTags(Arrays.asList(resourceName, resourceName, resourceName, resourceName, "tag2", "tag2"));
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void createResourceNonDefaultResourceTypeTest() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		String resourceType = ResourceTypeEnum.CP.toString();
		resourceDetails.setResourceType(resourceType);
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		resourceRespJavaObject.setResourceType(resourceType);
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void createResourceTest_costAndLicenseType() throws Exception {

		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response

		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);

	}

	// ////Benny
	@Test
	public void createResourceTest_CostIsMissing() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		// resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
	}

	@Test
	public void createResourceTest_LicenseTypeMissing() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		// resourceDetails.setLicenseType("User");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
	}

	@Test
	public void createResourceTest_LicenseType_Installation() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("99999.999");
		resourceDetails.setLicenseType("Installation");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
	}

	@Test
	public void createResourceTest_LicenseType_CPU() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("0.0");
		resourceDetails.setLicenseType("CPU");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		// validate response
		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncModifierDetails,
				resourceDetails.getUniqueId());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
	}

	@Test
	public void createResourceTest_LicenseType_Uppercase() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("0.0");
		resourceDetails.setLicenseType("INSTALLATION");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request", createResponse.getResponseMessage());
	}

	@Test
	public void createResourceTest_LicenseType_Invalid() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("0.0");
		resourceDetails.setLicenseType("CPUUU");

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request", createResponse.getResponseMessage());
	}

	@Test
	public void createResourceTest_CostValidation_noNumeric() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");
		resourceDetails.setCost("12355.34b");
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

	}

	@Test
	public void createResourceTest_CostValidation_valueLength() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";
		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");

		// Adding invalid cost
		resourceDetails.setCost("12355.3434");
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());
	}

	@Test
	public void createResourceTest_CostValidation_PriceLimitations() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";
		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");

		// Adding invalid cost
		RestResponse createResponse;
		// create resource

		resourceDetails.setCost("000000.000");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

		/*
		 * resourceDetails.setCost("0550.457"); createResponse =
		 * resourceUtils.createResource(resourceDetails, sdncModifierDetails);
		 * assertNotNull("check response object is not null after create resource"
		 * , createResponse);
		 * assertNotNull("check error code exists in response after create resource"
		 * , createResponse.getErrorCode());
		 * assertEquals("Check response code after create resource", 400,
		 * createResponse.getErrorCode().intValue());
		 * assertEquals("Check response code after create resource",
		 * "Bad Request", createResponse.getResponseMessage().toString());
		 */

		resourceDetails.setCost("1");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

		resourceDetails.setCost("123555.340");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

		resourceDetails.setCost("123.4570");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

		resourceDetails.setCost("123555.30");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

		resourceDetails.setCost("123.5550");
		createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request",
				createResponse.getResponseMessage().toString());

	}

	@Test
	public void createResourceTest_CostIsNull() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");
		resourceDetails.setCost("");
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request", createResponse.getResponseMessage());

	}

	@Test
	public void createResourceTest_LicenseIsNull() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// set resource details
		String resourceName = "CISCO4572";
		String description = "description";
		// Duplicate tags are allowed and should be de-duplicated by the server
		// side
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add(resourceName);
		resourceTags.add("tag2");
		resourceTags.add("tag2");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		// Adding cost and licenseType
		resourceDetails.setCost("12355.345");
		resourceDetails.setLicenseType("User");
		resourceDetails.setLicenseType("");
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 400, createResponse.getErrorCode().intValue());
		assertEquals("Check response code after create resource", "Bad Request", createResponse.getResponseMessage());

	}

	@Test
	public void createResourceTest_uri_methods() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		ResourceReqDetails resourceDetails = createRandomResource();

		Config config = Utils.getConfig();

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), contentTypeHeaderData);
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), sdncModifierDetails.getUserId());

		Gson gson = new Gson();
		String userBodyJson = gson.toJson(resourceDetails);
		log.debug(userBodyJson);
		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort());

		RestResponse createResourceResponse2 = http.httpSendByMethod(url, "PUT", userBodyJson, headersMap);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.NOT_ALLOWED.name());

		assertNotNull("check response object is not null after create resource", createResourceResponse2);
		assertNotNull("check error code exists in response after create resource",
				createResourceResponse2.getErrorCode());
		assertEquals("Check response code after create resource", errorInfo.getCode(),
				createResourceResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				createResourceResponse2.getResponse());

	}

	private ResourceReqDetails createRandomResource() {
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);
		return resourceDetails;
	}

	@Test
	public void createResource_role_tester() throws Exception {

		// init TESTER user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);

		ResourceReqDetails resourceDetails2 = createRandomResource();

		// create resource
		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				restResponse2.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
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

	// TODO DE171450(to check)
	@Test
	public void createResource_role_DESIGNER() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		ResourceReqDetails resourceDetails = createRandomResource();
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		// validate response
		assertNotNull("check response object is not null after create resource", restResponse);
		assertNotNull("check error code exists in response after create resource", restResponse.getErrorCode());
		assertEquals(
				"Check response code after create resource, response message is: " + restResponse.getResponseMessage(),
				201, restResponse.getErrorCode().intValue());

	}

	@Test
	public void createResource_missing_header() throws Exception {
		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		ResourceReqDetails resourceDetails = createRandomResource();

		// set null in UserId header
		sdncModifierDetails.setUserId(null);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_INFORMATION.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_INFORMATION.name(), variables,
				restResponse2.getResponse());

		// //validate audit
		//
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// resourceUtils.constructFieldsForAuditValidation(resourceDetails,resourceVersion);
		//
		// String auditAction="Create";
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setModifierUid("null null");
		// expectedResourceAuditJavaObject.setModifierName("null null");
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setCurrState("");
		// expectedResourceAuditJavaObject.setPrevVersion("");
		// expectedResourceAuditJavaObject.setCurrVersion("");
		// expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		//
		// String auditDesc =
		// AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		// expectedResourceAuditJavaObject.setDesc(auditDesc);
		//
		// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
		// auditAction);
		// TODO: yshlosberg enable back

	}

	@Test
	public void createResource_existing_resource() throws Exception {
		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// ResourceReqDetails resourceDetails = createRandomResource();
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", restResponse);
		assertNotNull("check error code exists in response after create resource", restResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());

		// set resource details
		ResourceReqDetails resourceDetails2 = ElementFactory.getDefaultResource();

		// clean ES DB
		DbUtils.cleanAllAudits();

		// create resource
		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource", resourceDetails2.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_category() throws Exception {

		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		;

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		category = null;

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CATEGORY.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CATEGORY.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_empty_category() throws Exception {

		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		category = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CATEGORY.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CATEGORY.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_tags() throws Exception {

		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		;

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();

		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource
		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_TAGS.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_TAGS.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	// TODO DE171450(to check)
	@Test
	public void createResourceTest_with_multiple_tags() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// // set resource details
		// String resourceName = "CISCO4";
		// String description = "description";
		// ArrayList<String> resourceTags = new ArrayList<String>();
		// resourceTags.add(resourceName);
		// resourceTags.add("tag2");
		// String category = ResourceServiceCategoriesEnum.VOIP.getValue();
		// ArrayList<String> derivedFrom = new ArrayList<String>();
		// derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		// String vendorName = "Oracle";
		// String vendorRelease = "1.5";
		// String icon = "myICON";
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		resourceDetails.setTags(Arrays.asList(resourceDetails.getName(), "tag2"));

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", restResponse);
		assertNotNull("check error code exists in response after create resource", restResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, restResponse.getErrorCode().intValue());

	}

	@Test
	public void createResourceTest_empty_tag() throws Exception {

		// init ADMIN user

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add("");
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		ResourceReqDetails resourceDetails = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource
		RestResponse restResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_FIELD_FORMAT.name());

		assertNotNull("check response object is not null after create resouce", restResponse);
		assertNotNull("check error code exists in response after create resource", restResponse.getErrorCode());
		assertEquals("Check response code after create resource", errorInfo.getCode(), restResponse.getErrorCode());

		List<String> variables = Arrays.asList("Resource", "tag");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), variables,
				restResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_with_empty_vendorName() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		vendorName = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource
		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_VENDOR_NAME.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_NAME.name(), variables,
				restResponse2.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_vendorName() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		vendorName = null;

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response
		assertNotNull("check response object is not null after create resource", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create resource", 400, restResponse2.getErrorCode().intValue());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_NAME.name(), variables,
				restResponse2.getResponse());

	}

	@Test
	public void createResourceTest_with_empty_vendorRelease() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add("root");
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		vendorRelease = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_VENDOR_RELEASE.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_RELEASE.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_vendorRelease() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		vendorRelease = null;

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_VENDOR_RELEASE.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_VENDOR_RELEASE.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_with_empty_contactId() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		contactId = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CONTACT.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CONTACT.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_contactId() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		contactId = null;

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CONTACT.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CONTACT.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_with_empty_icon() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		icon = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_ICON.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_ICON.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_icon() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		icon = null;

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_ICON.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_ICON.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_with_empty_description() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		description = "";

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createResourceTest_without_description() throws Exception {
		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// set resource details
		String resourceName = "CISCO4";
		String description = "description";
		ArrayList<String> resourceTags = new ArrayList<String>();
		resourceTags.add(resourceName);
		String category = ServiceCategoriesEnum.VOIP.getValue();
		ArrayList<String> derivedFrom = new ArrayList<String>();
		derivedFrom.add(NormativeTypesEnum.ROOT.getNormativeName());
		String vendorName = "Oracle";
		String vendorRelease = "1.5";
		String contactId = "jh0003";
		String icon = "myICON";

		// set resource details
		description = null;

		ResourceReqDetails resourceDetails2 = new ResourceReqDetails(resourceName, description, resourceTags, category,
				derivedFrom, vendorName, vendorRelease, contactId, icon);

		// create resource

		RestResponse restResponse2 = ResourceRestUtils.createResource(resourceDetails2, sdncModifierDetails);

		// validate response

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name());

		assertNotNull("check response object is not null after create resouce", restResponse2);
		assertNotNull("check error code exists in response after create resource", restResponse2.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse2.getErrorCode());

		List<String> variables = Arrays.asList("Resource");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), variables,
				restResponse2.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails2, resourceVersion);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createAndGetResourceByNameAndVersion() throws Exception {

		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		ResourceReqDetails resourceDetailsComp = ElementFactory.getDefaultResource("testresourceComp",
				NormativeTypesEnum.COMPUTE, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, sdncModifierDetails.getUserId());

		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetailsComp, sdncModifierDetails);
		// validate response
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		String resourceVersion = "0.1";
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetailsComp,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResourceByNameAndVersion(
				sdncModifierDetails.getUserId(), resourceDetailsComp.getName(), resourceDetailsComp.getVersion());
		assertEquals("Check response code after delete resource", 200, resourceGetResponse.getErrorCode().intValue());
		// Resource resource =
		// ResourceRestUtils.parseResourceFromListResp(resourceGetResponse);
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
		// resourceDetailsComp.setUniqueId(resource.getUniqueId());

	}

	@Test
	public void createResourceResourceTypeNotExistsTest() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		String resourceType = "NOT EXISTS";
		resourceDetails.setResourceType(resourceType);
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());

		assertNotNull("check response object is not null after create resouce", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), createResponse.getErrorCode());

		List<String> variables = new ArrayList<>();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(), variables,
				createResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = Convertor
				.constructFieldsForAuditValidation(resourceDetails, resourceVersion);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@Test
	public void createResourceResourceTypeEmptyTest() throws Exception {

		// init ADMIN user
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		// String resourceType = "";
		// resourceDetails.setResourceType(resourceType);
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);

		// ErrorInfo errorInfo =
		// ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_CONTENT.name());
		//
		// assertNotNull("check response object is not null after create
		// resouce", createResponse);
		// assertNotNull("check error code exists in response after create
		// resource", createResponse.getErrorCode());
		// assertEquals("Check response code after create service",
		// errorInfo.getCode(), createResponse.getErrorCode());
		//
		// List<String> variables = new ArrayList<>();
		// ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_CONTENT.name(),
		// variables, createResponse.getResponse());
		//
		// // validate audit
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// Convertor.constructFieldsForAuditValidation(resourceDetails,
		// resourceVersion);
		// String auditAction = "Create";
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setPrevVersion("");
		// expectedResourceAuditJavaObject.setCurrState("");
		// expectedResourceAuditJavaObject.setCurrVersion("");
		// expectedResourceAuditJavaObject.setResourceName("");
		// expectedResourceAuditJavaObject.setModifierUid(ElementFactory.getDefaultUser(UserRoleEnum.ADMIN).getUserId());
		// expectedResourceAuditJavaObject.setModifierName(ElementFactory.getDefaultUser(UserRoleEnum.ADMIN).getFullName());
		// expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		//
		// String auditDesc =
		// AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		// expectedResourceAuditJavaObject.setDesc(auditDesc);
		// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
		// auditAction, null, false);
	}

	@Test
	public void checkInvariantUuidIsImmutable() throws Exception {
		// choose the user to create resource
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResource();
		String invariantUuidDefinedByUser = "!!!!!!!!!!!!!!!!!!!!!!!!";
		resourceDetails.setInvariantUUID(invariantUuidDefinedByUser);
		String resourceName = resourceDetails.getName();
		resourceDetails.setTags(Arrays.asList(resourceName, resourceName, resourceName, resourceName, "tag2", "tag2"));
		// create resource
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, sdncUserDetails);
		BaseRestUtils.checkStatusCode(createResponse, "create request failed", false, 201);
		// validate response
		assertNotNull("check response object is not null after create resource", createResponse);
		assertNotNull("check error code exists in response after create resource", createResponse.getErrorCode());
		assertEquals("Check response code after create resource", 201, createResponse.getErrorCode().intValue());

		Resource resourceCreation = ResponseParser.convertResourceResponseToJavaObject(createResponse.getResponse());
		String invariantUUIDcreation = resourceCreation.getInvariantUUID();
		// validate response
		ResourceRespJavaObject resourceRespJavaObject = Convertor.constructFieldsForRespValidation(resourceDetails,
				resourceVersion);
		resourceRespJavaObject.setLifecycleState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		resourceRespJavaObject.setAbstractt("false");
		ResourceValidationUtils.validateResp(createResponse, resourceRespJavaObject);

		// validate get response
		RestResponse resourceGetResponse = ResourceRestUtils.getResource(sdncUserDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(resourceGetResponse);
		Resource resourceGetting = ResponseParser
				.convertResourceResponseToJavaObject(resourceGetResponse.getResponse());
		ResourceValidationUtils.validateResp(resourceGetResponse, resourceRespJavaObject);
		String invariantUUIDgetting = resourceGetting.getInvariantUUID();
		assertEquals(invariantUUIDcreation, invariantUUIDgetting);

		// Update resource with new invariant UUID
		RestResponse restResponseUpdate = ResourceRestUtils.updateResourceMetadata(resourceDetails, sdncUserDetails,
				resourceDetails.getUniqueId());
		BaseRestUtils.checkSuccess(restResponseUpdate);
		Resource updatedResource = ResponseParser.convertResourceResponseToJavaObject(restResponseUpdate.getResponse());
		String invariantUUIDupdating = updatedResource.getInvariantUUID();
		assertEquals(invariantUUIDcreation, invariantUUIDupdating);

		// Do checkin
		RestResponse restResponseCheckin = LifecycleRestUtils.changeResourceState(resourceDetails, sdncUserDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(restResponseCheckin);
		Resource checkinResource = ResponseParser
				.convertResourceResponseToJavaObject(restResponseCheckin.getResponse());
		String invariantUUIDcheckin = checkinResource.getInvariantUUID();
		String version = checkinResource.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckin);
		assertEquals(version, "0.1");

		// Do checkout
		RestResponse restResponseCheckout = LifecycleRestUtils.changeResourceState(resourceDetails, sdncUserDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		BaseRestUtils.checkSuccess(restResponseCheckout);
		Resource ResourceResource = ResponseParser
				.convertResourceResponseToJavaObject(restResponseCheckout.getResponse());
		String invariantUUIDcheckout = ResourceResource.getInvariantUUID();
		version = ResourceResource.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckout);
		assertEquals(version, "0.2");

		// do certification request
		RestResponse restResponseCertificationRequest = LifecycleRestUtils.changeResourceState(resourceDetails,
				sdncUserDetails, resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		BaseRestUtils.checkSuccess(restResponseCertificationRequest);
		Resource certificationRequestResource = ResponseParser
				.convertResourceResponseToJavaObject(restResponseCertificationRequest.getResponse());
		String invariantUUIDcertificationRequest = certificationRequestResource.getInvariantUUID();
		version = certificationRequestResource.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcertificationRequest);
		assertEquals(version, "0.2");

		// start certification
		RestResponse restResponseStartCertification = LifecycleRestUtils.changeResourceState(resourceDetails,
				sdncUserDetails, resourceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		BaseRestUtils.checkSuccess(restResponseStartCertification);
		Resource startCertificationRequestResource = ResponseParser
				.convertResourceResponseToJavaObject(restResponseStartCertification.getResponse());
		String invariantUUIDStartCertification = startCertificationRequestResource.getInvariantUUID();
		version = startCertificationRequestResource.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDStartCertification);
		assertEquals(version, "0.2");

		// certify
		RestResponse restResponseCertify = LifecycleRestUtils.changeResourceState(resourceDetails, sdncUserDetails,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		BaseRestUtils.checkSuccess(restResponseCertify);
		Resource certifyResource = ResponseParser
				.convertResourceResponseToJavaObject(restResponseCertify.getResponse());
		String invariantUUIDcertify = certifyResource.getInvariantUUID();
		version = certifyResource.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcertify);
		assertEquals(version, "1.0");

	}

	// US672129 BENNY

	private void getResourceValidateInvariantUuid(String resourceUniqueId, String invariantUUIDcreation)
			throws Exception {
		RestResponse getResource = ResourceRestUtils.getResource(ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
				resourceUniqueId);
		BaseRestUtils.checkSuccess(getResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(getResource.getResponse(), Resource.class);
		assertEquals(invariantUUIDcreation, resource.getInvariantUUID());
	}

	@Test
	public void resourceInvariantUuid() throws Exception {

		User designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		User testerUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		ResourceReqDetails resourceDetails = ElementFactory.getDefaultResourceByType("VF200", NormativeTypesEnum.ROOT,
				ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, designerUser.getUserId(), ResourceTypeEnum.VF.toString());
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("newtestservice1",
				ServiceCategoriesEnum.MOBILITY, designerUser.getUserId());

		// ResourceReqDetails resourceDetails =
		// ElementFactory.getDefaultResource();
		resourceDetails.setInvariantUUID("kokomoko");
		RestResponse createResponse = ResourceRestUtils.createResource(resourceDetails, designerUser);
		assertEquals("Check response code after create resource", BaseRestUtils.STATUS_CODE_CREATED,
				createResponse.getErrorCode().intValue());
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResponse.getResponse(), Resource.class);
		String invariantUUIDcreation = resource.getInvariantUUID(); // generated
																	// when the
																	// component
																	// is
																	// created
																	// and never
																	// changed
		// get resource and verify InvariantUuid is not changed
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);

		// Update resource with new invariant UUID
		resourceDetails.setInvariantUUID("1234567890");
		RestResponse updateResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, designerUser,
				resourceDetails.getUniqueId());
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS,
				updateResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);

		// checkIn resource
		RestResponse restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);

		// checkIn resource
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);
		// certification request
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);
		// start certification
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);
		// certify
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser, LifeCycleStatesEnum.CERTIFY);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getResourceValidateInvariantUuid(resource.getUniqueId(), invariantUUIDcreation);
		// update resource
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		resourceDetails.setDescription("updatedDescription");
		resourceDetails.setVendorRelease("1.2.3.4");
		updateResponse = ResourceRestUtils.updateResourceMetadata(resourceDetails, designerUser,
				resourceDetails.getUniqueId());
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, updateResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// certification request
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// checkout resource
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// certification request
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);
		// start certification
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// cancel certification
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser,
				LifeCycleStatesEnum.CANCELCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// start certification
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// failure
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, testerUser,
				LifeCycleStatesEnum.FAILCERTIFICATION);
		assertEquals(STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// upload artifact
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultArtifact();
		ArtifactRestUtils.addInformationalArtifactToResource(artifactDetails, designerUser,
				resourceDetails.getUniqueId());
		assertEquals(STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

		// checkIn resource
		restResponse = LifecycleRestUtils.changeResourceState(resourceDetails, designerUser,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		// create instance
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetails);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, designerUser, serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_CREATED,
				createResourceInstanceResponse.getErrorCode().intValue());
		getResourceValidateInvariantUuid(resourceDetails.getUniqueId(), invariantUUIDcreation);

	}

}

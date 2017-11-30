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

package org.openecomp.sdc.ci.tests.execute.service;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ErrorInfo;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ServiceValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CreateServiceMetadataApiTest extends ComponentBaseTest {
	private static Logger logger = LoggerFactory.getLogger(CreateServiceMetadataApiTest.class.getName());

	String serviceBaseVersion = "0.1";

	@Rule
	public static TestName name = new TestName();

	public CreateServiceMetadataApiTest() {
		super(name, CreateServiceMetadataApiTest.class.getName());
	}

	@Test
	public void createDefaultService() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// String creator =
		// ElementFactory.getDefaultUser(UserRoleEnum.ADMIN).getUserId();

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", 201, restResponse.getErrorCode().intValue());

		// validate create service response vs actual

		Service service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, service, sdncUserDetails,
				(LifecycleStateEnum) null);

		// validate get service response vs actual
		restResponse = ServiceRestUtils.getService(serviceDetails, sdncUserDetails);
		service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, service, sdncUserDetails,
				(LifecycleStateEnum) null);

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createDefaultServiceUserDesigner() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", 201, restResponse.getErrorCode().intValue());

		// validate create service response vs actual

		Service service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, service, sdncUserDetails,
				(LifecycleStateEnum) null);

		// validate get service response vs actual
		restResponse = ServiceRestUtils.getService(serviceDetails, sdncUserDetails);
		service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceUserNotFound() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		sdncUserDetails.setUserId("no1234");

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				restResponse.getResponse());

		// validate audit

		sdncUserDetails.setFirstName("");
		sdncUserDetails.setLastName("");
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setModifierName("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceUserNotAllowed() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrVersion("");
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState("");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceEmptyName() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		String serviceName = "";
		serviceDetails.setName(serviceName);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_COMPONENT_NAME.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_COMPONENT_NAME.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceEmptyCategory() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		String category = "";

		serviceDetails.setCategories(null);
		// serviceDetails.addCategory(category);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_CATEGORY.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_CATEGORY.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceEmptyTag() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("");
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setTags(tags);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_FIELD_FORMAT.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service", "tag");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceEmptyDescription() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		String description = "";
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setDescription(description);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_MISSING_DESCRIPTION.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceEmptyTags() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ArrayList<String> tags = new ArrayList<String>();
		tags.add("");
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setTags(tags);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.INVALID_FIELD_FORMAT.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service", "tag");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_FIELD_FORMAT.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceByPutHttpMethod() throws Exception {

		String method = "PUT";

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE

		RestResponse restResponse = ServiceRestUtils.createServiceByHttpMethod(serviceDetails, sdncUserDetails, method,
				Urls.CREATE_SERVICE);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.NOT_ALLOWED.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				restResponse.getResponse());

		// //validate audit
		//
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// resourceUtils.constructFieldsForAuditValidation(serviceDetails,
		// serviceBaseVersion, sdncUserDetails);
		//
		// String auditAction="Create";
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setPrevVersion("");
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		// expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		//
		// String auditDesc =
		// AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		// expectedResourceAuditJavaObject.setDesc(auditDesc);
		//
		// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
		// auditAction);

	}

	@Test
	public void createServiceByDeleteHttpMethod() throws Exception {

		String method = "DELETE";

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE

		RestResponse restResponse = ServiceRestUtils.createServiceByHttpMethod(serviceDetails, sdncUserDetails, method,
				Urls.CREATE_SERVICE);

		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.NOT_ALLOWED.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList();
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.NOT_ALLOWED.name(), variables,
				restResponse.getResponse());

		// //validate audit
		//
		// ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		// resourceUtils.constructFieldsForAuditValidation(serviceDetails,
		// serviceBaseVersion, sdncUserDetails);
		//
		// String auditAction="Create";
		// expectedResourceAuditJavaObject.setAction(auditAction);
		// expectedResourceAuditJavaObject.setPrevState("");
		// expectedResourceAuditJavaObject.setPrevVersion("");
		// expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		// expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		//
		// String auditDesc =
		// AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		// expectedResourceAuditJavaObject.setDesc(auditDesc);
		//
		// AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
		// auditAction);

	}

	@Test
	public void createServiceTagLengthExceedLimit() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		StringBuffer tagBuffer = new StringBuffer();
		for (int i = 0; i < 1025; i++) {
			tagBuffer.append("a");
		}
		ArrayList<String> tags = new ArrayList<String>();
		tags.add(tagBuffer.toString());
		serviceDetails.setTags(tags);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE

		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("50");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_SINGLE_TAG_EXCEED_LIMIT.name(), variables,
				restResponse.getResponse());

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc("50"));
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

		/*
		 * ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject =
		 * ServiceValidationUtils.constructFieldsForAuditValidation(
		 * serviceDetails, serviceBaseVersion, sdncUserDetails);
		 * 
		 * String auditAction="Create";
		 * expectedResourceAuditJavaObject.setAction(auditAction);
		 * expectedResourceAuditJavaObject.setPrevState("");
		 * expectedResourceAuditJavaObject.setPrevVersion("");
		 * expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.
		 * NOT_CERTIFIED_CHECKOUT).toString());
		 * expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().
		 * toString()); expectedResourceAuditJavaObject.setDesc(auditDesc);
		 * 
		 * AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject,
		 * auditAction, null);
		 */

	}

	@Test
	public void createServiceAlreadyExistException() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// create service with the same name
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());

		// validate create service response vs actual

		List<String> variables = Arrays.asList("Service", serviceDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.COMPONENT_NAME_ALREADY_EXIST.name(), variables,
				restResponse.getResponse());

		// validate audit

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());

		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void createServiceWrongContactId() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setContactId("123as");

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		ErrorInfo errorInfo = ErrorValidationUtils
				.parseErrorConfigYaml(ActionStatus.COMPONENT_INVALID_CONTACT.name());

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", errorInfo.getCode(), restResponse.getErrorCode());
	}

	@Test
	public void createServiceProjectName() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setProjectCode("12345");

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		Integer expectedCode = 201;

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", expectedCode, restResponse.getErrorCode());
		Service service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());

		assertEquals("12345", service.getProjectCode());
	}

	@Test
	public void createAndGetByNameAndVersion() throws Exception {

		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		// create
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		assertEquals("Check response code after create service", 201, restResponse.getErrorCode().intValue());

		// get
		restResponse = ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails, serviceDetails.getName(),
				serviceBaseVersion);
		assertEquals("Check response code after get service", 200, restResponse.getErrorCode().intValue());

		Service service = ResponseParser.convertServiceResponseToJavaObject(restResponse.getResponse());
		String uniqueId = service.getUniqueId();
		serviceDetails.setUniqueId(uniqueId);
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, service, sdncUserDetails,
				(LifecycleStateEnum) null);
	}

	//// US553874

	@JsonIgnore
	@Test
	public void createServiceIsVNF_isFalse() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after updating Interface Artifact", 201,
				restResponse.getErrorCode().intValue());

		// get service and verify that service created with isVNF defined in
		// serviceDetails
		RestResponse serviceByNameAndVersion = ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
				serviceDetails.getName(), serviceBaseVersion);
		Service serviceObject = ResponseParser
				.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, serviceObject, sdncUserDetails,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@JsonIgnore
	@Test
	public void createServiceIsVNF_isTrue() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after updating Interface Artifact", 201,
				restResponse.getErrorCode().intValue());

		// get service and verify that service created with isVNF defined in
		// serviceDetails
		RestResponse serviceByNameAndVersion = ServiceRestUtils.getServiceByNameAndVersion(sdncUserDetails,
				serviceDetails.getName(), serviceBaseVersion);
		Service serviceObject = ResponseParser
				.convertServiceResponseToJavaObject(serviceByNameAndVersion.getResponse());
		ServiceValidationUtils.validateServiceResponseMetaData(serviceDetails, serviceObject, sdncUserDetails,
				LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);

		// validate audit
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setDesc("OK");
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@JsonIgnore
	@Test(enabled = false)
	public void createServiceIsVNF_isNull() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		// clean audit DB before service creation
		DbUtils.cleanAllAudits();
		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		assertNotNull("check response object is not null after create service", restResponse);
		assertEquals("Check response code after updating Interface Artifact", 400,
				restResponse.getErrorCode().intValue());
		List<String> variables = Arrays.asList("VNF Service Indicator");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_DATA.name(), variables,
				restResponse.getResponse());

		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc("VNF Service Indicator"));
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);
	}

	@JsonIgnore
	@Test(enabled = false)
	public void createServiceEmptyIsVNF() throws Exception {

		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();

		DbUtils.cleanAllAudits();

		// send create service toward BE
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);

		assertNotNull("check response object is not null after create service", restResponse);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after create service", restResponse.getErrorCode(),
				restResponse.getErrorCode());

		// validate create service response vs actual
		List<String> variables = Arrays.asList("VNF Service Indicator");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_DATA.name(), variables,
				restResponse.getResponse());

		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, serviceBaseVersion, sdncUserDetails);

		String auditAction = "Create";
		expectedResourceAuditJavaObject.setAction(auditAction);
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		String auditDesc = AuditValidationUtils.buildAuditDescription(errorInfo, variables);
		expectedResourceAuditJavaObject.setDesc(auditDesc);

		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	private RestResponse createServiceWithMissingAttribute(String serviceDetails, User sdncModifierDetails)
			throws Exception {

		Config config = Utils.getConfig();

		Map<String, String> headersMap = ServiceRestUtils.prepareHeadersMap(sdncModifierDetails, false);
		headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), "no-cache");

		HttpRequest http = new HttpRequest();
		String url = String.format(Urls.CREATE_SERVICE, config.getCatalogBeHost(), config.getCatalogBePort());
		// TODO: ADD AUTHENTICATION IN REQUEST
		logger.debug(url);
		logger.debug("Send POST request to create service: {}", url);
		logger.debug("Service body: {}", serviceDetails);
		logger.debug("Service headers: {}", headersMap);
		RestResponse sendCreateUserRequest = http.httpSendPost(url, serviceDetails, headersMap);

		return sendCreateUserRequest;

	}

	@JsonIgnore
	@Test(enabled = false)
	public void createServiceVersion_isVNFDoesNotExistInJson() throws Exception {

		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		// new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		// clean audit DB before updating service
		DbUtils.cleanAllAudits();

		// remove isVNF from json sent to create service
		JSONObject jObject = new JSONObject(serviceDetails);
		jObject.remove("VNF");

		// send create service toward BE
		RestResponse restResponse = createServiceWithMissingAttribute(jObject.toString(), sdncUserDetails);
		assertNotNull("check error code exists in response after create service", restResponse.getErrorCode());
		assertEquals("Check response code after updating Interface Artifact", 400,
				restResponse.getErrorCode().intValue());
		List<String> variables = new ArrayList<String>();
		variables.add("VNF Service Indicator");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.MISSING_DATA.name(), variables,
				restResponse.getResponse());

		// validate audit
		ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = ServiceValidationUtils
				.constructFieldsForAuditValidation(serviceDetails, "0.1", sdncUserDetails);
		String auditAction = "Create";
		expectedResourceAuditJavaObject.setPrevState("");
		expectedResourceAuditJavaObject.setPrevVersion("");
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		// expectedResourceAuditJavaObject.setStatus("201");
		expectedResourceAuditJavaObject.setStatus(errorInfo.getCode().toString());
		expectedResourceAuditJavaObject.setDesc(errorInfo.getAuditDesc("VNF Service Indicator"));
		AuditValidationUtils.validateAudit(expectedResourceAuditJavaObject, auditAction, null, false);

	}

	@Test
	public void checkInvariantUuidIsImmutable() throws Exception {
		// choose the user to create service
		User sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		Component resourceDetailsVFCcomp = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.ADMIN, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVFCcomp, UserRoleEnum.ADMIN,
				true, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp, UserRoleEnum.ADMIN,
				LifeCycleStatesEnum.CERTIFY, true);

		// fill new service details
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		String invariantUuidDefinedByUser = "!!!!!!!!!!!!!!!!!!!!!!!!";
		serviceDetails.setInvariantUUID(invariantUuidDefinedByUser);

		// create service
		RestResponse restResponseCreation = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		BaseRestUtils.checkStatusCode(restResponseCreation, "create request failed", false, 201);
		Service service = ResponseParser.convertServiceResponseToJavaObject(restResponseCreation.getResponse());
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, service,
				UserRoleEnum.ADMIN, true);

		String invariantUUIDcreation = service.getInvariantUUID();

		// validate get service response vs actual
		RestResponse restResponseGetting = ServiceRestUtils.getService(serviceDetails, sdncUserDetails);
		BaseRestUtils.checkSuccess(restResponseGetting);
		service = ResponseParser.convertServiceResponseToJavaObject(restResponseGetting.getResponse());
		String invariantUUIDgetting = service.getInvariantUUID();

		assertEquals(invariantUUIDcreation, invariantUUIDgetting);

		// Update service with new invariant UUID
		RestResponse restResponseUpdate = ServiceRestUtils.updateService(serviceDetails, sdncUserDetails);
		BaseRestUtils.checkSuccess(restResponseUpdate);
		Service updatedService = ResponseParser.convertServiceResponseToJavaObject(restResponseUpdate.getResponse());
		String invariantUUIDupdating = updatedService.getInvariantUUID();
		assertEquals(invariantUUIDcreation, invariantUUIDupdating);

		// Do checkin
		RestResponse restResponseCheckin = LifecycleRestUtils.changeServiceState(serviceDetails, sdncUserDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(restResponseCheckin);
		Service checkinService = ResponseParser.convertServiceResponseToJavaObject(restResponseCheckin.getResponse());
		String invariantUUIDcheckin = checkinService.getInvariantUUID();
		String version = checkinService.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckin);
		assertEquals(version, "0.1");

		// Do checkout
		RestResponse restResponseCheckout = LifecycleRestUtils.changeServiceState(serviceDetails, sdncUserDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		BaseRestUtils.checkSuccess(restResponseCheckout);
		Service checkoutService = ResponseParser.convertServiceResponseToJavaObject(restResponseCheckout.getResponse());
		String invariantUUIDcheckout = checkoutService.getInvariantUUID();
		version = checkoutService.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcheckout);
		assertEquals(version, "0.2");

		// do certification request
		RestResponse restResponseCertificationRequest = LifecycleRestUtils.changeServiceState(serviceDetails,
				sdncUserDetails, serviceDetails.getVersion(), LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		BaseRestUtils.checkSuccess(restResponseCertificationRequest);
		Service certificationRequestService = ResponseParser
				.convertServiceResponseToJavaObject(restResponseCertificationRequest.getResponse());
		String invariantUUIDcertificationRequest = certificationRequestService.getInvariantUUID();
		version = certificationRequestService.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcertificationRequest);
		assertEquals(version, "0.2");

		// start certification
		RestResponse restResponseStartCertification = LifecycleRestUtils.changeServiceState(serviceDetails,
				sdncUserDetails, serviceDetails.getVersion(), LifeCycleStatesEnum.STARTCERTIFICATION);
		BaseRestUtils.checkSuccess(restResponseStartCertification);
		Service startCertificationRequestService = ResponseParser
				.convertServiceResponseToJavaObject(restResponseStartCertification.getResponse());
		String invariantUUIDStartCertification = startCertificationRequestService.getInvariantUUID();
		version = startCertificationRequestService.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDStartCertification);
		assertEquals(version, "0.2");

		// certify
		RestResponse restResponseCertify = LifecycleRestUtils.changeServiceState(serviceDetails, sdncUserDetails,
				serviceDetails.getVersion(), LifeCycleStatesEnum.CERTIFY);
		BaseRestUtils.checkSuccess(restResponseCertify);
		Service certifyService = ResponseParser.convertServiceResponseToJavaObject(restResponseCertify.getResponse());
		String invariantUUIDcertify = certifyService.getInvariantUUID();
		version = certifyService.getVersion();
		assertEquals(invariantUUIDcreation, invariantUUIDcertify);
		assertEquals(version, "1.0");

	}

	// US672129 Benny
	private void getServiceValidateInvariantUuid(String serviceUniqueId, String invariantUUIDcreation)
			throws Exception {
		RestResponse getService = ServiceRestUtils.getService(serviceUniqueId,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, getService.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(getService));
	}

	@Test // invariantUUID generated when the component is created and never
			// changed
	public void serviceInvariantUuid() throws Exception {
		User designerUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		User testerUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		User pmUser = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
		Component resourceDetailsVFCcomp = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp, UserRoleEnum.DESIGNER,
				LifeCycleStatesEnum.CERTIFY, true);
		// create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setInvariantUUID("123456");
		RestResponse restResponse = ServiceRestUtils.createService(serviceDetails, designerUser);
		assertEquals("Check response code after create resource", BaseRestUtils.STATUS_CODE_CREATED,
				restResponse.getErrorCode().intValue());
		Service service = ResponseParser.parseToObjectUsingMapper(restResponse.getResponse(), Service.class);
		// invariantUUID generated when the component is created and never
		// changed
		String invariantUUIDcreation = ResponseParser.getInvariantUuid(restResponse);
		// Add VF instance to service
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp, service,
				UserRoleEnum.DESIGNER, true);
		// get resource and verify InvariantUuid is not changed
		getServiceValidateInvariantUuid(service.getUniqueId(), invariantUUIDcreation);

		// Update service with new invariant UUID
		restResponse = ServiceRestUtils.updateService(serviceDetails, designerUser);
		assertEquals("Check response code after create resource", BaseRestUtils.STATUS_CODE_SUCCESS,
				restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// Checkin
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser, LifeCycleStatesEnum.CHECKIN);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// Checkout
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// certification request
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// start certification
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// certify
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser, LifeCycleStatesEnum.CERTIFY);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// update resource
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		serviceDetails.setDescription("updatedDescription");
		restResponse = ServiceRestUtils.updateService(serviceDetails, designerUser);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// certification request
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// Checkout
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// certification request
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// start certification
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// cancel certification
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser,
				LifeCycleStatesEnum.CANCELCERTIFICATION);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// start certification
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser,
				LifeCycleStatesEnum.STARTCERTIFICATION);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// failure
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, testerUser,
				LifeCycleStatesEnum.FAILCERTIFICATION);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// Checkout
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser,
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// Checkin
		restResponse = LifecycleRestUtils.changeServiceState(serviceDetails, designerUser, LifeCycleStatesEnum.CHECKIN);
		assertEquals(BaseRestUtils.STATUS_CODE_SUCCESS, restResponse.getErrorCode().intValue());
		assertEquals(invariantUUIDcreation, ResponseParser.getInvariantUuid(restResponse));
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

		// create instance
		ProductReqDetails productDetails = ElementFactory.getDefaultProduct();
		RestResponse createProductResponse = ProductRestUtils.createProduct(productDetails, pmUser);
		assertEquals(BaseRestUtils.STATUS_CODE_CREATED, createProductResponse.getErrorCode().intValue());
		ComponentInstanceReqDetails serviceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(serviceDetails);
		RestResponse createServiceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				serviceInstanceReqDetails, pmUser, productDetails.getUniqueId(), ComponentTypeEnum.PRODUCT);
		assertEquals("Check response code ", BaseRestUtils.STATUS_CODE_CREATED,
				createServiceInstanceResponse.getErrorCode().intValue());
		getServiceValidateInvariantUuid(serviceDetails.getUniqueId(), invariantUUIDcreation);

	}

}

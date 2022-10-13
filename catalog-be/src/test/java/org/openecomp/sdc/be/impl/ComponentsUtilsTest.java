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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.impl;

import fj.data.Either;
import java.util.ArrayList;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.constraints.EqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MaxLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentsUtilsTest {

	private ComponentsUtils createTestSubject() {
		return new ComponentsUtils(new AuditingManager(new AuditCassandraDao(mock(CassandraClient.class)), new TestConfigurationProvider()));
	}

	@Before
	public void init(){
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
	}

	@Test
	public void testGetAuditingManager() {
		ComponentsUtils testSubject;
		AuditingManager result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingManager();
		assertThat(result).isInstanceOf(AuditingManager.class);
	}


	@Test
	public void testGetResponseFormat() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String[] params = new String[] { "" };
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormat(actionStatus, params);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus().toString().startsWith("2")).isTrue();
	}


	@Test
	public void testGetResponseFormat_1()  {
		ComponentsUtils testSubject;
		StorageOperationStatus storageStatus = null;
		String[] params = new String[] { "" };
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormat(storageStatus, params);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus().toString().startsWith("5")).isTrue();
	}


	@Test
	public void testConvertToResponseFormatOrNotFoundErrorToEmptyList() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageOperationStatus = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		Either<List<T>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToResponseFormatOrNotFoundErrorToEmptyList(storageOperationStatus);
		assertThat(result.isRight()).isTrue();
	}

	@Test
	public void testConvertToResponseFormatOrNotFoundErrorToEmptyList_1() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageOperationStatus = StorageOperationStatus.NOT_FOUND;
		Either<List<T>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToResponseFormatOrNotFoundErrorToEmptyList(storageOperationStatus);
		assertThat(result.isLeft()).isTrue();
	}


	@Test
	public void testGetResponseFormatByResource() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		ResponseFormat result;
		Resource resource = null;
		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByResource(actionStatus, resource);

		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		resource = new Resource();
		result = testSubject.getResponseFormatByResource(actionStatus, resource);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, resource);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = testSubject.getResponseFormatByResource(ActionStatus.RESOURCE_NOT_FOUND, resource);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(404);

		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, resource);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_IN_USE, resource);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(403);
	}


	@Test
	public void testGetResponseFormatByResource_1() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String resourceName = "";
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		resourceName = null;
		result = testSubject.getResponseFormatByResource(actionStatus, resourceName);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		// test 2
		testSubject = createTestSubject();
		resourceName = "mock-name";
		result = testSubject.getResponseFormatByResource(actionStatus, resourceName);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		result = testSubject.getResponseFormatByResource(ActionStatus.RESOURCE_NOT_FOUND, resourceName);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(404);
	}


	@Test
	public void testGetResponseFormatByCapabilityType() throws Exception {
		ComponentsUtils testSubject;
		CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, null);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, capabilityType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, capabilityType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(500);
	}


	@Test
	public void testGetResponseFormatByElement() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		Object obj = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();

		obj = null;
		result = testSubject.getResponseFormatByElement(actionStatus, obj);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		obj = new Object();

		result = testSubject.getResponseFormatByElement(actionStatus, obj);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		List<Object> obj1 = new ArrayList<>();
		obj1.add(new RequirementDefinition());

		result = testSubject.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE, obj1);
		assertThat(result.getStatus()).isEqualTo(400);
	}


	@Test
	public void testGetResponseFormatByUser() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		User user = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		user = null;
		result = testSubject.getResponseFormatByUser(actionStatus, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		user = new User();
		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_USER_ID, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_EMAIL_ADDRESS, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_ROLE, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = testSubject.getResponseFormatByUser(ActionStatus.USER_NOT_FOUND, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(404);

		result = testSubject.getResponseFormatByUser(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED, user);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);
	}


	@Test
	public void testGetResponseFormatByUserId() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String userId = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByUserId(actionStatus, userId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testGetResponseFormatByDE() {
		ComponentsUtils testSubject;
		String serviceId = "";
		String envName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByDE(ActionStatus.ACCEPTED, serviceId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		result = testSubject.getResponseFormatByDE(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, serviceId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(500);

		result = testSubject.getResponseFormatByDE(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND, serviceId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);
	}


	@Test
	public void testGetResponseFormatByArtifactId() throws Exception {
		ComponentsUtils testSubject;
		String artifactId = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();

		result = testSubject.getResponseFormatByArtifactId(ActionStatus.ACCEPTED, artifactId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		result = testSubject.getResponseFormatByArtifactId(ActionStatus.RESOURCE_NOT_FOUND, artifactId);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(404);
	}

	@Test
	public void testAuditResource_1() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = new ResponseFormat();
		User modifier = null;
		String resourceName = "";
		AuditingActionEnum actionEnum = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditResource(responseFormat, modifier, resourceName, actionEnum);
	}



	@Test
	public void testAuditResource_3() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Resource resource = null;
		String resourceName = "";
		AuditingActionEnum actionEnum = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditResource(responseFormat, modifier, resource, resourceName, actionEnum);
	}


	@Test
	public void testAuditResource_4() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Resource resource = null;
		String resourceName = "";
		AuditingActionEnum actionEnum = null;
		ResourceVersionInfo prevResFields = null;
		String currentArtifactUuid = "";
		String artifactData = "";

		// test 1
		testSubject = createTestSubject();
		actionEnum = null;
		testSubject.auditResource(responseFormat, modifier, resource, resourceName, actionEnum, prevResFields,
				currentArtifactUuid, null);
	}

	@Test
	public void testConvertFromStorageResponse() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = null;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponse(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponse_1() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = null;
		ComponentTypeEnum type = null;
		ActionStatus result;

		// test 1
		testSubject = createTestSubject();
		storageResponse = null;
		result = testSubject.convertFromStorageResponse(storageResponse, type);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);;
	}


	@Test
	public void testConvertFromToscaError() {
		ComponentsUtils testSubject;
		ToscaError toscaError = null;
		ActionStatus result;

		// test 1
		testSubject = createTestSubject();
		toscaError = null;
		result = testSubject.convertFromToscaError(toscaError);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponseForCapabilityType() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForCapabilityType(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponseForLifecycleType() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForLifecycleType(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponseForResourceInstance() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		boolean isRelation = false;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForResourceInstance(storageResponse, isRelation);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testGetResponseFormatForResourceInstance() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String serviceName = "";
		String resourceInstanceName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatForResourceInstance(actionStatus, serviceName, resourceInstanceName);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testGetResponseFormatForResourceInstanceProperty() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String resourceInstanceName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatForResourceInstanceProperty(actionStatus, resourceInstanceName);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testConvertFromStorageResponseForResourceInstanceProperty() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForResourceInstanceProperty(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testAuditComponent() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		ResourceCommonInfo prevComponent = null;
		ResourceVersionInfo info = null;
		String comment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, prevComponent,info);
	}


	@Test
	public void testAuditComponent_1() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ResourceCommonInfo type = null;
		ResourceVersionInfo prevComponent = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, type, prevComponent);
	}


	@Test
	public void testValidateStringNotEmpty() {
		ComponentsUtils testSubject;
		String value = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateStringNotEmpty(value);
		assertThat(result).isFalse();
	}


	@Test
	public void testConvertFromStorageResponseForAdditionalInformation() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForAdditionalInformation(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromResultStatusEnum() {
		ComponentsUtils testSubject;
		ResultStatusEnum resultStatus = ResultStatusEnum.ELEMENT_NOT_FOUND;
		JsonPresentationFields elementType = null;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromResultStatusEnum(resultStatus, elementType);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testGetResponseFormatAdditionalProperty() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		NodeTypeEnum nodeType = null;
		AdditionalInformationEnum labelOrValue = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		additionalInfoParameterInfo = null;
		result = testSubject.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);

		// test 2
		testSubject = createTestSubject();
		labelOrValue = null;
		result = testSubject.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testGetResponseFormatAdditionalProperty_1() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatAdditionalProperty(actionStatus);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testConvertFromStorageResponseForConsumer() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForConsumer(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponseForGroupType() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForGroupType(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testConvertFromStorageResponseForDataType() {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForDataType(storageResponse);
		assertThat(result)
				.isInstanceOf(ActionStatus.class)
				.isEqualTo(ActionStatus.GENERAL_ERROR);
	}


	@Test
	public void testGetResponseFormatByGroupType() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		GroupTypeDefinition groupType = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		groupType = null;
		result = testSubject.getResponseFormatByGroupType(actionStatus, groupType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testGetResponseFormatByPolicyType() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByPolicyType(actionStatus, policyType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testGetResponseFormatByDataType() {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.AAI_ARTIFACT_GENERATION_FAILED;
		DataTypeDefinition dataType = null;
		List<String> properties = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		dataType = null;
		result = testSubject.getResponseFormatByDataType(actionStatus, dataType, properties);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(500);
	}

	@Test
	public void testconvertJsonToObject() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		User user = new User();
		ComponentsUtils testSubject = createTestSubject();
		String data="{ firstName=\"xyz\", lastName=\"xyz\", userId=\"12\", email=\"demo.z@ymail.com\",role=\"123\", lastlogintime=20180201233412 }";

		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);
		User assertuser = new User("xyz","xyz","12","demo.z@ymail.com","123",null);

		assertThat(response.isLeft()).isTrue();
		assertThat(response.left().value()).isEqualTo(assertuser);
	}

    @Test
    public void testconvertJsonToObjectUsingObjectMapper() {

        AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
        ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
        when(auditingmanager.auditEvent(any())).thenReturn("OK");

        User user = new User();
        String data =
                "[{\"constraints\":[{\"equal\":\"value\"}]},"
                        + "{\"constraints\":[{\"greaterOrEqual\":5}]},"
                        + "{\"constraints\":[{\"lessThan\":7}]},"
                        + "{\"constraints\":[{\"lessOrEqual\":9}]},"
                        + "{\"constraints\":[{\"inRange\":[\"5\", \"10\"]}]},"
                        + "{\"constraints\":[{\"validValues\":[\"abc\", \"def\", \"hij\"]}]},"
                        + "{\"constraints\":[{\"length\":11}]},"
                        + "{\"constraints\":[{\"minLength\":13}]},"
                        + "{\"constraints\":[{\"maxLength\":15}]}"
                +"]";


        Either<ComponentInstanceProperty[], ResponseFormat> response = compUtils.convertJsonToObjectUsingObjectMapper(data, user,
                ComponentInstanceProperty[].class, null, ComponentTypeEnum.RESOURCE_INSTANCE);

        assertThat(response.isLeft()).isTrue();
        ComponentInstanceProperty[] properties = response.left().value();
        assertEquals(9, properties.length);
        assertEquals("value", ((EqualConstraint)properties[0].getConstraints().iterator().next()).getEqual());
        assertEquals("5", ((GreaterOrEqualConstraint)properties[1].getConstraints().iterator().next()).getGreaterOrEqual());
        assertEquals("7", ((LessThanConstraint)properties[2].getConstraints().iterator().next()).getLessThan());
        assertEquals("9", ((LessOrEqualConstraint)properties[3].getConstraints().iterator().next()).getLessOrEqual());
        assertEquals("5", ((InRangeConstraint)properties[4].getConstraints().iterator().next()).getRangeMinValue());
        assertEquals("10", ((InRangeConstraint)properties[4].getConstraints().iterator().next()).getRangeMaxValue());
        assertEquals(3, ((ValidValuesConstraint)properties[5].getConstraints().iterator().next()).getValidValues().size());
        assertEquals(11, ((LengthConstraint)properties[6].getConstraints().iterator().next()).getLength());
        assertEquals(13, ((MinLengthConstraint)properties[7].getConstraints().iterator().next()).getMinLength());
        assertEquals(15, ((MaxLengthConstraint)properties[8].getConstraints().iterator().next()).getMaxLength());
    }

	@Test
	public void testconvertJsonToObject_NllData() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		User user = new User();
		String data=null;
		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);

		assertThat(response.isRight()).isTrue();
	}

	@Test
	public void testconvertJsonToObjectInvalidData() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		User user = new User();

		String data="{ User [ firstName=\"xyz\", lastName=\"xyz\", userId=\"12\", email=\"demo.z@ymail.com\",role=\"123\", lastlogintime=20180201233412 }";

		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);

		assertThat(response.isRight()).isTrue();
	}

	@Test
	public void testconvertToStorageOperationStatus() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		assertThat(compUtils.convertToStorageOperationStatus(CassandraOperationStatus.OK)).isEqualTo(StorageOperationStatus.OK);
		assertThat(compUtils.convertToStorageOperationStatus(CassandraOperationStatus.NOT_FOUND)).isEqualTo(StorageOperationStatus.NOT_FOUND);
		assertThat(compUtils.convertToStorageOperationStatus(CassandraOperationStatus.GENERAL_ERROR)).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
		assertThat(compUtils.convertToStorageOperationStatus(CassandraOperationStatus.CLUSTER_NOT_CONNECTED)).isEqualTo(StorageOperationStatus.CONNECTION_FAILURE);
		assertThat(compUtils.convertToStorageOperationStatus(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED)).isEqualTo(StorageOperationStatus.CONNECTION_FAILURE);
	}

	@Test
	public void testgetResponseFormatByDataType() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		DataTypeDefinition dataType = new DataTypeDefinition();
		dataType.setName("demo");
		List<String> properties;
		ResponseFormat result = compUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_ALREADY_EXIST, dataType,  null);

		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);
	}

	@Test
	public void testGetResponseFormatByPolicyType_POLICY_TYPE_ALREADY_EXIST() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		policyType.setType("Demo");
		ResponseFormat result = compUtils.getResponseFormatByPolicyType(ActionStatus.POLICY_TYPE_ALREADY_EXIST, policyType);

		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);
	}

	@Test
	public void testGetResponseFormatByPolicyType_PolicyID_NULL() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		ResponseFormat result = compUtils.getResponseFormatByPolicyType(ActionStatus.POLICY_TYPE_ALREADY_EXIST,  null);

		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);
	}

	@Test
	public void testGetResponseFormatByGroupType_GROUP_MEMBER_EMPTY() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		GroupTypeDefinition groupType = new GroupTypeDefinition();
		groupType.setType("Demo");

		ResponseFormat result = compUtils.getResponseFormatByGroupType(ActionStatus.GROUP_MEMBER_EMPTY, groupType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = compUtils.getResponseFormatByGroupType(ActionStatus.GROUP_TYPE_ALREADY_EXIST, groupType);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);
	}

	@Test
	public void testConvertFromStorageResponseForDataType_ALL() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.OK)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.CONNECTION_FAILURE)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.GRAPH_IS_LOCK)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.BAD_REQUEST)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.ENTITY_ALREADY_EXISTS)).isEqualTo(ActionStatus.DATA_TYPE_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.SCHEMA_VIOLATION)).isEqualTo(ActionStatus.DATA_TYPE_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY)).isEqualTo(ActionStatus.DATA_TYPE_CANNOT_BE_UPDATED_BAD_REQUEST);
	}

	@Test
	public void testConvertFromStorageResponseForGroupType_ALL() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.OK)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.CONNECTION_FAILURE)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.GRAPH_IS_LOCK)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.BAD_REQUEST)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.ENTITY_ALREADY_EXISTS)).isEqualTo(ActionStatus.GROUP_TYPE_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.SCHEMA_VIOLATION)).isEqualTo(ActionStatus.GROUP_TYPE_ALREADY_EXIST);
	}

	@Test
	public void testConvertFromStorageResponseForConsumer_ALL() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.OK)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.CONNECTION_FAILURE)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.GRAPH_IS_LOCK)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.BAD_REQUEST)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.ENTITY_ALREADY_EXISTS)).isEqualTo(ActionStatus.CONSUMER_ALREADY_EXISTS);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.SCHEMA_VIOLATION)).isEqualTo(ActionStatus.CONSUMER_ALREADY_EXISTS);
		assertThat(compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.NOT_FOUND)).isEqualTo(ActionStatus.ECOMP_USER_NOT_FOUND);
	}

	@Test
	public void testGetResponseFormatAdditionalProperty_ALL() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		NodeTypeEnum nodeType = null;
		AdditionalInformationEnum labelOrValue = null;

		ResponseFormat result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EXCEEDS_LIMIT, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_MAX_NUMBER_REACHED, additionalInfoParameterInfo, NodeTypeEnum.Group,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_KEY_NOT_ALLOWED_CHARACTERS, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_VALUE_NOT_ALLOWED_CHARACTERS, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(400);

		result = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_NOT_FOUND, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

	}

	@Test
	public void testConvertFromResultStatusEnum_ALL() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.OK, null)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_DEFAULT_VALUE, null)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_TYPE, null)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_VALUE, null)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_NAME, null)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromResultStatusEnum(ResultStatusEnum.MISSING_ENTRY_SCHEMA_TYPE, null)).isEqualTo(ActionStatus.INVALID_PROPERTY);
	}

	@Test
	public void testconvertFromStorageResponseForAdditionalInformation() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		assertThat(compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.OK)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.ENTITY_ALREADY_EXISTS)).isEqualTo(ActionStatus.COMPONENT_NAME_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.INVALID_ID)).isEqualTo(ActionStatus.ADDITIONAL_INFORMATION_NOT_FOUND);
	}

	@Test
	public void testgetResponseFormatByComponent() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Component component = new Resource();
		ResponseFormat result = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = compUtils.getResponseFormatByComponent(ActionStatus.RESOURCE_NOT_FOUND, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(404);

		result = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(409);

		result = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_IN_USE, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(403);

		result = compUtils.getResponseFormatByComponent(ActionStatus.SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(403);

		result = compUtils.getResponseFormatByComponent(ActionStatus.ACCEPTED, component, ComponentTypeEnum.RESOURCE);
		assertThat(result).isInstanceOf(ResponseFormat.class);
		assertThat(result.getStatus()).isEqualTo(202);
	}


	@Test
	public void testConvertFromStorageResponseForResourceInstanceProperty_ALL() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.OK)).isEqualTo(ActionStatus.OK);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.INVALID_ID)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.GRAPH_IS_LOCK)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.BAD_REQUEST)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.MATCH_NOT_FOUND)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.SCHEMA_VIOLATION)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.NOT_FOUND)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND);
	}

	@Test
	public void testConvertFromStorageResponseForResourceInstance_ALL() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.ARTIFACT_NOT_FOUND, false)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.INVALID_ID, false)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.INVALID_PROPERTY, false)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.GRAPH_IS_LOCK, false)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.BAD_REQUEST, false)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.MATCH_NOT_FOUND, false)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.SCHEMA_VIOLATION, false)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.NOT_FOUND, true)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.NOT_FOUND, false)).isEqualTo(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND);
	}

	@Test
	public void testConvertFromStorageResponse_ALL() {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.CONNECTION_FAILURE, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.GRAPH_IS_LOCK, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.GENERAL_ERROR);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.ENTITY_ALREADY_EXISTS, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.COMPONENT_NAME_ALREADY_EXIST);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.PARENT_RESOURCE_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.MULTIPLE_PARENT_RESOURCE_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.MULTIPLE_PARENT_RESOURCE_FOUND);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.COMPONENT_IN_USE);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.DISTRIBUTION_ENVIRONMENT_INVALID);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_TYPE, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.INVALID_CONTENT);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.CSAR_NOT_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.CSAR_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.MATCH_NOT_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.COMPONENT_SUB_CATEGORY_NOT_FOUND_FOR_CATEGORY);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.CATEGORY_NOT_FOUND, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_PROPERTY, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.INVALID_PROPERTY);
		assertThat(compUtils.convertFromStorageResponse(StorageOperationStatus.COMPONENT_IS_ARCHIVED, ComponentTypeEnum.RESOURCE)).isEqualTo(ActionStatus.COMPONENT_IS_ARCHIVED);
	}
}
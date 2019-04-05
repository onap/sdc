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
import junit.framework.Assert;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.List;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ComponentsUtilsTest {

	private ComponentsUtils createTestSubject() {
		return new ComponentsUtils(new AuditingManager(new AuditingDao(), new AuditCassandraDao(), new TestConfigurationProvider()));
	}

	@Before
	public void init(){
	String appConfigDir = "src/test/resources/config/catalog-be";
    ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
	ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
	ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
	}

	@Test
	public void testGetAuditingManager() throws Exception {
		ComponentsUtils testSubject;
		AuditingManager result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingManager();
	}

	
	@Test
	public void testGetResponseFormat() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String[] params = new String[] { "" };
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormat(actionStatus, params);
	}

	
	@Test
	public void testGetResponseFormat_1() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageStatus = null;
		String[] params = new String[] { "" };
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormat(storageStatus, params);
	}

	
	@Test
	public void testConvertToResponseFormatOrNotFoundErrorToEmptyList() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageOperationStatus = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		Either<List<T>, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToResponseFormatOrNotFoundErrorToEmptyList(storageOperationStatus);
	}

	
	@Test
	public void testGetResponseFormatByResource() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		ResponseFormat result;
		Resource resource = null;
		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByResource(actionStatus, resource);
		resource = new Resource();
		result = testSubject.getResponseFormatByResource(actionStatus, resource);
		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, resource);
		result = testSubject.getResponseFormatByResource(ActionStatus.RESOURCE_NOT_FOUND, resource);
		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, resource);
		result = testSubject.getResponseFormatByResource(ActionStatus.COMPONENT_IN_USE, resource);
	}

	
	@Test
	public void testGetResponseFormatByResource_1() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String resourceName = "";
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		resourceName = null;
		result = testSubject.getResponseFormatByResource(actionStatus, resourceName);

		// test 2
		testSubject = createTestSubject();
		resourceName = "";
		result = testSubject.getResponseFormatByResource(actionStatus, resourceName);
		result = testSubject.getResponseFormatByResource(ActionStatus.RESOURCE_NOT_FOUND, resourceName);
	}

	
	@Test
	public void testGetResponseFormatByCapabilityType() throws Exception {
		ComponentsUtils testSubject;
		CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, null);
		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, capabilityType);
		result = testSubject.getResponseFormatByCapabilityType(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED, capabilityType);
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
		obj = new Object();
		result = testSubject.getResponseFormatByElement(actionStatus, obj);
		result = testSubject.getResponseFormatByElement(ActionStatus.MISSING_CAPABILITY_TYPE, obj);
	}

	
	@Test
	public void testGetResponseFormatByUser() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		User user = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		user = null;
		result = testSubject.getResponseFormatByUser(actionStatus, user);
		user = new User();
		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_USER_ID, user);
		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_EMAIL_ADDRESS, user);
		result = testSubject.getResponseFormatByUser(ActionStatus.INVALID_ROLE, user);
		result = testSubject.getResponseFormatByUser(ActionStatus.USER_NOT_FOUND, user);
		result = testSubject.getResponseFormatByUser(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED, user);
	}

	
	@Test
	public void testGetResponseFormatByUserId() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String userId = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByUserId(actionStatus, userId);
	}

	
	@Test
	public void testGetResponseFormatByDE() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String serviceId = "";
		String envName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByDE(actionStatus, serviceId);
	}

	
	@Test
	public void testGetResponseFormatByArtifactId() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String artifactId = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByArtifactId(actionStatus, artifactId);
		result = testSubject.getResponseFormatByArtifactId(ActionStatus.RESOURCE_NOT_FOUND, artifactId);
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
	public void testConvertFromStorageResponse() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = null;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponse(storageResponse);
	}

	
	@Test
	public void testConvertFromStorageResponse_1() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = null;
		ComponentTypeEnum type = null;
		ActionStatus result;

		// test 1
		testSubject = createTestSubject();
		storageResponse = null;
		result = testSubject.convertFromStorageResponse(storageResponse, type);
	}

	
	@Test
	public void testConvertFromToscaError() throws Exception {
		ComponentsUtils testSubject;
		ToscaError toscaError = null;
		ActionStatus result;

		// test 1
		testSubject = createTestSubject();
		toscaError = null;
		result = testSubject.convertFromToscaError(toscaError);
	}

	
	@Test
	public void testConvertFromStorageResponseForCapabilityType() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForCapabilityType(storageResponse);
	}

	
	@Test
	public void testConvertFromStorageResponseForLifecycleType() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForLifecycleType(storageResponse);
	}

	
	@Test
	public void testConvertFromStorageResponseForResourceInstance() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		boolean isRelation = false;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForResourceInstance(storageResponse, isRelation);
	}

	
	@Test
	public void testGetResponseFormatForResourceInstance() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String serviceName = "";
		String resourceInstanceName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatForResourceInstance(actionStatus, serviceName, resourceInstanceName);
	}

	
	@Test
	public void testGetResponseFormatForResourceInstanceProperty() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		String resourceInstanceName = "";
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatForResourceInstanceProperty(actionStatus, resourceInstanceName);
	}

	
	@Test
	public void testConvertFromStorageResponseForResourceInstanceProperty() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForResourceInstanceProperty(storageResponse);
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
	public void testValidateStringNotEmpty() throws Exception {
		ComponentsUtils testSubject;
		String value = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateStringNotEmpty(value);
	}

	
	@Test
	public void testConvertFromStorageResponseForAdditionalInformation() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForAdditionalInformation(storageResponse);
	}

	
	@Test
	public void testConvertFromResultStatusEnum() throws Exception {
		ComponentsUtils testSubject;
		ResultStatusEnum resultStatus = ResultStatusEnum.ELEMENT_NOT_FOUND;
		JsonPresentationFields elementType = null;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromResultStatusEnum(resultStatus, elementType);
	}

	
	@Test
	public void testGetResponseFormatAdditionalProperty() throws Exception {
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

		// test 2
		testSubject = createTestSubject();
		labelOrValue = null;
		result = testSubject.getResponseFormatAdditionalProperty(actionStatus, additionalInfoParameterInfo, nodeType,
				labelOrValue);
	}

	
	@Test
	public void testGetResponseFormatAdditionalProperty_1() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatAdditionalProperty(actionStatus);
	}

	
	@Test
	public void testConvertFromStorageResponseForConsumer() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForConsumer(storageResponse);
	}

	
	@Test
	public void testConvertFromStorageResponseForGroupType() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForGroupType(storageResponse);
	}

	
	@Test
	public void testConvertFromStorageResponseForDataType() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForDataType(storageResponse);
	}

	
	@Test
	public void testGetResponseFormatByGroupType() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		GroupTypeDefinition groupType = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		groupType = null;
		result = testSubject.getResponseFormatByGroupType(actionStatus, groupType);
	}

	
	@Test
	public void testGetResponseFormatByPolicyType() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByPolicyType(actionStatus, policyType);
	}

	
	@Test
	public void testGetResponseFormatByDataType() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.AAI_ARTIFACT_GENERATION_FAILED;
		DataTypeDefinition dataType = null;
		List<String> properties = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		dataType = null;
		result = testSubject.getResponseFormatByDataType(actionStatus, dataType, properties);
	}

	@Test
	public void testconvertJsonToObject() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		User user = new User();
		ComponentsUtils testSubject = createTestSubject();
		String data="{ firstName=\"xyz\", lastName=\"xyz\", userId=\"12\", email=\"demo.z@ymail.com\",role=\"123\", lastlogintime=20180201233412 }";

		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);
		User assertuser = new User("xyz","xyz","12","demo.z@ymail.com","123",null);

		Assert.assertEquals(assertuser,response.left().value());
	}

	@Test
	public void testconvertJsonToObject_NllData() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		User user = new User();
		String data=null;
		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);

		Assert.assertEquals(true,response.isRight());
	}

	@Test
	public void testconvertJsonToObjectInvalidData() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		User user = new User();

		String data="{ User [ firstName=\"xyz\", lastName=\"xyz\", userId=\"12\", email=\"demo.z@ymail.com\",role=\"123\", lastlogintime=20180201233412 }";

		Either<User,ResponseFormat> response=compUtils.convertJsonToObject(data,user,User.class,AuditingActionEnum.ADD_USER);


		Assert.assertEquals(true,response.isRight());
	}

	@Test
	public void testconvertToStorageOperationStatus() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(StorageOperationStatus.OK,compUtils.convertToStorageOperationStatus(CassandraOperationStatus.OK));
		Assert.assertEquals(StorageOperationStatus.NOT_FOUND,compUtils.convertToStorageOperationStatus(CassandraOperationStatus.NOT_FOUND));
		Assert.assertEquals(StorageOperationStatus.GENERAL_ERROR,compUtils.convertToStorageOperationStatus(CassandraOperationStatus.GENERAL_ERROR));
		Assert.assertEquals(StorageOperationStatus.CONNECTION_FAILURE,compUtils.convertToStorageOperationStatus(CassandraOperationStatus.CLUSTER_NOT_CONNECTED));
		Assert.assertEquals(StorageOperationStatus.CONNECTION_FAILURE,compUtils.convertToStorageOperationStatus(CassandraOperationStatus.KEYSPACE_NOT_CONNECTED));
	}

	@Test
	public void testgetResponseFormatByDataType() {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		DataTypeDefinition dataType = new DataTypeDefinition();
		dataType.setName("demo");
		List<String> properties;
		ResponseFormat responseFormat = compUtils.getResponseFormatByDataType(ActionStatus.DATA_TYPE_ALREADY_EXIST, dataType,  null);
		Assert.assertNotNull(responseFormat);
		Assert.assertEquals((Integer) 409,responseFormat.getStatus());
	}

	@Test
	public void testGetResponseFormatByPolicyType_POLICY_TYPE_ALREADY_EXIST() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		policyType.setType("Demo");
		ResponseFormat responseFormat = compUtils.getResponseFormatByPolicyType(ActionStatus.POLICY_TYPE_ALREADY_EXIST, policyType);
		Assert.assertNotNull(responseFormat);
		Assert.assertEquals((Integer) 409,responseFormat.getStatus());
	}

	@Test
	public void testGetResponseFormatByPolicyType_PolicyID_NULL() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		ResponseFormat responseFormat = compUtils.getResponseFormatByPolicyType(ActionStatus.POLICY_TYPE_ALREADY_EXIST,  null);
		Assert.assertNotNull(responseFormat);
		Assert.assertEquals((Integer) 409,responseFormat.getStatus());
	}

	@Test
	public void testGetResponseFormatByGroupType_GROUP_MEMBER_EMPTY() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		GroupTypeDefinition groupType = new GroupTypeDefinition();
		groupType.setType("Demo");
		ResponseFormat responseFormat = compUtils.getResponseFormatByGroupType(ActionStatus.GROUP_MEMBER_EMPTY, groupType);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByGroupType(ActionStatus.GROUP_TYPE_ALREADY_EXIST, groupType);
		Assert.assertNotNull(responseFormat);
	}

	@Test
	public void testConvertFromStorageResponseForDataType_ALL() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.OK));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.CONNECTION_FAILURE));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.GRAPH_IS_LOCK));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.BAD_REQUEST));
		Assert.assertEquals(ActionStatus.DATA_TYPE_ALREADY_EXIST,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.ENTITY_ALREADY_EXISTS));
		Assert.assertEquals(ActionStatus.DATA_TYPE_ALREADY_EXIST,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.SCHEMA_VIOLATION));
		Assert.assertEquals(ActionStatus.DATA_TYPE_CANNOT_BE_UPDATED_BAD_REQUEST,compUtils.convertFromStorageResponseForDataType(StorageOperationStatus.CANNOT_UPDATE_EXISTING_ENTITY));

	}

	@Test
	public void testConvertFromStorageResponseForGroupType_ALL() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.OK));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.CONNECTION_FAILURE));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.GRAPH_IS_LOCK));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.BAD_REQUEST));
		Assert.assertEquals(ActionStatus.GROUP_TYPE_ALREADY_EXIST,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.ENTITY_ALREADY_EXISTS));
		Assert.assertEquals(ActionStatus.GROUP_TYPE_ALREADY_EXIST,compUtils.convertFromStorageResponseForGroupType(StorageOperationStatus.SCHEMA_VIOLATION));
	}

	@Test
	public void testConvertFromStorageResponseForConsumer_ALL() throws Exception {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.OK));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.CONNECTION_FAILURE));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.GRAPH_IS_LOCK));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.BAD_REQUEST));
		Assert.assertEquals(ActionStatus.CONSUMER_ALREADY_EXISTS,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.ENTITY_ALREADY_EXISTS));
		Assert.assertEquals(ActionStatus.CONSUMER_ALREADY_EXISTS,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.SCHEMA_VIOLATION));
		Assert.assertEquals(ActionStatus.ECOMP_USER_NOT_FOUND,compUtils.convertFromStorageResponseForConsumer(StorageOperationStatus.NOT_FOUND));
	}

	@Test
	public void testGetResponseFormatAdditionalProperty_ALL() throws Exception {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");

		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		NodeTypeEnum nodeType = null;
		AdditionalInformationEnum labelOrValue = null;

		ResponseFormat responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EXCEEDS_LIMIT, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_MAX_NUMBER_REACHED, additionalInfoParameterInfo, NodeTypeEnum.Group,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_EMPTY_STRING_NOT_ALLOWED, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_KEY_NOT_ALLOWED_CHARACTERS, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_VALUE_NOT_ALLOWED_CHARACTERS, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatAdditionalProperty(ActionStatus.ADDITIONAL_INFORMATION_NOT_FOUND, additionalInfoParameterInfo, nodeType,
				labelOrValue);
		Assert.assertNotNull(responseFormat);

	}

	@Test
	public void testConvertFromResultStatusEnum_ALL() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromResultStatusEnum(ResultStatusEnum.OK, null));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_DEFAULT_VALUE, null));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_TYPE, null));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_VALUE, null));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromResultStatusEnum(ResultStatusEnum.INVALID_PROPERTY_NAME, null));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromResultStatusEnum(ResultStatusEnum.MISSING_ENTRY_SCHEMA_TYPE, null));
	}

	@Test
	public void testconvertFromStorageResponseForAdditionalInformation() throws Exception{
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.OK));
		Assert.assertEquals(ActionStatus.COMPONENT_NAME_ALREADY_EXIST,compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.ENTITY_ALREADY_EXISTS));
		Assert.assertEquals(ActionStatus.ADDITIONAL_INFORMATION_NOT_FOUND,compUtils.convertFromStorageResponseForAdditionalInformation(StorageOperationStatus.INVALID_ID));
	}

	@Test
	public void testgetResponseFormatByComponent() throws Exception{
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Component component = new Resource();
		ResponseFormat responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_VERSION_ALREADY_EXIST, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.RESOURCE_NOT_FOUND, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_NAME_ALREADY_EXIST, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.COMPONENT_IN_USE, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.SERVICE_DEPLOYMENT_ARTIFACT_NOT_FOUND, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
		responseFormat = compUtils.getResponseFormatByComponent(ActionStatus.ACCEPTED, component, ComponentTypeEnum.RESOURCE);
		Assert.assertNotNull(responseFormat);
	}


	@Test
	public void testConvertFromStorageResponseForResourceInstanceProperty_ALL() throws Exception {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(ActionStatus.OK,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.OK));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.INVALID_ID));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.GRAPH_IS_LOCK));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.BAD_REQUEST));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.MATCH_NOT_FOUND));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.SCHEMA_VIOLATION));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND,compUtils.convertFromStorageResponseForResourceInstanceProperty(StorageOperationStatus.NOT_FOUND));
	}

	@Test
	public void testConvertFromStorageResponseForResourceInstance_ALL() throws Exception {
		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.ARTIFACT_NOT_FOUND, false));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_BAD_REQUEST,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.INVALID_ID, false));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.INVALID_PROPERTY, false));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.GRAPH_IS_LOCK, false));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.BAD_REQUEST, false));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.MATCH_NOT_FOUND, false));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_ALREADY_EXIST,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.SCHEMA_VIOLATION, false));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.NOT_FOUND, true));
		Assert.assertEquals(ActionStatus.RESOURCE_INSTANCE_NOT_FOUND,compUtils.convertFromStorageResponseForResourceInstance(StorageOperationStatus.NOT_FOUND, false));
	}

	@Test
	public void testConvertFromStorageResponse_ALL() throws Exception {

		AuditingManager auditingmanager = Mockito.mock(AuditingManager.class);
		ComponentsUtils compUtils = new ComponentsUtils(auditingmanager);
		when(auditingmanager.auditEvent(any())).thenReturn("OK");
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponse(StorageOperationStatus.CONNECTION_FAILURE, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.GENERAL_ERROR,compUtils.convertFromStorageResponse(StorageOperationStatus.GRAPH_IS_LOCK, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponse(StorageOperationStatus.BAD_REQUEST, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.COMPONENT_NAME_ALREADY_EXIST,compUtils.convertFromStorageResponse(StorageOperationStatus.ENTITY_ALREADY_EXISTS, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.PARENT_RESOURCE_NOT_FOUND,compUtils.convertFromStorageResponse(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.MULTIPLE_PARENT_RESOURCE_FOUND,compUtils.convertFromStorageResponse(StorageOperationStatus.MULTIPLE_PARENT_RESOURCE_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.COMPONENT_IN_USE,compUtils.convertFromStorageResponse(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE,compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_FOUND,compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_INVALID,compUtils.convertFromStorageResponse(StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_TYPE, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.INVALID_CONTENT,compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_VALUE, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.CSAR_NOT_FOUND,compUtils.convertFromStorageResponse(StorageOperationStatus.CSAR_NOT_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.PROPERTY_NAME_ALREADY_EXISTS,compUtils.convertFromStorageResponse(StorageOperationStatus.PROPERTY_NAME_ALREADY_EXISTS, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.COMPONENT_SUB_CATEGORY_NOT_FOUND_FOR_CATEGORY,compUtils.convertFromStorageResponse(StorageOperationStatus.MATCH_NOT_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.COMPONENT_CATEGORY_NOT_FOUND,compUtils.convertFromStorageResponse(StorageOperationStatus.CATEGORY_NOT_FOUND, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.INVALID_PROPERTY,compUtils.convertFromStorageResponse(StorageOperationStatus.INVALID_PROPERTY, ComponentTypeEnum.RESOURCE));
		Assert.assertEquals(ActionStatus.COMPONENT_IS_ARCHIVED,compUtils.convertFromStorageResponse(StorageOperationStatus.COMPONENT_IS_ARCHIVED, ComponentTypeEnum.RESOURCE));
	}
}
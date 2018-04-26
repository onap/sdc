package org.openecomp.sdc.be.impl;

import java.util.EnumMap;
import java.util.List;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;
import mockit.Deencapsulation;

public class ComponentsUtilsTest {

	private ComponentsUtils createTestSubject() {
		return new ComponentsUtils(new AuditingManager(new AuditingDao(), new AuditCassandraDao()));
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
	public void testConvertJsonToObjectUsingObjectMapper() throws Exception {
		ComponentsUtils testSubject;
		String data = "";
		User user = null;
		Class<T> clazz = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum typeEnum = null;
		Either<T, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertJsonToObjectUsingObjectMapper(data, user, clazz, actionEnum, typeEnum);
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
		Resource resource = new Resource();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByResource(actionStatus, resource);
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
	}

	
	@Test
	public void testGetResponseFormatByCapabilityType() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.getResponseFormatByCapabilityType(actionStatus, capabilityType);
	}

	
	@Test
	public void testGetResponseFormatByElement() throws Exception {
		ComponentsUtils testSubject;
		ActionStatus actionStatus = ActionStatus.ACCEPTED;
		T obj = null;
		ResponseFormat result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.getResponseFormatByElement(actionStatus, obj);
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
		result = testSubject.getResponseFormatByDE(actionStatus, serviceId, envName);
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
	}

	
	@Test
	public void testGetInvalidContentErrorAndAudit() throws Exception {
		ComponentsUtils testSubject;
		User user = null;
		String resourceName = "";
		AuditingActionEnum actionEnum = null;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvalidContentErrorAndAudit(user, resourceName, actionEnum);
	}

	@Test
	public void testGetInvalidContentErrorAndAuditComponent() throws Exception {
		ComponentsUtils testSubject;
		User user = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum typeEnum = null;
		ResponseFormat result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvalidContentErrorAndAuditComponent(user, actionEnum, typeEnum);
	}


	
	@Test
	public void testAuditResource_1() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
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
		ResourceAuditData prevResFields = null;
		String currentArtifactUuid = "";
		String artifactData = "";

		// test 1
		testSubject = createTestSubject();
		actionEnum = null;
		testSubject.auditResource(responseFormat, modifier, resource, resourceName, actionEnum, prevResFields,
				currentArtifactUuid, artifactData);
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
		ResourceAuditData prevComponent = null;
		String comment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, type, prevComponent, comment);
	}

	
	@Test
	public void testAuditComponentAdmin() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponentAdmin(responseFormat, modifier, component, actionEnum, type);
	}

	
	@Test
	public void testAuditComponentAdmin_1() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		ResourceAuditData prevComponent = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponentAdmin(responseFormat, modifier, component, actionEnum, type, prevComponent);
	}

	
	@Test
	public void testAuditComponent_1() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		ResourceAuditData prevComponent = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, type, prevComponent);
	}

	
	@Test
	public void testAuditComponent_2() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		AuditingActionEnum actionEnum = null;
		String compName = "";
		ComponentTypeEnum type = null;
		String comment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, actionEnum, compName, type, comment);
	}

	
	@Test
	public void testAuditComponent_3() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		ResourceAuditData prevComponent = null;
		ResourceAuditData currComponent = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, type, prevComponent, currComponent);
	}

	
	@Test
	public void testAuditComponent_4() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		ResourceAuditData prevComponent = null;
		ResourceAuditData currComponent = null;
		String compName = "";
		String comment = "";
		String artifactData = "";
		String did = "";

		// test 1
		testSubject = createTestSubject();
		actionEnum = null;
		testSubject.auditComponent(responseFormat, modifier, component, actionEnum, type, prevComponent, currComponent,
				compName, comment, artifactData, did);
	}

	
	@Test
	public void testAuditDistributionEngine() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
		String environmentName = "";
		String topicName = "";
		String role = "";
		String apiKey = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditDistributionEngine(actionEnum, environmentName, topicName, role, apiKey, status);
	}

	

	
	@Test
	public void testAuditDistributionNotification() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
		String serviceUUID = "";
		String resourceName = "";
		String resourceType = "";
		String currVersion = "";
		String modifierUid = "";
		String modifierName = "";
		String environmentName = "";
		String currState = "";
		String topicName = "";
		String distributionId = "";
		String description = "";
		String status = "";
		String workloadContext = "";
		String tenant = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditDistributionNotification(actionEnum, serviceUUID, resourceName, resourceType, currVersion,
				modifierUid, modifierName, environmentName, currState, topicName, distributionId, description, status,
				workloadContext, tenant);
	}

	
	@Test
	public void testAuditDistributionStatusNotification() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
		String distributionId = "";
		String consumerId = "";
		String topicName = "";
		String resourceUrl = "";
		String statusTime = "";
		String status = "";
		String errorReason = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditDistributionStatusNotification(actionEnum, distributionId, consumerId, topicName, resourceUrl,
				statusTime, status, errorReason);
	}

	
	@Test
	public void testAuditGetUebCluster() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
		String consumerId = "";
		String statusTime = "";
		String status = "";
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditGetUebCluster(actionEnum, consumerId, statusTime, status, description);
	}

	
	@Test
	public void testAuditMissingInstanceId() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_CATEGORY;
		String status = "";
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditMissingInstanceId(actionEnum, status, description);
	}

	
	@Test
	public void testAuditTopicACLKeys() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ADD_USER;
		String envName = "";
		String topicName = "";
		String role = "";
		String apiPublicKey = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditTopicACLKeys(actionEnum, envName, topicName, role, apiPublicKey, status);
	}

	
	@Test
	public void testAuditRegisterOrUnRegisterEvent() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
		String consumerId = "";
		String apiPublicKey = "";
		String envName = "";
		String status = "";
		String statusDesc = "";
		String notifTopicName = "";
		String statusTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.auditRegisterOrUnRegisterEvent(actionEnum, consumerId, apiPublicKey, envName, status, statusDesc,
				notifTopicName, statusTopicName);
	}

	
	@Test
	public void testAuditServiceDistributionDeployed() throws Exception {
		ComponentsUtils testSubject;
		AuditingActionEnum actionEnum = AuditingActionEnum.ACTIVATE_SERVICE_BY_API;
		String serviceName = "";
		String serviceVersion = "";
		String serviceUUID = "";
		String distributionId = "";
		String status = "";
		String desc = "";
		User modifier = new User();

		// default test
		testSubject = createTestSubject();
		testSubject.auditServiceDistributionDeployed(actionEnum, serviceName, serviceVersion, serviceUUID,
				distributionId, status, desc, modifier);
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
}
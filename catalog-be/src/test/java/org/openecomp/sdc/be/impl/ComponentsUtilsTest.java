package org.openecomp.sdc.be.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.tinkerpop.gremlin.structure.T;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.auditing.api.IAuditingManager;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.graph.datatype.AdditionalInformationEnum;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
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
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import com.fasterxml.jackson.databind.JsonDeserializer;

import fj.data.Either;

public class ComponentsUtilsTest {

	private ComponentsUtils createTestSubject() {
		return new ComponentsUtils();
	}

	
	@Test
	public void testInit() throws Exception {
		ComponentsUtils testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.Init();
	}

	
	@Test
	public void testGetAuditingManager() throws Exception {
		ComponentsUtils testSubject;
		IAuditingManager result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAuditingManager();
	}

	
	@Test
	public void testSetAuditingManager() throws Exception {
		ComponentsUtils testSubject;
		IAuditingManager auditingManager = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAuditingManager(auditingManager);
	}
	
	
	@Test
	public void testAuditResource() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Resource resource = null;
		String prevState = "";
		String prevVersion = "";
		AuditingActionEnum actionEnum = null;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = null;

		// test 1
		testSubject = createTestSubject();
		actionEnum = null;
		testSubject.auditResource(responseFormat, modifier, resource, prevState, prevVersion, actionEnum,
				additionalParams);
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
		Assert.assertEquals(ActionStatus.GENERAL_ERROR, result);
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
		Assert.assertEquals(ActionStatus.GENERAL_ERROR, result);
	}

	
	@Test
	public void testConvertFromStorageResponseForCapabilityType() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
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
	public void testConvertFromStorageResponseForResourceInstanceProperty() throws Exception {
		ComponentsUtils testSubject;
		StorageOperationStatus storageResponse = StorageOperationStatus.ARTIFACT_NOT_FOUND;
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertFromStorageResponseForResourceInstanceProperty(storageResponse);
	}

	
	@Test
	public void testAuditComponentAdmin() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		String prevState = "";
		String prevVersion = "";
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;

		// default test
		testSubject = createTestSubject();
		testSubject.auditComponentAdmin(responseFormat, modifier, component, prevState, prevVersion, actionEnum, type);
	}

	
	@Test
	public void testAuditComponent() throws Exception {
		ComponentsUtils testSubject;
		ResponseFormat responseFormat = null;
		User modifier = null;
		Component component = null;
		String prevState = "";
		String prevVersion = "";
		AuditingActionEnum actionEnum = null;
		ComponentTypeEnum type = null;
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = null;

		// test 1
		testSubject = createTestSubject();
		actionEnum = null;
		testSubject.auditComponent(responseFormat, modifier, component, prevState, prevVersion, actionEnum, type,
				additionalParams);
	}

	
	@Test
	public void testValidateStringNotEmpty_1() throws Exception {
		ComponentsUtils testSubject;
		String value = "";
		Boolean result;

		// test 1
		testSubject = createTestSubject();
		value = null;
		result = testSubject.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);

		// test 2
		testSubject = createTestSubject();
		value = "";
		result = testSubject.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);
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


	
}
package org.openecomp.sdc.be.components.validation;

import javax.annotation.Generated;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.common.util.ValidationUtils;

import fj.data.Either;
import mockit.Deencapsulation;

public class ComponentValidationsTest {

	@InjectMocks
	ComponentValidations testSubject;

	@Mock
	ToscaOperationFacade toscaOperationFacadeMock;

	@Mock
	GraphLockOperation graphLockOperationMock;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateComponentInstanceExist() throws Exception {
		Component component = new Resource();
		String instanceId = "";
		boolean result;

		// default test
		result = ComponentValidations.validateComponentInstanceExist(component, instanceId);
	}

	@Test
	public void testGetNormalizedName() throws Exception {
		ToscaDataDefinition toscaDataDefinition = new AdditionalInfoParameterDataDefinition();
		toscaDataDefinition.setToscaPresentationValue(JsonPresentationFields.NAME, "mock");
		String result;

		// default test
		result = ComponentValidations.getNormalizedName(toscaDataDefinition);
	}

	@Test
	public void testValidateNameIsUniqueInComponent() throws Exception {
		String currentName = "";
		String newName = "";
		String newName2 = "mock";
		Component component = new Resource();
		boolean result;

		// default test
		result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName, component);
		result = ComponentValidations.validateNameIsUniqueInComponent(currentName, newName2, component);
	}

	@Test(expected=ComponentException.class)
	public void testValidateComponentIsCheckedOutByUserAndLockIt() throws Exception {
		String componentId = "";
		String userId = "";
		Component result;
		
		Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(new Resource()));
		
		// default test
		result = testSubject.validateComponentIsCheckedOutByUserAndLockIt(ComponentTypeEnum.RESOURCE, componentId,
				userId);
	}

	@Test
	public void testGetComponent() throws Exception {
		String componentId = "mock";
		ComponentTypeEnum componentType = null;
		Component result;

		Mockito.when(toscaOperationFacadeMock.getToscaElement(Mockito.anyString(), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(new Resource()));
		
		// default test
		result = Deencapsulation.invoke(testSubject, "getComponent", componentId, ComponentTypeEnum.RESOURCE);
	}

	@Test(expected = StorageException.class)
	public void testLockComponent() throws Exception {
		Component component = new Resource();

		// default test
		Deencapsulation.invoke(testSubject, "lockComponent", component);
	}

	@Test(expected = StorageException.class)
	public void testOnToscaOperationError() throws Exception {
		Component result;

		// default test
		result = Deencapsulation.invoke(testSubject, "onToscaOperationError",
				StorageOperationStatus.ARTIFACT_NOT_FOUND);
	}
}
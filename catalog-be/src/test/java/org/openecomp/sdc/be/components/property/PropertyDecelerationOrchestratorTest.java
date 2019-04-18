package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PropertyDecelerationOrchestratorTest {

	@InjectMocks
	PropertyDeclarationOrchestrator testSubject;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	List<PropertyDeclarator> propertyDeceleratorsMock;

	@Mock
	private ComponentInstanceInputPropertyDeclarator componentInstanceInputPropertyDecelerator;
	@Mock
	private ComponentInstancePropertyDeclarator componentInstancePropertyDecelerator;
	@Mock
	private PolicyPropertyDeclarator policyPropertyDecelerator;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeclarePropertiesToInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Either<List<InputDefinition>, StorageOperationStatus> result;

		// default test
		result = testSubject.declarePropertiesToInputs(component, componentInstInputsMap);
	}

	@Test
	public void testDeclarePropertiesToListInputs() throws Exception {
		Component component = new Resource();
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		InputDefinition input = new InputDefinition();
		Either<InputDefinition, StorageOperationStatus> result;

		// default test
		result = testSubject.declarePropertiesToListInput(component, componentInstInputsMap, input);
	}

	@Test
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		Mockito.when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		Mockito.when(mockIter.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, inputToDelete);
	}

	@Test
	public void testUnDeclarePropertiesAsListInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDeclarator> mockIter = Mockito.mock(Iterator.class);
		Mockito.when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		Mockito.when(mockIter.hasNext()).thenReturn(false);

		// default test
		result = testSubject.unDeclarePropertiesAsListInputs(component, inputToDelete);
	}

	@Test
	public void testGetPropOwnerId() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		String result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropOwnerId", componentInstInputsMap);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetPropertyDecelerator() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithInputsMap() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIProperties() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceProperties.put("mock", value);
		componentInstInputsMap.setComponentInstanceProperties(componentInstanceProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIPolicy() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		policyProperties.put("mock", value);
		componentInstInputsMap.setPolicyProperties(policyProperties);
		PropertyDeclarator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDeclarator", componentInstInputsMap);
	}
}

package org.openecomp.sdc.be.components.property;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
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
import org.openecomp.sdc.test.utils.TestUtilsSdc;
import org.slf4j.LoggerFactory;

import fj.data.Either;
import mockit.Deencapsulation;

public class PropertyDecelerationOrchestratorTest {

	@InjectMocks
	PropertyDecelerationOrchestrator testSubject;

	@Mock
	List<PropertyDecelerator> propertyDeceleratorsMock;
	
	@Mock
	private ComponentInstanceInputPropertyDecelerator componentInstanceInputPropertyDecelerator;
	@Mock
	private ComponentInstancePropertyDecelerator componentInstancePropertyDecelerator;
	@Mock
	private PolicyPropertyDecelerator policyPropertyDecelerator;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
		
		TestUtilsSdc.setFinalStatic(testSubject.getClass(), "log", LoggerFactory.getLogger(testSubject.getClass()));
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
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = new Resource();
		InputDefinition inputToDelete = new InputDefinition();
		StorageOperationStatus result;

		Iterator<PropertyDecelerator> mockIter = Mockito.mock(Iterator.class);
		Mockito.when(propertyDeceleratorsMock.iterator()).thenReturn(mockIter);
		Mockito.when(mockIter.hasNext()).thenReturn(false);
		
		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, inputToDelete);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetPropertyDecelerator() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		PropertyDecelerator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDecelerator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithInputsMap() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceInputsMap = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceInputsMap.put("mock", value);
		componentInstInputsMap.setComponentInstanceInputsMap(componentInstanceInputsMap);
		PropertyDecelerator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDecelerator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIProperties() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> componentInstanceProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		componentInstanceProperties.put("mock", value);
		componentInstInputsMap.setComponentInstancePropInput(componentInstanceProperties);
		PropertyDecelerator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDecelerator", componentInstInputsMap);
	}

	@Test
	public void testGetPropertyDeceleratorWithCIPolicy() throws Exception {
		ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
		Map<String, List<ComponentInstancePropInput>> policyProperties = new HashMap<>();
		List<ComponentInstancePropInput> value = new LinkedList<>();
		policyProperties.put("mock", value);
		componentInstInputsMap.setPolicyProperties(policyProperties);
		PropertyDecelerator result;

		// default test
		result = Deencapsulation.invoke(testSubject, "getPropertyDecelerator", componentInstInputsMap);
	}
}
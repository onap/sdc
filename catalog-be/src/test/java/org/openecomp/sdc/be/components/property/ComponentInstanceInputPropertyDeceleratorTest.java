package org.openecomp.sdc.be.components.property;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;
import mockit.Deencapsulation;

public class ComponentInstanceInputPropertyDeceleratorTest {

	@InjectMocks
	ComponentInstanceInputPropertyDecelerator testSubject;

	@Mock
	private ComponentInstanceBusinessLogic componentInstanceBusinessLogicMock;

	@Mock
	private ToscaOperationFacade toscaOperationFacadeMock;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCreateDeclaredProperty() throws Exception {
		PropertyDataDefinition prop = new PropertyDataDefinition();
		ComponentInstanceInput result;

		// default test
		result = Deencapsulation.invoke(testSubject, "createDeclaredProperty", prop);
	}

	@Test
	public void testUpdatePropertiesValues() throws Exception {
		;
		Component component = new Resource();
		String cmptInstanceId = "";
		List<ComponentInstanceInput> properties = new LinkedList<>();
		Either<?, StorageOperationStatus> result;

		Component containerComponent;
		Map<String, List<ComponentInstanceInput>> instProperties;
		Mockito.when(toscaOperationFacadeMock.addComponentInstanceInputsToComponent(Mockito.any(Component.class),
				Mockito.any(Map.class))).thenReturn(Either.left(new Resource()));

		// default test
		result = Deencapsulation.invoke(testSubject, "updatePropertiesValues", component, cmptInstanceId, properties);
	}

	@Test
	public void testResolvePropertiesOwner() throws Exception {
		Component component = new Resource();
		String propertiesOwnerId = "mock";
		Optional<ComponentInstance> result;

		// default test
		result = Deencapsulation.invoke(testSubject, "resolvePropertiesOwner", component, propertiesOwnerId);
	}

	@Test
	public void testAddPropertiesListToInput() throws Exception {
		ComponentInstanceInput declaredProp = null;
		PropertyDataDefinition originalProp = null;
		InputDefinition input = null;

		// default test
		Deencapsulation.invoke(testSubject, "addPropertiesListToInput", new ComponentInstanceInput(),
				new PropertyDataDefinition(), new InputDefinition());
	}

	@Test
	public void testUnDeclarePropertiesAsInputs() throws Exception {
		Component component = null;
		InputDefinition input = new InputDefinition();
		StorageOperationStatus result;

		Mockito.when(componentInstanceBusinessLogicMock
				.getComponentInstanceInputsByInputId(Mockito.any(Component.class), Mockito.anyString()))
				.thenReturn(new LinkedList());
		// default test
		result = testSubject.unDeclarePropertiesAsInputs(component, input);
	}
}
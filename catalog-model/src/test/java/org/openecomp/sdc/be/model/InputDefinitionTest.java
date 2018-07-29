package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;


public class InputDefinitionTest {

	private InputDefinition createTestSubject() {
		return new InputDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new InputDefinition(new PropertyDefinition());
		new InputDefinition(new PropertyDataDefinition());
	}
	
	@Test
	public void testGetInputs() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceInput> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceInput> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}
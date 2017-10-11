package org.openecomp.sdc.be.model;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class InputDefinitionTest {

	private InputDefinition createTestSubject() {
		return new InputDefinition();
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
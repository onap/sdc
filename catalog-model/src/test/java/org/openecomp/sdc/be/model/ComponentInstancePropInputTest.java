package org.openecomp.sdc.be.model;

import org.junit.Test;


public class ComponentInstancePropInputTest {

	private ComponentInstancePropInput createTestSubject() {
		return new ComponentInstancePropInput();
	}

	
	@Test
	public void testGetPropertiesName() throws Exception {
		ComponentInstancePropInput testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertiesName();
	}

	
	@Test
	public void testSetPropertiesName() throws Exception {
		ComponentInstancePropInput testSubject;
		String propertiesName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertiesName(propertiesName);
	}

	
	@Test
	public void testGetInput() throws Exception {
		ComponentInstancePropInput testSubject;
		PropertyDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInput();
	}

	
	@Test
	public void testSetInput() throws Exception {
		ComponentInstancePropInput testSubject;
		PropertyDefinition input = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInput(input);
	}

	
	@Test
	public void testGetParsedPropNames() throws Exception {
		ComponentInstancePropInput testSubject;
		String[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParsedPropNames();
	}
}
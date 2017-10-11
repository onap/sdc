package org.openecomp.sdc.be.datatypes.elements;

import javax.annotation.Generated;

import org.junit.Test;


public class InputsValueDataDefinitionTest {

	private InputsValueDataDefinition createTestSubject() {
		return new InputsValueDataDefinition("", "");
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		InputsValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		InputsValueDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetName() throws Exception {
		InputsValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		InputsValueDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetValue() throws Exception {
		InputsValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	
	@Test
	public void testSetValue() throws Exception {
		InputsValueDataDefinition testSubject;
		String value = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}
}
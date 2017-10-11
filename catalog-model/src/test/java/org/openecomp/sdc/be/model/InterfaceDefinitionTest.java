package org.openecomp.sdc.be.model;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class InterfaceDefinitionTest {

	private InterfaceDefinition createTestSubject() {
		return new InterfaceDefinition();
	}

	@Test
	public void testIsDefinition() throws Exception {
		InterfaceDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}

	@Test
	public void testSetDefinition() throws Exception {
		InterfaceDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}

	@Test
	public void testGetOperationsMap() throws Exception {
		InterfaceDefinition testSubject;
		Map<String, Operation> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationsMap();
	}



	@Test
	public void testToString() throws Exception {
		InterfaceDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
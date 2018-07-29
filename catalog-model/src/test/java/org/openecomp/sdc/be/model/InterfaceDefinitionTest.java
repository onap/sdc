package org.openecomp.sdc.be.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class InterfaceDefinitionTest {

	private InterfaceDefinition createTestSubject() {
		return new InterfaceDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new InterfaceDefinition(new InterfaceDataDefinition());
		new InterfaceDefinition("mock", "mock", new HashMap<>());
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
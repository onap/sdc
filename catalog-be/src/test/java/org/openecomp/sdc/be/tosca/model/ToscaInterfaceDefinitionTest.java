package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;

import java.util.Map;

public class ToscaInterfaceDefinitionTest {

	private ToscaInterfaceDefinition createTestSubject() {
		return new ToscaInterfaceDefinition();
	}

	@Test
	public void testGetType() throws Exception {
		ToscaInterfaceDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		ToscaInterfaceDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetOperations() throws Exception {
		ToscaInterfaceDefinition testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperations();
	}

	@Test
	public void testSetOperations() throws Exception {
		ToscaInterfaceDefinition testSubject;
		Map<String, Object> toscaOperations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperations(toscaOperations);
	}
}
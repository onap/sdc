package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class OperationInputDefinitionTest {

	private OperationInputDefinition createTestSubject() {
		return new OperationInputDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		OperationInputDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new OperationInputDefinition("", "", true, "");
		new OperationInputDefinition("stam", testSubject, null, null);
	}

	@Test
	public void testGetLabel() throws Exception {
		OperationInputDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	@Test
	public void testSetLabel() throws Exception {
		OperationInputDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(name);
	}
}
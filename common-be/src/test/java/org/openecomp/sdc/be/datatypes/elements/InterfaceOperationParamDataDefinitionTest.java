package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class InterfaceOperationParamDataDefinitionTest {

	private InterfaceOperationParamDataDefinition createTestSubject() {
		return new InterfaceOperationParamDataDefinition();
	}

	@Test
	public void testOverloadConstructor() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new InterfaceOperationParamDataDefinition(testSubject);
		new InterfaceOperationParamDataDefinition("", "");
	}
	
	@Test
	public void testGetParamName() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParamName();
	}

	@Test
	public void testSetParamName() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String paramName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParamName(paramName);
	}

	@Test
	public void testGetParamId() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParamId();
	}

	@Test
	public void testSetParamId() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String paramId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParamId(paramId);
	}
}
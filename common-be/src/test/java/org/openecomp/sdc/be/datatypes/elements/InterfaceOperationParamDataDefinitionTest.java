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
		new InterfaceOperationParamDataDefinition("", "",true, "");
	}
	
	@Test
	public void testGetParamName() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetParamName() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String paramName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(paramName);
	}

	@Test
	public void testGetParamId() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testSetParamId() throws Exception {
		InterfaceOperationParamDataDefinition testSubject;
		String paramId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProperty(paramId);
	}
}
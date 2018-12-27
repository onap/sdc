package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class InterfaceOperationDataDefinitionTest {

	private InterfaceOperationDataDefinition createTestSubject() {
		return new InterfaceOperationDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		InterfaceOperationDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		new InterfaceOperationDataDefinition(testSubject);
	}
	
	@Test
	public void testGetInputParams() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		ListDataDefinition<InterfaceOperationParamDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputParams();
	}

	@Test
	public void testSetInputParams() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		ListDataDefinition<InterfaceOperationParamDataDefinition> inputParams = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputParams(inputParams);
	}

	@Test
	public void testGetOutputParams() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		ListDataDefinition<InterfaceOperationParamDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOutputParams();
	}

	@Test
	public void testSetOutputParams() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		ListDataDefinition<InterfaceOperationParamDataDefinition> outputParams = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOutputParams(outputParams);
	}

	@Test
	public void testGetUniqueId() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String uid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uid);
	}

	@Test
	public void testGetDescription() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetOperationType() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationType();
	}

	@Test
	public void testSetOperationType() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String operationType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOperationType(operationType);
	}

	@Test
	public void testGetToscaResourceName() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	@Test
	public void testSetToscaResourceName() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}

	@Test
	public void testGetArtifactUUID() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUUID();
	}

	@Test
	public void testSetArtifactUUID() throws Exception {
		InterfaceOperationDataDefinition testSubject;
		String artifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactUUID(artifactUUID);
	}
}
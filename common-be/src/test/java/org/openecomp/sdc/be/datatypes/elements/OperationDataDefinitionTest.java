package org.openecomp.sdc.be.datatypes.elements;

import java.util.Map;

import org.junit.Test;


public class OperationDataDefinitionTest {

	private OperationDataDefinition createTestSubject() {
		return new OperationDataDefinition();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		OperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		OperationDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetCreationDate() throws Exception {
		OperationDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationDate();
	}

	
	@Test
	public void testSetCreationDate() throws Exception {
		OperationDataDefinition testSubject;
		Long creationDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationDate(creationDate);
	}

	
	@Test
	public void testGetLastUpdateDate() throws Exception {
		OperationDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdateDate();
	}

	
	@Test
	public void testSetLastUpdateDate() throws Exception {
		OperationDataDefinition testSubject;
		Long lastUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdateDate(lastUpdateDate);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		OperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		OperationDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetImplementation() throws Exception {
		OperationDataDefinition testSubject;
		ArtifactDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImplementation();
	}

	
	@Test
	public void testSetImplementation() throws Exception {
		OperationDataDefinition testSubject;
		ArtifactDataDefinition implementation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImplementation(implementation);
	}

	
	@Test
	public void testGetInputs() throws Exception {
		OperationDataDefinition testSubject;
		Map<String, PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		OperationDataDefinition testSubject;
		Map<String, PropertyDataDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}
}
package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.List;

public class CapabilityTypeDataDefinitionTest {

	private CapabilityTypeDataDefinition createTestSubject() {
		return new CapabilityTypeDataDefinition();
	}
	
	@Test
	public void testCopyConstructor() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new CapabilityTypeDataDefinition(testSubject);
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetDescription() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetType() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetValidSourceTypes() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	@Test
	public void testSetValidSourceTypes() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	@Test
	public void testGetVersion() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetCreationTime() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	@Test
	public void testSetCreationTime() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	@Test
	public void testGetModificationTime() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	@Test
	public void testSetModificationTime() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	@Test
	public void testToString() throws Exception {
		CapabilityTypeDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
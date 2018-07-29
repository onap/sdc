package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.List;

public class RelationshipInstDataDefinitionTest {

	private RelationshipInstDataDefinition createTestSubject() {
		return new RelationshipInstDataDefinition();
	}
	
	@Test
	public void testCopyConstructor() throws Exception {
		RelationshipInstDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		new RelationshipInstDataDefinition(testSubject);
	}

	@Test
	public void testSetRequirement() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String requirement = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirement(requirement);
	}

	@Test
	public void testGetRequirement() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirement();
	}

	@Test
	public void testSetCapability() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	@Test
	public void testGetCapability() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	@Test
	public void testSetToId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object toId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToId(toId);
	}

	@Test
	public void testSetFromId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object fromId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFromId(fromId);
	}

	@Test
	public void testGetToId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToId();
	}

	@Test
	public void testGetFromId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromId();
	}

	@Test
	public void testSetRequirementId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object requirementId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementId(requirementId);
	}

	@Test
	public void testSetCapabilityId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object capabilityId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityId(capabilityId);
	}

	@Test
	public void testGetRequirementId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementId();
	}

	@Test
	public void testGetCapabilityId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityId();
	}

	@Test
	public void testSetRequirementOwnerId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object requirementOwnerId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementOwnerId(requirementOwnerId);
	}

	@Test
	public void testGetRequirementOwnerId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementOwnerId();
	}

	@Test
	public void testSetCapabilityOwnerId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Object capabilityOwnerId = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityOwnerId(capabilityOwnerId);
	}

	@Test
	public void testGetCapabilityOwnerId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityOwnerId();
	}

	@Test
	public void testGetUniqueId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetDescription() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetType() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetValidSourceTypes() throws Exception {
		RelationshipInstDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	@Test
	public void testSetValidSourceTypes() throws Exception {
		RelationshipInstDataDefinition testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	@Test
	public void testGetVersion() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetCreationTime() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	@Test
	public void testSetCreationTime() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	@Test
	public void testGetModificationTime() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	@Test
	public void testSetModificationTime() throws Exception {
		RelationshipInstDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	@Test
	public void testToString() throws Exception {
		RelationshipInstDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
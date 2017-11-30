package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import org.junit.Test;


public class RelationshipInstDataTest {

	private RelationshipInstData createTestSubject() {
		return new RelationshipInstData();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		RelationshipInstData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		RelationshipInstData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		RelationshipInstData testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		RelationshipInstData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		RelationshipInstData testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		RelationshipInstData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetType() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		RelationshipInstData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetCapabilityOwnerId() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityOwnerId();
	}

	
	@Test
	public void testSetCapabilityOwnerId() throws Exception {
		RelationshipInstData testSubject;
		String capabilityOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityOwnerId(capabilityOwnerId);
	}

	
	@Test
	public void testGetRequirementOwnerId() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementOwnerId();
	}

	
	@Test
	public void testSetRequirementOwnerId() throws Exception {
		RelationshipInstData testSubject;
		String requirementOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementOwnerId(requirementOwnerId);
	}

	
	@Test
	public void testGetCapabiltyId() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabiltyId();
	}

	
	@Test
	public void testSetCapabiltyId() throws Exception {
		RelationshipInstData testSubject;
		String capabiltyId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabiltyId(capabiltyId);
	}

	
	@Test
	public void testGetRequirementId() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementId();
	}

	
	@Test
	public void testSetRequirementId() throws Exception {
		RelationshipInstData testSubject;
		String requirementId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementId(requirementId);
	}

	
	@Test
	public void testToString() throws Exception {
		RelationshipInstData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
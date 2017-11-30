package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;


public class RequirementAndRelationshipPairTest {

	private RequirementAndRelationshipPair createTestSubject() {
		return new RequirementAndRelationshipPair();
	}

	
	@Test
	public void testGetRequirement() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirement();
	}

	
	@Test
	public void testSetRequirement() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String requirement = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirement(requirement);
	}

	
	@Test
	public void testGetCapabilityOwnerId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityOwnerId();
	}

	
	@Test
	public void testSetCapabilityOwnerId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String capabilityOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityOwnerId(capabilityOwnerId);
	}

	
	@Test
	public void testGetRequirementOwnerId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementOwnerId();
	}

	
	@Test
	public void testSetRequirementOwnerId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String requirementOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementOwnerId(requirementOwnerId);
	}

	
	@Test
	public void testGetRelationship() throws Exception {
		RequirementAndRelationshipPair testSubject;
		RelationshipImpl result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	
	@Test
	public void testSetRelationships() throws Exception {
		RequirementAndRelationshipPair testSubject;
		RelationshipImpl relationship = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationships(relationship);
	}

	
	@Test
	public void testGetCapability() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	
	@Test
	public void testSetCapability() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	
	@Test
	public void testGetCapabilityUid() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityUid();
	}

	
	@Test
	public void testSetCapabilityUid() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String capabilityUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityUid(capabilityUid);
	}

	
	@Test
	public void testGetRequirementUid() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementUid();
	}

	
	@Test
	public void testSetRequirementUid() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String requirementUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementUid(requirementUid);
	}

	
	@Test
	public void testGetId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	
	@Test
	public void testSetId() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	
	@Test
	public void testToString() throws Exception {
		RequirementAndRelationshipPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testEqualsTo() throws Exception {
		RequirementAndRelationshipPair testSubject;
		RelationshipInstDataDefinition savedRelation = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		savedRelation = null;
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
	}
}
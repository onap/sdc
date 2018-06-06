package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;

public class RelationshipInfoTest {

	private RelationshipInfo createTestSubject() {
		return new RelationshipInfo();
	}
	
	@Test
	public void testCtor() throws Exception {
		new RelationshipInfo("mock", new RelationshipImpl());
		new RelationshipInfo("mock", new RelationshipImpl(), "mock");
	}
	
	@Test
	public void testGetRequirement() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirement();
	}

	@Test
	public void testSetRequirement() throws Exception {
		RelationshipInfo testSubject;
		String requirement = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirement(requirement);
	}

	@Test
	public void testGetCapabilityOwnerId() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityOwnerId();
	}

	@Test
	public void testSetCapabilityOwnerId() throws Exception {
		RelationshipInfo testSubject;
		String capabilityOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityOwnerId(capabilityOwnerId);
	}

	@Test
	public void testGetRequirementOwnerId() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementOwnerId();
	}

	@Test
	public void testSetRequirementOwnerId() throws Exception {
		RelationshipInfo testSubject;
		String requirementOwnerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementOwnerId(requirementOwnerId);
	}

	@Test
	public void testGetRelationship() throws Exception {
		RelationshipInfo testSubject;
		RelationshipImpl result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	@Test
	public void testSetRelationships() throws Exception {
		RelationshipInfo testSubject;
		RelationshipImpl relationship = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationships(relationship);
	}

	@Test
	public void testGetCapability() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	@Test
	public void testSetCapability() throws Exception {
		RelationshipInfo testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	@Test
	public void testGetCapabilityUid() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityUid();
	}

	@Test
	public void testSetCapabilityUid() throws Exception {
		RelationshipInfo testSubject;
		String capabilityUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityUid(capabilityUid);
	}

	@Test
	public void testGetRequirementUid() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementUid();
	}

	@Test
	public void testSetRequirementUid() throws Exception {
		RelationshipInfo testSubject;
		String requirementUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementUid(requirementUid);
	}

	@Test
	public void testGetId() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	@Test
	public void testSetId() throws Exception {
		RelationshipInfo testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	@Test
	public void testToString() throws Exception {
		RelationshipInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testEqualsTo() throws Exception {
		RelationshipInfo testSubject;
		RelationshipInstDataDefinition savedRelation = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		savedRelation = new RelationshipInstDataDefinition();
		RelationshipImpl relationship = new RelationshipImpl();
		savedRelation.setType("mock");
		testSubject.setRelationships(relationship);
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		relationship.setType("mock");
		savedRelation.setCapabilityOwnerId("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		testSubject.setCapabilityOwnerId("mock");
		savedRelation.setRequirementOwnerId("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		savedRelation.setRequirementOwnerId("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		testSubject.setRequirementOwnerId("mock");
		savedRelation.setRequirementId("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		testSubject.setRequirementUid("mock");
		savedRelation.setCapabilityId("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		testSubject.setCapabilityUid("mock");
		savedRelation.setRequirement("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(false, result);
		testSubject.setRequirement("mock");
		result = testSubject.equalsTo(savedRelation);
		Assert.assertEquals(true, result);
	}
}
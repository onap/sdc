package org.openecomp.sdc.be.model;

import org.junit.Test;


public class RequirementInstanceTest {

	private RequirementInstance createTestSubject() {
		return new RequirementInstance();
	}

	
	@Test
	public void testGetNode() throws Exception {
		RequirementInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		RequirementInstance testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetRelationship() throws Exception {
		RequirementInstance testSubject;
		RelationshipImpl result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	
	@Test
	public void testSetRelationship() throws Exception {
		RequirementInstance testSubject;
		RelationshipImpl relationship = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationship(relationship);
	}

	
	@Test
	public void testToString() throws Exception {
		RequirementInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
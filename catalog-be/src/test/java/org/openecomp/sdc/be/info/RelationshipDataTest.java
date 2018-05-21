package org.openecomp.sdc.be.info;

import org.junit.Test;

public class RelationshipDataTest {

	private RelationshipData createTestSubject() {
		return new RelationshipData();
	}

	@Test
	public void testSetRelationshipkey() throws Exception {
		RelationshipData testSubject;
		String relationshipKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationshipkey(relationshipKey);
	}

	@Test
	public void testGetRelationshipKey() throws Exception {
		RelationshipData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationshipKey();
	}

	@Test
	public void testSetRelationshipValue() throws Exception {
		RelationshipData testSubject;
		String relationshipValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationshipValue(relationshipValue);
	}

	@Test
	public void testGetRelationshipValue() throws Exception {
		RelationshipData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationshipValue();
	}
}
package org.openecomp.sdc.be.info;

import org.junit.Test;

import java.util.List;

public class RelationshipTest {

	private Relationship createTestSubject() {
		return new Relationship();
	}

	@Test
	public void testGetRelatedTo() throws Exception {
		Relationship testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelatedTo();
	}

	@Test
	public void testSetRelatedTo() throws Exception {
		Relationship testSubject;
		String relatedTo = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelatedTo(relatedTo);
	}

	@Test
	public void testGetRelatedLink() throws Exception {
		Relationship testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelatedLink();
	}

	@Test
	public void testSetRelatedLink() throws Exception {
		Relationship testSubject;
		String relatedLink = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelatedLink(relatedLink);
	}

	@Test
	public void testGetRelationshipData() throws Exception {
		Relationship testSubject;
		List<RelationshipData> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationshipData();
	}

	@Test
	public void testSetRelationshipData() throws Exception {
		Relationship testSubject;
		List<RelationshipData> relationshipData = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationshipData(relationshipData);
	}

	@Test
	public void testGetRelationshipLabel() throws Exception {
		Relationship testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationshipLabel();
	}

	@Test
	public void testSetRelationshipLabel() throws Exception {
		Relationship testSubject;
		String relationshipLabel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationshipLabel(relationshipLabel);
	}

	@Test
	public void testGetRelatedToProperty() throws Exception {
		Relationship testSubject;
		List<RelatedToProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelatedToProperty();
	}

	@Test
	public void testSetRelatedToProperty() throws Exception {
		Relationship testSubject;
		List<RelatedToProperty> relatedToProperty = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelatedToProperty(relatedToProperty);
	}
}
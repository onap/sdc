package org.openecomp.sdc.be.components.merge.instance;

import java.util.List;

import org.junit.Test;

public class VfRelationsMergeInfoTest {

	private VfRelationsMergeInfo createTestSubject() {
		return new VfRelationsMergeInfo(null, null);
	}

	@Test
	public void testGetFromRelationsInfo() throws Exception {
		VfRelationsMergeInfo testSubject;
		List<RelationMergeInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromRelationsInfo();
	}

	@Test
	public void testSetFromRelationsInfo() throws Exception {
		VfRelationsMergeInfo testSubject;
		List<RelationMergeInfo> fromRelationsInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFromRelationsInfo(fromRelationsInfo);
	}

	@Test
	public void testGetToRelationsInfo() throws Exception {
		VfRelationsMergeInfo testSubject;
		List<RelationMergeInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToRelationsInfo();
	}

	@Test
	public void testSetToRelationsInfo() throws Exception {
		VfRelationsMergeInfo testSubject;
		List<RelationMergeInfo> toRelationsInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setToRelationsInfo(toRelationsInfo);
	}
}
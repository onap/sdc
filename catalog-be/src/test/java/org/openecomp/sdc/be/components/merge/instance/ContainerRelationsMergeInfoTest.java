package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Test;

import java.util.List;

public class ContainerRelationsMergeInfoTest {

	private ContainerRelationsMergeInfo createTestSubject() {
		return new ContainerRelationsMergeInfo(null, null);
	}

	@Test
	public void testGetFromRelationsInfo() throws Exception {
		ContainerRelationsMergeInfo testSubject;
		List<RelationMergeInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromRelationsInfo();
	}


	@Test
	public void testGetToRelationsInfo() throws Exception {
		ContainerRelationsMergeInfo testSubject;
		List<RelationMergeInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToRelationsInfo();
	}

}
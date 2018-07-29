package org.openecomp.sdc.be.components.merge.instance;

import org.junit.Test;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

public class RelationMergeInfoTest {

	private RelationMergeInfo createTestSubject() {
		return new RelationMergeInfo("", "", "", new RequirementCapabilityRelDef());
	}

	@Test
	public void testGetCapReqType() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapReqType();
	}


	@Test
	public void testGetRelDef() throws Exception {
		RelationMergeInfo testSubject;
		RequirementCapabilityRelDef result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelDef();
	}



	@Test
	public void testGetCapReqName() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapReqName();
	}

}
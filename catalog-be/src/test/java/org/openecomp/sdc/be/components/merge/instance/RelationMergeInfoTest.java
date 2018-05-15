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
	public void testSetCapReqType() throws Exception {
		RelationMergeInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapReqType(type);
	}

	@Test
	public void testGetVfcInstanceName() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfcInstanceName();
	}

	@Test
	public void testSetVfcInstanceName() throws Exception {
		RelationMergeInfo testSubject;
		String vfcInstanceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfcInstanceName(vfcInstanceName);
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
	public void testSetRelDef() throws Exception {
		RelationMergeInfo testSubject;
		RequirementCapabilityRelDef relDef = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelDef(relDef);
	}

	@Test
	public void testGetCapReqName() throws Exception {
		RelationMergeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapReqName();
	}

	@Test
	public void testSetCapReqName() throws Exception {
		RelationMergeInfo testSubject;
		String capReqName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapReqName(capReqName);
	}
}
package org.openecomp.sdc.be.model;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TargetCapabilityRelDefTest {

	private TargetCapabilityRelDef createTestSubject() {
		return new TargetCapabilityRelDef();
	}

	@Test
	public void testCtor() throws Exception {
		new TargetCapabilityRelDef("mock", new LinkedList<>());
	}
	
	@Test
	public void testGetToNode() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToNode();
	}

	@Test
	public void testSetToNode() throws Exception {
		TargetCapabilityRelDef testSubject;
		String toNode = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToNode(toNode);
	}

	@Test
	public void testGetRelationships() throws Exception {
		TargetCapabilityRelDef testSubject;
		List<CapabilityRequirementRelationship> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationships();
	}

	@Test
	public void testResolveSingleRelationship() throws Exception {
		TargetCapabilityRelDef testSubject;
		CapabilityRequirementRelationship result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.resolveSingleRelationship();
	}

	@Test
	public void testGetUid() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUid();
	}

	@Test
	public void testSetUid() throws Exception {
		TargetCapabilityRelDef testSubject;
		String uid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUid(uid);
	}

	@Test
	public void testSetRelationships() throws Exception {
		TargetCapabilityRelDef testSubject;
		List<CapabilityRequirementRelationship> relationships = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationships(relationships);
	}

	@Test
	public void testToString() throws Exception {
		TargetCapabilityRelDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
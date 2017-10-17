package org.openecomp.sdc.be.model;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class RequirementImplDefTest {

	private RequirementImplDef createTestSubject() {
		return new RequirementImplDef();
	}

	
	@Test
	public void testGetNodeId() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNodeId();
	}

	
	@Test
	public void testSetNodeId() throws Exception {
		RequirementImplDef testSubject;
		String nodeId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNodeId(nodeId);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		RequirementImplDef testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetRequirementProperties() throws Exception {
		RequirementImplDef testSubject;
		Map<String, CapabiltyInstance> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementProperties();
	}

	
	@Test
	public void testSetRequirementProperties() throws Exception {
		RequirementImplDef testSubject;
		Map<String, CapabiltyInstance> requirementProperties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementProperties(requirementProperties);
	}

	
	@Test
	public void testGetPoint() throws Exception {
		RequirementImplDef testSubject;
		Point result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPoint();
	}

	
	@Test
	public void testSetPoint() throws Exception {
		RequirementImplDef testSubject;
		Point point = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPoint(point);
	}

	
	@Test
	public void testToString() throws Exception {
		RequirementImplDef testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
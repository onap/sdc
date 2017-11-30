package org.openecomp.sdc.be.model;

import java.util.List;
import java.util.Map;

import org.junit.Test;


public class CapReqDefTest {

	private CapReqDef createTestSubject() {
		return new CapReqDef();
	}

	
	@Test
	public void testGetCapabilities() throws Exception {
		CapReqDef testSubject;
		Map<String, List<CapabilityDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		CapReqDef testSubject;
		Map<String, List<RequirementDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		CapReqDef testSubject;
		Map<String, List<CapabilityDefinition>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		CapReqDef testSubject;
		Map<String, List<RequirementDefinition>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}
}
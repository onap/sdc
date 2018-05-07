package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

public class PolicyTargetTypeTest {

	private PolicyTargetType createTestSubject() {
		return PolicyTargetType.COMPONENT_INSTANCES;
	}

	@Test
	public void testGetName() throws Exception {
		PolicyTargetType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testGetByNameIgnoreCase() throws Exception {
		String name = "";
		PolicyTargetType result;

		// default test
		result = PolicyTargetType.getByNameIgnoreCase(name);
	}
}
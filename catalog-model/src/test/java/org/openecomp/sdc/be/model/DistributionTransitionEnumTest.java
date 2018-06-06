package org.openecomp.sdc.be.model;

import org.junit.Test;

public class DistributionTransitionEnumTest {

	private DistributionTransitionEnum createTestSubject() {
		return DistributionTransitionEnum.APPROVE;
	}

	@Test
	public void testGetDisplayName() throws Exception {
		DistributionTransitionEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	@Test
	public void testGetFromDisplayName() throws Exception {
		String name = "";
		DistributionTransitionEnum result;

		// default test
		result = DistributionTransitionEnum.getFromDisplayName(name);
		result = DistributionTransitionEnum.getFromDisplayName(DistributionTransitionEnum.APPROVE.getDisplayName());
	}

	@Test
	public void testValuesAsString() throws Exception {
		String result;

		// default test
		result = DistributionTransitionEnum.valuesAsString();
	}
}
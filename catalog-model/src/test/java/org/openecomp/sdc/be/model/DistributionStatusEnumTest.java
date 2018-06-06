package org.openecomp.sdc.be.model;

import org.junit.Test;

public class DistributionStatusEnumTest {

	private DistributionStatusEnum createTestSubject() {
		return DistributionStatusEnum.DISTRIBUTED;
	}

	@Test
	public void testGetValue() throws Exception {
		DistributionStatusEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testFindState() throws Exception {
		String state = "";
		DistributionStatusEnum result;

		// default test
		result = DistributionStatusEnum.findState(state);
		DistributionStatusEnum.findState(DistributionStatusEnum.DISTRIBUTED.getValue());
	}
}
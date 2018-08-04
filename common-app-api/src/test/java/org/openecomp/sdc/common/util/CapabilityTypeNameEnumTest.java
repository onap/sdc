package org.openecomp.sdc.common.util;

import org.junit.Test;


public class CapabilityTypeNameEnumTest {

	private CapabilityTypeNameEnum createTestSubject() {
		return CapabilityTypeNameEnum.ATTACHMENT;
	}

	
	@Test
	public void testGetCapabilityName() throws Exception {
		CapabilityTypeNameEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityName();
	}
}
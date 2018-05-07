package org.openecomp.sdc.fe.utils;

import org.junit.Test;

public class BeProtocolTest {

	private BeProtocol createTestSubject() {
		return BeProtocol.HTTP;
	}

	@Test
	public void testGetProtocolName() throws Exception {
		BeProtocol testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProtocolName();
	}
}
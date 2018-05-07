package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class ConnectionPointEnumTest {

	private ConnectionPointEnum createTestSubject() {
		return ConnectionPointEnum.CAPABILITY;
	}

	@Test
	public void testToString() throws Exception {
		ConnectionPointEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetConnectionPointEnum() throws Exception {
		String data = "";
		ConnectionPointEnum result;

		// default test
		result = ConnectionPointEnum.getConnectionPointEnum(data);
	}
}
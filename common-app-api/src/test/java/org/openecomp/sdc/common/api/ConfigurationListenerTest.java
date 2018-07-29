package org.openecomp.sdc.common.api;

import org.junit.Test;

public class ConfigurationListenerTest {

	private ConfigurationListener createTestSubject() {
		return new ConfigurationListener(null, null);
	}

	@Test
	public void testGetType() throws Exception {
		ConfigurationListener testSubject;
		Class<? extends BasicConfiguration> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		ConfigurationListener testSubject;
		Class<? extends BasicConfiguration> type = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetCallBack() throws Exception {
		ConfigurationListener testSubject;
		FileChangeCallback result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCallBack();
	}

	@Test
	public void testSetCallBack() throws Exception {
		ConfigurationListener testSubject;
		FileChangeCallback callBack = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCallBack(callBack);
	}
}
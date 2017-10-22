package org.openecomp.sdc.common.datastructure;

import javax.annotation.Generated;

import org.junit.Test;


public class MonitoringFieldsKeysEnumTest {

	private MonitoringFieldsKeysEnum createTestSubject() {
		return MonitoringFieldsKeysEnum.MONITORING_APP_ID;
	}

	
	@Test
	public void testGetValueClass() throws Exception {
		MonitoringFieldsKeysEnum testSubject;
		Class<?> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValueClass();
	}

	
	@Test
	public void testGetDisplayName() throws Exception {
		MonitoringFieldsKeysEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	
	@Test
	public void testSetDisplayName() throws Exception {
		MonitoringFieldsKeysEnum testSubject;
		String displayName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDisplayName(displayName);
	}
}
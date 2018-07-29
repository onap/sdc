package org.openecomp.sdc.common.config;

import org.junit.Test;

import java.util.Map;


public class EcompErrorConfigurationTest {

	private EcompErrorConfiguration createTestSubject() {
		return new EcompErrorConfiguration();
	}

	
	@Test
	public void testGetErrors() throws Exception {
		EcompErrorConfiguration testSubject;
		Map<String, EcompErrorInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrors();
	}

	
	
	@Test
	public void testGetEcompErrorInfo() throws Exception {
		EcompErrorConfiguration testSubject;
		String key = "";
		EcompErrorInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompErrorInfo(key);
	}

	

	@Test
	public void testToString() throws Exception {
		EcompErrorConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		EcompErrorConfiguration.main(args);
	}
}
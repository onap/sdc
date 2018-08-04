package org.openecomp.sdc.be.config;

import java.util.Map;

import org.junit.Test;


public class ErrorConfigurationTest {

	private ErrorConfiguration createTestSubject() {
		return new ErrorConfiguration();
	}

	
	@Test
	public void testGetErrors() throws Exception {
		ErrorConfiguration testSubject;
		Map<String, ErrorInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrors();
	}

	
	@Test
	public void testSetErrors() throws Exception {
		ErrorConfiguration testSubject;
		Map<String, ErrorInfo> errors = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setErrors(errors);
	}

	


	
	@Test
	public void testToString() throws Exception {
		ErrorConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
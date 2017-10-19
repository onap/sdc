package org.openecomp.sdc.be.config;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class Neo4jErrorsConfigurationTest {

	private Neo4jErrorsConfiguration createTestSubject() {
		return new Neo4jErrorsConfiguration();
	}

	
	@Test
	public void testGetErrors() throws Exception {
		Neo4jErrorsConfiguration testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrors();
	}

	
	@Test
	public void testSetErrors() throws Exception {
		Neo4jErrorsConfiguration testSubject;
		Map<String, String> errors = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setErrors(errors);
	}

	


	
	@Test
	public void testToString() throws Exception {
		Neo4jErrorsConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
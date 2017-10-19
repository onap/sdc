package org.openecomp.sdc.be.config;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class CleanComponentsConfigurationTest {

	private CleanComponentsConfiguration createTestSubject() {
		return new CleanComponentsConfiguration();
	}

	
	@Test
	public void testGetCleanIntervalInMinutes() throws Exception {
		CleanComponentsConfiguration testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCleanIntervalInMinutes();
	}

	


	
	@Test
	public void testGetComponentsToClean() throws Exception {
		CleanComponentsConfiguration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsToClean();
	}

	
	@Test
	public void testSetComponentsToClean() throws Exception {
		CleanComponentsConfiguration testSubject;
		List<String> componentsToClean = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsToClean(componentsToClean);
	}
}
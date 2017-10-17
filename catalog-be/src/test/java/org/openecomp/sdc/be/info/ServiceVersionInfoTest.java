package org.openecomp.sdc.be.info;

import javax.annotation.Generated;

import org.junit.Test;

public class ServiceVersionInfoTest {

	private ServiceVersionInfo createTestSubject() {
		return new ServiceVersionInfo("", "", "");
	}

	
	@Test
	public void testGetVersion() throws Exception {
		ServiceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		ServiceVersionInfo testSubject;
		String serviceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(serviceVersion);
	}

	
	@Test
	public void testGetUrl() throws Exception {
		ServiceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUrl();
	}

	
	@Test
	public void testSetUrl() throws Exception {
		ServiceVersionInfo testSubject;
		String url = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUrl(url);
	}
}
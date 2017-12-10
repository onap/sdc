package org.openecomp.sdc.common.rest.api;

import org.junit.Test;


public class RestConfigurationInfoTest {

	private RestConfigurationInfo createTestSubject() {
		return new RestConfigurationInfo();
	}

	
	@Test
	public void testGetReadTimeoutInSec() throws Exception {
		RestConfigurationInfo testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getReadTimeoutInSec();
	}

	
	@Test
	public void testSetReadTimeoutInSec() throws Exception {
		RestConfigurationInfo testSubject;
		Integer readTimeoutInSec = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setReadTimeoutInSec(readTimeoutInSec);
	}

	
	@Test
	public void testGetIgnoreCertificate() throws Exception {
		RestConfigurationInfo testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIgnoreCertificate();
	}

	
	@Test
	public void testSetIgnoreCertificate() throws Exception {
		RestConfigurationInfo testSubject;
		Boolean ignoreCertificate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIgnoreCertificate(ignoreCertificate);
	}

	
	@Test
	public void testGetConnectionPoolSize() throws Exception {
		RestConfigurationInfo testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConnectionPoolSize();
	}

	
	@Test
	public void testSetConnectionPoolSize() throws Exception {
		RestConfigurationInfo testSubject;
		Integer connectionPoolSize = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnectionPoolSize(connectionPoolSize);
	}

	
	@Test
	public void testGetConnectTimeoutInSec() throws Exception {
		RestConfigurationInfo testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConnectTimeoutInSec();
	}

	
	@Test
	public void testSetConnectTimeoutInSec() throws Exception {
		RestConfigurationInfo testSubject;
		Integer connectTimeoutInSec = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnectTimeoutInSec(connectTimeoutInSec);
	}

	
	@Test
	public void testToString() throws Exception {
		RestConfigurationInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
package org.openecomp.sdc.common.http.config;

import org.junit.Assert;
import org.junit.Test;

public class TimeoutsTest {

	private Timeouts createTestSubject() {
		return new Timeouts(100, 200);
	}

	
	@Test
	public void testGetConnectTimeoutMs() throws Exception {
		Timeouts testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConnectTimeoutMs();
	}

	
	@Test
	public void testSetConnectTimeoutMs() throws Exception {
		Timeouts testSubject;
		int connectTimeoutMs = 100;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnectTimeoutMs(connectTimeoutMs);
	}

	
	@Test
	public void testGetReadTimeoutMs() throws Exception {
		Timeouts testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getReadTimeoutMs();
	}

	
	@Test
	public void testSetReadTimeoutMs() throws Exception {
		Timeouts testSubject;
		int readTimeoutMs = 100;

		// default test
		testSubject = createTestSubject();
		testSubject.setReadTimeoutMs(readTimeoutMs);
	}

	
	@Test
	public void testGetConnectPoolTimeoutMs() throws Exception {
		Timeouts testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConnectPoolTimeoutMs();
	}

	
	@Test
	public void testSetConnectPoolTimeoutMs() throws Exception {
		Timeouts testSubject;
		int connectPoolTimeoutMs = 100;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnectPoolTimeoutMs(connectPoolTimeoutMs);
	}

	
	@Test
	public void testHashCode() throws Exception {
		Timeouts testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		Timeouts testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testToString() throws Exception {
		Timeouts testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
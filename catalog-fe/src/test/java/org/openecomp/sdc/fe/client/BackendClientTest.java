package org.openecomp.sdc.fe.client;

import javax.net.ssl.HostnameVerifier;

import org.junit.Test;


public class BackendClientTest {

	private BackendClient createTestSubject() {
		return new BackendClient("", "", "");
	}

	
	@Test
	public void testGetHostnameVerifier() throws Exception {
		BackendClient testSubject;
		HostnameVerifier result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHostnameVerifier();
	}


}
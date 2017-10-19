package org.openecomp.sdc.fe.client;

import java.util.List;

import javax.annotation.Generated;
import javax.net.ssl.HostnameVerifier;
import javax.ws.rs.container.AsyncResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.http.HttpGenerator.ResponseInfo;
import org.junit.Test;
import org.openecomp.sdc.fe.impl.HttpRequestInfo;


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
package org.openecomp.sdc.be.dao.es;

import org.elasticsearch.client.Client;
import org.junit.Test;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

public class ElasticSearchClientTest extends DAOConfDependentTest{

	private ElasticSearchClient createTestSubject() {
		return new ElasticSearchClient();
	}

	@Test
	public void testInitialize() throws Exception {
		ElasticSearchClient testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setTransportClient("true");
		testSubject.setLocal("true");
		testSubject.initialize();
		testSubject.setTransportClient("false");
		testSubject.setClusterName("false");
		testSubject.initialize();
	}
	
	@Test
	public void testClose() throws Exception {
		ElasticSearchClient testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.close();
	}

	
	@Test
	public void testGetClient() throws Exception {
		ElasticSearchClient testSubject;
		Client result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClient();
	}

	
	@Test
	public void testGetServerHost() throws Exception {
		ElasticSearchClient testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServerHost();
	}

	
	@Test
	public void testGetServerPort() throws Exception {
		ElasticSearchClient testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServerPort();
	}

	
	@Test
	public void testSetClusterName() throws Exception {
		ElasticSearchClient testSubject;
		String clusterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setClusterName(clusterName);
	}

	
	@Test
	public void testSetLocal() throws Exception {
		ElasticSearchClient testSubject;
		String strIsLocal = "";

		// test 1
		testSubject = createTestSubject();
		strIsLocal = null;
		testSubject.setLocal(strIsLocal);

		// test 2
		testSubject = createTestSubject();
		strIsLocal = "";
		testSubject.setLocal(strIsLocal);
		
		strIsLocal = "true";
		testSubject.setLocal(strIsLocal);
	}

	
	@Test
	public void testIsTransportClient() throws Exception {
		ElasticSearchClient testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isTransportClient();
	}

	
	@Test
	public void testSetTransportClient() throws Exception {
		ElasticSearchClient testSubject;
		String strIsTransportclient = "";

		// test 1
		testSubject = createTestSubject();
		strIsTransportclient = null;
		testSubject.setTransportClient(strIsTransportclient);

		// test 2
		testSubject = createTestSubject();
		strIsTransportclient = "true";
		testSubject.setTransportClient(strIsTransportclient);
	}
}
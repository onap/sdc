package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.junit.Test;

public class ElasticSearchClientMockTest {

	private ElasticSearchClientMock createTestSubject() {
		return new ElasticSearchClientMock();
	}

	@Test
	public void testInitialize() throws Exception {
		ElasticSearchClientMock testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.initialize();
	}

	@Test
	public void testSetClusterName() throws Exception {
		ElasticSearchClientMock testSubject;
		String clusterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setClusterName(clusterName);
	}

	@Test
	public void testSetLocal() throws Exception {
		ElasticSearchClientMock testSubject;
		String strIsLocal = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLocal(strIsLocal);
	}

	@Test
	public void testSetTransportClient() throws Exception {
		ElasticSearchClientMock testSubject;
		String strIsTransportclient = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTransportClient(strIsTransportclient);
	}
}
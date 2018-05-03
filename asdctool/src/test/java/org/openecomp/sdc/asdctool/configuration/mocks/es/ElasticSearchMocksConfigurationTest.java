package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;

public class ElasticSearchMocksConfigurationTest {

	private ElasticSearchMocksConfiguration createTestSubject() {
		return new ElasticSearchMocksConfiguration();
	}

	@Test
	public void testElasticSearchClientMock() throws Exception {
		ElasticSearchMocksConfiguration testSubject;
		ElasticSearchClient result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.elasticSearchClientMock();
	}

	@Test
	public void testEsCatalogDAOMock() throws Exception {
		ElasticSearchMocksConfiguration testSubject;
		ICatalogDAO result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.esCatalogDAOMock();
	}

	@Test
	public void testEsHealthCheckDaoMock() throws Exception {
		ElasticSearchMocksConfiguration testSubject;
		IEsHealthCheckDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.esHealthCheckDaoMock();
	}
}
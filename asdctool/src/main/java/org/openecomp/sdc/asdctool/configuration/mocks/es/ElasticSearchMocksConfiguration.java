package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.IEsHealthCheckDao;
import org.openecomp.sdc.be.dao.es.ElasticSearchClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchMocksConfiguration {

    @Bean("elasticsearch-client")
    public ElasticSearchClient elasticSearchClientMock() {
        return new ElasticSearchClientMock();
    }

    @Bean("resource-dao")
    public ICatalogDAO esCatalogDAOMock() {
        return new ESCatalogDAOMock();
    }

    @Bean("esHealthCheckDao")
    public IEsHealthCheckDao esHealthCheckDaoMock() {
        return new EsHealthCheckDaoMock();
    }

}

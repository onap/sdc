package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.openecomp.sdc.be.dao.es.ElasticSearchClient;

import java.net.URISyntaxException;

public class ElasticSearchClientMock extends ElasticSearchClient {

    @Override
    public void initialize() {

    }

    @Override
    public void setClusterName(final String clusterName) {

    }

    @Override
    public void setLocal(final String strIsLocal) {
    }

    @Override
    public void setTransportClient(final String strIsTransportclient) {
    }
}

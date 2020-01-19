package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.FeatureToggleDao;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("feature_toggle_dao")
public class FeatureToggleDaoMock extends FeatureToggleDao {

    public FeatureToggleDaoMock(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    public void init() {}

    public CassandraOperationStatus save(FeatureToggleEvent featureToggleEvent) {
        return null;
    }

    public FeatureToggleEvent get(String feature_name) {
        return null;
    }

    public CassandraOperationStatus delete(String feature_name) {
        return null;
    }

    public List<FeatureToggleEvent> getAllFeatures() {
        return null;
    }
}

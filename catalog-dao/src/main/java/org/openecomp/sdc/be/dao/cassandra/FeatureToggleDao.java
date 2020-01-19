package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.resources.data.auditing.AuditingTypesConstants;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component("feature_toggle_dao")
public class FeatureToggleDao extends CassandraDao {

    private FeatureToggleAccessor featureToggleAccessor;
    private static Logger logger = Logger.getLogger(FeatureToggleDao.class.getName());

    public FeatureToggleDao(CassandraClient cassandraClient) {
        super(cassandraClient);
    }


    @PostConstruct
    public void init() {
        String keyspace = AuditingTypesConstants.REPO_KEYSPACE;
        if (client.isConnected()) {
            Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> result = client.connect(keyspace);
            if (result.isLeft()) {
                session = result.left().value().left;
                manager = result.left().value().right;
                featureToggleAccessor = manager.createAccessor(FeatureToggleAccessor.class);
                logger.info("** FeatureToggleDao created");
            } else {
                logger.info("** FeatureToggleDao failed");
                throw new RuntimeException(
                        "Repo keyspace [" + keyspace + "] failed to connect with error : " + result.right().value());
            }
        } else {
            logger.info("** Cassandra client isn't connected");
            logger.info("** FeatureToggleDao created, but not connected");
        }
    }

    public CassandraOperationStatus save(FeatureToggleEvent featureToggleEvent) {
       return client.save(featureToggleEvent, FeatureToggleEvent.class, manager);
    }

    public FeatureToggleEvent get(String feature_name) {
        return client.getById(feature_name, FeatureToggleEvent.class, manager)
            .left()
            .on(r -> {
                logger.debug("Failed to retrieve state of feature [{}] due to error {}", feature_name, r.toString());
                return null;
            });
    }

    public CassandraOperationStatus delete(String feature_name) {
        return client.delete(feature_name, FeatureToggleEvent.class, manager);
    }

    public List<FeatureToggleEvent> getAllFeatures() {
        return featureToggleAccessor.getAllFeatures().all();
    }

}

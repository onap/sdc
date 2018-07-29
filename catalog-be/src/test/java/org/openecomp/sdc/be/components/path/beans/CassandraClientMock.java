package org.openecomp.sdc.be.components.path.beans;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.springframework.stereotype.Component;

@Component("cassandra-client")
public class CassandraClientMock extends CassandraClient{
    public CassandraClientMock() {

    }

    @Override
    public Either<ImmutablePair<Session, MappingManager>, CassandraOperationStatus> connect(String keyspace) {
        return null;
    }
}

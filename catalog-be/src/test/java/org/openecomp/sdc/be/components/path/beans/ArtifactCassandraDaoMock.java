package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("artifact-cassandra-dao")
public class ArtifactCassandraDaoMock extends ArtifactCassandraDao {

    public ArtifactCassandraDaoMock(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    @Override
    public void init() {

    }
}

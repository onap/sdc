package org.openecomp.sdc.be.components.path.beans;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.springframework.stereotype.Component;

@Component("artifact-cassandra-dao")
public class ArtifactCassandraDaoMock extends ArtifactCassandraDao {
    @PostConstruct
    @Override
    public void init() {

    }
}

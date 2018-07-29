package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("artifact-cassandra-dao")
public class ArtifactCassandraDaoMock extends ArtifactCassandraDao {
    @PostConstruct
    @Override
    public void init() {

    }
}

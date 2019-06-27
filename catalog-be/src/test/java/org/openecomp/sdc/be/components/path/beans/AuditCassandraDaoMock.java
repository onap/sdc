package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("audit-cassandra-dao")
public class AuditCassandraDaoMock extends AuditCassandraDao{

    public AuditCassandraDaoMock(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    public void init() {

    }
}

package org.openecomp.sdc.be.components.path.beans;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.springframework.stereotype.Component;

@Component("audit-cassandra-dao")
public class AuditCassandraDaoMock extends AuditCassandraDao{

    @PostConstruct
    public void init() {

    }
}

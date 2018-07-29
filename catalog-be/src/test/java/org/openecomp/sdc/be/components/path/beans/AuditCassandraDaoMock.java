package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("audit-cassandra-dao")
public class AuditCassandraDaoMock extends AuditCassandraDao{

    @PostConstruct
    public void init() {

    }
}

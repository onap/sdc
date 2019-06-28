package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("operational-environment-dao")
public class EnvironmentCassandraDaoMock extends OperationalEnvironmentDao {

    public EnvironmentCassandraDaoMock(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    @Override
    public void init() {

    }
}

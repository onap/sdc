package org.openecomp.sdc.be.components.path.beans;

import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("sdc-schema-files-cassandra-dao")
public class SdcSchemaFilesCassandraDaoMock extends SdcSchemaFilesCassandraDao {

    public SdcSchemaFilesCassandraDaoMock(CassandraClient cassandraClient) {
        super(cassandraClient);
    }

    @PostConstruct
    @Override
    public void init() {
    }
}

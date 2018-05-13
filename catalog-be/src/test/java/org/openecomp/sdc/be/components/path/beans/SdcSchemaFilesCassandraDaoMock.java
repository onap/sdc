package org.openecomp.sdc.be.components.path.beans;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.dao.cassandra.SdcSchemaFilesCassandraDao;
import org.springframework.stereotype.Component;

@Component("sdc-schema-files-cassandra-dao")
public class SdcSchemaFilesCassandraDaoMock extends SdcSchemaFilesCassandraDao {

    @PostConstruct
    @Override
    public void init() {
    }
}

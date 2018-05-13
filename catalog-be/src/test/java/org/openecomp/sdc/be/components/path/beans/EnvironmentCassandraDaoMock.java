package org.openecomp.sdc.be.components.path.beans;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.springframework.stereotype.Component;

@Component("operational-environment-dao")
public class EnvironmentCassandraDaoMock extends OperationalEnvironmentDao {
    @PostConstruct
    @Override
    public void init() {

    }
}

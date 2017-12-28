package org.openecomp.sdc.cucumber.spring;

import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportTableConfig {
	@Bean(name = "cassandra-client")
	public CassandraClient cassandraClient() {
		return new CassandraClient();
	}
	
	@Bean(name = "operational-environment-dao")
	public OperationalEnvironmentDao operationalEnvironmentDao() {
		return new OperationalEnvironmentDao();
	}
}

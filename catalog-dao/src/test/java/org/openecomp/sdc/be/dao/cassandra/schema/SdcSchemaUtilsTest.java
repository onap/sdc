package org.openecomp.sdc.be.dao.cassandra.schema;

import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

import java.util.LinkedList;
import java.util.List;


public class SdcSchemaUtilsTest extends DAOConfDependentTest{
	
	@Test
	public void testExecuteStatement() throws Exception {
		String statement = "";
		boolean result;

		// default test
		result = SdcSchemaUtils.executeStatement(statement);
		
		List<String> cassandraHosts = new LinkedList<>();
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setCassandraHosts(cassandraHosts);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setAuthenticate(true);
		ConfigurationManager.getConfigurationManager().getConfiguration().getCassandraConfig().setSsl(true);
		
		result = SdcSchemaUtils.executeStatement(statement);
	}

	
	@Test
	public void testExecuteStatements() throws Exception {
		String[] statements = new String[] { "" };
		boolean result;

		// default test
		result = SdcSchemaUtils.executeStatements(statements);
	}
}
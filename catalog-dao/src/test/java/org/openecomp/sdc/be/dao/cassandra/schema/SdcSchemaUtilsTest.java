package org.openecomp.sdc.be.dao.cassandra.schema;

import org.junit.Test;


public class SdcSchemaUtilsTest {

	private SdcSchemaUtils createTestSubject() {
		return new SdcSchemaUtils();
	}

	


	
	@Test
	public void testExecuteStatement() throws Exception {
		String statement = "";
		boolean result;

		// default test
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
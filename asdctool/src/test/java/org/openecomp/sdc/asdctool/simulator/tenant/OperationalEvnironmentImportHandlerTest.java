package org.openecomp.sdc.asdctool.simulator.tenant;

import org.junit.Test;

public class OperationalEvnironmentImportHandlerTest {

	@Test
	public void testExecute() throws Exception {
		String fileName = "";

		// default test
		OperationalEvnironmentImportHandler.execute(fileName);
	}

	@Test
	public void testGetTableName() throws Exception {
		String result;

		// default test
		result = OperationalEvnironmentImportHandler.getTableName();
	}
}
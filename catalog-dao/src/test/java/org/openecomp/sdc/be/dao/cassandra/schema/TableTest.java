package org.openecomp.sdc.be.dao.cassandra.schema;

import org.junit.Test;


public class TableTest {

	private Table createTestSubject() {
		return  Table.ARTIFACT;
	}

	
	@Test
	public void testGetTableDescription() throws Exception {
		Table testSubject;
		ITableDescription result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTableDescription();
	}
}
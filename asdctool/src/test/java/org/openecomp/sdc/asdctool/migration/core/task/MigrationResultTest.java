package org.openecomp.sdc.asdctool.migration.core.task;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult.MigrationStatus;


public class MigrationResultTest {

	private MigrationResult createTestSubject() {
		return new MigrationResult();
	}

	
	@Test
	public void testGetMsg() throws Exception {
		MigrationResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMsg();
	}

	
	@Test
	public void testSetMsg() throws Exception {
		MigrationResult testSubject;
		String msg = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMsg(msg);
	}

	
	@Test
	public void testGetMigrationStatus() throws Exception {
		MigrationResult testSubject;
		MigrationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMigrationStatus();
	}

	
	@Test
	public void testSetMigrationStatus() throws Exception {
		MigrationResult testSubject;
		MigrationStatus migrationStatus = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMigrationStatus(migrationStatus);
	}
}
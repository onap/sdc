package org.openecomp.sdc.asdctool.migration.dao;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

import java.math.BigInteger;

public class MigrationTasksDaoTest {

	private MigrationTasksDao createTestSubject() {
		return new MigrationTasksDao();
	}

	@Test(expected=NullPointerException.class)
	public void testInit() throws Exception {
		MigrationTasksDao testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	@Test(expected=NullPointerException.class)
	public void testGetLatestMinorVersion() throws Exception {
		MigrationTasksDao testSubject;
		BigInteger majorVersion = null;
		BigInteger result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLatestMinorVersion(majorVersion);
	}

	@Test(expected=NullPointerException.class)
	public void testDeleteAllTasksForVersion() throws Exception {
		MigrationTasksDao testSubject;
		BigInteger majorVersion = null;

		// default test
		testSubject = createTestSubject();
		testSubject.deleteAllTasksForVersion(majorVersion);
	}

	@Test(expected=NullPointerException.class)
	public void testCreateMigrationTask() throws Exception {
		MigrationTasksDao testSubject;
		MigrationTaskEntry migrationTask = null;

		// default test
		testSubject = createTestSubject();
		testSubject.createMigrationTask(migrationTask);
	}
}
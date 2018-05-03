package org.openecomp.sdc.asdctool.migration.core.execution;

import org.junit.Test;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult.MigrationStatus;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

public class MigrationExecutionResultTest {

	private MigrationExecutionResult createTestSubject() {
		return new MigrationExecutionResult();
	}

	@Test(expected=NullPointerException.class)
	public void testToMigrationTaskEntry() throws Exception {
		MigrationExecutionResult testSubject;
		MigrationTaskEntry result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toMigrationTaskEntry();
	}

	@Test
	public void testGetMigrationStatus() throws Exception {
		MigrationExecutionResult testSubject;
		MigrationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMigrationStatus();
	}

	@Test
	public void testGetMsg() throws Exception {
		MigrationExecutionResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMsg();
	}

	@Test
	public void testGetVersion() throws Exception {
		MigrationExecutionResult testSubject;
		DBVersion result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		MigrationExecutionResult testSubject;
		DBVersion version = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetDescription() throws Exception {
		MigrationExecutionResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		MigrationExecutionResult testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}
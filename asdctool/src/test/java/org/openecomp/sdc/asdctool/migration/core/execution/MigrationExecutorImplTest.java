package org.openecomp.sdc.asdctool.migration.core.execution;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class MigrationExecutorImplTest {

    @Test
    public void testExecuteMigration() throws Exception {
        MigrationExecutionResult execute = new MigrationExecutorImpl().execute(new DummyMigration());

    }

    private void assertMigrationTaskEntryByMigrationExecutionResult(MigrationExecutionResult executionResult, Migration migration, MigrationResult result) {
        assertEquals(executionResult.getMsg(), result.getMsg());
        assertEquals(executionResult.getMigrationStatus().name(), result.getMigrationStatus());
        assertEquals(executionResult.getTaskName(), migration.getClass().getName());
        assertEquals(executionResult.getVersion(), migration.getVersion());
        assertNotNull(executionResult.getExecutionTime());
    }

    private class DummyMigration implements Migration {

        @Override
        public String description() {
            return null;
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
            migrationResult.setMsg("myMsg");
            return migrationResult;
        }
    }
}

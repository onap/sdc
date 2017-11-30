package org.openecomp.sdc.asdctool.migration.core.execution;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.openecomp.sdc.asdctool.migration.DummyMigrationFactory;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.testng.annotations.Test;

public class MigrationExecutorImplTest {

    @Test
    public void testExecuteMigration() throws Exception {
        MigrationExecutionResult execute = new MigrationExecutorImpl().execute(DummyMigrationFactory.SUCCESSFUL_MIGRATION);
        assertMigrationTaskEntryByMigrationExecutionResult(execute, DummyMigrationFactory.SUCCESSFUL_MIGRATION);
    }

    private void assertMigrationTaskEntryByMigrationExecutionResult(MigrationExecutionResult executionResult, Migration migration) {
        MigrationResult migrationResult = migration.migrate();
        assertEquals(executionResult.getMsg(), migrationResult.getMsg());
        assertEquals(executionResult.getMigrationStatus(), migrationResult.getMigrationStatus());
        assertEquals(executionResult.getTaskName(), migration.getClass().getName());
        assertEquals(executionResult.getVersion(), migration.getVersion());
        assertEquals(executionResult.getDescription(), migration.description());
        assertNotNull(executionResult.getExecutionTime());
    }

}

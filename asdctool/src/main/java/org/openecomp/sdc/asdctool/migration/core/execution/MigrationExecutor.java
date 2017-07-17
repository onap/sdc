package org.openecomp.sdc.asdctool.migration.core.execution;

import org.openecomp.sdc.asdctool.migration.core.MigrationException;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;

public interface MigrationExecutor {

    /**
     * @param migration the migration to execute
     * @return a {@link MigrationExecutionResult} with the relevant data on the current migration execution;
     * @throws MigrationException in case there was an unexpected exception during migration
     */
    MigrationExecutionResult execute(Migration migration) throws MigrationException;

}

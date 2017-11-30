package org.openecomp.sdc.asdctool.migration.core.execution;

import org.openecomp.sdc.asdctool.migration.core.MigrationException;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;


public class MigrationExecutorImpl implements MigrationExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationExecutorImpl.class);

    @Override
    public MigrationExecutionResult execute(IMigrationStage migration) throws MigrationException {
        try {
            LOGGER.info("starting migration {}. description: {}. version {}", migration.getClass().getName(), migration.description(),  migration.getVersion().toString());
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            MigrationResult migrationResult = migration.migrate();
            stopWatch.stop();
            double executionTime = stopWatch.getTotalTimeSeconds();
            return logAndCreateExecutionResult(migration, migrationResult, executionTime);
        } catch (RuntimeException e) {
            LOGGER.error("migration {} has failed!", migration.description(), e);
            throw new MigrationException("migration %s failed!!!", e);

        }
    }

    private MigrationExecutionResult logAndCreateExecutionResult(IMigrationStage migration, MigrationResult migrationResult, double executionTime) {
        LOGGER.info("finished migration {}. with version {}. migration status: {}, migration message: {}, execution time: {}", migration.getClass().getName(),  migration.getVersion().toString(), migrationResult.getMigrationStatus().name(), migrationResult.getMsg(), executionTime);
        return createMigrationTask(migration, migrationResult, executionTime);
    }

    private MigrationExecutionResult createMigrationTask(IMigrationStage migration, MigrationResult migrationResult, double totalTimeSeconds) {
        MigrationExecutionResult migrationExecutionResult = new MigrationExecutionResult();
        migrationExecutionResult.setExecutionTime(totalTimeSeconds);
        migrationExecutionResult.setMigrationStatus(migrationResult.getMigrationStatus());
        migrationExecutionResult.setMsg(migrationResult.getMsg());
        migrationExecutionResult.setTaskName(migration.getClass().getName());
        migrationExecutionResult.setVersion(migration.getVersion());
        migrationExecutionResult.setDescription(migration.description());
        return migrationExecutionResult;
    }

}

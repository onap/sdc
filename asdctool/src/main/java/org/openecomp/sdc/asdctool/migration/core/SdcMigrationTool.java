package org.openecomp.sdc.asdctool.migration.core;

import java.util.List;

import org.openecomp.sdc.asdctool.migration.core.execution.MigrationExecutionResult;
import org.openecomp.sdc.asdctool.migration.core.execution.MigrationExecutorImpl;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdcMigrationTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SdcMigrationTool.class);

    private MigrationResolver migrationsResolver;

    private SdcRepoService sdcRepoService;

    public SdcMigrationTool(MigrationResolver migrationsResolver, SdcRepoService sdcRepoService) {
        this.migrationsResolver = migrationsResolver;
        this.sdcRepoService = sdcRepoService;
    }

    public SdcMigrationTool() {
    }

    public boolean migrate(boolean enforceAll) {
        LOGGER.info("starting migration process");
        handleEnforceMigrationFlag(enforceAll);
        List<Migration> migrations = migrationsResolver.resolveMigrations();
        LOGGER.info("there are {} migrations task to execute", migrations.size());
        for (Migration migration : migrations) {
            try {
                MigrationExecutionResult executionResult = new MigrationExecutorImpl().execute(migration);
                if (migrationHasFailed(executionResult)) {
                    LOGGER.error("migration {} with version {} has failed. error msg: {}", migration.getClass().getName(), migration.getVersion().toString(), executionResult.getMsg());
                    return false;
                }
                sdcRepoService.createMigrationTask(executionResult.toMigrationTaskEntry());
            } catch (RuntimeException e) {
                LOGGER.error("migration {} with version {} has failed. error msg: {}", migration.getClass().getName(), migration.getVersion().toString(), e);
                return false;
            }
        }
        return true;
    }

    private boolean migrationHasFailed(MigrationExecutionResult migrationResult) {
        return migrationResult.getMigrationStatus().equals(MigrationResult.MigrationStatus.FAILED);
    }

    private void handleEnforceMigrationFlag(boolean enforceAll) {
        if (enforceAll) {
            LOGGER.info("enforcing migration for current version");
            sdcRepoService.clearTasksForCurrentMajor();
        }
    }

}

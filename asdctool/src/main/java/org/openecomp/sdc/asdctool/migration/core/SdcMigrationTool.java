/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.asdctool.migration.core;

import java.util.List;
import org.openecomp.sdc.asdctool.migration.core.execution.MigrationExecutionResult;
import org.openecomp.sdc.asdctool.migration.core.execution.MigrationExecutorImpl;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage.AspectMigrationEnum;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.asdctool.migration.resolver.MigrationResolver;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class SdcMigrationTool {

    private static final Logger LOGGER = Logger.getLogger(SdcMigrationTool.class);
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
        List<IMigrationStage> migrations = migrationsResolver.resolveMigrations();
        LOGGER.info("there are {} migrations task to execute", migrations.size());
        for (IMigrationStage migration : migrations) {
            try {
                MigrationExecutionResult executionResult = new MigrationExecutorImpl().execute(migration);
                if (migrationHasFailed(executionResult)) {
                    LOGGER.error("migration {} with version {} has failed. error msg: {}", migration.getClass().getName(),
                        migration.getVersion().toString(), executionResult.getMsg());
                    return false;
                }
                if (migration.getAspectMigration() == AspectMigrationEnum.MIGRATION) {
                    sdcRepoService.createMigrationTask(executionResult.toMigrationTaskEntry());
                }
            } catch (RuntimeException e) {
                LOGGER.error("migration {} with version {} has failed. error msg: {}", migration.getClass().getName(),
                    migration.getVersion().toString(), e);
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

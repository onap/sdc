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
package org.openecomp.sdc.asdctool.migration.core.execution;

import org.openecomp.sdc.asdctool.migration.core.MigrationException;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.util.StopWatch;

public class MigrationExecutorImpl implements MigrationExecutor {

    private static final Logger LOGGER = Logger.getLogger(MigrationExecutorImpl.class);

    @Override
    public MigrationExecutionResult execute(IMigrationStage migration) throws MigrationException {
        try {
            LOGGER.info("starting migration {}. description: {}. version {}", migration.getClass().getName(), migration.description(),
                migration.getVersion().toString());
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
        LOGGER.info("finished migration {}. with version {}. migration status: {}, migration message: {}, execution time: {}",
            migration.getClass().getName(), migration.getVersion().toString(), migrationResult.getMigrationStatus().name(), migrationResult.getMsg(),
            executionTime);
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

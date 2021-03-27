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
package org.openecomp.sdc.asdctool.migration.resolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;

public class SpringBeansMigrationResolver implements MigrationResolver {

    private List<Migration> migrations = new ArrayList<>();
    private List<PostMigration> postMigrations = new ArrayList<>();
    private SdcRepoService sdcRepoService;

    public SpringBeansMigrationResolver(List<Migration> migrations, List<PostMigration> postMigrations, SdcRepoService sdcRepoService) {
        this.migrations = migrations;
        this.postMigrations = postMigrations;
        this.sdcRepoService = sdcRepoService;
    }

    @Override
    public List<IMigrationStage> resolveMigrations() {
        migrations.sort(Comparator.comparing(Migration::getVersion));
        List<IMigrationStage> allTasks = resolveNonExecutedMigrations();
        allTasks.addAll(postMigrations);
        return allTasks;
    }

    //package private for testing
    void setMigrations(List<Migration> migrations) {
        this.migrations = migrations;
    }

    //package private for testing
    void setPostMigrations(List<PostMigration> postMigrations) {
        this.postMigrations = postMigrations;
    }

    private List<IMigrationStage> resolveNonExecutedMigrations() {
        DBVersion latestDBVersion = sdcRepoService.getLatestDBVersion();
        return migrations.stream().filter(mig -> isMigrationVersionGreaterThanLatestVersion(latestDBVersion, mig)).collect(Collectors.toList());
    }

    private boolean isMigrationVersionGreaterThanLatestVersion(DBVersion latestDBVersion, Migration mig) {
        return mig.getVersion().compareTo(latestDBVersion) > 0;
    }
}

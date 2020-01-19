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

package org.openecomp.sdc.asdctool.migration.service;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

import java.math.BigInteger;

public class SdcRepoService {

    private MigrationTasksDao migrationTasksDao;

    public SdcRepoService(MigrationTasksDao migrationTasksDao) {
        this.migrationTasksDao = migrationTasksDao;
    }

    public DBVersion getLatestDBVersion() {
        BigInteger currentMajorVersion = migrationTasksDao.getLatestMajorVersion();
        BigInteger latestMinorVersion = migrationTasksDao.getLatestMinorVersion(currentMajorVersion);
        return DBVersion.from(currentMajorVersion, latestMinorVersion);
    }

    public void clearTasksForCurrentMajor() {
        BigInteger currentMajorVersion = DBVersion.DEFAULT_VERSION.getMajor();
        migrationTasksDao.deleteAllTasksForVersion(currentMajorVersion);
    }

    public void createMigrationTask(MigrationTaskEntry migrationTaskEntry) {
        migrationTasksDao.createMigrationTask(migrationTaskEntry);
    }



}

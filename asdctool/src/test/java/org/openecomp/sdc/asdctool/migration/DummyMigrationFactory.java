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

package org.openecomp.sdc.asdctool.migration;


import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;

public class DummyMigrationFactory {

    public static Migration SUCCESSFUL_MIGRATION = new Migration() {
        @Override
        public String description() {
            return "success mig";
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
    };

    public static Migration FAILED_MIGRATION = new Migration() {
        @Override
        public String description() {
            return "failed mig";
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
            migrationResult.setMsg("myMsg");
            return migrationResult;
        }
    };

    public static Migration getMigration(String version, MigrationResult.MigrationStatus status) {
        return new Migration() {
            @Override
            public String description() {
                return "success mig";
            }

            @Override
            public DBVersion getVersion() {
                return DBVersion.fromString(version);
            }

            @Override
            public MigrationResult migrate() {
                MigrationResult migrationResult = new MigrationResult();
                migrationResult.setMigrationStatus(status);
                migrationResult.setMsg("myMsg");
                return migrationResult;
            }
        };
    }

}

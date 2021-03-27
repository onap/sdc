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
package org.openecomp.sdc.asdctool.migration.core.task;

public class MigrationResult {

    private String msg;
    private MigrationStatus migrationStatus;

    public static MigrationResult success() {
        MigrationResult success = new MigrationResult();
        success.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
        return success;
    }

    public static MigrationResult error(String msg) {
        MigrationResult error = new MigrationResult();
        error.setMigrationStatus(MigrationStatus.FAILED);
        error.setMsg(msg);
        return error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public enum MigrationStatus {COMPLETED, COMPLETED_WITH_ERRORS, FAILED}
}

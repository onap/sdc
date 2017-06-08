/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.storage;

/**
 * Created by TALIO on 11/22/2016.
 */
public class Storage {
    private Backup backup;
    private LogBackup logBackup;
    private  SnapshotBackup snapshotBackup;

    public Backup getBackup() {
        return backup;
    }

    public void setBackup(Backup backup) {
        this.backup = backup;
    }

    public LogBackup getLogBackup() {
        return logBackup;
    }

    public void setLogBackup(LogBackup logBackup) {
        this.logBackup = logBackup;
    }

    public SnapshotBackup getSnapshotBackup() {
        return snapshotBackup;
    }

    public void setSnapshotBackup(SnapshotBackup snapshotBackup) {
        this.snapshotBackup = snapshotBackup;
    }
}

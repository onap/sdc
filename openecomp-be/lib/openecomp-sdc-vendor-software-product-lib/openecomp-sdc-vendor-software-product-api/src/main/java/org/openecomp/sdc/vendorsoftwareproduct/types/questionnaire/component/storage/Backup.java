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
public class Backup {
    private String backupType;
    private String backupSolution;
    private String backupNIC;
    private Number backupStorageSize;

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public String getBackupSolution() {
        return backupSolution;
    }

    public void setBackupSolution(String backupSolution) {
        this.backupSolution = backupSolution;
    }

    public String getBackupNIC() {
        return backupNIC;
    }

    public void setBackupNIC(String backupNIC) {
        this.backupNIC = backupNIC;
    }

    public Number getBackupStorageSize() {
        return backupStorageSize;
    }

    public void setBackupStorageSize(Number backupStorageSize) {
        this.backupStorageSize = backupStorageSize;
    }
}

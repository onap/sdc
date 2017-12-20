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
public class LogBackup {
    private Number sizeOfLogFiles;
    private Number logBackupFrequency;
    private int logRetentionPeriod;
    private String logFileLocation;

    public Number getSizeOfLogFiles() {
        return sizeOfLogFiles;
    }

    public void setSizeOfLogFiles(Number sizeOfLogFiles) {
        this.sizeOfLogFiles = sizeOfLogFiles;
    }

    public Number getLogBackupFrequency() {
        return logBackupFrequency;
    }

    public void setLogBackupFrequency(Number logBackupFrequency) {
        this.logBackupFrequency = logBackupFrequency;
    }

    public int getLogRetentionPeriod() {
        return logRetentionPeriod;
    }

    public void setLogRetentionPeriod(int logRetentionPeriod) {
        this.logRetentionPeriod = logRetentionPeriod;
    }

    public String getLogFileLocation() {
        return logFileLocation;
    }

    public void setLogFileLocation(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }
}

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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute;

public class VmSizing {
    private int numOfCPUs;
    private String fileSystemSizeGB;
    private Number persistentStorageVolumeSize;
    private Number IOOperationsPerSec;
    private String cpuOverSubscriptionRatio;
    private String memoryRAM;

    public int getNumOfCPUs() {
        return numOfCPUs;
    }

    public void setNumOfCPUs(int numOfCPUs) {
        this.numOfCPUs = numOfCPUs;
    }

    public String getFileSystemSizeGB() {
        return fileSystemSizeGB;
    }

    public void setFileSystemSizeGB(String fileSystemSizeGB) {
        this.fileSystemSizeGB = fileSystemSizeGB;
    }

    public Number getPersistentStorageVolumeSize() {
        return persistentStorageVolumeSize;
    }

    public void setPersistentStorageVolumeSize(Number persistentStorageVolumeSize) {
        this.persistentStorageVolumeSize = persistentStorageVolumeSize;
    }

    public Number getIOOperationsPerSec() {
        return IOOperationsPerSec;
    }

    public void setIOOperationsPerSec(Number IOOperationsPerSec) {
        this.IOOperationsPerSec = IOOperationsPerSec;
    }

    public String getCpuOverSubscriptionRatio() {
        return cpuOverSubscriptionRatio;
    }

    public void setCpuOverSubscriptionRatio(String cpuOverSubscriptionRatio) {
        this.cpuOverSubscriptionRatio = cpuOverSubscriptionRatio;
    }

    public String getMemoryRAM() {
        return memoryRAM;
    }

    public void setMemoryRAM(String memoryRAM) {
        this.memoryRAM = memoryRAM;
    }
}

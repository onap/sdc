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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.vsp.general;

/**
 * Created by TALIO on 11/21/2016.
 */
public class StorageDataReplication {
    private boolean storageReplicationAcrossRegion;
    private Number storageReplicationSize;
    private Number storageReplicationFrequency;
    private String storageReplicationSource;
    private String storageReplicationDestination;

    public String getStorageReplicationDestination() {
        return storageReplicationDestination;
    }

    public void setStorageReplicationDestination(String storageReplicationDestination) {
        this.storageReplicationDestination = storageReplicationDestination;
    }

    public Number getStorageReplicationSize() {
        return storageReplicationSize;
    }

    public void setStorageReplicationSize(Number storageReplicationSize) {
        this.storageReplicationSize = storageReplicationSize;
    }

    public Number getStorageReplicationFrequency() {
        return storageReplicationFrequency;
    }

    public void setStorageReplicationFrequency(Number storageReplicationFrequency) {
        this.storageReplicationFrequency = storageReplicationFrequency;
    }

    public String getStorageReplicationSource() {
        return storageReplicationSource;
    }

    public void setStorageReplicationSource(String storageReplicationSource) {
        this.storageReplicationSource = storageReplicationSource;
    }

    public boolean isStorageReplicationAcrossRegion() {
        return storageReplicationAcrossRegion;
    }

    public void setStorageReplicationAcrossRegion(boolean storageReplicationAcrossRegion) {
        this.storageReplicationAcrossRegion = storageReplicationAcrossRegion;
    }
}

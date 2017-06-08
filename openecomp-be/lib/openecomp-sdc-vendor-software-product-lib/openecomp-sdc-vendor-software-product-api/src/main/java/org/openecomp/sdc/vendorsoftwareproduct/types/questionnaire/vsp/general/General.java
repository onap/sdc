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
 * Created by TALIO on 11/22/2016.
 */
public class General {

    private String affinityData;
    private Availability availability;
    private RegionsData regionsData;
    private StorageDataReplication storageDataReplication;

    public StorageDataReplication getStorageDataReplication() {
        return storageDataReplication;
    }

    public void setStorageDataReplication(StorageDataReplication storageDataReplication) {
        this.storageDataReplication = storageDataReplication;
    }

    public String getAffinityData() {
        return affinityData;
    }

    public void setAffinityData(String affinityData) {
        this.affinityData = affinityData;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public RegionsData getRegionsData() {
        return regionsData;
    }

    public void setRegionsData(RegionsData regionsData) {
        this.regionsData = regionsData;
    }
}

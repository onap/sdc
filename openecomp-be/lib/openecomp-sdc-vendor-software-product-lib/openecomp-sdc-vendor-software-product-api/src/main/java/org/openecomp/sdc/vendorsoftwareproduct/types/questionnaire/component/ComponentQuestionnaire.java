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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component;


import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.Compute;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.general.General;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.highAvailability.HighAvailabilityAndLoadBalancing;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.network.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.storage.Storage;

/**
 * Created by TALIO on 11/21/2016
 */
public class ComponentQuestionnaire {
    private General general;
    private Compute compute;
    private HighAvailabilityAndLoadBalancing highAvailabilityAndLoadBalancing;
    private Network network;
    private Storage storage;

    public General getGeneral() {
        return general;
    }

    public void setGeneral(General general) {
        this.general = general;
    }

    public Compute getCompute() {
        return compute;
    }

    public void setCompute(Compute compute) {
        this.compute = compute;
    }

    public HighAvailabilityAndLoadBalancing getHighAvailabilityAndLoadBalancing() {
        return highAvailabilityAndLoadBalancing;
    }

    public void setHighAvailabilityAndLoadBalancing(HighAvailabilityAndLoadBalancing highAvailabilityAndLoadBalancing) {
        this.highAvailabilityAndLoadBalancing = highAvailabilityAndLoadBalancing;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}

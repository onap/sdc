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
package org.openecomp.sdc.be.components.distribution.engine;

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

/**
 * a pojo which holds all the necessary data to communicate with the message bus
 * this class is a reflection ot the {@link OperationalEnvironmentEntry} class
 */
public class EnvironmentMessageBusData {

    private List<String> dmaaPuebEndpoints;
    private String uebPublicKey;
    private String uebPrivateKey;
    private String envId;
    private String tenant;

    public EnvironmentMessageBusData() {
    }

    public EnvironmentMessageBusData(OperationalEnvironmentEntry operationalEnvironment) {
        this.dmaaPuebEndpoints = new ArrayList<>(operationalEnvironment.getDmaapUebAddress());
        this.uebPublicKey = operationalEnvironment.getUebApikey();
        this.uebPrivateKey = operationalEnvironment.getUebSecretKey();
        this.envId = operationalEnvironment.getEnvironmentId();
        this.tenant = operationalEnvironment.getTenant();
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public List<String> getDmaaPuebEndpoints() {
        return dmaaPuebEndpoints;
    }

    public void setDmaaPuebEndpoints(List<String> dmaaPuebEndpoints) {
        this.dmaaPuebEndpoints = dmaaPuebEndpoints;
    }

    public String getUebPublicKey() {
        return uebPublicKey;
    }

    public void setUebPublicKey(String uebPublicKey) {
        this.uebPublicKey = uebPublicKey;
    }

    public String getUebPrivateKey() {
        return uebPrivateKey;
    }

    public void setUebPrivateKey(String uebPrivateKey) {
        this.uebPrivateKey = uebPrivateKey;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }
}

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

import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

/**
 * a pojo which holds all the necessary data to communicate with the message bus
 * this class is a reflection ot the {@link OperationalEnvironmentEntry} class
 */
public class EnvironmentMessageBusData {

    private String envId;
    private String tenant;

    public EnvironmentMessageBusData() {
    }

    public EnvironmentMessageBusData(OperationalEnvironmentEntry operationalEnvironment) {
        this.envId = operationalEnvironment.getEnvironmentId();
        this.tenant = operationalEnvironment.getTenant();
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }
}

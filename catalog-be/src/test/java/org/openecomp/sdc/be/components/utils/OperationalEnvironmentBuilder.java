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

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.Set;

public class OperationalEnvironmentBuilder {

    private OperationalEnvironmentEntry operationalEnvironmentEntry;

    public OperationalEnvironmentBuilder() {
        operationalEnvironmentEntry = new OperationalEnvironmentEntry();
    }

    public OperationalEnvironmentBuilder setEnvId(String envId) {
        operationalEnvironmentEntry.setEnvironmentId(envId);
        return this;
    }

    public OperationalEnvironmentBuilder setDmaapUebAddress(Set<String> addresses) {
        operationalEnvironmentEntry.setDmaapUebAddress(addresses);
        return this;
    }

    public OperationalEnvironmentBuilder setStatus(EnvironmentStatusEnum status) {
        operationalEnvironmentEntry.setStatus(status);
        return this;
    }

    public OperationalEnvironmentEntry build() {
        return operationalEnvironmentEntry;
    }

}

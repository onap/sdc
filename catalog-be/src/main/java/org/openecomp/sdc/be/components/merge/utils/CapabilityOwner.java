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
package org.openecomp.sdc.be.components.merge.utils;

import static java.util.Collections.emptyMap;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.model.CapabilityDefinition;

public class CapabilityOwner {

    private String uniqueId;
    private String name;
    private Map<String, List<CapabilityDefinition>> capabilities;

    public CapabilityOwner(String uniqueId, String name, Map<String, List<CapabilityDefinition>> capabilities) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.capabilities = capabilities;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public Map<String, List<CapabilityDefinition>> getCapabilities() {
        return capabilities == null ? emptyMap() : capabilities;
    }
}

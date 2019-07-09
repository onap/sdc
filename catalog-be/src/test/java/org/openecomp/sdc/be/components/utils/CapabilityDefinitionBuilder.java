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

import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;

import java.util.ArrayList;

public class CapabilityDefinitionBuilder {

    private CapabilityDefinition capabilityDefinition;

    public CapabilityDefinitionBuilder() {
        capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setProperties(new ArrayList<>());
    }

    public CapabilityDefinitionBuilder addProperty(ComponentInstanceProperty property) {
        capabilityDefinition.getProperties().add(property);
        return this;
    }

    public CapabilityDefinitionBuilder setOwnerId(String ownerId) {
        capabilityDefinition.setOwnerId(ownerId);
        return this;
    }

    public CapabilityDefinitionBuilder setOwnerName(String ownerName) {
        capabilityDefinition.setOwnerName(ownerName);
        return this;
    }

    public CapabilityDefinitionBuilder setType(String type) {
        capabilityDefinition.setType(type);
        return this;
    }

    public CapabilityDefinitionBuilder setId(String ownerId) {
        capabilityDefinition.setUniqueId(ownerId);
        return this;
    }

    public CapabilityDefinitionBuilder setName(String name) {
        capabilityDefinition.setName(name);
        return this;
    }


    public CapabilityDefinition build() {
        return capabilityDefinition;
    }
}

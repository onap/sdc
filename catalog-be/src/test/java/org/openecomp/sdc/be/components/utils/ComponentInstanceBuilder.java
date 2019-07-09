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

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class ComponentInstanceBuilder {

    private ComponentInstance componentInstance;

    public ComponentInstanceBuilder() {
        componentInstance = new ComponentInstance();
        componentInstance.setCapabilities(new HashMap<>());
        componentInstance.setDeploymentArtifacts(new HashMap<>());
    }

    public ComponentInstanceBuilder(ComponentInstance componentInstance) {
        this.componentInstance = componentInstance;
    }

    public ComponentInstanceBuilder setName(String name) {
        componentInstance.setName(name);
        return this;
    }

    public ComponentInstanceBuilder setNormalizedName(String name) {
        componentInstance.setNormalizedName(name);
        return this;
    }

    public ComponentInstanceBuilder setUniqueId(String uniqueId) {
        componentInstance.setUniqueId(uniqueId);
        return this;
    }

    public ComponentInstanceBuilder setComponentUid(String componentUid) {
        componentInstance.setComponentUid(componentUid);
        return this;
    }

    public ComponentInstanceBuilder setId(String id) {
        componentInstance.setUniqueId(id);
        return this;
    }

    public ComponentInstanceBuilder setToscaName(String toscaName) {
        componentInstance.setToscaComponentName(toscaName);
        return this;
    }

    public ComponentInstanceBuilder addDeploymentArtifact(ArtifactDefinition artifactDefinition) {
        componentInstance.getDeploymentArtifacts().put(artifactDefinition.getArtifactName(), artifactDefinition);
        return this;
    }

    public ComponentInstanceBuilder addCapability(CapabilityDefinition capabilityDefinition) {
        componentInstance.getCapabilities().computeIfAbsent(capabilityDefinition.getType(), key -> new ArrayList<>()).add(capabilityDefinition);
        return this;
    }

    public ComponentInstanceBuilder addCapabilities(CapabilityDefinition ... capabilities) {
        Stream.of(capabilities).forEach(this::addCapability);
        return this;
    }

    public ComponentInstance build() {
        return componentInstance;
    }
}

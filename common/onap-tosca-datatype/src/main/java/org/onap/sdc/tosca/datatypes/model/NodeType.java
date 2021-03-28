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
package org.onap.sdc.tosca.datatypes.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;

@Getter
@Setter
@NoArgsConstructor
public class NodeType implements Cloneable {

    private String derived_from;
    private String version;
    private Map<String, String> metadata;
    private String description;
    private Map<String, PropertyDefinition> properties;
    private Map<String, AttributeDefinition> attributes;
    private List<Map<String, RequirementDefinition>> requirements;
    private Map<String, CapabilityDefinition> capabilities;
    private Map<String, Object> interfaces;
    private Map<String, ArtifactDefinition> artifacts;

    public Map<String, InterfaceDefinitionType> getNormalizeInterfaces() {
        if (MapUtils.isEmpty(interfaces)) {
            return new HashMap<>();
        }
        Map<String, InterfaceDefinitionType> normativeInterfaceDefinition = new HashMap<>();
        for (Map.Entry<String, Object> interfaceEntry : interfaces.entrySet()) {
            InterfaceDefinitionType interfaceDefinitionType = new InterfaceDefinitionType(interfaceEntry.getValue());
            normativeInterfaceDefinition.put(interfaceEntry.getKey(), interfaceDefinitionType);
        }
        return normativeInterfaceDefinition;
    }

    @Override
    public NodeType clone() {
        NodeType clone = new NodeType();
        clone.setCapabilities(this.getCapabilities());
        clone.setDerived_from(this.getDerived_from());
        clone.setProperties(this.getProperties());
        clone.setRequirements(this.getRequirements());
        clone.setDescription(this.getDescription());
        clone.setAttributes(this.getAttributes());
        clone.setInterfaces(this.getInterfaces());
        clone.setVersion(this.getVersion());
        clone.setArtifacts(this.getArtifacts());
        return clone;
    }
}

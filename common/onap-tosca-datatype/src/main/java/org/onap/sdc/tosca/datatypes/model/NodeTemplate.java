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
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.onap.sdc.tosca.services.YamlUtil;


public class NodeTemplate implements Template, Cloneable {

    private String type;
    private String description;
    private Map<String, String> metadata;
    private List<String> directives;
    private Map<String, Object> properties;
    private Map<String, Object> attributes;
    private List<Map<String, RequirementAssignment>> requirements;
    private Map<String, CapabilityAssignment> capabilities;
    private Map<String, Object> interfaces;
    private Map<String, ArtifactDefinition> artifacts;
    private NodeFilter node_filter;
    private String copy;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public List<String> getDirectives() {
        return directives;
    }

    public void setDirectives(List<String> directives) {
        this.directives = directives;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public List<Map<String, RequirementAssignment>> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Map<String, RequirementAssignment>> requirements) {
        this.requirements = requirements;
    }

    public Map<String, CapabilityAssignment> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, CapabilityAssignment> capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, Object> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Map<String, Object> interfaces) {
        this.interfaces = interfaces;
    }

    public Map<String, InterfaceDefinitionTemplate> getNormalizeInterfaces() {
        if (MapUtils.isEmpty(interfaces)) {
            return new HashMap<>();
        }
        Map<String, InterfaceDefinitionTemplate> normativeInterfaceDefinition = new HashMap<>();
        for (Map.Entry<String, Object> interfaceEntry : interfaces.entrySet()) {
            InterfaceDefinitionTemplate interfaceDefinitionTemplate =
                    new InterfaceDefinitionTemplate(interfaceEntry.getValue());
            normativeInterfaceDefinition.put(interfaceEntry.getKey(), interfaceDefinitionTemplate);
        }
        return normativeInterfaceDefinition;
    }

    public void addInterface(String interfaceKey, InterfaceDefinitionTemplate interfaceDefinitionTemplate) {
        if (MapUtils.isEmpty(this.interfaces)) {
            this.interfaces = new HashMap<>();
        }

        Optional<Object> toscaInterfaceObj = interfaceDefinitionTemplate.convertInterfaceDefTemplateToToscaObj();
        if (!toscaInterfaceObj.isPresent()) {
            throw new ToscaRuntimeException("Illegal Statement");
        }
        if (this.interfaces.containsKey(interfaceKey)) {
            this.interfaces.remove(interfaceKey);
        }
        this.interfaces.put(interfaceKey, toscaInterfaceObj.get());

    }

    public Map<String, ArtifactDefinition> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, ArtifactDefinition> artifacts) {
        this.artifacts = artifacts;
    }

    public NodeFilter getNode_filter() {
        return node_filter;
    }

    public void setNode_filter(NodeFilter nodeFilter) {
        this.node_filter = nodeFilter;
    }

    public String getCopy() {
        return copy;
    }

    public void setCopy(String copy) {
        this.copy = copy;
    }

    @Override
    public NodeTemplate clone() {
        YamlUtil yamlUtil = new YamlUtil();
        return yamlUtil.yamlToObject(yamlUtil.objectToYaml(this), NodeTemplate.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeTemplate)) {
            return false;
        }
        NodeTemplate that = (NodeTemplate) o;
        return Objects.equals(type, that.type)
            && Objects.equals(description, that.description)
            && Objects.equals(metadata, that.metadata)
            && Objects.equals(directives, that.directives)
            && Objects.equals(properties, that.properties)
            && Objects.equals(attributes, that.attributes)
            && Objects.equals(requirements, that.requirements)
            && Objects.equals(capabilities, that.capabilities)
            && Objects.equals(interfaces, that.interfaces)
            && Objects.equals(artifacts, that.artifacts)
            && Objects.equals(node_filter, that.node_filter)
            && Objects.equals(copy, that.copy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, description, metadata, directives, properties, attributes, requirements, capabilities,
                interfaces, artifacts, node_filter, copy);
    }
}

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

package org.openecomp.sdc.be.tosca.model;

import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class ToscaNodeTemplate {

    private String type;
    private List<String> directives;
    private ToscaMetadata metadata;
    private String description;
    private Map<String, Object> properties;
    private List<Map<String, ToscaTemplateRequirement>> requirements;
    private Map<String, ToscaTemplateCapability> capabilities;
    private Map<String, ToscaTemplateArtifact> artifacts;
    private NodeFilter node_filter;
    private Map<String, Object> interfaces;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public List<Map<String, ToscaTemplateRequirement>> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<Map<String, ToscaTemplateRequirement>> requirements) {
        this.requirements = requirements;
    }

    public Map<String, ToscaTemplateCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, ToscaTemplateCapability> capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, ToscaTemplateArtifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, ToscaTemplateArtifact> artifacts) {
        this.artifacts = artifacts;
    }

    public ToscaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ToscaMetadata metadata) {
        this.metadata = metadata;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDirectives() {
        return directives;
    }

    public void setDirectives(List<String> directives) {
        if (CollectionUtils.isEmpty(directives)) {
            this.directives = null;
            return;
        }
        this.directives = directives;
    }

    public NodeFilter getNode_filter() {
        return node_filter;
    }

    public void setNode_filter(NodeFilter node_filter) {
        this.node_filter = node_filter;
    }

    public void setInterfaces(
            Map<String, Object> interfaces) {
        this.interfaces = interfaces;
    }

    public void addInterface(String interfaceName, Object interfaceDataDefinition) {
        if (MapUtils.isEmpty(this.interfaces)) {
            this.interfaces = new HashMap<>();
        }

        this.interfaces.put(interfaceName, interfaceDataDefinition);
    }
}

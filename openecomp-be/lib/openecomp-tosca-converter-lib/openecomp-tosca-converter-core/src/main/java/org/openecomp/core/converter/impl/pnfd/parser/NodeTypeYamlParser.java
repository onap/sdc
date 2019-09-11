/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter.impl.pnfd.parser;

import java.util.List;
import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;

/**
 * Handles YAML from/to {@link NodeType} conversions
 */
public class NodeTypeYamlParser {

    private NodeTypeYamlParser() {
    }

    /**
     * Parses the given a YAML object to a {@link NodeType} instance.
     * @param nodeTypeYaml the YAML object representing a TOSCA Node Type
     * @return A new instance of {@link NodeType}.
     */
    public static NodeType parse(final Map<String, Object> nodeTypeYaml) {
        final NodeType nodeType = new NodeType();
        nodeType.setDerived_from((String) nodeTypeYaml.get(ToscaTagNamesEnum.DERIVED_FROM.getElementName()));
        nodeType.setDescription((String) nodeTypeYaml.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
        nodeType.setVersion((String) nodeTypeYaml.get("version"));
        nodeType.setProperties(
            (Map<String, PropertyDefinition>) nodeTypeYaml.get(ToscaTagNamesEnum.PROPERTIES.getElementName()));
        nodeType.setArtifacts((Map<String, ArtifactDefinition>) nodeTypeYaml.get("artifacts"));
        nodeType.setMetadata((Map<String, String>) nodeTypeYaml.get("metadata"));
        nodeType.setInterfaces(
            (Map<String, Object>) nodeTypeYaml.get(ToscaTagNamesEnum.INTERFACES.getElementName()));
        nodeType.setRequirements(
            (List<Map<String, RequirementDefinition>>) nodeTypeYaml.get(ToscaTagNamesEnum.REQUIREMENTS.getElementName()));
        nodeType.setCapabilities(
            (Map<String, CapabilityDefinition>) nodeTypeYaml.get(ToscaTagNamesEnum.CAPABILITIES.getElementName()));
        nodeType.setAttributes(
            (Map<String, AttributeDefinition>) nodeTypeYaml.get(ToscaTagNamesEnum.ATTRIBUTES.getElementName()));

        return nodeType;
    }

}

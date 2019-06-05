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
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.error.ToscaRuntimeException;
import org.onap.sdc.tosca.services.YamlUtil;


@Getter
@Setter
@EqualsAndHashCode
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


    @Override
    public NodeTemplate clone() {
        YamlUtil yamlUtil = new YamlUtil();
        return yamlUtil.yamlToObject(yamlUtil.objectToYaml(this), NodeTemplate.class);
    }


}

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.onap.sdc.tosca.datatypes.model;

import static org.onap.sdc.tosca.services.DataModelConvertUtil.convertToscaImports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.heatextend.AnnotationType;
import org.onap.sdc.tosca.error.ToscaRuntimeException;

@Getter
@Setter
@NoArgsConstructor
public class ServiceTemplate implements Template {

    private String tosca_definitions_version;
    private Map<String, String> metadata;
    private Object dsl_definitions;
    private Map<String, Repository> repositories;
    private String description;
    private List<Map<String, Import>> imports;
    private Map<String, ArtifactType> artifact_types;
    private Map<String, DataType> data_types;
    private Map<String, CapabilityType> capability_types;
    private Map<String, Object> interface_types;
    private Map<String, RelationshipType> relationship_types;
    private Map<String, NodeType> node_types;
    private Map<String, GroupType> group_types;
    private Map<String, PolicyType> policy_types;
    private Map<String, AnnotationType> annotation_types;
    private TopologyTemplate topology_template;

    public List getImports() {
        return imports;
    }

    public void setImports(List imports) {
        this.imports = convertToscaImports(imports);
    }

    public Map<String, InterfaceType> getNormalizeInterfaceTypes() {
        if (MapUtils.isEmpty(interface_types)) {
            return new HashMap<>();
        }
        Map<String, InterfaceType> normativeInterfaceTypes = new HashMap<>();
        for (Map.Entry<String, Object> interfaceEntry : interface_types.entrySet()) {
            InterfaceType interfaceType = new InterfaceType(interfaceEntry.getValue());
            normativeInterfaceTypes.put(interfaceEntry.getKey(), interfaceType);
        }
        return normativeInterfaceTypes;
    }

    public void addInterfaceType(String interfaceKey, InterfaceType interfaceType) {
        if (MapUtils.isEmpty(this.interface_types)) {
            this.interface_types = new HashMap<>();
        }
        Optional<Object> toscaInterfaceObj = interfaceType.convertInterfaceTypeToToscaObj();
        if (!toscaInterfaceObj.isPresent()) {
            throw new ToscaRuntimeException("Illegal Statement");
        }
        if (this.interface_types.containsKey(interfaceKey)) {
            this.interface_types.remove(interfaceKey);
        }
        this.interface_types.put(interfaceKey, toscaInterfaceObj.get());
    }

}

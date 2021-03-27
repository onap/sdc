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
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.heatextend.AnnotationType;
import org.onap.sdc.tosca.error.ToscaRuntimeException;

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

    public String getTosca_definitions_version() {
        return tosca_definitions_version;
    }

    public void setTosca_definitions_version(String toscaDefinitionsVersion) {
        this.tosca_definitions_version = toscaDefinitionsVersion;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Object getDsl_definitions() {
        return dsl_definitions;
    }

    public void setDsl_definitions(Object dslDefinitions) {
        this.dsl_definitions = dslDefinitions;
    }

    public Map<String, Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(Map<String, Repository> repositories) {
        this.repositories = repositories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List getImports() {
        return imports;
    }

    public void setImports(List imports) {
        this.imports = convertToscaImports(imports);
    }

    public Map<String, ArtifactType> getArtifact_types() {
        return artifact_types;
    }

    public void setArtifact_types(Map<String, ArtifactType> artifactTypes) {
        this.artifact_types = artifactTypes;
    }

    public Map<String, DataType> getData_types() {
        return data_types;
    }

    public void setData_types(Map<String, DataType> dataTypes) {
        this.data_types = dataTypes;
    }

    public Map<String, CapabilityType> getCapability_types() {
        return capability_types;
    }

    public void setCapability_types(Map<String, CapabilityType> capabilityTypes) {
        this.capability_types = capabilityTypes;
    }

    public Map<String, RelationshipType> getRelationship_types() {
        return relationship_types;
    }

    public void setRelationship_types(Map<String, RelationshipType> relationshipTypes) {
        this.relationship_types = relationshipTypes;
    }

    public Map<String, NodeType> getNode_types() {
        return node_types;
    }

    public void setNode_types(Map<String, NodeType> nodeTypes) {
        this.node_types = nodeTypes;
    }

    public Map<String, GroupType> getGroup_types() {
        return group_types;
    }

    public void setGroup_types(Map<String, GroupType> groupTypes) {
        this.group_types = groupTypes;
    }

    public Map<String, Object> getInterface_types() {
        return interface_types;
    }

    public void setInterface_types(Map<String, Object> interfaceTypes) {
        this.interface_types = interfaceTypes;
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

    public Map<String, PolicyType> getPolicy_types() {
        return policy_types;
    }

    public void setPolicy_types(Map<String, PolicyType> policyTypes) {
        this.policy_types = policyTypes;
    }

    public TopologyTemplate getTopology_template() {
        return topology_template;
    }

    public void setTopology_template(TopologyTemplate topologyTemplate) {
        this.topology_template = topologyTemplate;
    }

    public Map<String, AnnotationType> getAnnotation_types() {
        return annotation_types;
    }

    public void setAnnotation_types(Map<String, AnnotationType> annotationTypes) {
        this.annotation_types = annotationTypes;
    }
}

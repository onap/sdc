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

package org.openecomp.sdc.be.model.jsonjanusgraph.datamodel;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ComponentInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CompositionDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapAttributesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapComponentInstanceExternalRefs;
import org.openecomp.sdc.be.datatypes.elements.MapGroupsDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapListRequirementDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MapPropertiesDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.MapInterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.enums.JsonConstantKeysEnum;

@Getter
@Setter
public class TopologyTemplate extends ToscaElement {

    public TopologyTemplate() {
        super(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE);
    }

    private Map<String, PropertyDataDefinition> inputs;
    private Map<String, AttributeDataDefinition> outputs;
    private Map<String, MapPropertiesDataDefinition> instInputs;
    private Map<String, MapAttributesDataDefinition> instOutputs;
    private Map<String, ? extends ToscaDataDefinition> heatParameters;
    private Map<String, MapAttributesDataDefinition> instAttributes;
    private Map<String, MapPropertiesDataDefinition> instProperties;
    private Map<String, GroupDataDefinition> groups;
    private Map<String, PolicyDataDefinition> policies;
    private Map<String, MapGroupsDataDefinition> instGroups;
    private Map<String, ArtifactDataDefinition> serviceApiArtifacts;
    private Map<String, ForwardingPathDataDefinition> forwardingPaths;
    private Map<String, CompositionDataDefinition> compositions;
    private Map<String, MapListCapabilityDataDefinition> calculatedCapabilities;
    private Map<String, MapListRequirementDataDefinition> calculatedRequirements;
    private Map<String, MapListCapabilityDataDefinition> fullfilledCapabilities;
    private Map<String, MapListRequirementDataDefinition> fullfilledRequirements;
    private Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties;
    private Map<String, MapArtifactDataDefinition> instDeploymentArtifacts;
    private Map<String, MapArtifactDataDefinition> instanceArtifacts;
    private Map<String, InterfaceDataDefinition> interfaces;
    private Map<String, MapInterfaceInstanceDataDefinition> instInterfaces;
    private Map<String, MapInterfaceDataDefinition> componentInstInterfaces;
    private Map<String, CINodeFilterDataDefinition> nodeFilterComponents;
    private Map<String, SubstitutionFilterDataDefinition> substitutionFilterDataDefinitionMap;

    //Component Instances External References (instanceId -> ExternalRefsMap)
    //-----------------------------------------------------------------------
    private Map<String, MapComponentInstanceExternalRefs> mapComponentInstancesExternalRefs;
    //-----------------------------------------------------------------------

    public void addInstInterface(String compId, MapInterfaceInstanceDataDefinition
        mapInterfaceInstanceDataDefinition) {
        if (MapUtils.isEmpty(this.instInterfaces)) {
            this.instInterfaces = new HashMap<>();
        }

        this.instInterfaces.put(compId, mapInterfaceInstanceDataDefinition);
    }

    public void addComponentInstanceInterfaceMap(String componentInstanceId, MapInterfaceDataDefinition
        mapInterfaceDataDefinition) {
        if (MapUtils.isEmpty(this.componentInstInterfaces)) {
            this.componentInstInterfaces = new HashMap<>();
        }

        this.componentInstInterfaces.put(componentInstanceId, mapInterfaceDataDefinition);
    }

    /**
     * Adds component instance to composition of topology template Note that component instance will be overrided in
     * case if the topology template already contains a component instance with the same name
     *
     * @param componentInstance
     */
    public void addComponentInstance(ComponentInstanceDataDefinition componentInstance) {
        if (getCompositions() == null) {
            compositions = new HashMap<>();
        }
        if (MapUtils.isEmpty(getCompositions())) {
            compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
        }
        if (MapUtils
            .isEmpty(getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances())) {
            getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(new HashMap<>());
        }
        getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances()
            .put(componentInstance.getUniqueId(), componentInstance);
    }

    /**
     * Returns map of component instances from composition
     *
     * @return
     */
    public Map<String, ComponentInstanceDataDefinition> getComponentInstances() {
        Map<String, ComponentInstanceDataDefinition> instances = null;
        if (getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null) {
            instances = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances();
        }
        return instances;
    }


    /**
     * Sets map of component instances to composition of topology template Note that component instances will be
     * overrided in case if the topology template already contains a component instances
     *
     * @param instances
     */
    public void setComponentInstances(Map<String, ComponentInstanceDataDefinition> instances) {
        if (getCompositions() == null) {
            compositions = new HashMap<>();
        }
        if (MapUtils.isEmpty(getCompositions())) {
            compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
        }
        getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(instances);
    }

    public Map<String, RelationshipInstDataDefinition> getRelations() {
        Map<String, RelationshipInstDataDefinition> relations = null;
        if (getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null) {
            relations = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getRelations();
        }
        return relations;
    }
}

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

package org.openecomp.sdc.be.model.jsontitan.datamodel;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.elements.MapCapabilityProperty;
import org.openecomp.sdc.be.datatypes.elements.MapListCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;
import org.openecomp.sdc.be.model.MapInterfaceDataDefinition;
import org.openecomp.sdc.be.model.MapInterfaceInstanceDataDefinition;
import org.openecomp.sdc.be.model.jsontitan.enums.JsonConstantKeysEnum;

import java.util.HashMap;
import java.util.Map;

public class TopologyTemplate extends ToscaElement{

    public TopologyTemplate() {
        super(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE);
    }
    private Map<String, PropertyDataDefinition> inputs;
    private Map<String, MapPropertiesDataDefinition> instInputs;
    private Map<String, ? extends ToscaDataDefinition> heatParameters;
    private Map<String, MapPropertiesDataDefinition> instAttributes;
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
    //Component Instances External References (instanceId -> ExternalRefsMap)
    //-----------------------------------------------------------------------
    private Map<String, MapComponentInstanceExternalRefs> mapComponentInstancesExternalRefs;
    
    public Map<String, MapComponentInstanceExternalRefs> getMapComponentInstancesExternalRefs() {
        return this.mapComponentInstancesExternalRefs;
    }
    
    public void setComponentInstancesExternalRefs(Map<String, MapComponentInstanceExternalRefs> mapComponentInstancesExternalRefs) {
        this.mapComponentInstancesExternalRefs = mapComponentInstancesExternalRefs;
    }
    //-----------------------------------------------------------------------

    public Map<String, InterfaceDataDefinition> getInterfaces() {
      return interfaces;
    }

    public void setInterfaces(Map<String, InterfaceDataDefinition> interfaces) {
      this.interfaces = interfaces;
    }

    public Map<String, MapInterfaceInstanceDataDefinition> getInstInterfaces() {
        return instInterfaces;
    }

    public void setInstInterfaces(
            Map<String, MapInterfaceInstanceDataDefinition> instInterfaces) {
        this.instInterfaces = instInterfaces;
    }

    public void addInstInterface(String compId, MapInterfaceInstanceDataDefinition
            mapInterfaceInstanceDataDefinition) {
        if(MapUtils.isEmpty(this.instInterfaces)) {
            this.instInterfaces = new HashMap<>();
        }

        this.instInterfaces.put(compId, mapInterfaceInstanceDataDefinition);
    }

    public Map<String, MapInterfaceDataDefinition> getComponentInstInterfaces() {
        return componentInstInterfaces;
    }

    public void setComponentInstInterfaces(
            Map<String, MapInterfaceDataDefinition> componentInstInterfaces) {
        this.componentInstInterfaces = componentInstInterfaces;
    }

    public void addComponentInstanceInterfaceMap(String componentInstanceId, MapInterfaceDataDefinition
            mapInterfaceDataDefinition) {
        if(MapUtils.isEmpty(this.componentInstInterfaces)) {
            this.componentInstInterfaces = new HashMap<>();
        }

        this.componentInstInterfaces.put(componentInstanceId, mapInterfaceDataDefinition);
    }


    public Map<String, PropertyDataDefinition> getInputs() {
        return inputs;
    }
    public void setInputs(Map<String, PropertyDataDefinition> inputs) {
        this.inputs = inputs;
    }
    public Map<String, MapPropertiesDataDefinition> getInstInputs() {
        return instInputs;
    }
    public void setInstInputs(Map<String, MapPropertiesDataDefinition> instInputs) {
        this.instInputs = instInputs;
    }
    public Map<String, ? extends ToscaDataDefinition> getHeatParameters() {
        return heatParameters;
    }
    public void setHeatParameters(Map<String, ? extends ToscaDataDefinition> heatParameters) {
        this.heatParameters = heatParameters;
    }
    public Map<String, MapPropertiesDataDefinition> getInstAttributes() {
        return instAttributes;
    }
    public void setInstAttributes(Map<String, MapPropertiesDataDefinition> instAttributes) {
        this.instAttributes = instAttributes;
    }
    public Map<String, MapPropertiesDataDefinition> getInstProperties() {
        return instProperties;
    }
    public void setInstProperties(Map<String, MapPropertiesDataDefinition> instProperties) {
        this.instProperties = instProperties;
    }
    public Map<String, GroupDataDefinition> getGroups() {
        return groups;
    }
    public void setGroups(Map<String, GroupDataDefinition> groups) {
        this.groups = groups;
    }
    public Map<String, PolicyDataDefinition> getPolicies() {
        return policies;
    }
    public void setPolicies(Map<String, PolicyDataDefinition> policies) {
        this.policies = policies;
    }
    public Map<String, MapGroupsDataDefinition> getInstGroups() {
        return instGroups;
    }
    public void setInstGroups(Map<String, MapGroupsDataDefinition> instGroups) {
        this.instGroups = instGroups;
    }
    public Map<String, ArtifactDataDefinition> getServiceApiArtifacts() {
        return serviceApiArtifacts;
    }
    public void setServiceApiArtifacts(Map<String, ArtifactDataDefinition> serviceApiArtifacts) {
        this.serviceApiArtifacts = serviceApiArtifacts;
    }
    public Map<String, CompositionDataDefinition> getCompositions() {
        return compositions;
    }
    public void setCompositions(Map<String, CompositionDataDefinition> compositions) {
        this.compositions = compositions;
    }
    public Map<String, MapListCapabilityDataDefinition> getCalculatedCapabilities() {
        return calculatedCapabilities;
    }
    public void setCalculatedCapabilities(Map<String, MapListCapabilityDataDefinition> calculatedCapabilities) {
        this.calculatedCapabilities = calculatedCapabilities;
    }
    public Map<String, MapListRequirementDataDefinition> getCalculatedRequirements() {
        return calculatedRequirements;
    }
    public void setCalculatedRequirements(Map<String, MapListRequirementDataDefinition> calculatedRequirements) {
        this.calculatedRequirements = calculatedRequirements;
    }
    public Map<String, MapListCapabilityDataDefinition> getFullfilledCapabilities() {
        return fullfilledCapabilities;
    }
    public void setFullfilledCapabilities(Map<String, MapListCapabilityDataDefinition> fullfilledCapabilities) {
        this.fullfilledCapabilities = fullfilledCapabilities;
    }
    public Map<String, MapListRequirementDataDefinition> getFullfilledRequirements() {
        return fullfilledRequirements;
    }
    public void setFullfilledRequirements(Map<String, MapListRequirementDataDefinition> fullfilledRequirements) {
        this.fullfilledRequirements = fullfilledRequirements;
    }

    public Map<String, MapArtifactDataDefinition> getInstDeploymentArtifacts() {
        return instDeploymentArtifacts;
    }
    public void setInstDeploymentArtifacts(Map<String, MapArtifactDataDefinition> instDeploymentArtifacts) {
        this.instDeploymentArtifacts = instDeploymentArtifacts;
    }

    public Map<String, MapCapabilityProperty> getCalculatedCapabilitiesProperties() {
        return calculatedCapabilitiesProperties;
    }
    public void setCalculatedCapabilitiesProperties(Map<String, MapCapabilityProperty> calculatedCapabilitiesProperties) {
        this.calculatedCapabilitiesProperties = calculatedCapabilitiesProperties;
    }

    public Map<String, MapArtifactDataDefinition> getInstanceArtifacts() {
        return instanceArtifacts;
    }
    public void setInstanceArtifacts(Map<String, MapArtifactDataDefinition> instanceArtifacts) {
        this.instanceArtifacts = instanceArtifacts;
    }

    public Map<String, ForwardingPathDataDefinition> getForwardingPaths() {
        return forwardingPaths;
    }

    public void setForwardingPaths(Map<String, ForwardingPathDataDefinition> forwardingPaths) {
        this.forwardingPaths = forwardingPaths;
    }

    public Map<String, CINodeFilterDataDefinition> getNodeFilterComponents() {
        return nodeFilterComponents;
    }

    public void setNodeFilterComponents(Map<String, CINodeFilterDataDefinition> nodeFilters) {
        this.nodeFilterComponents = nodeFilters;
    }

      /**
     * Adds component instance to composition of topology template
     * Note that component instance will be overrided in case if the topology template already contains a component instance with the same name
     * @param componentInstance
     */
    public void addComponentInstance(ComponentInstanceDataDefinition componentInstance){
        if(getCompositions() == null){
            compositions = new HashMap<>();
        }
        if(MapUtils.isEmpty(getCompositions())){
            compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
        }
        if(MapUtils.isEmpty(getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances())){
            getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(new HashMap<>());
        }
        getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances().put(componentInstance.getUniqueId(), componentInstance);
    }
    /**
     * Returns map of component instances from composition
     * @return
     */
    public Map<String, ComponentInstanceDataDefinition> getComponentInstances() {
        Map<String, ComponentInstanceDataDefinition> instances = null;
        if(getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null ){
            instances = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getComponentInstances();
        }
        return instances;
    }


    /**
     * Sets map of component instances to composition of topology template
     * Note that component instances will be overrided in case if the topology template already contains a component instances
     * @param instances
     */
    public void setComponentInstances(Map<String, ComponentInstanceDataDefinition> instances) {
        if(getCompositions() == null){
            compositions = new HashMap<>();
        }
        if(MapUtils.isEmpty(getCompositions())){
            compositions.put(JsonConstantKeysEnum.COMPOSITION.getValue(), new CompositionDataDefinition());
        }
        getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).setComponentInstances(instances);
    }
    public Map<String, RelationshipInstDataDefinition> getRelations() {
        Map<String, RelationshipInstDataDefinition> relations = null;
        if( getCompositions() != null && getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()) != null ){
            relations = getCompositions().get(JsonConstantKeysEnum.COMPOSITION.getValue()).getRelations();
        }
        return relations;
    }
}

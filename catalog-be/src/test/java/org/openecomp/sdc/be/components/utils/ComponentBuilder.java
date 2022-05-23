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

import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceOutput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ComponentBuilder<T extends Component, B extends ComponentBuilder<T, B>> {

    private T component;

    protected abstract T component();

    protected abstract ComponentBuilder<T, B> self();

    public ComponentBuilder() {
        this.component = component();
    }

    public ComponentBuilder(T component) {
        this.component = component;
    }

    public ComponentBuilder<T, B> setUniqueId(String id) {
        component.setUniqueId(id);
        return self();
    }

    public ComponentBuilder<T, B> setLifeCycleState(LifecycleStateEnum lifeCycleState) {
        component.setLifecycleState(lifeCycleState);
        return self();
    }

    public ComponentBuilder<T, B> setLastUpdaterUserId(String lastUpdater) {
        component.setLastUpdaterUserId(lastUpdater);
        return self();
    }

    public ComponentBuilder<T, B> setInvariantUUid(String invariantUUid) {
        component.setInvariantUUID(invariantUUid);
        return self();
    }

    public ComponentBuilder<T, B> setName(String name) {
        component.setName(name);
        return self();
    }

    public ComponentBuilder<T, B> setComponentType(ComponentTypeEnum type) {
        component.setComponentType(type);
        return self();
    }

    public ComponentBuilder<T, B> setSystemName(String systemName) {
        component.setSystemName(systemName);
        return self();
    }

    public ComponentBuilder<T, B> addComponentInstance(String instanceName) {
        ComponentInstance instance = new ComponentInstanceBuilder()
                .setUniqueId(instanceName)
                .setName(instanceName)
                .build();
        return addComponentInstance(instance);
    }
    
    public ComponentBuilder<T, B> addComponentInstance(String instanceName, String uniqueId) {
        ComponentInstance instance = new ComponentInstanceBuilder()
                .setUniqueId(uniqueId)
                .setName(instanceName)
                .build();
        return addComponentInstance(instance);
    }

    public ComponentBuilder<T, B> addComponentInstance(ComponentInstance componentInstance) {
        initInstances();
        component.getComponentInstances().add(componentInstance);
        return self();
    }

    private void initInstances() {
        if (component.getComponentInstances() == null) {
            component.setComponentInstances(new ArrayList<>());
        }
    }

    public ComponentBuilder<T, B> addInput(InputDefinition input) {
        if (component.getInputs() == null) {
            component.setInputs(new ArrayList<>());
        }
        component.getInputs().add(input);
        return self();
    }

    public ComponentBuilder<T, B> addInput(String inputName) {
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputName);
        this.addInput(inputDefinition);
        return self();
    }

    public ComponentBuilder<T, B> addOutput(final OutputDefinition output) {
        if (component.getOutputs() == null) {
            component.setOutputs(new ArrayList<>());
        }
        component.getOutputs().add(output);
        return self();
    }

    public ComponentBuilder<T, B> addOutput(final String outputName) {
        final OutputDefinition outputDefinition = new OutputDefinition();
        outputDefinition.setName(outputName);
        outputDefinition.setUniqueId(outputName);
        this.addOutput(outputDefinition);
        return self();
    }

    public ComponentBuilder<T, B> addInstanceProperty(String instanceId, ComponentInstanceProperty prop) {
        if (component.getComponentInstancesProperties() == null) {
            component.setComponentInstancesProperties(new HashMap<>());
        }
        component.getComponentInstancesProperties().computeIfAbsent(instanceId, key -> new ArrayList<>()).add(prop);
        return self();
    }

    public ComponentBuilder<T, B> addInstanceProperty(String instanceId, String propName) {
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(propName);
        componentInstanceProperty.setUniqueId(propName);
        this.addInstanceProperty(instanceId, componentInstanceProperty);
        return self();
    }

    public ComponentBuilder<T, B> addInstanceInput(String instanceId, ComponentInstanceInput prop) {
        if (component.getComponentInstancesInputs() == null) {
            component.setComponentInstancesInputs(new HashMap<>());
        }
        component.getComponentInstancesInputs().computeIfAbsent(instanceId, key -> new ArrayList<>()).add(prop);
        return self();
    }

    public ComponentBuilder<T, B> addInstanceInput(String instanceId, String propName) {
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setName(propName);
        componentInstanceInput.setUniqueId(propName);
        this.addInstanceInput(instanceId, componentInstanceInput);
        return self();
    }

    public ComponentBuilder<T, B> addInstanceInput(String instanceId, String propName, List<GetInputValueDataDefinition> getInputValues) {
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setName(propName);
        componentInstanceInput.setUniqueId(propName);
        componentInstanceInput.setGetInputValues(getInputValues);
        this.addInstanceInput(instanceId, componentInstanceInput);
        return self();
    }

    public void addInstanceAttribute(String instanceId, ComponentInstanceAttribute attribute) {
        Map<String, List<ComponentInstanceAttribute>> compInstAttribute = component.safeGetComponentInstancesAttributes();
        if (compInstAttribute == null || compInstAttribute.isEmpty()) {
            component.setComponentInstancesAttributes(new HashMap<>());
        }
        Map<String, List<ComponentInstanceAttribute>> instAttribute = component.safeGetComponentInstancesAttributes();
        instAttribute.computeIfAbsent(instanceId, key -> new ArrayList<>()).add(attribute);
        self();
    }

    public ComponentBuilder<T, B> addInstanceAttribute(String instanceId, String AttributeName) {
        ComponentInstanceAttribute componentInstanceAttribute = new ComponentInstanceAttribute();
        componentInstanceAttribute.setName(AttributeName);
        componentInstanceAttribute.setUniqueId(AttributeName);
        this.addInstanceAttribute(instanceId, componentInstanceAttribute);
        return self();
    }

    public void addInstanceOutput(String instanceId, ComponentInstanceOutput attribute) {
        if (component.getComponentInstancesOutputs() == null) {
            component.setComponentInstancesOutputs(new HashMap<>());
        }
        component.getComponentInstancesOutputs().computeIfAbsent(instanceId, key -> new ArrayList<>()).add(attribute);
        self();
    }


    public ComponentBuilder<T, B> addRelationship(RequirementCapabilityRelDef requirementCapabilityRelDef) {
        if (component.getComponentInstancesRelations() == null) {
            component.setComponentInstancesRelations(new ArrayList<>());
        }
        component.getComponentInstancesRelations().add(requirementCapabilityRelDef);
        return self();
    }

    public ComponentBuilder<T, B> addPolicy(PolicyDefinition policyDefinition) {
        if (component.getPolicies() == null) {
            component.setPolicies(new HashMap<>());
        }
        component.getPolicies().put(policyDefinition.getUniqueId(), policyDefinition);
        return self();
    }

    public ComponentBuilder<T, B> addPolicyProperty(String policyId, String propName){
        PolicyDefinition policyWithProp = PolicyDefinitionBuilder.create()
                .addProperty(propName)
                .setUniqueId(policyId)
                .build();
        return addPolicy(policyWithProp);
    }

    public ComponentBuilder<T, B> addGroup(GroupDefinition groupDefinition){
        initGroups();
        component.getGroups().add(groupDefinition);
        return self();
    }

    public ComponentBuilder<T, B> addGroupProperty(String groupId, String propName){
        GroupDefinition groupWithProp = GroupDefinitionBuilder.create()
                .addProperty(propName)
                .setUniqueId(groupId)
                .build();
        return addGroup(groupWithProp);
    }

    private void initGroups() {
        if(component.getGroups() == null){
            component.setGroups(new ArrayList<>());
        }
    }

    public ComponentBuilder<T, B> setPolicies(List<PolicyDefinition> policies) {
        component.setPolicies(MapUtil.toMap(policies, PolicyDefinition::getUniqueId));
        return self();
    }

    public ComponentBuilder<T, B> setGroups(List<GroupDefinition> groups) {
        component.setGroups(groups);
        return self();
    }

    public T build() {
        return component;
    }
}

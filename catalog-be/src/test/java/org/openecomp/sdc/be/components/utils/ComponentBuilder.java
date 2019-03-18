package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

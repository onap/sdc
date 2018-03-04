package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyDefinitionBuilder {

    private PolicyDefinition policyDefinition;

    private PolicyDefinitionBuilder() {
        this.policyDefinition = new PolicyDefinition();
    }

    public static PolicyDefinitionBuilder create() {
        return new PolicyDefinitionBuilder();
    }

    public PolicyDefinitionBuilder setName(String name) {
        policyDefinition.setName(name);
        return this;
    }

    public PolicyDefinitionBuilder setUniqueId(String uid) {
        policyDefinition.setUniqueId(uid);
        return this;
    }

    public PolicyDefinitionBuilder setProperties(PropertyDataDefinition ... props) {
        policyDefinition.setProperties(Arrays.asList(props));
        return this;
    }

    public PolicyDefinitionBuilder setTargets(Map<PolicyTargetType, List<String>> targets ){
        policyDefinition.setTargets(targets);
        return this;
    }

    public PolicyDefinitionBuilder addComponentInstanceTarget(String instId) {
        Map<PolicyTargetType, List<String>> targets = getTargets();
        targets.computeIfAbsent(PolicyTargetType.COMPONENT_INSTANCES, k -> new ArrayList<>())
               .add(instId);
        return this;
    }

    public PolicyDefinitionBuilder addGroupTarget(String groupId) {
        Map<PolicyTargetType, List<String>> targets = getTargets();
        targets.computeIfAbsent(PolicyTargetType.GROUPS, k -> new ArrayList<>()).add(groupId);
        return this;
    }

    public PolicyDefinition build() {
        return policyDefinition;
    }

    private Map<PolicyTargetType, List<String>> getTargets() {
        Map<PolicyTargetType, List<String>> targets = policyDefinition.getTargets();
        if (targets == null) {
            targets = new HashMap<>();
            policyDefinition.setTargets(targets);
        }
        return targets;
    }
}

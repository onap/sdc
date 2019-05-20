package org.openecomp.sdc.be.components.merge.policy;

import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.core.annotation.Order;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.ANY_ORDER_COMMAND;

@org.springframework.stereotype.Component
@Order(ANY_ORDER_COMMAND)
public class PoliciesMergeCommand implements ComponentsGlobalMergeCommand, VspComponentsMergeCommand {

    private static final Logger log = Logger.getLogger(PoliciesMergeCommand.class);
    private final ToscaOperationFacade toscaOperationFacade;
    private final ComponentsUtils componentsUtils;

    public PoliciesMergeCommand(ToscaOperationFacade toscaOperationFacade, ComponentsUtils componentsUtils) {
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        log.debug("#mergeComponents - merging user defined policies to current component {}", currentComponent.getUniqueId());
        if (isEmpty(prevComponent.getPolicies())) {
            return ActionStatus.OK;
        }
        Map<String, PolicyDefinition> policiesToMerge = resolvePoliciesForMerge(prevComponent, currentComponent);
        return associatePoliciesToComponent(currentComponent, policiesToMerge);
    }

    private ActionStatus associatePoliciesToComponent(Component currentComponent, Map<String, PolicyDefinition> policiesToMerge) {
        log.debug("#associatePoliciesToComponent - associating {} policies into component {}", policiesToMerge.size(), currentComponent.getUniqueId());
        currentComponent.setPolicies(policiesToMerge);
        StorageOperationStatus associateResult = toscaOperationFacade.associatePoliciesToComponent(currentComponent.getUniqueId(), new ArrayList<>(policiesToMerge.values()));
        return componentsUtils.convertFromStorageResponse(associateResult);
    }

    @Override
    public String description() {
        return "merge component policies";
    }

    private Map<String, PolicyDefinition> resolvePoliciesForMerge(Component prevComponent, Component currentComponent) {
        Map<String, PolicyDefinition> policies = prevComponent.getPolicies();
        policies.values().forEach(policy -> updatePolicyTargets(policy, prevComponent, currentComponent));
        return policies;
    }

    private void updatePolicyTargets(PolicyDefinition policy, Component prevComponent, Component currComponent) {
        log.debug("#updatePolicyTargets - updating policy {} targets for component {}", policy.getUniqueId(), currComponent.getUniqueId());
        if (isEmpty(policy.getTargets())) {
            return;
        }
        Map<PolicyTargetType, List<String>> targets =  buildPolicyTargetsMap(policy, prevComponent, currComponent);
        policy.setTargets(targets);
    }

    private Map<PolicyTargetType, List<String>> buildPolicyTargetsMap(PolicyDefinition policy, Component prevComponent, Component currComponent) {
        List<String> componentInstanceTargets = resolveNewComponentInstanceTargets(policy, prevComponent, currComponent);
        List<String> groupTargets = resolveNewGroupTargets(policy, prevComponent, currComponent);
        Map<PolicyTargetType, List<String>> targets = new HashMap<>();
        targets.put(PolicyTargetType.COMPONENT_INSTANCES, componentInstanceTargets);
        targets.put(PolicyTargetType.GROUPS, groupTargets);
        return targets;
    }

    private List<String> resolveNewComponentInstanceTargets(PolicyDefinition policy, Component prevComponent, Component currComponent) {
        List<String> prevCompInstanceTargets = policy.resolveComponentInstanceTargets();
        if (isEmpty(prevCompInstanceTargets)) {
            return emptyList();
        }
        return resolveInstanceTargetsByInstanceName(prevComponent, currComponent, prevCompInstanceTargets);
    }

    private List<String> resolveNewGroupTargets(PolicyDefinition policy, Component prevComponent, Component currComponent) {
        List<String> prevGroupTargets = policy.resolveGroupTargets();
        if (isEmpty(prevGroupTargets)) {
            return emptyList();
        }
        return resolveGroupTargetsByInvariantName(prevComponent, currComponent, prevGroupTargets);
    }

    private List<String> resolveInstanceTargetsByInstanceName(Component prevComponent, Component currComponent, List<String> prevCompInstanceTargets) {
        return prevCompInstanceTargets.stream()
                .map(prevInstId -> resolveNewInstId(prevComponent, currComponent, prevInstId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private List<String> resolveGroupTargetsByInvariantName(Component prevComponent, Component currComponent, List<String> prevGroupTargets) {
        return prevGroupTargets.stream()
                .map(prevGroupId -> resolveNewGroupId(prevComponent, currComponent, prevGroupId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<String> resolveNewInstId(Component prevCmpt, Component newCmpt, String prevInstanceId) {
        return prevCmpt.getComponentInstanceById(prevInstanceId)
                .map(ComponentInstance::getName)
                .flatMap(newCmpt::getComponentInstanceByName)
                .map(ComponentInstance::getUniqueId);
    }

    private Optional<String> resolveNewGroupId(Component prevCmpt, Component newCmpt, String prevGroupId) {
        return prevCmpt.getGroupById(prevGroupId)
                .map(GroupDefinition::getInvariantName)
                .flatMap(newCmpt::getGroupByInvariantName)
                .map(GroupDefinition::getUniqueId);
    }

}

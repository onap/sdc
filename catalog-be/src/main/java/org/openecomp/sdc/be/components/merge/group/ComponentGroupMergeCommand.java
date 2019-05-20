package org.openecomp.sdc.be.components.merge.group;

import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.MapUtils.isEmpty;
import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.FIRST_COMMAND;

@org.springframework.stereotype.Component
@Order(FIRST_COMMAND)//must run before policies merge command
public class ComponentGroupMergeCommand implements VspComponentsMergeCommand, ComponentsGlobalMergeCommand {

    private static final Logger log = Logger.getLogger(ComponentGroupMergeCommand.class);
    private final GroupsOperation groupsOperation;
    private final ComponentsUtils componentsUtils;

    public ComponentGroupMergeCommand(GroupsOperation groupsOperation, ComponentsUtils componentsUtils) {
        this.groupsOperation = groupsOperation;
        this.componentsUtils = componentsUtils;
    }

    @Override
    public String description() {
        return "merge group from old component to new component";
    }

    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        log.debug("#mergeComponents - merging user defined groups to component {}", currentComponent.getUniqueId());
        if (isEmpty(prevComponent.getGroups())) {
            return ActionStatus.OK;
        }
        List<GroupDefinition> prevUserDefinedGroups = getAllPreviouslyUserDefinedGroups(prevComponent, currentComponent);
        if (isEmpty(prevUserDefinedGroups)) {
            return ActionStatus.OK;
        }
        updateGroupsMembers(prevUserDefinedGroups, prevComponent, currentComponent);
        return associateGroupsToComponent(currentComponent, prevUserDefinedGroups);

    }

    private List<GroupDefinition> getAllPreviouslyUserDefinedGroups(Component prevComponent, Component currCmpt) {
        return prevComponent.getGroups()
                .stream()
                .filter(GroupDefinition::isUserDefined)
                .filter(group -> !currCmpt.containsGroupWithInvariantName(group.getInvariantName()))
                .collect(toList());
    }

    private void updateGroupsMembers(List<GroupDefinition> prevUserDefinedGroups, Component prevComponent, Component currentComponent) {
        log.debug("#updateGroupsMembers - updating groups member with members taken from component {}", currentComponent.getUniqueId());
        prevUserDefinedGroups.forEach(grp -> grp.setMembers(resolveNewGroupMembers(grp, prevComponent, currentComponent)));
    }

    private Map<String, String> resolveNewGroupMembers(GroupDefinition grp, Component prevComponent, Component currentComponent) {
        log.debug("#resolveNewGroupMembers - updating group member for group {}", grp.getUniqueId());
        Map<String, String> prevGroupMembers = grp.getMembers();
        if (isEmpty(prevGroupMembers)) {
            return null;
        }
        return findNewInstancesByPrevInstancesNames(prevComponent, currentComponent, prevGroupMembers);
    }

    private Map<String, String> findNewInstancesByPrevInstancesNames(Component prevComponent, Component currentComponent, Map<String, String> prevGroupMembers) {
        return prevGroupMembers.values()
                .stream()
                .map(prevComponent::getComponentInstanceById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(prevInstance -> currentComponent.getComponentInstanceByName(prevInstance.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(ComponentInstance::getName, ComponentInstance::getUniqueId));
    }

    private ActionStatus associateGroupsToComponent(Component currentComponent, List<GroupDefinition> prevUserDefinedGroups) {
        currentComponent.addGroups(prevUserDefinedGroups);
        return groupsOperation.addGroups(currentComponent, prevUserDefinedGroups)
                .either(addedGroups -> ActionStatus.OK,
                        componentsUtils::convertFromStorageResponse);
    }




}

package org.openecomp.sdc.be.components.merge.group;

import org.openecomp.sdc.be.components.merge.ComponentsGlobalMergeCommand;
import org.openecomp.sdc.be.components.merge.VspComponentsMergeCommand;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.merge.resource.ResourceDataMergeBusinessLogic.ANY_ORDER_COMMAND;

@org.springframework.stereotype.Component
@Order(ANY_ORDER_COMMAND)
public class GroupPropertiesMergeCommand implements VspComponentsMergeCommand, ComponentsGlobalMergeCommand {

    private final GroupsOperation groupsOperation;
    private final ComponentsUtils componentsUtils;
    private final DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    public GroupPropertiesMergeCommand(GroupsOperation groupsOperation, ComponentsUtils componentsUtils, DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic) {
        this.groupsOperation = groupsOperation;
        this.componentsUtils = componentsUtils;
        this.propertyValuesMergingBusinessLogic = propertyValuesMergingBusinessLogic;
    }

    @Override
    public String description() {
        return "merge groups user defined properties values";
    }

    /**
     * merge user defined group properties values from previous version into vsp defined groups in new version
     * @param prevComponent the old component, whose group properties need to be merged from
     * @param currentComponent the new component, whose group properties need to be merged into
     * old and new component inputs are needed in order to determine if a "get_input" property value should be merged
     * @return the status of the merge operation
     */
    @Override
    public ActionStatus mergeComponents(Component prevComponent, Component currentComponent) {
        List<GroupDefinition> groupsToUpdate = updateOldGrpsPropertiesValuesIntoNewVspGroupsProps(prevComponent, currentComponent);
        return updateGroups(currentComponent, groupsToUpdate);
    }

    private List<GroupDefinition> updateOldGrpsPropertiesValuesIntoNewVspGroupsProps(Component prevComponent, Component currentComponent) {
        List<GroupDefinition> prevGroups = prevComponent.getGroups();
        List<GroupDefinition> newGroups = currentComponent.getGroups();
        if (isEmpty(prevGroups) || isEmpty(newGroups)) {
            return emptyList();
        }
        return mergeGroupPropertiesValues(prevComponent, currentComponent, prevGroups, newGroups);
    }

    private List<GroupDefinition> mergeGroupPropertiesValues(Component prevComponent, Component currentComponent, List<GroupDefinition> prevGroups, List<GroupDefinition> newGroups) {
        Map<String, GroupDefinition> prevGroupsByInvariantName = getVspGroupsMappedByInvariantName(prevGroups);
        List<GroupDefinition> newGroupsExistInPrevVersion = getNewGroupsExistInPrevComponent(prevGroupsByInvariantName, newGroups);
        newGroupsExistInPrevVersion.forEach(newGroup -> {
            GroupDefinition prevGroup = prevGroupsByInvariantName.get(newGroup.getInvariantName());
            mergeGroupProperties(prevGroup, prevComponent.safeGetInputs(), newGroup, currentComponent.safeGetInputs());
        });
        return newGroupsExistInPrevVersion;
    }

    private void mergeGroupProperties(GroupDefinition prevGroup, List<InputDefinition> prevInputs, GroupDefinition newGroup, List<InputDefinition> currInputs) {
        propertyValuesMergingBusinessLogic.mergeInstanceDataDefinitions(prevGroup.getProperties(), prevInputs, newGroup.getProperties(), currInputs);
    }

    private List<GroupDefinition> getNewGroupsExistInPrevComponent(Map<String, GroupDefinition> prevGroupsByInvariantName, List<GroupDefinition> newGroups) {
        return newGroups.stream()
                .filter(newGroup -> prevGroupsByInvariantName.containsKey(newGroup.getInvariantName()))
                .filter(newGroup -> isNotEmpty(newGroup.getProperties()))
                .collect(toList());
    }

    private Map<String, GroupDefinition> getVspGroupsMappedByInvariantName(List<GroupDefinition> newGroups) {
        return newGroups.stream()
                .filter(GroupDataDefinition::isVspOriginated)
                .filter(grp -> isNotEmpty(grp.getProperties()))
                .collect(toMap(GroupDataDefinition::getInvariantName,
                               group -> group));
    }

    private ActionStatus updateGroups(Component currentComponent, List<GroupDefinition> groupsToUpdate) {
        if (isEmpty(groupsToUpdate)) {
            return ActionStatus.OK;
        }
        return groupsOperation.updateGroups(currentComponent, groupsToUpdate, false)
                .either(updatedGroups -> ActionStatus.OK,
                        err -> componentsUtils.convertFromStorageResponse(err, currentComponent.getComponentType()));
    }

}

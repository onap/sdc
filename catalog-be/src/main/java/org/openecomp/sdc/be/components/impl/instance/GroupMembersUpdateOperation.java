package org.openecomp.sdc.be.components.impl.instance;

import org.openecomp.sdc.be.components.impl.group.GroupMembersUpdater;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.List;
import java.util.function.Consumer;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
@org.springframework.stereotype.Component
public class GroupMembersUpdateOperation implements OnComponentInstanceChangeOperation {

    private static final Logger log = Logger.getLogger(GroupMembersUpdateOperation.class);
    private final GroupsOperation groupsOperation;
    private final ComponentsUtils componentsUtils;
    private final GroupMembersUpdater groupMembersUpdater;

    public GroupMembersUpdateOperation(GroupsOperation groupsOperation, ComponentsUtils componentsUtils, GroupMembersUpdater groupMembersUpdater) {
        this.groupsOperation = groupsOperation;
        this.componentsUtils = componentsUtils;
        this.groupMembersUpdater = groupMembersUpdater;
    }

    @Override
    public ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#onChangeVersion - replacing all group members for component instance {} with new component instance on component", prevVersion.getUniqueId(), newVersion.getUniqueId(), container.getUniqueId());
        Consumer<List<GroupDefinition>> replaceGroupMemberTask = (groups) -> groupMembersUpdater.replaceMember(groups, prevVersion.getUniqueId(), newVersion.getUniqueId());
        return doUpdateGroupMembers(container, prevVersion.getUniqueId(), replaceGroupMemberTask);
    }

    @Override
    public ActionStatus onDelete(Component container, String deletedEntityId) {
        log.debug("#onDelete - deleting group member referencing component instance {} on component {}.", deletedEntityId, container.getUniqueId());
        Consumer<List<GroupDefinition>> deleteGroupMemberTask = (groups) -> groupMembersUpdater.removeMember(groups, deletedEntityId);
        return doUpdateGroupMembers(container, deletedEntityId, deleteGroupMemberTask);
    }

    private ActionStatus doUpdateGroupMembers(Component container, String memberId, Consumer<List<GroupDefinition>> updateGroupMemberTask) {
        List<GroupDefinition> groupsWithPrevInstAsMember = container.resolveGroupsByMember(memberId);
        if (isEmpty(groupsWithPrevInstAsMember)) {
            log.debug("#doUpdateGroupMembers - container {} has no groups with component instance {} as member.", container.getUniqueId(), memberId);
            return ActionStatus.OK;
        }
        updateGroupMemberTask.accept(groupsWithPrevInstAsMember);
        return updateGroups(container, groupsWithPrevInstAsMember);
    }

    private ActionStatus updateGroups(Component container, List<GroupDefinition> groupsToUpdate) {
        log.debug("#updateGroups - updating {} groups for container {}", groupsToUpdate.size(), container.getUniqueId());
        return groupsOperation.updateGroups(container, groupsToUpdate, false)
                .either(groupsUpdated -> ActionStatus.OK,
                        err -> componentsUtils.convertFromStorageResponse(err, container.getComponentType()));
    }


}

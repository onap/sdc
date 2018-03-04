package org.openecomp.sdc.be.components.impl.version;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.jsontitan.operations.GroupsOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
@org.springframework.stereotype.Component
public class GroupMembersUpdateOperation implements PostChangeVersionOperation {

    private static final Logger log = LoggerFactory.getLogger(GroupMembersUpdateOperation.class);
    private final GroupsOperation groupsOperation;
    private final ComponentsUtils componentsUtils;

    public GroupMembersUpdateOperation(GroupsOperation groupsOperation, ComponentsUtils componentsUtils) {
        this.groupsOperation = groupsOperation;
        this.componentsUtils = componentsUtils;
    }

    @Override
    public ActionStatus onChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        return updateGroupMembersOnChangeVersion(container, prevVersion, newVersion);
    }

    private ActionStatus updateGroupMembersOnChangeVersion(Component container, ComponentInstance prevVersion, ComponentInstance newVersion) {
        log.debug("#updateGroupMembersOnChangeVersion - replacing all group members for component instance {} with new component instance.", prevVersion.getUniqueId(), newVersion.getUniqueId());
        if (isEmpty(container.getGroups())) {
            log.debug("#updateGroupMembersOnChangeVersion - container {} has no groups.", container.getUniqueId());
            return ActionStatus.OK;
        }
        List<GroupDefinition> groupsWithPrevInstAsMember = container.resolveGroupsByMember(prevVersion.getUniqueId());
        if (isEmpty(groupsWithPrevInstAsMember)) {
            log.debug("#updateGroupMembersOnChangeVersion - container {} has no groups with component instance {} as member.", container.getUniqueId(), prevVersion.getUniqueId());
            return ActionStatus.OK;
        }
        replacePrevInstanceMemberWithNewInstance(prevVersion, newVersion, groupsWithPrevInstAsMember);
        return updateGroups(container, groupsWithPrevInstAsMember);
    }

    private ActionStatus updateGroups(Component container, List<GroupDefinition> groupsToUpdate) {
        log.debug("#updateGroups - updating {} groups for container {}", groupsToUpdate.size(), container.getUniqueId());
        return groupsOperation.updateGroups(container, groupsToUpdate)
                .either(groupsUpdated -> ActionStatus.OK,
                        err -> componentsUtils.convertFromStorageResponse(err, container.getComponentType()));
    }

    private void replacePrevInstanceMemberWithNewInstance(ComponentInstance prevVersion, ComponentInstance newVersion, List<GroupDefinition> groupsWithPrevInstAsMember) {
        groupsWithPrevInstAsMember.forEach(grp -> replacePrevInstanceMemberWithNewInstance(grp, prevVersion.getUniqueId(), newVersion.getUniqueId()));
    }

    private void replacePrevInstanceMemberWithNewInstance(GroupDefinition groupDefinition, String prevInstanceId, String newInstanceId) {
        Map<String, String> membersNameToId = groupDefinition.getMembers();
        String prevInstanceMemberName = MapUtils.invertMap(membersNameToId).get(prevInstanceId).toString();
        membersNameToId.replace(prevInstanceMemberName, newInstanceId);
    }

}

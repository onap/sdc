package org.openecomp.sdc.be.components.impl.group;

import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.MapUtils.isEmpty;

/**
 * A Helper class which handles altering the members state of a group
 */
@Component
public class GroupMembersUpdater {

    public void removeMember(List<GroupDefinition> groups, String memberId) {
        groups.forEach(group -> removeGroupMember(group, memberId));
    }

    public void replaceMember(List<GroupDefinition> groups, String oldMemberId, String newMemberId) {
        groups.forEach(grp -> replaceMember(grp, oldMemberId, newMemberId));
    }

    private void removeGroupMember(GroupDefinition group, String memberId) {
        Map<String, String> membersNameToId = group.getMembers();
        String groupMemberKey = findGroupMemberKey(membersNameToId, memberId);
        if (groupMemberKey != null) {
            membersNameToId.remove(groupMemberKey);
        }
    }

    private void replaceMember(GroupDefinition group, String oldMemberId, String newMemberId) {
        Map<String, String> membersNameToId = group.getMembers();
        String groupMemberKey = findGroupMemberKey(membersNameToId, oldMemberId);
        if (groupMemberKey != null) {
            membersNameToId.replace(groupMemberKey, newMemberId);
        }
    }

    private String findGroupMemberKey(Map<String, String> members, String memberToFind) {
        if (isEmpty(members)) {
            return null;
        }
        Map invertedMap = MapUtils.invertMap(members);
        if (!invertedMap.containsKey(memberToFind)) {
            return null;
        }
        return invertedMap.get(memberToFind).toString();
    }




}

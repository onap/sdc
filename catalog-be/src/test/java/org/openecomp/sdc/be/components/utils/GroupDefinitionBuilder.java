package org.openecomp.sdc.be.components.utils;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.model.GroupDefinition;

public class GroupDefinitionBuilder {
    private GroupDefinition groupDefinition;

    private GroupDefinitionBuilder() {
        this.groupDefinition = new GroupDefinition();
    }

    public static GroupDefinitionBuilder create() {
        return new GroupDefinitionBuilder();
    }

    public GroupDefinitionBuilder setUniqueId(String uid) {
        groupDefinition.setUniqueId(uid);
        return this;
    }

    public GroupDefinition build() {
        return groupDefinition;
    }

    public GroupDefinitionBuilder addMember(String memberName, String memberId) {
        Map<String, String> members = groupDefinition.getMembers();
        if (members == null) {
            members = new HashMap<>();
            groupDefinition.setMembers(members);
        }
        members.put(memberId, memberId);
        return this;
    }
}


package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.CreatedFrom;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public GroupDefinitionBuilder setType(String type) {
        groupDefinition.setType(type);
        return this;
    }

    public GroupDefinition build() {
        return groupDefinition;
    }

    public GroupDefinitionBuilder addMember(String name, String memberId) {
        Map<String, String> groupMembers = getGroupMembers();
        groupMembers.put(name, memberId);
        return this;
    }

    public GroupDefinitionBuilder addMember(String memberId) {
        Map<String, String> members = getGroupMembers();
        members.put(memberId + "name", memberId);
        return this;
    }

    private Map<String, String> getGroupMembers() {
        Map<String, String> members = groupDefinition.getMembers();
        if (members == null) {
            members = new HashMap<>();
            groupDefinition.setMembers(members);
        }
        return members;
    }

    public GroupDefinitionBuilder setInvariantName(String name) {
        groupDefinition.setInvariantName(name);
        return this;
    }
       
    public GroupDefinitionBuilder setInvariantUUID(String invariantUUID) {
        groupDefinition.setInvariantUUID(invariantUUID);
        return this;
    }
    
    public GroupDefinitionBuilder setGroupUUID(String groupUUID) {
        groupDefinition.setGroupUUID(groupUUID);
        return this;
    }

    public GroupDefinitionBuilder setName(String name) {
        groupDefinition.setName(name);
        return this;
    }
    
    public GroupDefinitionBuilder setVersion(String version) {
        groupDefinition.setVersion(version);
        return this;
    }
    
    public GroupDefinitionBuilder setCreatedFrom(CreatedFrom createdfrom) {
        groupDefinition.setCreatedFrom(createdfrom);
        return this;
    }

    public GroupDefinitionBuilder addProperty(String propertyName) {
        List<PropertyDataDefinition> grpProps = getGroupProperties();
        PropertyDefinition prop = new PropertyDataDefinitionBuilder()
                .setName(propertyName)
                .build();
        grpProps.add(prop);
        return this;
    }

    private List<PropertyDataDefinition> getGroupProperties() {
        List<PropertyDataDefinition> grpProps = groupDefinition.getProperties();
        if (grpProps == null) {
            grpProps = new ArrayList<>();
            groupDefinition.setProperties(grpProps);
        }
        return grpProps;
    }
}


package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.GroupTypeDefinition;

public class GroupTypeBuilder {

    private GroupTypeDefinition groupTypeDataDefinition;

    public static GroupTypeBuilder create() {
        return new GroupTypeBuilder();
    }

    private GroupTypeBuilder() {
        this.groupTypeDataDefinition = new GroupTypeDefinition();
    }

    public GroupTypeBuilder setType(String type) {
        groupTypeDataDefinition.setType(type);
        return this;
    }

    public GroupTypeBuilder setUniqueId(String uid) {
        groupTypeDataDefinition.setUniqueId(uid);
        return this;
    }

    public GroupTypeBuilder setName(String name) {
        groupTypeDataDefinition.setName(name);
        return this;
    }

    public GroupTypeBuilder setIcon(String icon) {
        groupTypeDataDefinition.setIcon(icon);
        return this;
    }

    public GroupTypeBuilder setVersion(String version) {
        groupTypeDataDefinition.setVersion(version);
        return this;
    }

    public GroupTypeBuilder setDerivedFrom(String derivedFrom) {
        groupTypeDataDefinition.setDerivedFrom(derivedFrom);
        return this;
    }

    public GroupTypeDefinition build() {
        return groupTypeDataDefinition;
    }



}
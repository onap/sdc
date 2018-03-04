package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public enum PolicyTargetType {

    GROUPS("GROUPS"),
    COMPONENT_INSTANCES("COMPONENT_INSTANCES"),
    TYPE_DOES_NOT_EXIST("TYPE_DOES_NOT_EXIST");

    private String name;

    PolicyTargetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public static PolicyTargetType getByNameIgnoreCase(String name) {
        for (PolicyTargetType inst : PolicyTargetType.values()) {
            if (inst.getName().equalsIgnoreCase(name)) {
                return inst;
            }
        }
        return null;
    }
}


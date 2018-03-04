package org.openecomp.sdc.ci.tests.datatypes.enums;

import java.util.ArrayList;
import java.util.List;

public enum GroupPropertyEnum {

    IS_BASE("isBase"),
    MIN_VF_MODULE_INSTANCES("min_vf_module_instances"),
    MAX_VF_MODULE_INSTANCES("max_vf_module_instances"),
    VF_MODULE_LABEL("vf_module_label"),
    VFC_LIST("vfc_list"),
    VF_MODULE_TYPE("vf_module_type"),
    VF_MODULE_DESCRIPTION("vf_module_description"),
    INITIAL_COUNT("initial_count"),
    VOLUME_GROUP("volume_group"),
    AVAILABILITY_ZONE_COUNT("availability_zone_count");

    private String propertyName;

    GroupPropertyEnum(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public static List<String> getGroupPropertyNamesWithoutIsbase(){
        List<String> groupPropertyNames = new ArrayList<>();

        for(GroupPropertyEnum groupProperty : GroupPropertyEnum.values()) {
            if (!groupProperty.getPropertyName().equals(GroupPropertyEnum.IS_BASE)){
                groupPropertyNames.add(groupProperty.getPropertyName());
            }
        }
        return groupPropertyNames;
    }

    public static List<String> getGroupPropertyNames(){
        List<String> groupPropertyNames = GroupPropertyEnum.getGroupPropertyNamesWithoutIsbase();
        groupPropertyNames.add(GroupPropertyEnum.IS_BASE.getPropertyName());
        return groupPropertyNames;
    }

}

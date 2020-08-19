/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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

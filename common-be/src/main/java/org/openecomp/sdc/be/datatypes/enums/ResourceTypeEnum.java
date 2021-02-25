/*-
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 */

package org.openecomp.sdc.be.datatypes.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resource Type Enum
 *
 * @author mshitrit
 */
@Getter
@AllArgsConstructor
public enum ResourceTypeEnum {

    VFC("VFC"/* (Virtual Function Component)"*/, true),
    VF("VF"/* (Virtual Function)" */, false),
    CR("CR"/* (Complex Resource"*/, false),
    CP("CP"/* (Connection Point)"*/, true),
    PNF("PNF"/* (Physical Network Function)" */, false),
    CVFC("CVFC"/* Complex Virtual Function Component*/, false),
    VL("VL"/* (Virtual Link)"*/, true),
    VFCMT("VFCMT"/* (VFC Monitoring Template)"*/, true),
    Configuration("Configuration", true),
    ServiceProxy("ServiceProxy", true),
    //Generic VFC/VF/PNF/Service Type
    ABSTRACT("Abstract", true),
    SERVICE("Service"/*(Network Service)"*/, false);

    private final String value;
    private final boolean isAtomicType;

    public static ResourceTypeEnum getType(final String type) {
        if (type == null) {
            return null;
        }
        return Arrays.stream(ResourceTypeEnum.values())
            .filter(resourceTypeEnum -> resourceTypeEnum.name().equals(type))
            .findFirst()
            .orElse(null);
    }

    public static ResourceTypeEnum getTypeByName(final String type) {
        if (type == null) {
            return null;
        }
        return Arrays.stream(ResourceTypeEnum.values())
            .filter(resourceTypeEnum -> resourceTypeEnum.name().equalsIgnoreCase(type))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns ResourceTypeEnum matching to received String ignore case
     *
     * @param type the resource type
     * @return the resource type as a enum if found, {@code null} otherwise
     */
    public static ResourceTypeEnum getTypeIgnoreCase(final String type) {
        if (type == null) {
            return null;
        }
        return Arrays.stream(ResourceTypeEnum.values())
            .filter(resourceTypeEnum -> resourceTypeEnum.name().equalsIgnoreCase(type))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if enum exist with given type
     *
     * @param type the resource type
     * @return {@code true} if the given resource type exists, {@code false} otherwise
     */
    public static boolean containsName(final String type) {
        if (type == null) {
            return false;
        }
        return Arrays.stream(ResourceTypeEnum.values())
            .anyMatch(resourceTypeEnum -> resourceTypeEnum.name().equals(type));
    }

    /**
     * Checks if enum exist with given type ignore case
     *
     * @param type the resource type
     * @return {@code true} if the type exists, {@code false} otherwise
     */
    public static boolean containsIgnoreCase(final String type) {
        if (type == null) {
            return false;
        }
        return Arrays.stream(ResourceTypeEnum.values())
            .anyMatch(resourceTypeEnum -> resourceTypeEnum.name().equalsIgnoreCase(type));
    }

}

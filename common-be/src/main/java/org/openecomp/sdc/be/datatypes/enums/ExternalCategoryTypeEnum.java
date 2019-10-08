/*-
 * Copyright (C) 2019 Telstra Intellectual Property. All rights reserved.
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

/**
 * Category Type Enum
 * Any service category to be supported by SDC Ext API can be added here
 *
 * @author atifhusain
 */
public enum ExternalCategoryTypeEnum {

    PARTNER_DOMAIN_SERVICE("Partner Domain Service"/* (Partner Domain Service)"*/, true);

    private String value;
    private boolean isAtomicType;

    ExternalCategoryTypeEnum(String value, boolean isAtomicType) {
        this.value = value;
        this.isAtomicType = isAtomicType;
    }

    public String getValue() {
        return value;
    }

    public boolean isAtomicType() {
        return isAtomicType;
    }

    public static ExternalCategoryTypeEnum getType(String type) {
        for (ExternalCategoryTypeEnum e : ExternalCategoryTypeEnum.values()) {
            if (e.name().equals(type)) {
                return e;
            }
        }
        return null;
    }

    public static ExternalCategoryTypeEnum getTypeByName(String type) {
        for (ExternalCategoryTypeEnum e : ExternalCategoryTypeEnum.values()) {
            if (e.getValue().equalsIgnoreCase(type)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Returns CategoryTypeEnum matching to received String ignore case
     *
     * @param type
     * @return
     */
    public static ExternalCategoryTypeEnum getTypeIgnoreCase(String type) {
        for (ExternalCategoryTypeEnum e : ExternalCategoryTypeEnum.values()) {
            if (e.getValue().toLowerCase().equals(type.toLowerCase())) {
                return e;
            }
        }
        return null;
    }

    /**
     * Checks if enum exist with given type
     *
     * @param type
     * @return
     */
    public static boolean containsName(String type) {

        for (ExternalCategoryTypeEnum e : ExternalCategoryTypeEnum.values()) {
            if (e.getValue().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if enum exist with given type ignore case
     *
     * @param type
     * @return
     */
    public static boolean containsIgnoreCase(String type) {

        for (ExternalCategoryTypeEnum e : ExternalCategoryTypeEnum.values()) {
            if (e.getValue().toLowerCase().equals(type.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}


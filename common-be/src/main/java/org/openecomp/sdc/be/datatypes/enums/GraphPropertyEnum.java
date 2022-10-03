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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum GraphPropertyEnum {
    // @formatter:off
    //                              field name,             class type,            unique,        indexed
    UNIQUE_ID(              "uid",                  String.class,   true,   true),
    LABEL(                  "nodeLabel",            String.class,   false,  true),
    JSON(                   "json",                 String.class,   false,  false),
    METADATA(               "metadata",             String.class,   false,  false),
    VERSION(                "version",              String.class,   false,  true),
    STATE(                  "state",                String.class,   false,  true),
    IS_HIGHEST_VERSION(     "highestVersion",       Boolean.class,  false,  true),
    IS_DELETED(             "deleted",              Boolean.class,  false,  true),
    NORMALIZED_NAME(        "normalizedName",       String.class,   false,  true),
    NAME(                   "name",                 String.class,   false,  true),
    TOSCA_RESOURCE_NAME(    "toscaResourceName",    String.class,   false,  true),
    DISTRIBUTION_STATUS(    "distributionStatus",   String.class,   false,  false),
    RESOURCE_TYPE(          "resourceType",         String.class,   false,  true),
    COMPONENT_TYPE(         "componentType",        String.class,   false,  true),
    UUID(                   "uuid",                 String.class,   false,  true),
    SYSTEM_NAME(            "systemName",           String.class,   false,  true),
    IS_ABSTRACT(            "abstract",             Boolean.class,  false,  true),
    INVARIANT_UUID(         "invariantUuid",        String.class,   false,  true),
    CSAR_UUID(              "csarUuid",             String.class,   false,  true),
    CSAR_VERSION_UUID(      "csarVersionUuid",      String.class,   false,  true),
    //used for user (old format, no json for users)
    USERID(                 "userId",               String.class,   true,   true),
    ROLE(                   "role",                 String.class,   false,  false),
    FIRST_NAME(             "firstName",            String.class,   false,  false),
    LAST_NAME(              "lastName",             String.class,   false,  false),
    EMAIL(                  "email",                String.class,   false,  false),
    LAST_LOGIN_TIME(        "lastLoginTime",        Long.class,     false,  false),
    //used for category (old format, no json for categories)
    ICONS(                  "icons",                String.class,   false,  false),
    METADATA_KEYS(          "metadataKeys",         String.class,   false,  false),
    USE_SUBSTITUTION_FOR_NESTED_SERVICES("useServiceSubstitutionForNestedServices",Boolean.class,false,false),
    DATA_TYPES(             "data_types",           Map.class,      false,  false),
    //Archive/Restore
    IS_ARCHIVED(            "isArchived",           Boolean.class,  false,  true),
    IS_VSP_ARCHIVED(        "isVspArchived",        Boolean.class,  false,  true),
    ARCHIVE_TIME(           "archiveTime",          Long.class,     false,  true),
    PREV_CATALOG_UPDATE_TIME("previousUpdateTime",  Long.class,     false,  true),
    CURRENT_CATALOG_UPDATE_TIME("currentUpdateTime",Long.class,     false,  true),
    //Healing
    HEALING_VERSION(        "healVersion",          Integer.class,  false,  true),
    MODEL(                  "model",                String.class,   false,  false),
    MODEL_TYPE(             "modelType",            String.class,   false,  false);
    // @formatter:on

    private final String property;
    private final Class<?> clazz;
    private final boolean unique;
    private final boolean indexed;

    public static GraphPropertyEnum getByProperty(String property) {
        for (GraphPropertyEnum currProperty : GraphPropertyEnum.values()) {
            if (currProperty.getProperty().equals(property)) {
                return currProperty;
            }
        }
        return null;
    }

    public static List<String> getAllProperties() {
        List<String> arrayList = new ArrayList<>();
        for (GraphPropertyEnum graphProperty : GraphPropertyEnum.values()) {
            arrayList.add(graphProperty.getProperty());
        }
        return arrayList;
    }
}

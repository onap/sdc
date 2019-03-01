/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.dao.neo4j;

public enum GraphPropertiesDictionary {
//						field name					class type    				unique		indexed
//													stored in graph 			index	
    // Common
    LABEL("nodeLabel", String.class, false, true),
    HEALTH_CHECK("healthcheckis", String.class, true, true),
    // Resource
    NAME("name", String.class, false, true),
    TOSCA_RESOURCE_NAME("toscaResourceName", String.class, false, true),
    CATEGORY_NAME("categoryName", String.class, false, true),
    VERSION("version", String.class, false, true),
    CREATION_DATE("creationDate", Long.class, false, false),
    LAST_UPDATE_DATE("modificationDate", Long.class, false, false),
    IS_HIGHEST_VERSION("highestVersion", Boolean.class, false, true),
    IS_ABSTRACT("abstract", Boolean.class, false, true),
    DESCRIPTION("description", String.class, false, false),
    UNIQUE_ID("uid", String.class, true, true),
    STATE("state", String.class, false, true),
    TYPE("type", String.class, false, true),
    REQUIRED("required", Boolean.class, false, false),
    DEFAULT_VALUE("defaultValue", String.class, false, false),
    CONSTRAINTS("constraints", String.class, false, false),
    CONTACT_ID("contactId", String.class, false, false),
    VENDOR_NAME("vendorName", String.class, false, false),
    VENDOR_RELEASE("vendorRelease", String.class, false, false),
    CONFORMANCE_LEVEL("conformanceLevel", String.class, false, false),
    ICON("icon", String.class, false, false),
    TAGS("tags", String.class, false, false),
    UUID("uuid", String.class, false, true),
    COST("cost", String.class, false, false),
    LICENSE_TYPE("licenseType", String.class, false, false),
    NORMALIZED_NAME("normalizedName", String.class, false, true),
    SYSTEM_NAME("systemName", String.class, false, true),
    IS_DELETED("deleted", Boolean.class, false, true),
    RESOURCE_TYPE("resourceType", String.class, false, true),
    ENTRY_SCHEMA("entry_schema", String.class, false, false),
    CSAR_UUID("csarUuid", String.class, false, true),
    CSAR_VERSION("csarVersion", String.class, false, true),
    IMPORTED_TOSCA_CHECKSUM("importedToscaChecksum", String.class, false, true),
    GENERATED("generated", Boolean.class, false, false),
    // User
    USERID("userId", String.class, true, true),
    EMAIL("email", String.class, false, false),
    FIRST_NAME("firstName", String.class, false, false),
    LAST_NAME("lastName", String.class, false, false),
    ROLE("role", String.class, false, true),
    USER_STATUS("status", String.class, false, true),
    VALID_SOURCE_TYPES("validSourceTypes", String.class, false, false),
    VALID_TARGET_TYPES("validTargetTypes", String.class, false, false),
    NODE("node", String.class, false, false),
    VALUE("value", String.class, false, false),
    HIDDEN("Hidden", Boolean.class, false, false),
    PROPERTIES("properties", String.class, false, false),
    POSITION_X("positionX", String.class, false, false),
    POSITION_Y("positionY", String.class, false, false),
    RELATIONSHIP_TYPE("relationshipType", String.class, false, false),
    ARTIFACT_TYPE("artifactType", String.class, false, true),
    ARTIFACT_REF("artifactRef", String.class, false, false),
    ARTIFACT_REPOSITORY("artifactRepository", String.class, false, false),
    ARTIFACT_CHECKSUM("artifactChecksum", String.class, false, false),
    CREATOR("creator", String.class, false, false),
    CREATOR_ID("creatorId", String.class, false, false),
    LAST_UPDATER("lastUpdater", String.class, false, false),
    CREATOR_FULL_NAME("creatorFullName", String.class, false, false),
    UPDATER_FULL_NAME("updaterFullName", String.class, false, false),
    ES_ID("esId", String.class, false, false),
    ARTIFACT_LABEL("artifactLabel", String.class, false, true),
    ARTIFACT_DISPLAY_NAME("artifactDisplayName", String.class, false, true),
    INSTANCE_COUNTER("instanceCounter", Integer.class, false, false),
    PROJECT_CODE("projectCode", String.class, false, false),
    DISTRIBUTION_STATUS("distributionStatus", String.class, false, false),
    IS_VNF("isVNF", Boolean.class, false, false),
    LAST_LOGIN_TIME("lastLoginTime", Long.class, false, true),
    ATTRIBUTE_COUNTER("attributeCounter", Integer.class, false, false),
    INPUT_COUNTER("inputCounter", Integer.class, false, false),
    PROPERTY_COUNTER("propertyCounter", Integer.class, false, false),
    API_URL("apiUrl", String.class, false, false),
    SERVICE_API("serviceApi", Boolean.class, false, true),
    ADDITIONAL_INFO_PARAMS("additionalInfo", String.class, false, false),
    ADDITIONAL_INFO_ID_TO_KEY("idToKey", String.class, false, false),
    ARTIFACT_GROUP_TYPE("artifactGroupType", String.class, false, true),
    ARTIFACT_TIMEOUT("timeout", Integer.class, false, false),
    IS_ACTIVE("isActive", Boolean.class, false, true),
    PROPERTY_VALUE_RULES("propertyValueRules", String.class, false, false),
    //authantication
    CONSUMER_NAME("consumerName", String.class, true, true),
    CONSUMER_PASSWORD("consumerPassword", String.class, false, false),
    CONSUMER_SALT("consumerSalt", String.class, false, false),
    CONSUMER_LAST_AUTHENTICATION_TIME("consumerLastAuthenticationTime", Long.class, false, false),
    CONSUMER_DETAILS_LAST_UPDATED_TIME("consumerDetailsLastupdatedtime", Long.class, false, false),
    LAST_MODIFIER_USER_ID("lastModfierUserId", String.class, false, false),
    ARTIFACT_VERSION("artifactVersion", String.class, false, false),
    ARTIFACT_UUID("artifactUUID", String.class, false, false),
    PAYLOAD_UPDATE_DATE("payloadUpdateDate", Long.class, false, false),
    HEAT_PARAMS_UPDATE_DATE("heatParamsUpdateDate", Long.class, false, false),
    //product
    FULL_NAME("fullName", String.class, false, true),
    //was changed as part of migration from 1602 to 1602 ( in 1602 was defined as unique. it's problem to reconfigure the index )
    CONSTANT_UUID("constantUuidNew", String.class, false, true),
    CONTACTS("contacts", String.class, false, false),
    //categorys
    ICONS("icons", String.class, false, false),
    //relation
    CAPABILITY_OWNER_ID("capOwnerId", String.class, false, false),
    REQUIREMENT_OWNER_ID("reqOwnerId", String.class, false, false),
    CAPABILITY_ID("capabiltyId", String.class, false, false),
    REQUIREMENT_ID("requirementId", String.class, false, false),
    PROPERTY_ID("propertyId", String.class, false, false),
    PROPERTY_NAME("propertyName", String.class, false, false),
    //component instance
    ORIGIN_TYPE("originType", String.class, false, false),
    //requirement & capabilty
    MIN_OCCURRENCES("minOccurrences", String.class, false, false),
    MAX_OCCURRENCES("maxOccurrences", String.class, false, false),
    //Data type
    DERIVED_FROM("derivedFrom", String.class, false, false),
    MEMBERS("members", String.class, false, false),
    TARGETS("targets ", String.class, false, false),
    METADATA("metadata", String.class, false, false),
    INVARIANT_UUID("invariantUuid", String.class, false, true),
    IS_BASE("isBase", Boolean.class, false, true),
    GROUP_UUID("groupUuid", String.class, false, true),
    STATUS("status", String.class, false, false),
    FUNCTIONAL_MENU("functionalMenu", String.class, false, false),
    REQUIRED_ARTIFACTS("requiredArtifacts", String.class, false, false),
    CUSTOMIZATION_UUID("customizationUUID", String.class, false, false),
    IS_ARCHIVED("isArchived", Boolean.class, false, true),
    IS_VSP_ARCHIVED("isVspArchived", Boolean.class, false, true),
    ARCHIVE_TIME("archiveTime", Long.class, false, true);


    private final String property;
    private final Class clazz;
    private final boolean unique;
    private final boolean indexed;

    GraphPropertiesDictionary(String property, Class clazz, boolean unique, boolean indexed) {
        this.property = property;
        this.clazz = clazz;
        this.unique = unique;
        this.indexed = indexed;
    }


    public String getProperty() {
        return property;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isIndexed() {
        return indexed;
    }
}

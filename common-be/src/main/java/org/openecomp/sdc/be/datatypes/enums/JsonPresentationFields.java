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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString(onlyExplicitlyIncluded = true)
public enum JsonPresentationFields {
    // @formatter:off
    UNIQUE_ID("uniqueId", GraphPropertyEnum.UNIQUE_ID),
    HIGHEST_VERSION("highestVersion", GraphPropertyEnum.IS_HIGHEST_VERSION),
    LIFECYCLE_STATE("lifecycleState", GraphPropertyEnum.STATE),
    CREATION_DATE("creationDate", null),
    LAST_UPDATE_DATE("lastUpdateDate", null),
    SYSTEM_NAME("systemName", GraphPropertyEnum.SYSTEM_NAME),
    NAME("name", GraphPropertyEnum.NAME),
    VERSION("version", GraphPropertyEnum.VERSION),
    NORMALIZED_NAME("normalizedName", GraphPropertyEnum.NORMALIZED_NAME),
    UUID("UUID", GraphPropertyEnum.UUID),
    RESOURCE_TYPE("resourceType", GraphPropertyEnum.RESOURCE_TYPE),
    COMPONENT_TYPE("componentType", GraphPropertyEnum.COMPONENT_TYPE),
    IS_DELETED("isDeleted", GraphPropertyEnum.IS_DELETED),
    ECOMP_GENERATED_NAMING("ecompGeneratedNaming", null),
    NAMING_POLICY("namingPolicy", null),
    ENVIRONMENT_CONTEXT("environmentContext", null),
    TOSCA_RESOURCE_NAME("toscaResourceName", GraphPropertyEnum.TOSCA_RESOURCE_NAME),
    DESCRIPTION("description", null),
    TYPE("type", null),
    DERIVED_FROM("derivedFrom", null),
    VENDOR_NAME("vendorName", null),
    VENDOR_RELEASE("vendorRelease", null),
    CATEGORY("category", null),
    SUB_CATEGORY("subcategory", null),
    RESOURCE_VENDOR("resourceVendor", null),
    RESOURCE_VENDOR_RELEASE("resourceVendorRelease", null),
    RESOURCE_VENDOR_MODEL_NUMBER("reourceVendorModelNumber", null),
    SERVICE_TYPE("serviceType", null),
    SERVICE_ROLE("serviceRole", null),
    INSTANTIATION_TYPE("instantiationType", null),
    CONFORMANCE_LEVEL("conformanceLevel", null),
    ICON("icon", null),
    TAGS("tags", null),
    INVARIANT_UUID("invariantUuid", GraphPropertyEnum.INVARIANT_UUID),
    CSAR_UUID("csarUuid", GraphPropertyEnum.CSAR_UUID),
    CSAR_VERSION("csarVersion", null),
    CSAR_VERSION_ID("csarVersionId", null),
    IMPORTED_TOSCA_CHECKSUM("importedToscaChecksum", null),
    CONTACT_ID("contactId", null),
    PROJECT_CODE("projectCode", null),
    DISTRIBUTION_STATUS("distributionStatus", GraphPropertyEnum.DISTRIBUTION_STATUS),
    DERIVED_FROM_GENERIC_TYPE("derivedFromGenericType", null),
    DERIVED_FROM_GENERIC_VERSION("derivedFromGenericVersion", null),
    SERVICE_FUNCTION("serviceFunction", null),
    MODEL("model", null),
    NORMATIVE("normative", null),
    DATA_TYPES("data_types", GraphPropertyEnum.DATA_TYPES),

    ////Inputs
    ANNOTATIONS("annotations", null),
    ////Artifact
    ARTIFACT_TYPE("artifactType", null),
    ARTIFACT_REF("artifactRef", null),
    ARTIFACT_REPOSITORY("artifactRepository", null),
    ARTIFACT_CHECKSUM("artifactChecksum", null),
    ARTIFACT_CREATOR("artifactCreator", null),
    USER_ID_CREATOR("userIdCreator", null),
    USER_ID_LAST_UPDATER("userIdLastUpdater", null),
    CREATOR_FULL_NAME("creatorFullName", null),
    UPDATER_FULL_NAME("updaterFullName", null),
    IS_FROM_CSAR("isFromCsar", null),

    ES_ID("esId", null),
    ARTIFACT_LABEL("artifactLabel", null),
    IS_ABSTRACT("mandatory", null),
    ARTIFACT_DISPLAY_NAME("artifactDisplayName", null),
    API_URL("apiUrl", null),
    SERVICE_API("serviceApi", null),
    ARTIFACT_VERSION("artifactVersion", null),
    ARTIFACT_UUID("artifactUUID", null),
    PAYLOAD_UPDATE_DATE("payloadUpdateDate", null),
    HEAT_PARAMS_UPDATE_DATE("heatParamsUpdateDate", null),
    GENERATED("generated", null),
    ARTIFACT_GROUP_TYPE("artifactGroupType", null),
    ARTIFACT_TIMEOUT("timeout", null),
    REQUIRED_ARTIFACTS("requiredArtifacts", null),
    DUPLICATED("duplicated", null),
    HEAT_PARAMETERS("heatParameters", null),
    GENERATED_FROM_ID("generatedFromId", null),

    // end artifacts

    //property
    DEFINITION("definition", null),
    DEFAULT_VALUE("defaultValue", null),
    DEFAULT("default", null),
    REQUIRED("required", null),
    PASSWORD("password", null),
    CONSTRAINTS("constraints", null),
    PROPERTIES("properties", null),
    PROPERTY("property", null),
    SCHEMA("schema", null),
    ENTRY_SCHEMA("entry_schema", null),
    VALUE("value", null),
    PARENT_UNIQUE_ID("parentUniqueId", null),

    COMPONENT_INSTANCES("componentInstances", null),
    RELATIONS("relations", null),

    //attribute
    STATUS("status", null),
    //capability
    VALID_SOURCE_TYPE("validSourceTypes", null),
    CREATION_TIME("creationTime", null),
    MODIFICATION_TIME("modificationTime", null),
    CAPABILITY_SOURCES("capabilitySources", null),
    MAX_OCCURRENCES("maxOccurrences", null),
    MIN_OCCURRENCES("minOccurrences", null),
    OWNER_TYPE("ownerType", null),
    OWNER_NAME("ownerName", null),
    OWNER_ID("ownerId", null),
    LEFT_OCCURRENCES("leftOccurences", null),
    CAPABILITY_ID("capabiltyId", null),
    PATH("path", null),
    SOURCE("source", null),

    //Requirement
    CAPABILITY("capability", null),
    NODE("node", null),
    RELATIONSHIP("relationship", null),
    VALID_SOURCE_TYPES("validSourceTypes", null),
    VALID_TARGET_TYPES("validTargetTypes", null),
    REQUIREMENT_ID("requirementId", null),
    PARENT_NAME("parentName", null),
    PREVIOUS_NAME("previousName", null),
    NODE_FILTER_CAPABILITIES("capabilities", null),
    PROPERTY_FILTER_NAME("propertyName", null),
    PROPERTY_FILTER_CONSTRAINT("propertyFilterConstraint", null),
    EQUAL_CONSTRAINT("equal", null),
    CONSTRAINT_VALUE("constraintValue", null),
    CONSTRAINT_FUNCTION("constraintFunction", null),
    NODE_FILTER("nodeFilter", null),
    TOSCA_ID("tosca_id", null),

    //Relation
    CAPABILTY_OWNER_ID("capabilityOwnerId", null), REQUIREMENT_OWNER_ID("requirementOwnerId", null), FROM_ID("fromId", null), TO_ID("toId",
        null), REQUIREMENT("requirement", null), ORIGIN_UI("originUI", null),
    //Groups

    GROUP_INVARIANT_UUID("invariantUUID", null),
    GROUP_UUID("groupUUID", null),
    GROUP_MEMBER("members", null),
    GROUP_ARTIFACTS("artifacts", null),
    GROUP_ARTIFACTS_UUID("artifactsUuid", null),
    GROUP_PROPERTIES("properties", null),
    GROUP_UNIQUE_ID("groupUid", null),
    POS_X("posX", null),
    POS_Y("posY", null),
    PROPERTY_VALUE_COUNTER("propertyValueCounter", null),
    CUSTOMIZATION_UUID("customizationUUID", null),
    GROUP_NAME("groupName", null),
    GROUP_INSTANCE_ARTIFACTS("groupInstanceArtifacts", null),
    GROUP_INSTANCE_ARTIFACTS_UUID("groupInstanceArtifactsUuid", null),
    GROUP_INSTANCE_PROPERTIES("groupInstancesProperties", null),

    //Component instance

    CI_COMPONENT_UID("componentUid", null),
    CI_POS_X("posX", null),
    CI_POS_Y("posY", null),
    CI_PROP_VALUE_COUNTER("propertyValueCounter", null),
    CI_ATTR_VALUE_COUNTER("attributeValueCounter", null),
    CI_INPUT_VALUE_COUNTER("inputValueCounter", null),
    CI_ORIGIN_TYPE("originType", null),
    CI_COMPONENT_NAME("componentName", null),
    CI_COMPONENT_VERSION("componentVersion", null),
    CI_TOSCA_COMPONENT_NAME("toscaComponentName", null),
    CI_INVARIANT_NAME("invariantName", null),
    CI_ICON("icon", null),
    CI_SOURCE_MODEL_UUID("sourceModelUuid", null),
    CI_SOURCE_MODEL_UID("sourceModelUid", null),
    CI_SOURCE_MODEL_INVARIANT("sourceModelInvariant", null),
    CI_SOURCE_MODEL_NAME("sourceModelName", null),
    CI_IS_PROXY("isProxy", null),
    CI_DIRECTIVES("directives", null),
    CI_ARTIFACTS("artifacts", null),
    CI_MAX_OCCURRENCES("maxOccurrences", null),
    CI_MIN_OCCURRENCES("minOccurrences", null),
    CI_INSTANCE_COUNT("instanceCount", null),

    //path
    FORWARDING_PATH("forwardingPath", null),
    FROM_NODE("fromNode", null),
    TO_NODE("toNode", null),
    PATH_FROM_CP("fromCP", null),
    PATH_TO_CP("toCP", null),
    PATH_TO_CP_ORIGIN("toCPOrigin", null),
    PATH_FROM_CP_ORIGIN("fromCPOrigin", null),
    PATH_NAME("pathName", null),
    PATH_PROTOCOL("pathProtocol", null),
    PATH_DESTINATION_PORT_NUMBER("destinationPortNumber", null),
    PATH_ELEMENT_LIST("pathElementList", null),

    TYPE_UNIQUE_ID("typeUniqueId", null),
    METADATA("metadata", null),
    TARGETS("targets", null),

    //External Refs
    EXTERNAL_REF("externalRef", null), CREATED_FROM("createdFrom", null),
    //Archive
    IS_ARCHIVED("isArchived", GraphPropertyEnum.IS_ARCHIVED),
    ARCHIVE_TIME("archiveTime", GraphPropertyEnum.ARCHIVE_TIME),
    IS_VSP_ARCHIVED("isVspArchived", GraphPropertyEnum.IS_VSP_ARCHIVED),
    CI_IS_ORIGIN_ARCHIVED("isOriginArchived", null),

    //Interface Operation
    IO_OPERATION_TYPE("operationType", null),
    IO_INPUT_PARAMETERS("inputParams", null),
    IO_OUTPUT_PARAMETERS("outputParams", null),
    IO_PARAM_NAME("paramName", null),
    IO_PARAM_ID("paramId", null),
    IO_NAME("name", null),
    IO_MANDATORY("mandatory", null),
    IO_TYPE("type", null),
    IO_PROPERTY("property", null),
    IO_WORKFLOW_NAME("workflowName", null),
    IO_WORKFLOW_VERSION("workflowVersion", null),
    IO_WORKFLOW_ID("workflowId", null),
    IO_WORKFLOW_VERSION_ID("workflowVersionId", null),
    IO_WORKFLOW_ASSOCIATION_TYPE("workflowAssociationType", null),

    //Interface
    INTERFACES("interfaces", null),
    INTERFACE_INPUT("input", null),
    OPERATIONS("operations", null),
    OPERATION_IMPLEMENTATION("implementation", null),
    OPERATION_INPUTS("inputs", null),
    OPERATION_OUTPUTS("outputs", null),
    INPUTS("inputs", null),

    GET_PROPERTY("get_property", null),
    GET_INPUT("get_input", null),
    GET_OPERATION_OUTPUT("get_operation_output", null),

    ATTRIBUTES("attributes", null),

    TOSCA_DEFINITIONS_VERSION("tosca_definitions_version", null);
    // @formatter:on

    @ToString.Include
    private final String presentation;
    @ToString.Exclude
    private final GraphPropertyEnum storedAs;

    public static String getPresentationByGraphProperty(GraphPropertyEnum property) {
        for (JsonPresentationFields currPresentation : JsonPresentationFields.values()) {
            if (currPresentation.getStoredAs() == property) {
                return currPresentation.getPresentation();
            }
        }
        return null;
    }

    @JsonCreator
    public static JsonPresentationFields getByPresentation(String presentation) {
        for (JsonPresentationFields inst : JsonPresentationFields.values()) {
            if (inst.getPresentation().equals(presentation)) {
                return inst;
            }
        }
        return null;
    }

    @JsonValue
    public String getPresentation() {
        return presentation;
    }
}

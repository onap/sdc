/*
 * Copyright © 2016-2018 European Support Limited
 *
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
package org.openecomp.sdc.common.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Messages {
    // @formatter:off
    CANT_LOAD_HEALING_CLASS("Can't load healing class %s."),
    VERSION_UPGRADE("Item %s is of old version. A check out was made in order to get new " + "functionalities"),
    PACKAGE_PROCESS_ERROR("Could not process package '%s'"),
    PACKAGE_INVALID_EXTENSION("Invalid package '%s' extension. Expecting %s."),
    PACKAGE_EMPTY_ERROR("The given package is empty '%s'"),
    PACKAGE_PROCESS_INTERNAL_PACKAGE_ERROR("Could not process internal package '%s'"),
    PACKAGE_INVALID_ERROR("Invalid package content '%s'"),
    PACKAGE_MISSING_INTERNAL_PACKAGE("Missing expected internal package"),
    COULD_NOT_READ_MANIFEST_FILE("Could not read manifest file: %s [%s]"),
    INVALID_ZIP_FILE("Invalid zip file"),
    INVALID_CSAR_FILE("Invalid csar file"),
    CSAR_FILE_NOT_FOUND("Each CSAR file must contain %s file."),
    CSAR_DIRECTORIES_NOT_ALLOWED("Directory : %s , is not allowed."),
    CSAR_FILES_NOT_ALLOWED("File : %s , are not allowed."),
    MANIFEST_INVALID_LINE("Manifest contains invalid line: %s: %s"),
    MANIFEST_START_METADATA("Manifest must starts with 'metadata:'"),
    MANIFEST_NO_METADATA("Manifest must contain metadata"),
    MANIFEST_NO_SOURCES("Manifest must contain Source"),
    MANIFEST_METADATA_MISSING_ENTRY("Manifest metadata missing entry %s"),
    MANIFEST_INVALID_NAME("Manifest file %s and TOSCA definitions file %s must have the same name"),
    MANIFEST_INVALID_EXT("Manifest file must have extension \".mf\" "),
    MANIFEST_METADATA_INVALID_ENTRY("Manifest metadata should only have pnf or vnf entries"),
    MANIFEST_METADATA_INVALID_ENTRY1("Invalid Manifest metadata entry: '%s'."),
    MANIFEST_METADATA_DUPLICATED_ENTRY("Duplicated Manifest metadata entry: '%s'."),
    MANIFEST_METADATA_UNEXPECTED_ENTRY_TYPE("Manifest metadata should have either pnf or vnf entries, not both together"),
    MANIFEST_INVALID_PNF_METADATA("%s TOSCA.meta file is applicable for VF only"),
    MANIFEST_INVALID_NON_MANO_KEY("Invalid non mano key '%s'"),
    MANIFEST_EMPTY_NON_MANO_KEY("Expecting a 'Source' entry for the non mano key '%s'"),
    MANIFEST_EMPTY_NON_MANO_SOURCE("Empty non mano source"),
    MANIFEST_EXPECTED_HASH_ENTRY("Expected Hash entry"),
    MANIFEST_EXPECTED_HASH_VALUE("Expected Hash entry value"),
    MANIFEST_EXPECTED_SOURCE_PATH("Expected Source entry path"),
    MANIFEST_EXPECTED_ALGORITHM_VALUE("Expected Algorithm entry value"),
    MANIFEST_EXPECTED_ALGORITHM_BEFORE_HASH("Expected 'Algorithm' entry before 'Hash' entry"),
    MANIFEST_EXPECTED_SIGNATURE_VALUE("Expected 'Signature' entry value"),
    MANIFEST_EXPECTED_CERTIFICATE_VALUE("Expected 'Certificate' entry value"),
    MANIFEST_EXPECTED_SIGNATURE_BEFORE_CERTIFICATE("Expected 'Signature' entry before 'Certificate' entry"),
    MANIFEST_DUPLICATED_CMS_SIGNATURE("Duplicated CMS signature"),
    MANIFEST_SIGNATURE_DUPLICATED("Duplicated manifest signature"),
    MANIFEST_SIGNATURE_LAST_ENTRY("The manifest signature must be the last entry of the manifest."),
    MANIFEST_METADATA_DOES_NOT_MATCH_LIMIT("Manifest metadata must only have the required number [%s] of " + "entries"),
    MANIFEST_EMPTY("The manifest is empty"),
    MANIFEST_ERROR_WITH_LINE("%s;%nAt line %s: '%s'."),
    MANIFEST_PARSER_INTERNAL("Invalid manifest file"),
    MANIFEST_UNEXPECTED_ERROR("An unexpected error occurred while validating manifest '%s': %s"),
    MANIFEST_VALIDATION_HELM_IS_BASE_MISSING("Definition of 'isBase' is missing in %d charts."),
    MANIFEST_VALIDATION_HELM_IS_BASE_NOT_UNIQUE("More than one chart is marked as 'isBase'."),
    MANIFEST_VALIDATION_HELM_IS_BASE_NOT_SET("None of charts is marked as 'isBase'."),
    METADATA_PARSER_INTERNAL("Invalid Metadata file"),
    METADATA_MISSING_OPTIONAL_FOLDERS("Missing folder %s in package"),
    METADATA_UNSUPPORTED_ENTRY("Following entry not supported in TOSCA.meta %s"),
    METADATA_INVALID_VERSION("Invalid entry %s value %s"),
    METADATA_INVALID_VALUE("Invalid value %s in TOSCA.meta file"),
    METADATA_MISSING_ENTRY("TOSCA.meta file in TOSCA-metadata directory missing entry %s"),
    METADATA_NO_ENTRY_DEFINITIONS("TOSCA.meta must contain Entry Definitions"),
    METADATA_INVALID_ENTRY_DEFINITIONS("TOSCA.meta must contain key:value entries"),
    EMPTY_SW_INFORMATION_NON_MANO_ERROR("Non-mano Software Information artifact was declared in the manifest, but not provided"),
    UNIQUE_SW_INFORMATION_NON_MANO_ERROR("Only one software information non-mano artifact is allowed. " + "Found %s."),
    INVALID_SW_INFORMATION_NON_MANO_ERROR("Invalid software information non-mano artifact: '%s'"),
    INCORRECT_SW_INFORMATION_NON_MANO_ERROR("Incorrect software information non-mano artifact. The software version information is missing or it has "
        + "one or more incorrect software version entries: '%s'"),
    EMPTY_ONAP_CNF_HELM_NON_MANO_ERROR("Non-mano onap_cnf_helm artifact was declared in the manifest, but not provided"),
    UNIQUE_ONAP_CNF_HELM_NON_MANO_ERROR("Only one onap_cnf_helm non-mano artifact is allowed. " + "Found %s."),
    FAILED_TO_VALIDATE_METADATA("Failed to validate metadata file"),
    ARTIFACT_INVALID_SIGNATURE("Invalid signature '%s' provided for artifact '%s'"),
    ARTIFACT_SIGNATURE_VALIDATION_ERROR("Could not validate signature '%s' provided for artifact '%s' with certificate '%s': %s"),
    FAILED_TO_TRANSLATE_ZIP_FILE("Failed to translate zip file"),
    ZIP_NOT_EXIST("Zip file doesn't exist"),
    PERMISSIONS_ERROR("Permissions Error. The user does not have permission to perform this action."),
    PERMISSIONS_OWNER_ERROR("Permissions Error. Only one owner is allowed."),
    ENTITY_NOT_FOUND("Incorrect item/version details provided."),
    SUB_ENTITY_NOT_FOUND("Incorrect sub entity details provided."),
    FAILED_TO_SYNC("Non existing version cannot be synced."),
    FAILED_TO_PUBLISH_OUT_OF_SYNC("Publish is not allowed since the version status is Out of sync"),
    DELETE_VSP_ERROR("Failed to delete VSP '%s' from database"),
    DELETE_CERTIFIED_VSP_ERROR("Certified VSP '%s' must be archived before it can be deleted."),
    DELETE_VSP_ERROR_USED_BY_VF("VSP cannot be deleted as it is used by VF %s. The VSP will only be available for deletion if VF %s is deleted."),
    DELETE_VSP_UNEXPECTED_ERROR_USED_BY_VF("An error occurred while retrieving the usage of VSP %s through the rest endpoint %s"),
    VSP_NOT_FOUND("Vendor Software Product with id '%s' was not found."),
    VSP_VERSION_NOT_FOUND("Vendor Software Product with id '%s' and version id '%s' was not found."),
    DELETE_NOT_ARCHIVED_VSP_ERROR("The certified VSP '%s' must be archived before it can be deleted."),
    DELETE_VSP_FROM_STORAGE_ERROR("Failed to delete VSP '%s' from Storage"),
    DELETE_VLM_ERROR("VLM has been certified and cannot be deleted."),
    DELETE_VSP_ARCHIVED_ERROR("VSP has not been archived and cannot be deleted."),
    CONFIG_ERROR("Configuration could not be loaded."),
    ZIP_SHOULD_NOT_CONTAIN_FOLDERS("Zip file should not contain folders"),
    VES_ZIP_SHOULD_CONTAIN_YML_ONLY("Wrong VES EVENT Artifact was uploaded - all files contained in Artifact must be YAML files"
        + " (using .yaml/.yml extensions)"),
    MANIFEST_NOT_EXIST("Manifest doesn't exist"),
    MANIFEST_NOT_FOUND("Manifest file %s referenced in TOSCA.meta does not exist"),
    FILE_TYPE_NOT_LEGAL("File type not legal as data for other file"),
    MODULE_IN_MANIFEST_NO_YAML("Module '%s', has no yaml file reference"),
    NO_MODULES_IN_MANIFEST("At least one Base/Module must be defined \n"),
    MODULE_IN_MANIFEST_VOL_ENV_NO_VOL("Module '%s', has volume Env. reference with no Volume " + "reference"),
    ILLEGAL_MANIFEST("Illegal Manifest"),
    NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST("no %s file was uploaded or file doesn't exist"),
    MAPPING_OBJECTS_FAILURE("Failed to map object %s to %s. Exception message: %s"),
    MORE_THEN_ONE_VOL_FOR_HEAT("heat contains more then one vol. selecting only first vol"),
    FILE_LOAD_CONTENT_ERROR("Failed to load file '%s' content"),
    CREATE_MANIFEST_FROM_ZIP("cannot create manifest from the attached zip file"),
    CANDIDATE_PROCESS_FAILED("Candidate zip file process failed"),
    FOUND_UNASSIGNED_FILES("cannot process zip since it has unassigned files"),
    GENERATED_ARTIFACT_IN_USE("Artifact with file name %s is generated by SDC. " + "Please remove this artifact from manifest and zip files"),
    /* Monitor uploads related errors*/
    ILLEGAL_MONITORING_ARTIFACT_TYPE("Illegal monitoring artifact type for component id %s, vsp id %s"),
    /* manifest errors*/
    MISSING_FILE_IN_ZIP("Missing file in zip"),
    MISSING_FILE_IN_MANIFEST("Missing file in manifest"),
    MISSING_FILE_NAME_IN_MANIFEST("Missing file name in manifest"),
    MISSING_NESTED_FILE("Missing nested file - %s"),
    MISSING_ARTIFACT("Missing artifact - %s"),
    MISSING_MANIFEST_SOURCE("%s artifact %s referenced in manifest file does not exist"),
    MISSING_MANIFEST_REFERENCE("'%s' artifact is not being referenced in manifest file"),
    MISSING_METADATA_FILES("%s file referenced in TOSCA.meta does not exist"),
    WRONG_HEAT_FILE_EXTENSION("Wrong HEAT file extension - %s"),
    WRONG_ENV_FILE_EXTENSION("Wrong ENV file extension - %s"),
    INVALID_MANIFEST_FILE("invalid manifest file"),
    INVALID_FILE_TYPE("Missing or Unknown file type in Manifest"),
    ENV_NOT_ASSOCIATED_TO_HEAT("ENV file must be associated to a HEAT file"),
    CSAR_MANIFEST_FILE_NOT_EXIST("CSAR manifest file does not exist"),
    CSAR_FAILED_TO_READ("CSAR file is not readable"),
    TOSCA_PARSING_FAILURE("Invalid tosca file. Error code : %s, Error message : %s/"),
    /*definition errors*/
    MISSING_DEFINITION_FILE("Definition file %s referenced in TOSCA.meta does not exist"),
    MISSING_IMPORT_FILE("Package must contain the referenced import file '%s'"),
    MISSING_MAIN_DEFINITION_FILE("Package must contain the given main definition file '%s'"),
    INVALID_IMPORT_STATEMENT("Definition file '%s' contains an invalid import statement: '%s'"),
    INVALID_YAML_EXTENSION("Expecting yaml or yml extension for file: %s"),
    /* content errors*/
    INVALID_YAML_FORMAT("Invalid YAML format: %s"),
    INVALID_YAML_FORMAT_1("Invalid YAML format in file '%s'. Format error:%n%s"),
    INVALID_YAML_FORMAT_REASON("Invalid YAML format Problem - [%s]"),
    EMPTY_YAML_FILE("empty yaml"),
    EMPTY_YAML_FILE_1("The yaml file '%s' is empty"),
    GENERAL_YAML_PARSER_ERROR("general parser error"),
    GENERAL_HEAT_PARSER_ERROR("general parser error"),
    INVALID_HEAT_FORMAT_REASON("Invalid HEAT format problem - [%s]"),
    MISSING_RESOURCE_IN_DEPENDS_ON("a Missing resource in depend On, Missing Resource ID [%s]"),
    REFERENCED_PARAMETER_NOT_FOUND("Referenced parameter - %s - not found, used in resource [%s]"),
    GET_ATTR_NOT_FOUND("get_attr attribute not found, Attribute name [%s], Resource ID [%s]"),
    MISSING_PARAMETER_IN_NESTED("Referenced parameter not found in nested file - %s, parameter name [%s], Resource ID [%s]"),
    NESTED_LOOP("Nested files loop - %s"),
    MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT("Resource Port %s exceed allowed relations from NovaServer"),
    SERVER_NOT_DEFINED_FROM_NOVA("Missing server group definition - %s, %s"),
    WRONG_POLICY_IN_SERVER_GROUP("Wrong policy in server group - %s"),
    MISSING_IMAGE_AND_FLAVOR("Missing both Image and Flavor in NOVA Server, Resource ID [%s]"),
    ENV_INCLUDES_PARAMETER_NOT_IN_HEAT("Env file %s includes a parameter not in HEAT - %s"),
    PARAMETER_ENV_VALUE_NOT_ALIGN_WITH_TYPE("Parameter env value %s not align with type"),
    PARAMETER_DEFAULT_VALUE_NOT_ALIGN_WITH_TYPE("Parameter - %s default value not align with type %s"),
    INVALID_RESOURCE_TYPE("A resource has an invalid or unsupported type - %s, Resource ID [%s]"),
    ARTIFACT_FILE_NOT_REFERENCED("Artifact file is not referenced."),
    RESOURCE_NOT_IN_USE("%s not in use, Resource Id [%s]"),
    PORT_NO_BIND_TO_ANY_NOVA_SERVER("Port not bind to any NOVA Server, Resource Id [%s]"),
    INVALID_GET_RESOURCE_SYNTAX("invalid get_resource syntax is in use - %s , get_resource function should get the resource id of the "
        + "referenced resource"),
    INVALID_RESOURCE_GROUP_TYPE("OS::Heat::ResourceGroup resource with resource_def which is not pointing to nested heat file is not supported, "
        + "Resource ID [%s], resource_def type [%s]"),
    WRONG_VALUE_TYPE_ASSIGNED_NESTED_INPUT("Wrong value type assigned to a nested input parameter, nested resource [%s], property name [%s], "
        + "nested file [%s]"),
    NOVA_NAME_IMAGE_FLAVOR_NOT_CONSISTENT("Nova Server naming convention in image, flavor and name properties is not consistent, Resource ID [%s]"),
    RESOURCE_GROUP_INVALID_INDEX_VAR("Wrong value assigned to a ResourceGroup index_var property (functions are not allowed but only strings),"
        + " Resource ID [%s]"),
    CONTRAIL_2_IN_USE("Contrail 2.x deprecated resource is in use, Resource ID [%s]"),
    /* warnings */
    REFERENCED_RESOURCE_NOT_FOUND("Referenced resource - %s not found"),
    MISSING_GET_PARAM("Missing get_param in %s, Resource Id [%s]"),
    /*OPENECOMP Guide lines*/
    MISSING_NOVA_SERVER_METADATA("Missing Nova Server Metadata property, Resource ID [%s]"),
    MISSING_NOVA_SERVER_VNF_ID("Missing VNF_ID in Metadata property, Resource ID [%s]"),
    MISSING_NOVA_SERVER_VF_MODULE_ID("Missing VF_MODULE_ID in Metadata property, Resource id [%s]"),
    NETWORK_PARAM_NOT_ALIGNED_WITH_GUIDE_LINE("Network Parameter Name not aligned with Guidelines, Parameter Name [%s] Resource ID [%s]"),
    MISSIN_BASE_HEAT_FILE("Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources"),
    MULTI_BASE_HEAT_FILE("Multi Base HEAT. Expected only one. Files %s."),
    RESOURCE_NOT_DEFINED_IN_OUTPUT("Resource is not defined as output and thus cannot be Shared, Resource ID [%s]"),
    RESOURCE_CONNECTED_TO_TWO_EXTERNAL_NETWORKS_WITH_SAME_ROLE("A resource is connected twice to the same network role, Network Role [%s], "
        + "Resource ID [%s]"),
    VOLUME_HEAT_NOT_EXPOSED("Volume is not defined as output and thus cannot be attached %s"),
    FORBIDDEN_RESOURCE_IN_USE("%s is in use, Resource ID [%s]"),
    PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES("%s '%s' Parameter Name not aligned with Guidelines, Parameter Name [%s], Resource ID [%s]."
        + " As a result, VF/VFC Profile may miss this information"),
    /* Contrail validator messages*/
    MERGE_OF_CONTRAIL2_AND_CONTRAIL3_RESOURCES("HEAT Package includes both Contrail 2 and Contrail 3 resources. "
        + "Contrail 2 resources can be found in %s. Contrail 3 resources can be found in %s"),
    CONTRAIL_VM_TYPE_NAME_NOT_ALIGNED_WITH_NAMING_CONVENSION("Service Template naming convention in Image and Flavor "
        + "properties is not consistent in Resource, Resource ID %s"),
    /* Notifications */
    FAILED_TO_MARK_NOTIFICATION_AS_READ("Failed to mark notifications as read"),
    FAILED_TO_UPDATE_LAST_SEEN_NOTIFICATION("Failed to update last seen notification for user %s"),
    FAILED_TO_VERIFY_SIGNATURE("Could not verify signature of signed package."),
    EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING("externalCsarStore configuration failure, missing '%s'"),
    ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT("Write '%s' to object storage failed, check object store logs for details, possibly due to insufficient storage capacity"),
    ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE("An error has occurred while reducing the artifact's size: %s"),
    UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING("An unexpected problem happened while getting '%s'"),
    PACKAGE_REDUCER_NOT_CONFIGURED("Could not process the package. Package reducer is not configured");
    // @formatter:on

    private final String errorMessage;

    /**
     * Formats the message with the given parameters.
     *
     * @param params The message string parameters to apply
     * @return The formatted message.
     */
    public String formatMessage(final Object... params) {
        return String.format(errorMessage, params);
    }
}

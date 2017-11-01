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

package org.openecomp.sdc.common.errors;


public enum Messages {
  CANT_LOAD_HEALING_CLASS("Can't load healing class %s."),

  VERSION_UPGRADE("Item %s is of old version. A check out was made in order to get new " +
      "functionalities"),

  INVALID_ZIP_FILE("Invalid zip file"),
  INVALID_CSAR_FILE("Invalid csar file"),
  CSAR_FILE_NOT_FOUND("Each CSAR file must contain %s file."),
  CSAR_DIRECTORIES_NOT_ALLOWED("Directory : %s , is not allowed."),
  CSAR_FILES_NOT_ALLOWED("File : %s , are not allowed."),
  MANIFEST_INVALID_LINE("Manifest contains invalid line : %s"),
  MANIFEST_NO_METADATA("Manifest must contain metadata"),
  MANIFEST_NO_SOURCES("Manifest must contain source"),
  MANIFEST_PARSER_INTERNAL("Invalid manifest file"),
  FAILED_TO_TRANSLATE_ZIP_FILE("Failed to translate zip file"),
  ZIP_NOT_EXIST("Zip file doesn't exist"),

  ZIP_SHOULD_NOT_CONTAIN_FOLDERS("Zip file should not contain folders"),
  VES_ZIP_SHOULD_CONTAIN_YML_ONLY(
      "Wrong VES EVENT Artifact was uploaded - all files contained in Artifact must be YAML files" +
          " (using .yaml/.yml extensions)"),
  MANIFEST_NOT_EXIST("Manifest doesn't exist"),
  FILE_TYPE_NOT_LEGAL("File type not legal as data for other file"),
  MODULE_IN_MANIFEST_NO_YAML("Module '%s', has no yaml file reference"),
  NO_MODULES_IN_MANIFEST("At least one Base/Module must be defined \n"),
  MODULE_IN_MANIFEST_VOL_ENV_NO_VOL("Module '%s', has volume Env. reference with no Volume " +
      "reference"),
  ILLEGAL_MANIFEST("Illegal Manifest"),
  NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST("no zip file was uploaded or zip file doesn't exist"),
  MAPPING_OBJECTS_FAILURE("Failed to map object %s to %s. Exception message: %s"),
  MORE_THEN_ONE_VOL_FOR_HEAT("heat contains more then one vol. selecting only first vol"),
  FILE_CONTENT_MAP("failed to load %s content"),
  CREATE_MANIFEST_FROM_ZIP("cannot create manifest from the attached zip file"),
  CANDIDATE_PROCESS_FAILED("Candidate zip file process failed"),
  FOUND_UNASSIGNED_FILES("cannot process zip since it has unassigned files"),

  /* Monitor uploads related errors*/
  ILLEGAL_MONITORING_ARTIFACT_TYPE("Illegal monitoring artifact type for component id %s, vsp id " +
      "%s"),


  /* manifest errors*/
  MISSING_FILE_IN_ZIP("Missing file in zip"),
  MISSING_FILE_IN_MANIFEST("Missing file in manifest"),
  MISSING_FILE_NAME_IN_MANIFEST("Missing file name in manifest - %s"),
  MISSING_NESTED_FILE("Missing nested file - %s"),
  MISSING_ARTIFACT("Missing artifact - %s"),
  WRONG_HEAT_FILE_EXTENSION("Wrong HEAT file extension - %s"),
  WRONG_ENV_FILE_EXTENSION("Wrong ENV file extension - %s"),
  INVALID_MANIFEST_FILE("invalid manifest file"),
  INVALID_FILE_TYPE("Missing or Unknown file type in Manifest"),
  ENV_NOT_ASSOCIATED_TO_HEAT("ENV file must be associated to a HEAT file"),
  CSAR_MANIFEST_FILE_NOT_EXIST("CSAR manifest file does not exist"),
  CSAR_FAILED_TO_READ("CSAR file is not readable"),
  TOSCA_PARSING_FAILURE("Invalid tosca file. Error code : %s, Error message : %s/"),
  /* content errors*/
  INVALID_YAML_FORMAT("Invalid YAML format - %s"),
  INVALID_YAML_FORMAT_REASON("Invalid YAML format Problem - [%s]"),
  EMPTY_YAML_FILE("empty yaml"),
  GENERAL_YAML_PARSER_ERROR("general parser error"),
  GENERAL_HEAT_PARSER_ERROR("general parser error"),
  INVALID_HEAT_FORMAT_REASON("Invalid HEAT format problem - [%s]"),
  MISSING_RESOURCE_IN_DEPENDS_ON("a Missing resource in depend On, Missing Resource ID [%s]"),
  REFERENCED_PARAMETER_NOT_FOUND("Referenced parameter - %s - not found, used in resource [%s]"),
  GET_ATTR_NOT_FOUND("get_attr attribute not found, Attribute name [%s], Resource ID [%s]"),
  MISSING_PARAMETER_IN_NESTED(
      "Referenced parameter not found in nested file - %s, parameter name [%s], Resource ID [%s]"),
  NESTED_LOOP("Nested files loop - %s"),
  MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT("Resource Port %s exceed allowed relations from NovaServer"),
  SERVER_NOT_DEFINED_FROM_NOVA("Missing server group definition - %s, %s"),
  WRONG_POLICY_IN_SERVER_GROUP("Wrong policy in server group - %s"),
  MISSING_IMAGE_AND_FLAVOR("Missing both Image and Flavor in NOVA Server, Resource ID [%s]"),
  ENV_INCLUDES_PARAMETER_NOT_IN_HEAT("Env file %s includes a parameter not in HEAT - %s"),
  PARAMETER_ENV_VALUE_NOT_ALIGN_WITH_TYPE("Parameter env value %s not align with type"),
  PARAMETER_DEFAULT_VALUE_NOT_ALIGN_WITH_TYPE(
      "Parameter - %s default value not align with type %s"),
  INVALID_RESOURCE_TYPE("A resource has an invalid or unsupported type - %s, Resource ID [%s]"),
  ARTIFACT_FILE_NOT_REFERENCED("Artifact file is not referenced."),
  RESOURCE_NOT_IN_USE("%s not in use, Resource Id [%s]"),
  PORT_NO_BIND_TO_ANY_NOVA_SERVER("Port not bind to any NOVA Server, Resource Id [%s]"),
  INVALID_GET_RESOURCE_SYNTAX(
      "invalid get_resource syntax is in use - %s , get_resource"
          + " function should get the resource id of the referenced resource"),
  INVALID_RESOURCE_GROUP_TYPE(
      "OS::Heat::ResourceGroup resource with resource_def which is not pointing to "
          + "nested heat file is not supported, Resource ID [%s], resource_def type [%s]"),
  WRONG_VALUE_TYPE_ASSIGNED_NESTED_INPUT(
      "Wrong value type assigned to a nested input parameter, nested resource [%s],"
          + " property name [%s], nested file [%s]"),
  NOVA_NAME_IMAGE_FLAVOR_NOT_CONSISTENT(
      "Nova Server naming convention in image, flavor and name properties is not "
          + "consistent, Resource ID [%s]"),
  RESOURCE_GROUP_INVALID_INDEX_VAR(
      "Wrong value assigned to a ResourceGroup index_var property (functions are not allowed"
          + " but only strings), Resource ID [%s]"),
  CONTRAIL_2_IN_USE("Contrail 2.x deprecated resource is in use, Resource ID [%s]"),

  /* warnings */
  REFERENCED_RESOURCE_NOT_FOUND("Referenced resource - %s not found"),
  MISSING_GET_PARAM("Missing get_param in %s, Resource Id [%s]"),

  /*OPENECOMP Guide lines*/
  MISSING_NOVA_SERVER_METADATA("Missing Nova Server Metadata property, Resource ID [%s]"),
  MISSING_NOVA_SERVER_VNF_ID("Missing VNF_ID in Metadata property, Resource ID [%s]"),
  MISSING_NOVA_SERVER_VF_MODULE_ID("%s Missing VF_MODULE_ID in Metadata property, Resource id [%s]"),
  NETWORK_PARAM_NOT_ALIGNED_WITH_GUIDE_LINE(
      "Network Parameter Name not aligned with Guidelines, Parameter Name [%s] Resource ID [%s]"),
  MISSIN_BASE_HEAT_FILE(
      "Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources"),
  MULTI_BASE_HEAT_FILE("Multi Base HEAT. Expected only one. Files %s."),
  RESOURCE_NOT_DEFINED_IN_OUTPUT(
      "Resource is not defined as output and thus cannot be Shared, Resource ID [%s]"),
  RESOURCE_CONNECTED_TO_TWO_EXTERNAL_NETWORKS_WITH_SAME_ROLE(
      "A resource is connected twice to the same network role, Network Role [%s],"
          + " Resource ID [%s]"),
  VOLUME_HEAT_NOT_EXPOSED("Volume is not defined as output and thus cannot be attached %s"),
  FORBIDDEN_RESOURCE_IN_USE("%s is in use, Resource ID [%s]"),
  PARAMETER_NAME_NOT_ALIGNED_WITH_GUIDELINES(
      "%s '%s' Parameter Name not aligned with Guidelines, Parameter Name [%s], Resource ID [%s]."
          + " As a result, VF/VFC Profile may miss this information"),
  /* Contrail validator messages*/
  MERGE_OF_CONTRAIL2_AND_CONTRAIL3_RESOURCES(
      "HEAT Package includes both Contrail 2 and Contrail 3 resources. "
          + "Contrail 2 resources can be found in %s. Contrail 3 resources can be found in %s"),
  CONTRAIL_VM_TYPE_NAME_NOT_ALIGNED_WITH_NAMING_CONVENSION(
      "Service Template naming convention in Image and Flavor "
          + "properties is not consistent in Resource, Resource ID %s");

  private String errorMessage;

  Messages(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

}

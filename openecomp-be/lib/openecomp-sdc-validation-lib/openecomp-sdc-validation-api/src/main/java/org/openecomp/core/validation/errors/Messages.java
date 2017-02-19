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

package org.openecomp.core.validation.errors;

public enum Messages {
  INVALID_ZIP_FILE("Invalid zip file"),
  /* upload errors */

  //NO_ZIP_UPLOADED("No zip file was uploaded or zip file doesn't exist"),
  ZIP_SHOULD_NOT_CONTAIN_FOLDERS("Zip file should not contain folders"),
  MANIFEST_NOT_EXIST("Manifest doesn't exist"),
  NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST("no zip file was uploaded or zip file doesn't exist"),


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

  /* content errors*/
  INVALID_YAML_FORMAT("Invalid YAML format - %s"),
  INVALID_YAML_FORMAT_REASON("Invalid YAML format Problem - [%s]"),
  EMPTY_YAML_FILE("empty yaml"),
  GENERAL_YAML_PARSER_ERROR("general parser error"),
  GENERAL_HEAT_PARSER_ERROR("general parser error"),
  INVALID_HEAT_FORMAT_REASON("Invalid HEAT format problem - [%s]"),
  MISSING_RESOURCE_IN_DEPENDS_ON("a Missing resource in depend On Missing Resource ID [%s]"),
  REFERENCED_PARAMETER_NOT_FOUND("Referenced parameter - %s - not found, used in resource - %s"),
  GET_ATTR_NOT_FOUND("get_attr attribute not found - %s in resource %s"),
  MISSING_PARAMETER_IN_NESTED(
      "Referenced parameter not found in nested file - %s, resource name - %s, "
              + "parameter name - %s"),
  NESTED_LOOP("Nested files loop - %s"),
  MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT("Resource Port %s exceed allowed relations from NovaServer"),
  SERVER_NOT_DEFINED_FROM_NOVA("Missing server group definition - %s, %s"),
  WRONG_POLICY_IN_SERVER_GROUP("Wrong policy in server group - %s"),
  MISSING_IMAGE_AND_FLAVOR("Missing both Image and Flavor in NOVA Server - %s"),
  ENV_INCLUDES_PARAMETER_NOT_IN_HEAT("Env file %s includes a parameter not in HEAT - %s"),
  PARAMETER_ENV_VALUE_NOT_ALIGN_WITH_TYPE("Parameter env value %s not align with type"),
  PARAMETER_DEFAULT_VALUE_NOT_ALIGN_WITH_TYPE(
      "Parameter - %s default value not align with type %s"),
  INVALID_RESOURCE_TYPE("A resource has an invalid or unsupported type - %s, Resource ID [%s]"),
  ARTIFACT_FILE_NOT_REFERENCED("Artifact file is not referenced."),
  SERVER_OR_SECURITY_GROUP_NOT_IN_USE("%s not in use, Resource Id [%s]"),
  PORT_NO_BIND_TO_ANY_NOVA_SERVER("Port not bind to any NOVA Server, Resource Id [%s]"),
  INVALID_GET_RESOURCE_SYNTAX(
      "invalid get_resource syntax is in use - %s , get_resource function should"
              + " get the resource id of the referenced resource"),
  INVALID_RESOURCE_GROUP_TYPE(
      "OS::Heat::ResourceGroup resource with resource_def which is not "
              + "pointing to nested heat file is not supported,"
              + " Resource ID [%s], resource_def type [%s]"),

  /* warnings */
  REFERENCED_RESOURCE_NOT_FOUND("Referenced resource - %s not found"),
  MISSING_GET_PARAM("Missing get_param in %s, Resource Id [%s]"),

  /*Ecomp Guide lines*/
  MISSING_NOVA_SERVER_METADATA("Missing Nova Server Metadata property Resource id [%s]"),
  MISSING_NOVA_SERVER_VNF_ID("Missing VNF_ID Resource id [%s]"),
  MISSING_NOVA_SERVER_VF_MODULE_ID("Missing VF_MODULE_ID, Resource id [%s]"),
  NETWORK_PARAM_NOT_ALIGNED_WITH_GUIDE_LINE(
      "Network Parameter Name not aligned with Guidelines Parameter Name [%s] Resource ID [%s]"),
  MISSIN_BASE_HEAT_FILE(
      "Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources"),
  MULTI_BASE_HEAT_FILE("Multi Base HEAT. Expected only one. Files %s."),
  RESOURCE_NOT_DEFINED_IN_OUTPUT(
      "Resource is not defined as output and thus cannot be Shared. resource id - %s"),
  RESOURCE_CONNECTED_TO_TWO_EXTERNAL_NETWORKS_WITH_SAME_ROLE(
      "A resource is connected twice to the same network role Resource ID [%s] Network Role [%s]."),
  VOLUME_HEAT_NOT_EXPOSED("Volume is not defined as output and thus cannot be attached %s"),
  FLOATING_IP_NOT_IN_USE("OS::Neutron::FloatingIP is in use, Resource ID [%s]"),
  FIXED_IPS_NOT_ALIGNED_WITH_GUIDE_LINES("Fixed_IPS not aligned with Guidelines, Resource ID [%s]"),
  NOVA_SERVER_NAME_NOT_ALIGNED_WITH_GUIDE_LINES(
      "Server Name not aligned with Guidelines, Resource ID [%s]"),
  AVAILABILITY_ZONE_NOT_ALIGNED_WITH_GUIDE_LINES(
      "Server Availability Zone not aligned with Guidelines, Resource ID [%s]"),
  WRONG_IMAGE_OR_FLAVOR_NAME_NOVA_SERVER("Wrong %s name format in NOVA Server, Resource ID [%s]");


  private String errorMessage;

  Messages(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }


}

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

package org.openecomp.sdc.generator.data;

public class GeneratorConstants {

  public static final String GENERATOR_AAI_GENERATED_ARTIFACT_EXTENSION = "xml";

  //Error codes
  public static final String GENERATOR_INVOCATION_ERROR_CODE =
      "ARTIFACT_GENERATOR_INVOCATION_ERROR";


  //Error Constants
  public static final String GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION =
      "Invalid Client Configuration";
  public static final String GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED =
      "Unable to generate artifacts for the provided input";
  public static final String GENERATOR_ERROR_SERVICE_INSTANTIATION_FAILED =
      "Artifact Generation Service Instantiation failed";

  //AAI Generator Error Messages
  public static final String GENERATOR_AAI_ERROR_CHECKSUM_MISMATCH =
      "Checksum Mismatch for file : %s";
  public static final String GENERATOR_AAI_ERROR_INVALID_TOSCA =
      "Invalid format for Tosca YML  : %s";
  public static final String GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION =
      "Invalid Service/Resource definition mandatory attribute <%s> missing in Artifact: <%s>";
  public static final String GENERATOR_AAI_ERROR_INVALID_ID =
      "Invalid value for mandatory attribute <%s> in Artifact: <%s>";
  public static final String GENERATOR_AAI_ERROR_UNSUPPORTED_WIDGET_OPERATION =
      "Operation Not Supported for Widgets";
  public static final String GENERATOR_AAI_ERROR_MISSING_SERVICE_TOSCA =
      "Service tosca missing from list of input artifacts";
  public static final String GENERATOR_AAI_ERROR_NULL_RESOURCE_VERSION_IN_SERVICE_TOSCA =
      "Invalid Service definition mandatory attribute version missing for resource with UUID: <%s>";

  public static final String GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA =
      "Cannot generate artifacts. Invalid Resource version in Service tosca for resource with "
          + "UUID: "
          + "<%s>";
  public static final String GENERATOR_AAI_ERROR_MISSING_RESOURCE_TOSCA =
      "Cannot generate artifacts. Resource Tosca missing for resource with UUID: <%s>";

  public static final String GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION =
      "Cannot generate artifacts. Service version is not specified";

  public static final String GENERATOR_AAI_INVALID_SERVICE_VERSION =
      "Cannot generate artifacts. Service version is incorrect";

  //Logging constants
  public static final String REQUEST_ID = "uuid";
  public static final String SERVICE_INSTANCE_ID = "serviceInstanceID";
  public static final String PARTNER_NAME = "userId";
  public static final String SERVICE_NAME = "ServiceName";
  public static final String INSTANCE_UUID = "InstanceUUID";
  public static final String REMOTE_HOST = "RemoteHost";
  public static final String CLIENT_IP = "ClientIP";
  public static final String CATEGORY_LOG_LEVEL = "level";
  public static final String STATUS_CODE = "StatusCode";
  public static final String RESPONSE_CODE = "ResponseCode";
  public static final String RESPONSE_DESCRIPTION = "ResponseDescription";
  public static final String ELAPSED_TIME = "ElapsedTime";
  public static final String BEGIN_TIMESTAMP = "BeginTimestamp";
  public static final String TARGET_SERVICE_NAME = "TargetServiceName";
  public static final String TARGET_ENTITY = "TargetEntity";
  public static final String END_TIMESTAMP = "EndTimestamp";
  public static final String ERROR_CATEGORY = "ErrorCategory";
  public static final String ERROR_CODE = "ErrorCode";
  public static final String ERROR_DESCRIPTION = "ErrorDescription";
  public static final String MDC_SDC_INSTANCE_UUID = "SDC_INSTANCE_UUID";
  public static final String SERVICE_METRIC_BEGIN_TIMESTAMP = "SERVICE-METRIC-BEGIN-TIMESTAMP";
  public static final String LOCAL_ADDR = "localAddr"; //map ServerIPAddress from loggingfilter
  public static final String BE_FQDN = "beFqdn"; //map ServerFQDN from logging filter
  public static final String ARTIFACT_MODEL_INFO = "ARTIFACT_MODEL_INFO";

  public static final String GENERATOR_ERROR_CODE = "300F";
  public static final String GENERATOR_PARTNER_NAME = "SDC Catalog";
  public static final String GENERATOR_METRICS_TARGET_ENTITY = "SDC Catalog";
  public static final String GENERATOR_METRICS_FAILURE_RESPONSE_CODE = "300E";
  public static final String GENERATOR_METRICS_FAILURE_RESPONSE_DESC = "Artifact generation "
      + "failed for artifact type %s";
  public static final String GENERATOR_METRICS_SUCCESS_RESPONSE_CODE = "010";
  public static final String GENERATOR_METRICS_SUCCESS_RESPONSE_DESC = "Artifacts for type %s "
      + "were generated successfully";
  public static final String GENERATOR_AUDIT_NO_ARTIFACT_TYPE_RESPONSE_DESC = "No Artifact Type "
      + "found" ;

  //AAI Generator Error Messages for Logging
  public static final String GENERATOR_AAI_ERROR_INVALID_TOSCA_MSG =
      "Invalid format for Tosca YML";
  public static final String GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION_MSG =
      "Invalid Service/Resource definition mandatory attribute";
  public static final String GENERATOR_AAI_ERROR_MISSING_SERVICE_TOSCA_MSG =
      "Service tosca missing from list of input artifacts";
  public static final String GENERATOR_ERROR_INVALID_CLIENT_CONFIGURATION_MSG =
      "Invalid Client Configuration";
  public static final String GENERATOR_ERROR_ARTIFACT_GENERATION_FAILED_MSG =
      "Unable to generate artifacts for the provided input";
  public static final String GENERATOR_AAI_CONFIGFILE_NOT_FOUND =
      "Cannot generate artifacts. Artifact Generator Configuration file not found at %s";
  public static final String GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND =
      "Cannot generate artifacts. artifactgenerator.config system property not configured";
  public static final String GENERATOR_AAI_CONFIGLPROP_NOT_FOUND =
      "Cannot generate artifacts. Widget configuration not found for %s";
  public static final String GENERATOR_AAI_PROVIDING_SERVICE_MISSING =
      "Cannot generate artifacts. Providing Service is missing for allotted resource %s";
  public static final String GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING =
      "Cannot generate artifacts. Providing Service Metadata is missing for allotted resource %s";
  public static final int ID_LENGTH = 36;
}

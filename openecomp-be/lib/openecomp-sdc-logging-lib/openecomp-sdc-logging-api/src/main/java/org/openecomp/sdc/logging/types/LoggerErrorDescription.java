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

package org.openecomp.sdc.logging.types;


public class LoggerErrorDescription {
  public static final String ARTIFACT_NOT_REFERENCED = "Artifact not referenced";
  public static final String WRONG_FILE_EXTENSION = "Wrong file extention";
  public static final String INVALID_HEAT_FORMAT = "Invalid HEAT format";
  public static final String PARAMETER_NOT_FOUND = "Referenced parameter not found";
  public static final String ENV_PARAMETER_NOT_IN_HEAT = "env includes parameter not in HEAT";
  public static final String PARAMETER_DEFAULT_VALUE_NOT_ALIGNED_WITH_TYPE =
      "Parameter default value not alinged with type";
  public static final String INVALID_RESOURCE_TYPE = "Invalid resource type";
  public static final String INVALID_GET_RESOURCE_SYNTAX = "Invalid get resource syntax";
  public static final String RESOURCE_NOT_FOUND = "Resource not found";
  public static final String INVALID_INDEX_VAR = "Invalid Resource Group index var";
  public static final String MISSING_FILE = "Missing file";
  public static final String MISSING_NOVA_PROPERTIES = "Missing NOVA server properties";
  public static final String NESTED_LOOP = "Found nested loop";
  public static final String MISSING_PARAMETER_IN_NESTED = "Missing parameter in nested file";
  public static final String WRONG_VALUE_ASSIGNED_NESTED_PARAMETER =
      "Wrong value assigned in nested parameters";
  public static final String SERVER_NOT_DEFINED_NOVA = "Server not defined from nova";
  public static final String WRONG_POLICY_SERVER_GROUP = "Wrong policy in server group";
  public static final String SERVER_GROUP_SECURITY_GROUP_NOT_IN_USE =
      "Server group or security group is not in use";
  public static final String NETWORK_ATTACH_POLICY_NOT_IN_USE =
      "Network Attach Policy is not in use";
  public static final String NO_BIND_FROM_PORT_TO_NOVA = "No bind from port to nova sever";
  public static final String PORT_BINDS_MORE_THAN_ONE_NOVA =
      "Port binds to more than one nova sever";
  public static final String MISSING_RESOURCE_DEPENDS_ON = "Missing resource in depends_on";
  public static final String GET_ATTR_NOT_FOUND = "get_attr not found";
  public static final String MISSING_GET_PARAM = "Missing get_param";
  public static final String EMPTY_FILE = "Empty file";
  public static final String VALIDATE_FILE = "Can't validate file";
  public static final String INVALID_FILE_TYPE = "Invalid file type";
  public static final String INVALID_ZIP = "Invalid zip file";
  public static final String INVALID_VES_FILE = "Invalid VES file";



  public static final String RESOURCE_UNIQUE_NETWORK_ROLE =
      "Resource connects to two networks with the same role";
  public static final String NAME_NOT_ALIGNED_WITH_GUIDELINES = "Name not aligned with guidelines";
  public static final String VOLUME_FILE_NOT_EXPOSED = "Volume file not exposed";
  public static final String RESOURCE_NOT_DEFINED_AS_OUTPUT = "Resource is not defined as output";
  public static final String FLOATING_IP_IN_USE = "Floating IP resource type is in use";
  public static final String MISSING_BASE_HEAT = "Missing base HEAT file";
  public static final String MULTI_BASE_HEAT = "Multi base HEAT file";

  public static final String EXTRACT_COMPOSITION_DATA = "Can't extract composition data";

  public static final String MERGE_CONTRAIL_2_AND_3 =
      "Merge of Contrail 2 and Contrail 3 resources";
  public static final String CONTRAIL_2_IN_USE = "Contrail 2 resource is in use";

  public static final String INVALID_MANIFEST = "Invalid manifest file";

  public static final String INVALID_YAML_FORMAT = "Invalid YAML format";

  public static final String CREATE_SERVICE_ARTIFACT = "Can't create service artifact";
  public static final String CREATE_ENRICH_SERVICE_ARTIFACT =
      "Can't create enriched service artifact";
  public static final String CREATE_SERVICE_TEMPLATE = "Can't create service template";
  public static final String CREATE_ENRICH_SERVICE_TEMPLATE =
      "Can't create enriched service template";
  public static final String GET_SERVICE_MODEL = "Can't get service model";
  public static final String MISSING_MANDATORY_PROPERTY = "Missing mandatory property";
  public static final String INVALID_PROPERTY = "Invalid Property";
  public static final String INVALID_ADD_ACTION = "Invalid add action";
  public static final String UNSUPPORTED_ENTITY = "Unsupported entity";
  public static final String CREATE_CSAR = "Can't create CSAR file";
  public static final String PACK_ARTIFACTS = "Can't pack artifacts into CSAR file";

  public static final String TRANSLATE_HEAT = "Can't translate HEAT file";
  public static final String RESOURCE_FILE_READ_ERROR = "Can't read resource file from class path.";
  public static final String FAILED_TO_GENERATE_GLOBAL_TYPES = "Failed to generate globalTypes";

  public static final String CHECKOUT_ENTITY = "Can't checkout versionable entity";
  public static final String SUBMIT_ENTITY = "Can't submit versionable entity";
  public static final String ENTITY_NOT_FOUND = "Versionable entity not found";
  public static final String INVALID_VALUE = "Invalid value";

  public static final String UNSUPPORTED_OPERATION = "Unsupported operation";
  public static final String INVALID_JSON = "Invalid JSON format";

  public static final String INSERT_INTO_APPLICATION_CONFIG =
      "Can't insert value into application config table";

  public static final String CANT_HEAL = "Can't perform healing operation";
}

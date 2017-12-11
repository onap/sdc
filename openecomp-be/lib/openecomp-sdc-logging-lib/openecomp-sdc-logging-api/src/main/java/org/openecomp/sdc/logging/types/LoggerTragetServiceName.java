/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.logging.types;


public class LoggerTragetServiceName {
  /*HEAT validator*/
  public static final String VALIDATE_HEAT_BASE_STRUCTURE = "Validate Heat Base Structure";
  public static final String VALIDATE_ARTIFACTS_EXISTENCE = "Validate Artifacts Existence";
  public static final String VALIDATE_RESOURCE_REFERENCE_EXISTENCE =
      "Validate Resource Reference Existence";
  public static final String VALIDATE_PARAMETER_REFERENCE_EXITENCE =
      "Validate Parameter Reference Existence";
  public static final String VALIDATE_GET_ATTR_FROM_NESTED = "Validate get_attr From Nested";
  public static final String VALIDATE_ENV_FILE = "Validate env File";
  public static final String VALIDATE_BASE_PORTS_EXISTENCE = "Validate Base Ports Existence";
  public static final String VALIDATE_ASSIGNED_VALUES_FOR_NOVA_IMAGE_FLAVOR =
      "Validate Assigned Value For Nova Server Image Or Flavor";
  public static final String VALIDATE_NOVA_SERVER_PORT_BINDING =
      "Validate Ports Binding From Nova Server";
  public static final String VALIDATE_SERVER_GROUP_EXISTENCE =
      "Validate Nova Server Group Existence";
  public static final String VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS =
      "Validate All Properties Match Nested Parameters";
  public static final String VALIDATE_NESTING_LOOPS = "Validate Nested Loops";
  public static final String VALIDATE_NOVA_SEVER_GROUP_POLICY = "Validate Nova Server Group Policy";
  public static final String VALIDATE_RESOURCE_GROUP_TYPE = "Validate Resource Group Resource Type";
  public static final String VALIDATE_ALL_SECURITY_GROUP_USED =
      "Validate All Security Group Are Used From Neutron Port Resource";
  public static final String VALIDATE_CONTRAIL_ATTACH_POLICY_TYPE =
      "Validate Contrail Attach Policy Type";
  public static final String VALIDATE_SECURITY_GROUP_FROM_BASE_OUTPUT =
      "Validate Security Groups From Base File Outputs";
  public static final String CHECK_FOR_ORPHAN_PORTS = "Validate All Referenced Ports Are Used";
  public static final String CHECK_FOR_ORPHAN_ARTIFACTS = "Validate All Artifacts Are Referenced";
  public static final String CHECK_FOR_VALID_FILE_EXTENTION = "Validate File Extention";
  public static final String VALIDATE_PARAMTER_DEFAULT_MATCH_TYPE =
      "Validate Parameter Default Aligns With Type";
  public static final String VALIDATE_ENV_PARAMETER_MATCH_TYPE =
      "Validate env Parameter Matches Type";
  public static final String VALIDATE_RESOURCE_TYPE = "Validate Resource Type";
  public static final String VALIDATE_FILE_EXISTENCE = "Validate File Exitence";
  public static final String GET_RESOURCE_LIST_BY_TYPE =
      "Get All Resources By Specific Resource Type";
  public static final String VALIDATE_ALL_SERVER_GROUP_OR_SECURITY_GROUP_IN_USE =
      "Validate All Security Group Or Server Group Are In Use";
  public static final String VALIDATE_ATTACH_POLICY_IN_USE =
      "Validate All Network Attach Policies Are In Use";
  public static final String CHECK_RESOURCE_DEPENDS_ON = "Check Resource dependes_on";
  public static final String GET_SHARED_RESOURCES_FROM_OUTPUTS =
      "Get Shared Resources From Outputs";
  public static final String VALIDATE_GET_RESOURCE = "Validate get_resource Syntax";
  public static final String VALIDATE_FILE = "Validate File";

  /*manifest validator*/
  public static final String VALIDATE_MANIFEST_CONTENT = "Validate Manifest Content";
  public static final String VALIDATE_FILE_IN_ZIP = "Validate File In Zip";
  public static final String VALIDATE_FILE_IN_MANIFEST = "Validate File In Manifest";
  public static final String VALIDATE_FILE_TYPE_AND_NAME =
      "Validate File Type And Name In Manifest";
  public static final String SCAN_MANIFEST_STRUCTURE = "Scan Manifest Structure";
  public static final String VALIDATE_FILE_TYPE = "Validate File Type";

  /*YAML validator*/
  public static final String VALIDATE_YAML_CONTENT = "Validate YAML Content";


  /*OPENECOMP guide line validator*/
  public static final String VALIDATE_BASE_FILE =
      "Validate If All Resources In Base File Are Exposed";
  public static final String VALIDATE_FORBIDDEN_RESOURCE = "Validate If Resource Type Is Forbidden";
  public static final String VALIDATE_FIXED_IPS_NAME = "Validate fixed_ips Naming Convention";
  public static final String VALIDATE_IMAGE_AND_FLAVOR_NAME =
      "Validate image And flavor Naming Convention";
  public static final String VALIDATE_NOVA_SERVER_NAME = "Validate Nova Server Naming Convention";
  public static final String VALIDATE_AVAILABILITY_ZONE_NAME =
      "Validate availability_zone Naming Convention";
  public static final String VALIDATE_NOVA_META_DATA_NAME =
      "Validate Nova Server Meta Data Naming Convention";
  public static final String VALIDATE_PORT_NETWORK_NAME = "Validate Port Network Naming Convention";
  public static final String VALIDATE_VM_SYNC_IN_IMAGE_FLAVOR =
      "Validate VM Name Is Sync In Image And Flavor";
  public static final String VALIDATE_RESOURCE_NETWORK_UNIQUE_ROLW =
      "Validate Nova Server Unique Role";
  public static final String VALIDATE_VOLUME_FILE = "Validate HEAT Volume File";


  public static final String VALIDATE_CONTRAIL_VM_NAME =
      "Validate Contrail VM Name Aligned With Guidelines";
  public static final String MERGE_OF_CONTRAIL_2_AND_3 = "Merge Of Contrail 2 And Contrail 3";
  public static final String CONTRAIL_2_IN_USE = "Validate Contrail 2 Resource Is In Use";


  public static final String VALIDATE_MANIFEST_PRE_CONDITION = "Validate Manifest Pre-Condition";
  public static final String VALIDATE_HEAT_FORMAT = "Validate HEAT Format";

  public static final String CREATE_ENTITY = "Create Entity";
  public static final String CHECKOUT_ENTITY = "Checkout Entity";
  public static final String UNDO_CHECKOUT_ENTITY = "Undo Checkout Entity";
  public static final String CHECKIN_ENTITY = "Checkin Entity";
  public static final String SUBMIT_ENTITY = "Submit Entity";
  public static final String DELETE_ENTITY = "Delete Entity";
  public static final String UNDO_DELETE_ENTITY = "Undo Delete Entity";

  public static final String SUBMIT_VSP = "Submit VSP";
  public static final String GET_VSP = "Get VSP";
  public static final String ADD_VSP = "Add VSP";
  public static final String DELETE_VSP = "Delete VSP";
  public static final String UPDATE_VSP = "Update VSP";
  public static final String ENRICH = "Enrich";
  public static final String UPLOAD_FILE = "Upload file";
  public static final String GET_UPLOADED_HEAT = "Get Uploaded HEAT File";
  public static final String GET_TRANSLATED_FILE = "Get Translated File";
  public static final String CREATE_TRANSLATED_FILE = "Create Translated File";
  public static final String CREATE_PACKAGE = "Create Package";

  public static final String SELF_HEALING = "Self Healing";
  public static final String WRITE_ARTIFACT_XML = "Write Artifact To XML File";

  public static final String GET_VLM = "Get VLM";

  public static final String VALIDATE_MONITORING_FILE = "Validate Monitoring File";
  public static final String UPLOAD_MONITORING_FILE = "Upload Monitoring file";

  public static final String GET_VERSION_INFO = "Get version info";

  public static final String CREATE_NETWORK = "Create Network";
  public static final String DELETE_NETWORK = "Delete Network";

  public static final String CREATE_COMPONENT = "Create Component";
  public static final String DELETE_COMPONENT = "Delete Component";
  public static final String UPDATE_COMPONENT = "Update Component";

  public static final String GET_PROCESS_ARTIFACT = "Get Process Artifact";
  public static final String UPLOAD_PROCESS_ARTIFACT = "Upload Process Artifact";

  public static final String CREATE_NIC = "Create Nic";
  public static final String DELETE_NIC = "Delete Nic";
  public static final String UPDATE_NIC = "Update Nic";

  public static final String CREATE_DEPLOYMENT_FLAVOR = "Create Deployment Flavor";
  public static final String DELETE_DEPLOYMENT_FLAVOR = "Delete Deployment Flavor";
  public static final String UPDATE_DEPLOYMENT_FLAVOR = "Update Deployment Flavor";

  public static final String EXTRACT_COMPOSITION_DATA = "Extract Composition Data";

  public static final String TRANSLATE_RESOURCE = "Translate Resource";
  public static final String GET_RESOURCE = "Get Resource";
  public static final String VALIDATE_HEAT_BEFORE_TRANSLATE = "Validate HEAT Before Translate";

  public static final String CREATE_SERVICE_ARTIFACT = "Create Service Artifact In DB";
  public static final String CREATE_SERVICE_ENRICH_ARTIFACT =
      "Create Service Enriched Artifact In DB";
  public static final String CREATE_SERVICE_TEMPLATE = "Create Service Template In DB";
  public static final String CREATE_ENRICH_SERVICE_TEMPLATE =
      "Create Enriched Service Template In DB";
  public static final String GET_SERVICE_MODEL = "Get service model";
  public static final String GET_SERVICE_TEMPLATE = "Get Service Template";
  public static final String CREATE_REQUIREMENT_ASSIGNMENT = "Create Requierment Assignment";
  public static final String GENERATE_TRANSLATED_ID = "Generate Translated Id";
  public static final String ADD_ENTITIES_TO_TOSCA = "Add Entities To TOSCA";
  public static final String CREATE_CSAR = "Create CSAR";
  public static final String PACK_ARTIFACTS = "Pack Artifacts Into CSAR File";

  public static final String VALIDATE_CHOICE_VALUE = "Check Chosen Value";

  public static final String INSERT_INTO_APPLICATION_CONFIG =
      "Insert Value Into Application Config";

  public static final String VALIDATE_FILE_DATA_STRUCTURE = "Validate Manifest Sent From User";
  public static final String CREATE_IMAGE = "Create Image";
  public static final String DELETE_IMAGE = "Delete Image";
  public static final String UPDATE_IMAGE = "Update Image";

  public static final String CREATE_COMPUTE = "Create Compute";
  public static final String UPDATE_COMPUTE = "Update Compute";
  public static final String DELETE_COMPUTE = "Delete Compute";

  public static final String COLLECT_MANUAL_VSP_TOSCA_DATA = "Collect Manual Vsp Tosca data";
  public static final String GENERATE_MANUAL_VSP_TOSCA = "Generate Manual Vsp Tosca";

  public static final String VALIDATE_DATE_RANGE = "Validate Date Range";

  public static final String CREATE_COMPONENT_DEPENDENCY_MODEL = "Create Component Dependency "
      + "Model";

  public static final String READ_RESOURCE_FILE = "Read Resource File";
}

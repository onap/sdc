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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

/**
 * Created by TALIO on 4/24/2016.
 */
public class VendorSoftwareProductErrorCodes {

  public static final String VSP_NOT_FOUND = "VSP_NOT_FOUND";
  public static final String VSP_INVALID = "VSP_INVALID";
  public static final String VFC_INVALID = "VFC_INVALID";
  public static final String FAILED_TO_CREATE_VSP = "FAILED_TO_CREATE_VSP";

  public static final String UPLOAD_INVALID = "UPLOAD_INVALID";

  public static final String PACKAGE_NOT_FOUND = "PACKAGE_NOT_FOUND";

  public static final String PACKAGE_INVALID = "PACKAGE_INVALID";
  public static final String VSP_COMPOSITION_EDIT_NOT_ALLOWED = "VSP_COMPOSITION_EDIT_NOT_ALLOWED";

  public static final String CREATE_PACKAGE_FOR_NON_FINAL_VSP = "CREATE_PACKAGE_FOR_NON_FINAL_VSP";

  public static final String TRANSLATION_FILE_CREATION = "TRANSLATION_FILE_CREATION";

  public static final String HEAT_PACKAGE_FILE_CREATION = "HEAT_PACKAGE_FILE_CREATION";

  public static final String TOSCA_ENTRY_NOT_FOUND = "TOSCA_ENTRY_NOT_FOUND";
  public static final String TOSCA_INVALID_SUBSTITUTE_NODE_TEMPLATE =
      "TOSCA_INVALID_SUBSTITUTE_NODE_TEMPLATE";

  public static final String MONITORING_UPLOAD_INVALID = "MONITORING_UPLOAD_INVALID";

  public static final String ORCHESTRATION_NOT_FOUND = "ORCHESTRATION_NOT_FOUND";


  public static final String CYCLIC_DEPENDENCY_IN_COMPONENTS = "CYCLIC_DEPENDENCY_IN_COMPONENTS";

  public static final String INVALID_COMPONENT_RELATION_TYPE = "INVALID_COMPONENT_RELATION_TYPE";

  public static final String NO_SOURCE_COMPONENT = "NO_SOURCE_COMPONENT";

  public static final String SAME_SOURCE_TARGET_COMPONENT = "SAME_SOURCE_TARGET_COMPONENT";

  public static final String VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED =
      "VSP_ONBOARD_METHOD_UPDATE_NOT_ALLOWED";

  public static final String DUPLICATE_NIC_NAME_NOT_ALLOWED = "DUPLICATE_NIC_NAME_NOT_ALLOWED";
  public static final String NIC_NAME_FORMAT_NOT_ALLOWED = "NIC_NAME_FORMAT_NOT_ALLOWED";

  public static final String NULL_NETWORKID_NOT_ALLOWED = "NULL_NETWORKID_NOT_ALLOWED";

  public static final String NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK =
      "NETWORKID_NOT_ALLOWED_FOR_EXTERNAL_NETWORK";

  public static final String VFC_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING =
      "VFC_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING";
  public static final String VSP_VFC_COUNT_EXCEED = "VSP_VFC_COUNT_EXCEED";
  public static final String VSP_VFC_DUPLICATE_NAME = "VSP_VFC_DUPLICATE_NAME";
  public static final String VSP_INVALID_ONBOARDING_METHOD = "VSP_INVALID_ONBOARDING_METHOD";
  public static final String
      ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING = "ADD_NIC_NOT_ALLOWED_IN_HEAT_ONBOARDING";
  public static final String NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK
      = "NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK";

  public static final String VFC_ATTRIBUTE_UPDATE_NOT_ALLOWED =
      "VFC_ATTRIBUTE_UPDATE_NOT_ALLOWED";
  public static final String NETWORK_TYPE_UPDATE_NOT_ALLOWED
      = "NETWORK_TYPE_UPDATE_NOT_ALLOWED";
  public static final String DELETE_NIC_NOT_ALLOWED = "DELETE_NIC_NOT_ALLOWED";

  public static final String CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING =
      "CREATE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING";

  public static final String DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING =
      "DELETE_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING";

  public static final String EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING =
      "EDIT_DEPLOYMENT_FLAVOR_NOT_ALLOWED_IN_HEAT_ONBOARDING";

  public static final String FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR =
      "FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR";

  public static final String
      ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING = "ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING";

  public static final String DUPLICATE_IMAGE_NAME_NOT_ALLOWED = "DUPLICATE_IMAGE_NAME_NOT_ALLOWED";
  public static final String DUPLICATE_IMAGE_VERSION_NOT_ALLOWED = "DUPLICATE_IMAGE_VERSION_NOT_ALLOWED";
  public static final String IMAGE_NAME_FORMAT_NOT_ALLOWED = "IMAGE_NAME_FORMAT_NOT_ALLOWED";
  public static final String VFC_IMAGE_INVALID_FORMAT = "VFC_IMAGE_INVALID_FORMAT";
  public static final String FEATURE_GROUP_NOT_EXIST_FOR_VSP = "FEATURE_GROUP_NOT_EXIST_FOR_VSP";

  public static final String
      ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING = "ADD_COMPUTE_NOT_ALLOWED_IN_HEAT_ONBOARDING";
  public static final String VFC_COMPUTE_INVALID_FORMAT = "VFC_COMPUTE_INVALID_FORMAT";
  public static final String UPDATE_COMPUTE_NOT_ALLOWED = "UPDATE_COMPUTE_NOT_ALLOWED";
  public static final String
      INVALID_COMPONENT_COMPUTE_ASSOCIATION = "INVALID_COMPONENT_COMPUTE_ASSOCIATION";
  public static final String SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED
      = "SAME_VFC_ASSOCIATION_MORE_THAN_ONCE_NOT_ALLOWED";
  public static final String DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED
      = "DUPLICATE_DEPLOYMENT_FLAVOR_MODEL_NOT_ALLOWED";
  public static final String DEPLOYMENT_FLAVOR_NAME_FORMAT_NOT_ALLOWED =
          "DEPLOYMENT_FLAVOR_NAME_FORMAT_NOT_ALLOWED";
  public static final String INVALID_COMPUTE_FLAVOR_ID = "INVALID_COMPUTE_FLAVOR_ID";
  public static final String DUPLICATE_COMPUTE_NAME_NOT_ALLOWED = "DUPLICATE_COMPUTE_NAME_NOT_ALLOWED";
  public static final String COMPUTE_NAME_FORMAT_NOT_ALLOWED = "COMPUTE_NAME_FORMAT_NOT_ALLOWED";

  public static final String DELETE_IMAGE_NOT_ALLOWED = "DELETE_IMAGE_NOT_ALLOWED";
  public static final String UPDATE_IMAGE_NOT_ALLOWED = "UPDATE_IMAGE_NOT_ALLOWED";

  public static final String INVALID_EXTENSION = "INVALID_EXTENSION";

}

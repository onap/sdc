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

  public static final String MIB_UPLOAD_INVALID = "MIB_UPLOAD_INVALID";

  public static final String ORCHESTRATION_NOT_FOUND = "ORCHESTRATION_NOT_FOUND";


  public static final String CYCLIC_DEPENDENCY_IN_COMPONENTS = "CYCLIC_DEPENDENCY_IN_COMPONENTS";

  public static final String INVALID_COMPONENT_RELATION_TYPE = "INVALID_COMPONENT_RELATION_TYPE";

  public static final String NO_SOURCE_COMPONENT = "NO_SOURCE_COMPONENT";

  public static final String SAME_SOURCE_TARGET_COMPONENT = "SAME_SOURCE_TARGET_COMPONENT";

}

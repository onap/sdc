/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.tosca.errors;

class ToscaErrorCodes {

  private ToscaErrorCodes() {
    throw new IllegalStateException("Utility class");
  }

  static final String INVALID_SUBSTITUTE_NODE_TEMPLATE = "INVALID_SUBSTITUTE_NODE_TEMPLATE";
  static final String INVALID_SUBSTITUTION_SERVICE_TEMPLATE =
      "INVALID_SUBSTITUTION_SERVICE_TEMPLATE";
  static final String TOSCA_ENTRY_NOT_FOUND = "TOSCA_ENTRY_NOT_FOUND";
  static final String MISSING_SUBSTITUTION_MAPPING_FOR_REQ_CAP =
      "MISSING_SUBSTITUTION_MAPPING_FOR_REQ_CAP";
  static final String TOSCA_INVALID_SUBSTITUTE_NODE_TEMPLATE =
      "TOSCA_INVALID_SUBSTITUTE_NODE_TEMPLATE";
  static final String TOSCA_INVALID_ADD_ACTION_NULL_ENTITY = "TOSCA_INVALID_ADD_ACTION_NULL_ENTITY";
  static final String INVALID_INTERFACE_VALUE = "INVALID_INTERFACE_VALUE";
  static final String INVALID_TOSCA_FILE = "INVALID_TOSCA_FILE";
  static final String INVALID_TOSCA_META_FILE = "INVALID_TOSCA_META_FILE";
  static final String INVALID_TOSCA_ENTRY_DEF_WAS_NOT_FOUND = "INVALID_TOSCA_ENTRY_DEF_WAS_NOT_FOUND";


}

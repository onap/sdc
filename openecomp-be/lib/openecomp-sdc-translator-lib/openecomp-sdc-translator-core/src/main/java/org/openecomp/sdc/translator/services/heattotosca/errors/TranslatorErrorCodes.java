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

package org.openecomp.sdc.translator.services.heattotosca.errors;


public class TranslatorErrorCodes {
  public static final String MISSING_MANDATORY_PROPERTY = "MISSING_MANDATORY_PROPERTY";
  public static final String HEAT_TO_TOSCA_MAPPING_COLLISION = "HEAT_TO_TOSCA_MAPPING_COLLISION";
  public static final String INCORRECT_RESOURCE_REFERENCE = "INCORRECT_RESOURCE_REFERENCE";
  public static final String REFERENCE_TO_UNSUPPORTED_RESOURCE =
      "REFERENCE_TO_UNSUPPORTED_RESOURCE";
  public static final String NOT_IN_SYNC_NUMBER_OF_INTERFACES = "NOT_IN_SYNC_NUMBER_OF_INTERFACES";
  public static final String INVALID_PROPERTY_VALUE = "INVALID_PROPERTY_VALUE";
  public static final String DUPLICATE_RESOURCE_ID_IN_DIFFERENT_FILES = "DUPLICATE_RESOURCE_ID_IN_DIFFERENT_FILES";
}

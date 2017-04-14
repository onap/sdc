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

/**
 * The type Json mapping error builder.
 */
public class JsonMappingErrorBuilder extends BaseErrorBuilder {

  private static final String JSON_MAPPING_ERROR_ERR_ID = "JSON_MAPPING_ERROR_ERR_ID";
  private static final String JSON_MAPPING_ERROR_ERR_ERR_MSG =
      "Error occurred while parsing the JSON input/body. Further info can be found in the log";

  /**
   * Instantiates a new Json mapping error builder.
   */
  public JsonMappingErrorBuilder() {
    builder.withId(JSON_MAPPING_ERROR_ERR_ID);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(JSON_MAPPING_ERROR_ERR_ERR_MSG));
  }

}

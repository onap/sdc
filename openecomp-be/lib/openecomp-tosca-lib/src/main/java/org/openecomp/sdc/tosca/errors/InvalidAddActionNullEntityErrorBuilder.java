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

package org.openecomp.sdc.tosca.errors;

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * The type Invalid add action null entity error builder.
 */
public class InvalidAddActionNullEntityErrorBuilder {

  private static final String INVALID_ACTION_NULL_ENTITY_ERR_MSG =
      "Invalid action, can't add '%s' to '%s', '%s' entity is NULL.";

  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Invalid add action null entity error builder.
   *
   * @param addedData     the added data
   * @param updatedEntity the updated entity
   */
  public InvalidAddActionNullEntityErrorBuilder(String addedData, String updatedEntity) {
    builder.withId(ToscaErrorCodes.TOSCA_INVALID_ADD_ACTION_NULL_ENTITY);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(
        String.format(INVALID_ACTION_NULL_ENTITY_ERR_MSG, addedData, updatedEntity, updatedEntity));
  }

  /**
   * Build error code.
   *
   * @return the error code
   */
  public ErrorCode build() {
    return builder.build();
  }

}

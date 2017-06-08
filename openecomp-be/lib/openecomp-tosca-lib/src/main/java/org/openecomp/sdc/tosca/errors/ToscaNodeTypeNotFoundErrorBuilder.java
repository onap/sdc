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
 * The type Tosca node type not found error builder.
 */
public class ToscaNodeTypeNotFoundErrorBuilder {
  private static final String ENTRY_NOT_FOUND_MSG =
      "NodeType '%s' or one of its derivedFrom node type hierarchy, "
          + "is not defined in tosca service model";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Tosca node type not found error builder.
   *
   * @param nodeType the node type
   */
  public ToscaNodeTypeNotFoundErrorBuilder(String nodeType) {
    builder.withId(ToscaErrorCodes.TOSCA_ENTRY_NOT_FOUND);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String.format(ENTRY_NOT_FOUND_MSG, nodeType));
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

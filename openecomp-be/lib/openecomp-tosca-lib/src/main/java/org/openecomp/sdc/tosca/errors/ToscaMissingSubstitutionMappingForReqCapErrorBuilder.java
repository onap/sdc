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
 * The type Tosca missing substitution mapping for req cap error builder.
 */
public class ToscaMissingSubstitutionMappingForReqCapErrorBuilder {

  private static final String MISSING_SUBSTITUTION_MAPPING_FOR_REQ_CAP_MSG =
      "Invalid Substitution, Missing Substitution Mapping for %s with Id %s.";
  private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

  /**
   * Instantiates a new Tosca missing substitution mapping for req cap error builder.
   *
   * @param exposedEntry the exposed entry
   * @param id           the id
   */
  public ToscaMissingSubstitutionMappingForReqCapErrorBuilder(MappingExposedEntry exposedEntry,
                                                              String id) {
    builder.withId(ToscaErrorCodes.MISSING_SUBSTITUTION_MAPPING_FOR_REQ_CAP);
    builder.withCategory(ErrorCategory.APPLICATION);
    builder.withMessage(String
        .format(MISSING_SUBSTITUTION_MAPPING_FOR_REQ_CAP_MSG, exposedEntry.getDisplayName(), id));
  }

  /**
   * Build error code.
   *
   * @return the error code
   */
  public ErrorCode build() {
    return builder.build();
  }

  /**
   * The enum Mapping exposed entry.
   */
  public enum MappingExposedEntry {

    CAPABILITY("Capability"),
    REQUIREMENT("Requirement"),;

    private String displayName;

    MappingExposedEntry(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }

  }
}

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

package org.openecomp.sdc.versioning.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

/**
 * The type Requested version invalid error builder.
 */
public class RequestedVersionInvalidErrorBuilder extends BaseErrorBuilder {
  private static final String REQUESTED_VERSION_INVALID_MSG = "Invalid requested version.";

  /**
   * Instantiates a new Requested version invalid error builder.
   */
  public RequestedVersionInvalidErrorBuilder() {
    getErrorCodeBuilder().withId(VersioningErrorCodes.REQUESTED_VERSION_INVALID);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(REQUESTED_VERSION_INVALID_MSG);
  }

/*    private static List<String> toStringList(Set<Version> versions) {
        List<String> versionStrings = new ArrayList<>(versions.size());
        for (Version version : versions) {
            versionStrings.add(version.toString());
        }
        return versionStrings;
    }*/
}

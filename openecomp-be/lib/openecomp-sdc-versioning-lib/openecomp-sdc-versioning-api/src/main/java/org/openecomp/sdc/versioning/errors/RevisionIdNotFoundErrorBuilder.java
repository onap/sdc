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
 * The mandatory field revision id not found error builder.
 */
public class RevisionIdNotFoundErrorBuilder extends BaseErrorBuilder {

  private static final String REVISION_ID_NOT_FOUND_MSG =
      "Mandatory field revision id missing";

  /**
   * Instantiates a new Versionable sub entity not found error builder.
   */
  public RevisionIdNotFoundErrorBuilder() {
    getErrorCodeBuilder().withId(VersioningErrorCodes.MANDATORY_FIELD_REVISION_ID_MISSING);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(REVISION_ID_NOT_FOUND_MSG);
  }


}

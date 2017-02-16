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
 * The type Submit locked entity not allowed error builder.
 */
public class SubmitLockedEntityNotAllowedErrorBuilder extends BaseErrorBuilder {

  private static final String SUBMIT_LOCKED_ENTITY_NOT_ALLOWED_MSG =
      "Versionable entity %s with id %s can not be submitted since it is currently "
          + "locked by user %s.";

  /**
   * Instantiates a new Submit locked entity not allowed error builder.
   *
   * @param entityType  the entity type
   * @param entityId    the entity id
   * @param lockingUser the locking user
   */
  public SubmitLockedEntityNotAllowedErrorBuilder(String entityType, String entityId,
                                                  String lockingUser) {
    getErrorCodeBuilder().withId(VersioningErrorCodes.SUBMIT_LOCKED_ENTITY_NOT_ALLOWED);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(
        String.format(SUBMIT_LOCKED_ENTITY_NOT_ALLOWED_MSG, entityType, entityId, lockingUser));
  }
}

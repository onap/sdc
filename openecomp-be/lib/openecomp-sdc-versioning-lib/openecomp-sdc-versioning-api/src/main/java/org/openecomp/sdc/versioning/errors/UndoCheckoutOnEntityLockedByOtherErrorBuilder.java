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

import static org.openecomp.sdc.versioning.errors.VersioningErrorCodes
    .UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

/**
 * The type Undo checkout on entity locked by other error builder.
 */
public class UndoCheckoutOnEntityLockedByOtherErrorBuilder extends BaseErrorBuilder {

  private static final String UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG =
      "Can not undo checkout on versionable entity %s with id %s since it is checked out by "
          + "other user: %s.";

  /**
   * Instantiates a new Undo checkout on entity locked by other error builder.
   *
   * @param entityType  the entity type
   * @param entityId    the entity id
   * @param lockingUser the locking user
   */
  public UndoCheckoutOnEntityLockedByOtherErrorBuilder(String entityType, String entityId,
                                                       String lockingUser) {
    getErrorCodeBuilder().withId(UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG, entityType, entityId,
            lockingUser));
  }
}

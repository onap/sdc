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
import org.openecomp.sdc.errors.ErrorCategory;

/**
 * The type Edit on entity locked by other error builder.
 */
public class EditOnEntityLockedByOtherErrorBuilder extends BaseErrorBuilder {

    static final String EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG = "Versionable entity %s with id %s can not be edited since it is locked by other user %s.";

    /**
     * Instantiates a new Edit on entity locked by other error builder.
     *
     * @param entityType  the entity type
     * @param entityId    the entity id
     * @param lockingUser the locking user
     */
    public EditOnEntityLockedByOtherErrorBuilder(String entityType, String entityId, String lockingUser) {
        getErrorCodeBuilder().withId(VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER);
        getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
        getErrorCodeBuilder().withMessage(String.format(EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG, entityType, entityId, lockingUser));
    }
}

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
 * The type Entity not exist error builder.
 */
public class EntityNotExistErrorBuilder extends BaseErrorBuilder {

    static final String VERSIONABLE_ENTITY_NOT_EXIST_MSG = "Versionable entity %s with id %s does not exist.";

    /**
     * Instantiates a new Entity not exist error builder.
     *
     * @param entityType the entity type
     * @param entityId   the entity id
     */
    public EntityNotExistErrorBuilder(String entityType, String entityId) {
        getErrorCodeBuilder().withId(VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST);
        getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
        getErrorCodeBuilder().withMessage(String.format(VERSIONABLE_ENTITY_NOT_EXIST_MSG, entityType, entityId));
    }
}

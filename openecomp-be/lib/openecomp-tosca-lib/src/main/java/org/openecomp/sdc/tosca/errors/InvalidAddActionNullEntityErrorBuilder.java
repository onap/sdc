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

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class InvalidAddActionNullEntityErrorBuilder {

    private static final String INVALID_ACTION_NULL_ENTITY_ERR_MSG = "Invalid action, can't add '%s' to '%s', '%s' entity is NULL.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Invalid Action Error builder.
     *
     * @param addedData     Added data
     * @param updatedEntity Updated Entity
     */
    public InvalidAddActionNullEntityErrorBuilder(String addedData, String updatedEntity) {
        builder.withId(ToscaErrorCodes.TOSCA_INVALID_ADD_ACTION_NULL_ENTITY);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_ACTION_NULL_ENTITY_ERR_MSG, addedData, updatedEntity, updatedEntity));
    }

    public ErrorCode build() {
        return builder.build();
    }
}

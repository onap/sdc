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
package org.openecomp.sdc.common.errors;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class ValidationErrorBuilder {

    public static final String FIELD_VALIDATION_ERROR_ERR_ID = "FIELD_VALIDATION_ERROR_ERR_ID";
    private static final String FIELD_VALIDATION_ERROR_ERR_MSG = "Field does not conform to predefined criteria : %s : %s";
    private static final String FIELD_VALIDATION_ERROR_ERR_MSG_USE_PREDEFINED_FOR_FIELD = "%s";
    private static final String FIELD_WITH_PREDEFINED_MESSAGE = "arg\\d";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Validation error builder.
     *
     * @param detailedMessage the detailed message
     * @param fieldName       the field name
     */
    public ValidationErrorBuilder(String detailedMessage, String fieldName) {
        builder.withId(FIELD_VALIDATION_ERROR_ERR_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        if (fieldName.matches(FIELD_WITH_PREDEFINED_MESSAGE)) {
            builder.withMessage(String.format(FIELD_VALIDATION_ERROR_ERR_MSG_USE_PREDEFINED_FOR_FIELD, detailedMessage));
        } else {
            builder.withMessage(String.format(FIELD_VALIDATION_ERROR_ERR_MSG, fieldName, detailedMessage));
        }
    }

    public ErrorCode build() {
        return builder.build();
    }
}

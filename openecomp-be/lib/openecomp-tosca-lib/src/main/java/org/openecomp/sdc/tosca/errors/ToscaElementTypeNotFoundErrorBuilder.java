/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.tosca.errors;

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

/**
 * The type Tosca data type not found error builder.
 */
public class ToscaElementTypeNotFoundErrorBuilder {

    private static final String ENTRY_NOT_FOUND_MSG =
        "Entity Type '%s' or one of its derivedFrom type hierarchy, " + "is not defined in tosca service model";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Tosca data type not found error builder.
     *
     * @param elementType element type
     */
    public ToscaElementTypeNotFoundErrorBuilder(String elementType) {
        builder.withId(ToscaErrorCodes.TOSCA_ENTRY_NOT_FOUND);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(ENTRY_NOT_FOUND_MSG, elementType));
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

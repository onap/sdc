/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
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


public class ToscaEntryDefinitionWasNotFound {

    private static final String ENTRY_DEFINITION_WAS_NOT_FOUND = "TOSCA Entry Definition was not found";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new invalid TOSCA entry definition was not found error builder.
     */
    public ToscaEntryDefinitionWasNotFound() {
        builder.withId(ToscaErrorCodes.INVALID_TOSCA_ENTRY_DEF_WAS_NOT_FOUND);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(ENTRY_DEFINITION_WAS_NOT_FOUND);
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

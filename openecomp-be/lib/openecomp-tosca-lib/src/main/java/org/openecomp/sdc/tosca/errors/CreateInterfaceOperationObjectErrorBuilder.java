/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

public class CreateInterfaceOperationObjectErrorBuilder {

    private static final String COULD_NOT_CREATE_OBJECT_MSG = "Could not create Operation from %s. Reason - %s";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    public CreateInterfaceOperationObjectErrorBuilder(String operationId, String reason) {
        builder.withId(ToscaErrorCodes.INVALID_INTERFACE_VALUE);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(COULD_NOT_CREATE_OBJECT_MSG, operationId, reason));
    }

    public ErrorCode build() {
        return builder.build();
    }
}

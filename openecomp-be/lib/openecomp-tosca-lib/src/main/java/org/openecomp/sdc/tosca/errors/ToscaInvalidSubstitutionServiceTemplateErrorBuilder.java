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

import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

/**
 * The type Tosca invalid substitution service template error builder.
 */
public class ToscaInvalidSubstitutionServiceTemplateErrorBuilder {

    private static final String INVALID_SUBSTITUTION_SERVICE_TEMPLATE_MSG =
        "Invalid Substitution Service Template %s, missing mandatory file 'Node type' " + "in substitution mapping.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Tosca invalid substitution service template error builder.
     *
     * @param serviceTemplateFileName the service template file name
     */
    public ToscaInvalidSubstitutionServiceTemplateErrorBuilder(String serviceTemplateFileName) {
        builder.withId(ToscaErrorCodes.INVALID_SUBSTITUTION_SERVICE_TEMPLATE);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(INVALID_SUBSTITUTION_SERVICE_TEMPLATE_MSG, serviceTemplateFileName));
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

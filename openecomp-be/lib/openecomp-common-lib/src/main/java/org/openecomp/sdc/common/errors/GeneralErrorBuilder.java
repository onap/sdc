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
package org.openecomp.sdc.common.errors;

import org.apache.commons.text.RandomStringGenerator;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class GeneralErrorBuilder {

    private static final String GENERAL_ERROR_REST_ID = "GENERAL_ERROR_REST_ID";
    private static final String GENERAL_ERROR_REST_MSG = "An internal error has occurred. Please contact support. Error ID: %s";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new General error builder.
     */
    public GeneralErrorBuilder() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('A', 'Z').build();
        builder.withId(GENERAL_ERROR_REST_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(GENERAL_ERROR_REST_MSG, generator.generate(8)));
    }

    public ErrorCode build() {
        return builder.build();
    }
}

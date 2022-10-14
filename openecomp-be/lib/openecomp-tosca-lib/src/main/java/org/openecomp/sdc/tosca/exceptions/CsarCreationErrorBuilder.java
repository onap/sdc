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
package org.openecomp.sdc.tosca.exceptions;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.errors.ErrorCategory;

public class CsarCreationErrorBuilder extends BaseErrorBuilder {

    private static final String MANDATORY_PROPERTY_IS_MISSING_MSG = "Failed to create CSAR file from ToscaServiceModel";
    private static final String ZIP_FILE_CREATION = "ZIP_FILE_CREATION";

    /**
     * Constructor.
     */
    public CsarCreationErrorBuilder() {
        getErrorCodeBuilder().withId(ZIP_FILE_CREATION);
        getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
        getErrorCodeBuilder().withMessage(MANDATORY_PROPERTY_IS_MISSING_MSG);
    }
}

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
package org.openecomp.core.converter.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

public class SubstitutionMappingsConverterErrorBuilder extends BaseErrorBuilder {

    private static final String SUB_MAPPINGS_CAPABILITY_REQUIREMENT_ENTRY_VALUE_ILLEGAL =
        "%s value" + " in substitution mappings is invalid, expected it to be %s";
    private static final String IMPORT_TOSCA = "IMPORT_TOSCA";

    public SubstitutionMappingsConverterErrorBuilder(String section, String expectedType) {
        getErrorCodeBuilder().withId(IMPORT_TOSCA).withCategory(ErrorCategory.APPLICATION)
            .withMessage(String.format(SUB_MAPPINGS_CAPABILITY_REQUIREMENT_ENTRY_VALUE_ILLEGAL, section, expectedType));
    }
}

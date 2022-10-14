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
import org.openecomp.sdc.errors.ErrorCategory;

public class CreateToscaObjectErrorBuilder extends BaseErrorBuilder {

    private static final String CANT_CREATE_OBJECT_FROM_CLASS = "Can't create %s from %s.";
    private static final String IMPORT_TOSCA = "IMPORT_TOSCA";

    public CreateToscaObjectErrorBuilder(String className, String objectId) {
        getErrorCodeBuilder().withId(IMPORT_TOSCA).withCategory(ErrorCategory.APPLICATION)
            .withMessage(String.format(CANT_CREATE_OBJECT_FROM_CLASS, className, objectId));
    }
}

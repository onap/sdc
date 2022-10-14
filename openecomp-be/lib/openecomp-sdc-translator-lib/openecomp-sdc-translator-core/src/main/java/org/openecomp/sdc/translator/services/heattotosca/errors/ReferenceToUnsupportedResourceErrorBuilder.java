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
package org.openecomp.sdc.translator.services.heattotosca.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.errors.ErrorCategory;

public class ReferenceToUnsupportedResourceErrorBuilder extends BaseErrorBuilder {

    private static final String REFERENCE_TO_UNSUPPORTED_RESOURCE_MSG =
        "Resource id '%s' with type '%s' has reference " + "to unsupported resource '%s' with type '%s' in property '%s'";

    /**
     * Instantiates a new Reference to unsupported resource error builder.
     *
     * @param sourceResourceId   the source resource id
     * @param sourceResourceType the source resource type
     * @param targetResourceId   the target resource id
     * @param targetResourceType the target resource type
     * @param property           the property
     */
    public ReferenceToUnsupportedResourceErrorBuilder(String sourceResourceId, String sourceResourceType, String targetResourceId,
                                                      String targetResourceType, String property) {
        getErrorCodeBuilder().withId(TranslatorErrorCodes.REFERENCE_TO_UNSUPPORTED_RESOURCE);
        getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
        getErrorCodeBuilder().withMessage(String
            .format(REFERENCE_TO_UNSUPPORTED_RESOURCE_MSG, sourceResourceId, sourceResourceType, targetResourceId, targetResourceType, property));
    }
}

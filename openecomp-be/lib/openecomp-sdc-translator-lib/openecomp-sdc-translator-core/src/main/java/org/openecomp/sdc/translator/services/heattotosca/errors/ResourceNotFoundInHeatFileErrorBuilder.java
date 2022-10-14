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

import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class ResourceNotFoundInHeatFileErrorBuilder {

    private static final String RESOURCE_NOT_FOUND_IN_FILE_ERR_ID = "RESOURCE_NOT_FOUND_IN_FILE_ERR_ID";
    private static final String RESOURCE_NOT_FOUND_IN_FILE_ERR_MSG = "resource with id = %s was not found in heat file = %s.";
    private final ErrorCode.ErrorCodeBuilder builder = new ErrorCode.ErrorCodeBuilder();

    /**
     * Instantiates a new Resource not found in heat file error builder.
     *
     * @param resourceId   the resource id
     * @param heatfileName the heatfile name
     */
    public ResourceNotFoundInHeatFileErrorBuilder(String resourceId, String heatfileName) {
        builder.withId(RESOURCE_NOT_FOUND_IN_FILE_ERR_ID);
        builder.withCategory(ErrorCategory.APPLICATION);
        builder.withMessage(String.format(RESOURCE_NOT_FOUND_IN_FILE_ERR_MSG, resourceId, heatfileName));
    }

    public ErrorCode build() {
        return builder.build();
    }
}

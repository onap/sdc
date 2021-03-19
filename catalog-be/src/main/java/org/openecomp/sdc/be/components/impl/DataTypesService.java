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
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import java.util.Map;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component
public class DataTypesService {

    private final ComponentsUtils componentsUtils;

    public DataTypesService(ComponentsUtils componentsUtils) {
        this.componentsUtils = componentsUtils;
    }

    public Either<Map<String, DataTypeDefinition>, ResponseFormat> getAllDataTypes(ApplicationDataTypeCache applicationDataTypeCache) {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
        if (allDataTypes.isRight()) {
            JanusGraphOperationStatus operationStatus = allDataTypes.right().value();
            if (operationStatus == JanusGraphOperationStatus.NOT_FOUND) {
                BeEcompErrorManager.getInstance()
                    .logInternalDataError("FetchDataTypes", "Data types are not loaded", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.DATA_TYPE_CANNOT_BE_EMPTY));
            } else {
                BeEcompErrorManager.getInstance()
                    .logInternalFlowError("FetchDataTypes", "Failed to fetch data types", BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
            }
        }
        return Either.left(allDataTypes.left().value());
    }
}

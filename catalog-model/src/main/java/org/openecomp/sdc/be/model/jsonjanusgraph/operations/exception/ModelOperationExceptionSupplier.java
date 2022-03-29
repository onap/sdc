/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception;

import java.util.function.Supplier;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;

/**
 * Supplies operation exception needed by the the Model logic
 */
public class ModelOperationExceptionSupplier {

    private ModelOperationExceptionSupplier() {

    }

    public static Supplier<OperationException> invalidModel(final String modelName) {
        return () -> new OperationException(ActionStatus.INVALID_MODEL, modelName);
    }

    public static Supplier<OperationException> emptyModelImports() {
        return () -> new OperationException(ActionStatus.MODEL_IMPORTS_IS_EMPTY);
    }

    public static Supplier<OperationException> couldNotReadImports() {
        return () -> new OperationException(ActionStatus.COULD_NOT_READ_MODEL_IMPORTS);
    }

    public static Supplier<OperationException> modelAlreadyExists(final String modelName) {
        return () -> new OperationException(ActionStatus.MODEL_ALREADY_EXISTS, modelName);
    }

    public static Supplier<OperationException> failedToRetrieveModels(final JanusGraphOperationStatus janusGraphOperationStatus) {
        var errorMsg = String.format("Failed to retrieve models. Status '%s'", janusGraphOperationStatus);
        return () -> new OperationException(ActionStatus.GENERAL_ERROR, errorMsg);
    }
    
    public static Supplier<OperationException> unknownModelType(final String modelType) {
        return () -> new OperationException(ActionStatus.UNKNOWN_MODEL_TYPE, modelType);
    }

    public static Supplier<OperationException> componentInUse(final String stringOfServices) {
        return () -> new OperationException(ActionStatus.COMPONENT_IN_USE_BY_ANOTHER_COMPONENT, stringOfServices);
    }

}

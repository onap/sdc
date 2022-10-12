/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.exception.supplier;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.exception.OperationException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTypeOperationExceptionSupplier {

    public static Supplier<OperationException> unexpectedErrorWhileFetchingProperties(final String uniqueId) {
        return () -> new OperationException(ActionStatus.UNEXPECTED_ERROR, String.format("retrieving the data type '%s' properties", uniqueId));
    }

    public static Supplier<OperationException> dataTypeNotFound(final String dataTypeId) {
        return () -> new OperationException(ActionStatus.DATA_TYPE_NOT_FOUND, dataTypeId);
    }

    public static Supplier<OperationException> dataTypePropertyAlreadyExists(final String dataTypeId, final String propertyName) {
        return () -> new OperationException(ActionStatus.DATA_TYPE_PROPERTY_ALREADY_EXISTS, dataTypeId, propertyName);
    }

    public static Supplier<OperationException> unexpectedErrorWhileCreatingProperty(String dataTypeId, String propertyName) {
        return () -> new OperationException(ActionStatus.UNEXPECTED_ERROR,
            String.format("creating the property '%s' in the data type '%s'", propertyName, dataTypeId)
        );
    }

}

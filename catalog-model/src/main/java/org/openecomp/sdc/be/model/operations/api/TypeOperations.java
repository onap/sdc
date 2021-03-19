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
package org.openecomp.sdc.be.model.operations.api;

import fj.data.Either;
import javax.validation.constraints.NotNull;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public interface TypeOperations<T extends ToscaDataDefinition> {

    static <T> Either<T, StorageOperationStatus> mapOkStatus(StorageOperationStatus status, T obj) {
        return status != StorageOperationStatus.OK ? Either.right(status) : Either.left(obj);
    }

    T addType(T newTypeDefinition);

    T getType(String uniqueId);

    T getLatestType(String uniqueId);

    boolean isSameType(@NotNull T type1, @NotNull T type2);

    T updateType(T currentTypeDefinition, T newTypeDefinition);
}

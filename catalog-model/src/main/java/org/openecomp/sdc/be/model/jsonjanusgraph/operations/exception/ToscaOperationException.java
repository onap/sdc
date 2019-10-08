/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

public class ToscaOperationException extends Exception {

    private final StorageOperationStatus storageOperationStatus;

    public ToscaOperationException(String s, StorageOperationStatus storageOperationStatus) {
        super(String.format("%s. Operation Status: %s", s, storageOperationStatus.name()));
        this.storageOperationStatus = storageOperationStatus;
    }

    public StorageOperationStatus getStorageOperationStatus() {
        return storageOperationStatus;
    }
}

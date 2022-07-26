/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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
 *
 *
 */
package org.openecomp.sdc.be.model;

import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import java.util.HashMap;
import java.util.Map;

public class UploadInterfaceInfo extends UploadInfo {
    private Object value;
    private String description;
    private String type;
    private Map<String, OperationDataDefinition> operations;

    public Map<String, OperationDataDefinition> getOperations() {
        if (operations == null) {
            operations = new HashMap<>();
        }
        return operations;
    }

    public void setOperations(Map<String, OperationDataDefinition> operations) {
        this.operations = operations;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

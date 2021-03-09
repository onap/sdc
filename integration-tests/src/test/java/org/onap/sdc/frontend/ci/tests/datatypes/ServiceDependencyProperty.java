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

package org.onap.sdc.frontend.ci.tests.datatypes;

import org.onap.sdc.frontend.ci.tests.pages.ServiceDependenciesEditor;

import lombok.Data;

/**
 * Represents the a property within the Service Dependencies Editor
 * @see ServiceDependenciesEditor
 */
@Data
public class ServiceDependencyProperty {
    private final String name;
    private final String value;
    private final String source;
    private final LogicalOperator logicalOperator;

    public ServiceDependencyProperty(String name, String value, LogicalOperator logicalOperator) {
        this.name = name;
        this.value = value;
        this.source = "Static";
        this.logicalOperator = logicalOperator;
    }
}

/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;

@Getter
@Setter
@NoArgsConstructor
public class OutputDefinition extends AttributeDefinition {

    private ComponentInstanceAttribute attribute;

    public OutputDefinition(final AttributeDataDefinition attributeDataDefinition) {
        super(attributeDataDefinition);
    }

    public OutputDefinition(AttributeDefinition attributeDefinition) {
        super(attributeDefinition);
    }

    public OutputDefinition(final OutputDefinition outputDefinition) {
        super(outputDefinition);
        this.attribute = outputDefinition.getAttribute();
    }
}

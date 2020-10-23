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

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ComponentInstanceOutput extends OutputDefinition
    implements IComponentInstanceConnectedElement, IPropertyOutputCommon {

    /**
     * The unique id of the property value on graph
     */
    private String valueUniqueUid;
    private List<String> path = null;
    private List<PropertyRule> rules = null;
    private String componentInstanceName;
    private String componentInstanceId;

    public ComponentInstanceOutput(final PropertyDataDefinition propertyDataDefinition,
                                   final String outputId,
                                   final String value,
                                   final String valueUniqueUid) {
        super(propertyDataDefinition);
        setOutputId(outputId);
        setValue(value);
        this.valueUniqueUid = valueUniqueUid;
    }

    public ComponentInstanceOutput(final OutputDefinition outputDefinition,
                                   final String value,
                                   final String valueUniqueUid) {
        super(outputDefinition);
        setValue(value);
        this.valueUniqueUid = valueUniqueUid;
    }

    public ComponentInstanceOutput(final PropertyDataDefinition propertyDataDefinition) {
        super(propertyDataDefinition);
        if (propertyDataDefinition.getGetAttributeValues() != null && !propertyDataDefinition.getGetAttributeValues()
            .isEmpty()) {
            setOutputId(propertyDataDefinition.getGetAttributeValues().get(0).getOutputId());
        }
    }

}

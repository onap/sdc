/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.be.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.elements.AttributeDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class ComponentInstanceOutput extends OutputDefinition implements IComponentInstanceConnectedElement, IAttributeOutputCommon {

    /**
     * The unique id of the attribute value on graph
     */
    private String valueUniqueUid;

    private List<String> path;

    private List<PropertyRule> rules;
    private String componentInstanceName;
    private String componentInstanceId;

    public ComponentInstanceOutput(final AttributeDataDefinition curPropertyDef,
                                   final String outputId,
                                   final String value,
                                   final String valueUniqueUid) {
        super(curPropertyDef);
        setOutputId(outputId);
        setValue(value);
        this.valueUniqueUid = valueUniqueUid;
    }

    public ComponentInstanceOutput(final OutputDefinition pd,
                                   final String value,
                                   final String valueUniqueUid) {
        super(pd);

        setValue(value);
        this.valueUniqueUid = valueUniqueUid;
    }

    public ComponentInstanceOutput(AttributeDataDefinition attributeDataDefinition) {
        super(attributeDataDefinition);
        if (attributeDataDefinition.getGetOutputValues() != null && !attributeDataDefinition.getGetOutputValues()
            .isEmpty()) {
            setOutputId(attributeDataDefinition.getGetOutputValues().get(0).getOutputId());
        }
    }

}

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.io.Serializable;

public class RequirementNodeFilterCapabilityDataDefinition extends ToscaDataDefinition implements Serializable {

    /**
     * Default Constructor
     */
    public RequirementNodeFilterCapabilityDataDefinition() {
    }

    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
    }

    public void setName(String name) {
        setToscaPresentationValue(JsonPresentationFields.NAME, name);
    }

    public ListDataDefinition<PropertyFilterDataDefinition> getProperties() {
        return (ListDataDefinition<PropertyFilterDataDefinition>) getToscaPresentationValue(
                JsonPresentationFields.PROPERTIES);
    }

    public void setProperties(ListDataDefinition<PropertyFilterDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTIES, properties);
    }
}

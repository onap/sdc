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
package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class SubstitutionFilterDataDefinition extends ToscaDataDefinition implements Serializable {

    @JsonCreator
    public SubstitutionFilterDataDefinition() {
    }

    public SubstitutionFilterDataDefinition(final SubstitutionFilterDataDefinition substitutionFilterDataDefinition) {
        super();
        this.setID(substitutionFilterDataDefinition.getID());
        this.setName(substitutionFilterDataDefinition.getName());
        this.setTosca_id(substitutionFilterDataDefinition.getTosca_id());
        this.setProperties(substitutionFilterDataDefinition.getProperties());
        this.setCapabilities(substitutionFilterDataDefinition.getCapabilities());
    }

    public void setTosca_id(final Object tosca_id) {
        setToscaPresentationValue(JsonPresentationFields.TOSCA_ID, tosca_id);
    }

    public Object getTosca_id() {
        return getToscaPresentationValue(JsonPresentationFields.TOSCA_ID);
    }

    public ListDataDefinition<SubstitutionFilterPropertyDataDefinition> getProperties() {
        return (ListDataDefinition<SubstitutionFilterPropertyDataDefinition>) getToscaPresentationValue(
                JsonPresentationFields.PROPERTIES);
    }

    public void setProperties(final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> properties) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTIES, properties);
    }

    public ListDataDefinition<RequirementSubstitutionFilterCapabilityDataDefinition> getCapabilities() {
        return (ListDataDefinition<RequirementSubstitutionFilterCapabilityDataDefinition>) getToscaPresentationValue(
                JsonPresentationFields.NODE_FILTER_CAPABILITIES);
    }

    public void setCapabilities(ListDataDefinition<RequirementSubstitutionFilterCapabilityDataDefinition> capabilities) {
        setToscaPresentationValue(JsonPresentationFields.NODE_FILTER_CAPABILITIES, capabilities);
    }

    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.NAME);
    }

    public void setName(final String name) {
        setToscaPresentationValue(JsonPresentationFields.NAME, name);
    }

    public String getID() {
        return (String) getToscaPresentationValue(JsonPresentationFields.UNIQUE_ID);
    }

    public void setID(final String name) {
        setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, name);
    }


}

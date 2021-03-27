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

import java.io.Serializable;
import java.util.List;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class RequirementSubstitutionFilterPropertyDataDefinition extends ToscaDataDefinition implements Serializable {

    public List<String> getConstraints() {
        return (List<String>) getToscaPresentationValue(JsonPresentationFields.PROPERTY_FILTER_CONSTRAINT);
    }

    public void setConstraints(final List<String> constraints) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTY_FILTER_CONSTRAINT, constraints);
    }

    public String getName() {
        return (String) getToscaPresentationValue(JsonPresentationFields.PROPERTY_FILTER_NAME);
    }

    public void setName(final String name) {
        setToscaPresentationValue(JsonPresentationFields.PROPERTY_FILTER_NAME, name);
    }
}

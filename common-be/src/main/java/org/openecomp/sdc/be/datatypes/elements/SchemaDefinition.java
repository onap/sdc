/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

/**
 * Schema allows to create new types that can be used along TOSCA definitions.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SchemaDefinition extends ToscaDataDefinition {

    private String derivedFrom;
    private List<String> constraints;
    private Map<String, PropertyDataDefinition> properties;
    private PropertyDataDefinition property;

    public SchemaDefinition(String derivedFrom, List<String> constraints,
                            Map<String, PropertyDataDefinition> properties) {
        this.setDerivedFrom(derivedFrom);
        this.setConstraints(constraints);
        this.setProperties(properties);

    }

    public void addProperty(String key, PropertyDataDefinition property) {
        properties.put(key, property);
    }

    @Override
    public String toString() {
        return "SchemaDefinition [" + "derivedFrom='" + derivedFrom + ", constraints=" + constraints + ", properties="
            + properties + ", property=" + property + ']';
    }
}

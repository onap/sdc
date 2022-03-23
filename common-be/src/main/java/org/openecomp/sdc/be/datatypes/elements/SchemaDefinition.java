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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

/**
 * Schema allows to create new types that can be used along TOSCA definitions.
 */
public class SchemaDefinition extends ToscaDataDefinition {

    private String derivedFrom;
    private List<String> constraints;
    private Map<String, PropertyDataDefinition> properties;

    private PropertyDataDefinition property;


    public SchemaDefinition() {
    }

    public SchemaDefinition(String derivedFrom, List<String> constraints,
                            Map<String, PropertyDataDefinition> properties) {
        this.setDerivedFrom(derivedFrom);
        this.setConstraints(constraints);
        this.setProperties(properties);
    }

    public SchemaDefinition(final SchemaDefinition schemaDefinition) {
        if (schemaDefinition == null) {
            return;
        }
        this.derivedFrom = schemaDefinition.getDerivedFrom();
        if (CollectionUtils.isNotEmpty(schemaDefinition.getConstraints())) {
            this.constraints = new ArrayList<>(schemaDefinition.getConstraints());
        }
        if (schemaDefinition.getProperty() != null) {
            this.property = new PropertyDataDefinition(schemaDefinition.getProperty());
        }
        if (MapUtils.isNotEmpty(schemaDefinition.getProperties())) {
            this.properties = new HashMap<>();
            for (final Entry<String, PropertyDataDefinition> propertyEntry : schemaDefinition.getProperties().entrySet()) {
                this.properties.put(propertyEntry.getKey(), new PropertyDataDefinition(propertyEntry.getValue()));
            }
        }
    }

    public String getDerivedFrom() {
        return derivedFrom;
    }

    public void setProperty(PropertyDataDefinition property) {
        this.property = property;
    }

    public PropertyDataDefinition getProperty() {
        return this.property;
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public Map<String, PropertyDataDefinition> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, PropertyDataDefinition> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, PropertyDataDefinition property) {

        properties.put(key, property);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constraints == null) ? 0 : constraints.hashCode());
        result = prime * result + ((derivedFrom == null) ? 0 : derivedFrom.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SchemaDefinition other = (SchemaDefinition) obj;
        if (constraints == null) {
            if (other.constraints != null) {
                return false;
            }
        } else if (!constraints.equals(other.constraints)) {
            return false;
        }
        if (derivedFrom == null) {
            if (other.derivedFrom != null) {
                return false;
            }
        } else if (!derivedFrom.equals(other.derivedFrom)) {
            return false;
        }
        if (properties == null) {
            if (other.properties != null) {
                return false;
            }
        } else if (!properties.equals(other.properties)) {
            return false;
        }
        if (property == null) {
            return other.property == null;
        } else {
            return property.equals(other.property);
        }
    }

    @Override
    public String toString() {
        return "SchemaDefinition [" + "derivedFrom='" + derivedFrom + ", constraints=" + constraints + ", properties="
                + properties + ", property=" + property + ']';
    }
}

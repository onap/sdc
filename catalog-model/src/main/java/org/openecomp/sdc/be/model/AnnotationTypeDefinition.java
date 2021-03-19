/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.Objects;
import org.openecomp.sdc.be.datatypes.elements.AnnotationTypeDataDefinition;
import org.openecomp.sdc.be.model.utils.TypeCompareUtils;

public class AnnotationTypeDefinition extends AnnotationTypeDataDefinition {

    protected List<PropertyDefinition> properties;

    public AnnotationTypeDefinition() {
        super();
    }

    public AnnotationTypeDefinition(AnnotationTypeDataDefinition annotationTypeDataDefinition) {
        super(annotationTypeDataDefinition);
    }

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }

    /**
     * This method compares definition properties and ignores products such as actual graph ids that were already assigned
     */
    public boolean isSameDefinition(AnnotationTypeDefinition other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return Objects.equals(type, other.type) && Objects.equals(description, other.description) && TypeCompareUtils
            .propertiesEquals(properties, other.properties);
    }
}

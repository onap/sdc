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

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.ArrayList;
import java.util.List;

public class AnnotationBuilder {

    private Annotation annotation;

    private AnnotationBuilder() {
        annotation = new Annotation();
    }

    public static AnnotationBuilder create() {
        return new AnnotationBuilder();
    }

    public AnnotationBuilder setType(String type) {
        annotation.setType(type);
        return this;
    }

    public AnnotationBuilder setName(String name) {
        annotation.setName(name);
        return this;
    }

    public AnnotationBuilder addProperty(String name) {
        PropertyDefinition prop = new PropertyDataDefinitionBuilder()
                .setName(name)
                .build();
        List<PropertyDataDefinition> annotationProps = getAnnotationProps();
        annotationProps.add(prop);
        return this;
    }

    public Annotation build() {
        return annotation;
    }

    private List<PropertyDataDefinition> getAnnotationProps() {
        if (annotation.getProperties() == null) {
            annotation.setProperties(new ArrayList<>());
        }
        return annotation.getProperties();
    }

}

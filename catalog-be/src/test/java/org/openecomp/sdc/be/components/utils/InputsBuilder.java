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
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.ArrayList;
import java.util.List;

public class InputsBuilder {

    private InputDefinition input;

    private InputsBuilder() {
        this.input = new InputDefinition();
    }

    public static InputsBuilder create() {
        return new InputsBuilder();
    }

    public InputsBuilder setName(String name) {
        input.setName(name);
        return this;
    }

    public InputsBuilder setPropertyId(String propertyId) {
        input.setPropertyId(propertyId);
        return this;
    }

    public InputsBuilder addAnnotation(Annotation annotation) {
        List<Annotation> annotations = getAnnotations();
        annotations.add(annotation);
        return this;
    }

    private List<Annotation> getAnnotations() {
        if (input.getAnnotations() == null) {
            input.setAnnotations(new ArrayList<>());
        }
        return input.getAnnotations();
    }

    public InputDefinition build() {
        return input;
    }

}

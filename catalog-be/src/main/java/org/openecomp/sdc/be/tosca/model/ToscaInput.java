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
package org.openecomp.sdc.be.tosca.model;

import java.util.HashMap;
import java.util.Map;

public class ToscaInput extends ToscaProperty {

    private Map<String, ToscaAnnotation> annotations;

    //copy constructor
    public ToscaInput(ToscaProperty toscaProperty) {
        super(toscaProperty);
    }

    public Map<String, ToscaAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, ToscaAnnotation> annotations) {
        this.annotations = annotations;
    }

    public void addAnnotation(String name, ToscaAnnotation annotaion) {
        if (annotations == null) {
            annotations = new HashMap<>();
        }
        annotations.put(name, annotaion);
    }
}

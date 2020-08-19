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

package org.openecomp.sdc.ci.tests.tosca.datatypes;

import org.openecomp.sdc.ci.tests.datatypes.enums.ToscaKeysEnum;
import org.yaml.snakeyaml.TypeDescription;

import java.util.HashMap;
import java.util.Map;

public class ToscaAnnotationsTypesDefinition extends ToscaTypesDefinition {

    public static final String SOURCE_ANNOTATION = "org.openecomp.annotations.Source";

    private Map<String, ToscaTypeDefinition> annotation_types = new HashMap<>();

    public Map<String, ToscaTypeDefinition> getAnnotation_types() {
        return annotation_types;
    }

    public void setAnnotation_types(Map<String, ToscaTypeDefinition> annotation_types) {
        this.annotation_types = annotation_types;
    }

    public static TypeDescription getTypeDescription(){
        TypeDescription typeDescription = new TypeDescription(ToscaAnnotationsTypesDefinition.class);
        typeDescription.putListPropertyType(ToscaKeysEnum.IMPORTS.getToscaKey(), String.class);
        typeDescription.putMapPropertyType(ToscaKeysEnum.ANNOTATION_TYPES.getToscaKey(), String.class, ToscaTypeDefinition.class);
        return typeDescription;
    }
}

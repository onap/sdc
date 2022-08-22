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
package org.openecomp.sdc.be.tosca.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.model.ToscaAnnotation;
import org.openecomp.sdc.be.tosca.model.ToscaInput;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class InputConverter {

    private static final Logger log = Logger.getLogger(ToscaExportHandler.class);
    private PropertyConvertor propertyConvertor;

    @Autowired
    public InputConverter(PropertyConvertor propertyConvertor) {
        this.propertyConvertor = propertyConvertor;
    }

    /**
     * This is the converter made for input input is derived from properties and  is similar to properties now that it was added annotations , we
     * created a new convertetor for it Input List of annotation Annotation: name type description list of properties
     */
    public Map<String, ToscaProperty> convertInputs(List<InputDefinition> inputDef, Map<String, DataTypeDefinition> dataTypes) {
        log.debug("convert inputs to tosca");
        Map<String, ToscaProperty> inputs = new HashMap<>();
        if (inputDef != null) {
            inputDef.forEach(i -> {
                //Extract input the same as property
                ToscaProperty toscaProperty = propertyConvertor.convertProperty(dataTypes, i, PropertyConvertor.PropertyType.INPUT);
                //now that we have Tosca property we create new object called tosca input which drives from it
                ToscaInput toscaInput = new ToscaInput(toscaProperty);
                List<Annotation> annotations = i.getAnnotations();
                extractAnnotations(dataTypes, toscaInput, annotations);
                inputs.put(i.getName(), toscaInput);
            });
        }
        return inputs;
    }

    private void extractAnnotations(Map<String, DataTypeDefinition> dataTypes, ToscaInput toscaInput, List<Annotation> annotationsList) {
        if (annotationsList != null) {
            annotationsList.forEach(inputAnnotation -> {
                ToscaAnnotation annotation = new ToscaAnnotation();
                if ((inputAnnotation.getType()) != null) {
                    annotation.setType(inputAnnotation.getType());
                }
                if (inputAnnotation.getDescription() != null) {
                    annotation.setDescription(inputAnnotation.getDescription());
                }
                if (inputAnnotation.getProperties() != null) {
                    Map<String, Object> properties = new HashMap<>();
                    inputAnnotation.getProperties().forEach(k -> {
                        propertyConvertor.convertAndAddValue(dataTypes, properties, k, k::getValue);
                    });
                    annotation.setProperties(properties);
                }
                toscaInput.addAnnotation(inputAnnotation.getName(), annotation);
            });
        }
    }
}

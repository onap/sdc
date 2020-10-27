/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaAnnotation;
import org.openecomp.sdc.be.tosca.model.ToscaOutput;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutputConverter {

    private PropertyConvertor propertyConvertor;
    private static final Logger log = Logger.getLogger(OutputConverter.class);

    @Autowired
    public OutputConverter(final PropertyConvertor propertyConvertor) {
        this.propertyConvertor = propertyConvertor;
    }

    public Map<String, ToscaProperty> convertOutputs(final List<OutputDefinition> outputDefinitions,
                                                     final Map<String, DataTypeDefinition> dataTypes) {
        log.debug("convert outputs to TOSCA");
        final Map<String, ToscaProperty> outputs = new HashMap<>();
        if (outputDefinitions != null) {
            outputDefinitions.forEach(outputDefinition -> {
                //Extract output the same as property
                final ToscaProperty toscaProperty = propertyConvertor.convertProperty(dataTypes, outputDefinition,
                    PropertyType.OUTPUT);
                //now that we have Tosca property we create new object called tosca output which drives from it
                final ToscaOutput toscaOutput = new ToscaOutput(toscaProperty);
                final List<Annotation> annotations = outputDefinition.getAnnotations();
                extractAnnotations(dataTypes, toscaOutput, annotations);
                outputs.put(outputDefinition.getName(), toscaOutput);
            });
        }
        return outputs;
    }

    private void extractAnnotations(final Map<String, DataTypeDefinition> dataTypes,
                                    final ToscaOutput toscaOutput, final List<Annotation> annotationsList) {
        if (annotationsList != null) {
            annotationsList.forEach(outputAnnotation -> {
                final ToscaAnnotation annotation = new ToscaAnnotation();
                if ((outputAnnotation.getType()) != null) {
                    annotation.setType(outputAnnotation.getType());
                }
                if (outputAnnotation.getDescription() != null) {
                    annotation.setDescription(outputAnnotation.getDescription());
                }
                if (outputAnnotation.getProperties() != null) {
                    final Map<String, Object> properties = new HashMap<>();
                    outputAnnotation.getProperties().forEach(k -> propertyConvertor
                        .convertAndAddValue(dataTypes, properties, k, k::getValue));
                    annotation.setProperties(properties);
                }
                toscaOutput.addAnnotation(outputAnnotation.getName(), annotation);
            });
        }
    }
}




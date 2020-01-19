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

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.components.validation.AnnotationValidator;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Component
public class AnnotationBusinessLogic {

    private final AnnotationTypeOperations annotationTypeOperations;

    private final AnnotationValidator annotationValidator;

    public AnnotationBusinessLogic(AnnotationTypeOperations annotationTypeOperations,
                                   AnnotationValidator annotationValidator){
        this.annotationTypeOperations = annotationTypeOperations;
        this.annotationValidator = annotationValidator;
    }

    public void validateAndMergeAnnotationsAndAssignToInput(Map<String, InputDefinition> inputs) {
        if (!inputs.isEmpty()){
            for (InputDefinition input : inputs.values()) {
                List<Annotation> inputAnnotationList = input.getAnnotations();
                if (isNotEmpty(inputAnnotationList)) {
                    for (Annotation annotation : inputAnnotationList) {
                        AnnotationTypeDefinition dbAnnotationTypeDefinition = annotationTypeOperations.getLatestType(annotation.getType());
                        validateMergeAndSetAnnoProps(annotation, dbAnnotationTypeDefinition);
                    }
                }
                input.setAnnotations(inputAnnotationList);
            }
        }
    }

    public AnnotationTypeOperations getAnnotationTypeOperations() {
        return annotationTypeOperations;
    }

    private void validateMergeAndSetAnnoProps(Annotation annotation, AnnotationTypeDefinition dbAnnotationTypeDefinition) {
        annotationValidator.validateAnnotationsProperties(annotation, dbAnnotationTypeDefinition);
        List<PropertyDataDefinition> mergedPropertiesList = mergePropsOfAnnoDataTypeWithParsedAnnoProps(annotation.getProperties(), dbAnnotationTypeDefinition.getProperties());
        annotation.setProperties(mergedPropertiesList);
    }

    private List<PropertyDataDefinition> mergePropsOfAnnoDataTypeWithParsedAnnoProps(List<PropertyDataDefinition> annoProperties, List<PropertyDefinition> typePropertiesList) {
        Set<PropertyDataDefinition> mergedPropertiesSet = new HashSet<>(typePropertiesList);
        Map<String, PropertyDefinition> typePropsMap = MapUtil.toMap(typePropertiesList, PropertyDefinition::getName);
        for (PropertyDataDefinition propertyDataDefinitionObject : annoProperties) {
            PropertyDefinition foundTypePropertyDefinitionObject = typePropsMap.get(propertyDataDefinitionObject.getName());
            //The case of unexisting property was already handled in the validation process (result: failure)
            PropertyDataDefinition modifiedPropertyDataObject = new PropertyDataDefinition(foundTypePropertyDefinitionObject);
            modifiedPropertyDataObject.setValue(propertyDataDefinitionObject.getValue());
            mergedPropertiesSet.add(modifiedPropertyDataObject);
        }
        return new ArrayList<>(mergedPropertiesSet);
    }

}

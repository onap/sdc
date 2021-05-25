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
package org.openecomp.sdc.be.components.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component
public class PropertyValidator {

    private final PropertyOperation propertyOperation;
    private final ComponentsUtils componentsUtils;
    private final ExceptionUtils exceptionUtils;

    public PropertyValidator(PropertyOperation propertyOperation, ComponentsUtils componentsUtils,
                             ExceptionUtils exceptionUtils) {
        this.exceptionUtils = exceptionUtils;
        this.propertyOperation = propertyOperation;
        this.componentsUtils = componentsUtils;
    }

    public void thinPropertiesValidator(List<PropertyDefinition> properties, List<PropertyDefinition> dbAnnotationTypeDefinitionProperties,
                                        Map<String, DataTypeDefinition> allDataTypes) {
        for (PropertyDefinition property : properties) {
            PropertyDefinition annotationTypeSpecificProperty = isPropertyInsideAnnotationTypeProperties(dbAnnotationTypeDefinitionProperties,
                property);
            if (annotationTypeSpecificProperty != null) {
                verifyPropertyIsOfDefinedType(property, annotationTypeSpecificProperty, allDataTypes);
            }
        }
    }

    private void verifyPropertyIsOfDefinedType(PropertyDefinition property, PropertyDefinition typeSpecificProperty,
                                               Map<String, DataTypeDefinition> allDataTypes) {
        propertyOperation
            .validateAndUpdatePropertyValue(typeSpecificProperty.getType(), property.getValue(), typeSpecificProperty.getSchemaType(), allDataTypes)
            .left().on(error -> exceptionUtils.rollBackAndThrow(ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName()));
    }

    private PropertyDefinition isPropertyInsideAnnotationTypeProperties(List<PropertyDefinition> dbAnnotationTypeDefinitionProperties,
                                                                        PropertyDefinition property) {
        Optional<PropertyDefinition> optionalResult = dbAnnotationTypeDefinitionProperties.stream()
            .filter(prop -> prop.getName().equals(property.getName())).findFirst();
        if (optionalResult.isPresent()) {
            return optionalResult.get();
        }
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.PROPERTY_NOT_FOUND, property.getType(), property.getName());
        exceptionUtils.rollBackAndThrow(responseFormat);
        return null;
    }
}

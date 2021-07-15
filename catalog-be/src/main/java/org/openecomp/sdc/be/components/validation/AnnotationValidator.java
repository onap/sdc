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

import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.springframework.stereotype.Component;

@Component
public class AnnotationValidator {

    private final PropertyValidator propertyValidator;
    private final ExceptionUtils exceptionUtils;
    private final ApplicationDataTypeCache applicationDataTypeCache;
    private final ComponentsUtils componentsUtils;

    public AnnotationValidator(PropertyValidator propertyValidator, ExceptionUtils exceptionUtils, ApplicationDataTypeCache applicationDataTypeCache,
                               ComponentsUtils componentsUtils) {
        this.propertyValidator = propertyValidator;
        this.exceptionUtils = exceptionUtils;
        this.applicationDataTypeCache = applicationDataTypeCache;
        this.componentsUtils = componentsUtils;
    }

    public List<Annotation> validateAnnotationsProperties(final Annotation annotation, final AnnotationTypeDefinition dbAnnotationTypeDefinition,
                                                          final String model) {
        List<Annotation> validAnnotations = new ArrayList<>();
        if (annotation != null && propertiesValidator(annotation.getProperties(), dbAnnotationTypeDefinition.getProperties(), model)) {
            validAnnotations.add(annotation);
        }
        return validAnnotations;
    }

    private boolean propertiesValidator(final List<PropertyDataDefinition> properties,
                                        final List<PropertyDefinition> dbAnnotationTypeDefinitionProperties, final String model) {
        List<PropertyDefinition> propertyDefinitionsList = new ArrayList<>();
        if (properties == null || dbAnnotationTypeDefinitionProperties == null) {
            return false;
        }
        properties.stream().forEach(property -> propertyDefinitionsList.add(new PropertyDefinition(property)));
        propertyValidator.thinPropertiesValidator(propertyDefinitionsList, dbAnnotationTypeDefinitionProperties,
            applicationDataTypeCache.getAll(model).left().on(error -> exceptionUtils.rollBackAndThrow(error)));
        return true;
    }
}

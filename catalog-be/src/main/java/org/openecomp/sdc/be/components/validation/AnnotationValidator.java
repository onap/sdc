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
import java.util.Map;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

@Component
public class AnnotationValidator {

    private static final Logger log = Logger.getLogger(ResourceImportManager.class);
    private final PropertyValidator propertyValidator;
    private final ExceptionUtils exceptionUtils;
    private final ApplicationDataTypeCache dataTypeCache;
    private final ComponentsUtils componentsUtils;

    public AnnotationValidator(PropertyValidator propertyValidator, ExceptionUtils exceptionUtils, ApplicationDataTypeCache dataTypeCache,
                               ComponentsUtils componentsUtils) {
        this.propertyValidator = propertyValidator;
        this.exceptionUtils = exceptionUtils;
        this.dataTypeCache = dataTypeCache;
        this.componentsUtils = componentsUtils;
    }

    public List<Annotation> validateAnnotationsProperties(Annotation annotation, AnnotationTypeDefinition dbAnnotationTypeDefinition) {
        List<Annotation> validAnnotations = new ArrayList<>();
        if (annotation != null && propertiesValidator(annotation.getProperties(), dbAnnotationTypeDefinition.getProperties())) {
            validAnnotations.add(annotation);
        }
        return validAnnotations;
    }

    private boolean propertiesValidator(List<PropertyDataDefinition> properties, List<PropertyDefinition> dbAnnotationTypeDefinitionProperties) {
        List<PropertyDefinition> propertyDefinitionsList = new ArrayList<>();
        if (properties == null || dbAnnotationTypeDefinitionProperties == null) {
            return false;
        }
        properties.stream().forEach(property -> propertyDefinitionsList.add(new PropertyDefinition(property)));
        Map<String, DataTypeDefinition> allDataTypes = dataTypeCache.getAll().left().on(error -> exceptionUtils.rollBackAndThrow(error));
        propertyValidator.thinPropertiesValidator(propertyDefinitionsList, dbAnnotationTypeDefinitionProperties, allDataTypes);
        return true;
    }
}

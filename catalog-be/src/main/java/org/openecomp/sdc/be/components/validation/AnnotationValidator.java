package org.openecomp.sdc.be.components.validation;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AnnotationValidator {


    private final PropertyValidator propertyValidator;
    private final ExceptionUtils exceptionUtils;
    private final ApplicationDataTypeCache dataTypeCache;
    private final ComponentsUtils componentsUtils;

    private static final Logger log = Logger.getLogger(ResourceImportManager.class);


    public AnnotationValidator(PropertyValidator propertyValidator
                               , ExceptionUtils exceptionUtils, ApplicationDataTypeCache dataTypeCache
                               ,ComponentsUtils componentsUtils) {
        this.propertyValidator = propertyValidator;
        this.exceptionUtils = exceptionUtils;
        this.dataTypeCache = dataTypeCache;
        this.componentsUtils = componentsUtils;
    }

    public List<Annotation> validateAnnotationsProperties(Annotation annotation, AnnotationTypeDefinition dbAnnotationTypeDefinition) {
        List<Annotation> validAnnotations = new ArrayList<>();
        if (annotation != null && propertiesValidator(
                annotation.getProperties(), dbAnnotationTypeDefinition.getProperties())) {
                    validAnnotations.add(annotation);
        }
        return validAnnotations;
    }

    private boolean propertiesValidator(List<PropertyDataDefinition> properties, List<PropertyDefinition> dbAnnotationTypeDefinitionProperties) {
        List<PropertyDefinition> propertyDefinitionsList = new ArrayList<>();
        if (properties == null || dbAnnotationTypeDefinitionProperties == null) {
            return false;
        }
        properties.stream()
                .forEach(property -> propertyDefinitionsList.add(new PropertyDefinition(property)));
        Map<String, DataTypeDefinition> allDataTypes = dataTypeCache.getAll()
                .left()
                .on( error -> exceptionUtils
                .rollBackAndThrow(error));
        propertyValidator.thinPropertiesValidator(propertyDefinitionsList, dbAnnotationTypeDefinitionProperties, allDataTypes);
        return true;
    }

}

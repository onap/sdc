package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.utils.ExceptionUtils;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PropertyValidator {

    private final PropertyOperation propertyOperation;
    private final ComponentsUtils componentsUtils;
    private final ApplicationDataTypeCache applicationDataTypeCache;
    private final ExceptionUtils exceptionUtils;
    private static final Logger log = LoggerFactory.getLogger(ResourceBusinessLogic.class);

    public PropertyValidator(PropertyOperation propertyOperation, ComponentsUtils componentsUtils,
                             ApplicationDataTypeCache applicationDataTypeCache, ExceptionUtils exceptionUtils) {
        this.exceptionUtils = exceptionUtils;
        this.propertyOperation = propertyOperation;
        this.componentsUtils = componentsUtils;
        this.applicationDataTypeCache = applicationDataTypeCache;
    }

    public void thinPropertiesValidator(List<PropertyDefinition> properties,
                                        List<PropertyDefinition> dbAnnotationTypeDefinitionProperties,
                                        Map<String, DataTypeDefinition> allDataTypes){
        for (PropertyDefinition property : properties) {
            PropertyDefinition annotationTypeSpecificProperty = isPropertyInsideAnnotationTypeProperties(
                    dbAnnotationTypeDefinitionProperties, property);
            if(annotationTypeSpecificProperty!=null){
                verifyPropertyIsOfDefinedType(property, annotationTypeSpecificProperty, allDataTypes);
            }
        }
    }

    private void verifyPropertyIsOfDefinedType(PropertyDefinition property,
                                               PropertyDefinition typeSpecificProperty,
                                               Map<String, DataTypeDefinition> allDataTypes) {
            propertyOperation.validateAndUpdatePropertyValue(typeSpecificProperty.getType(),
                    property.getValue(), typeSpecificProperty.getSchemaType(), allDataTypes)
                    .left()
                    .on( error ->
                    exceptionUtils.rollBackAndThrow(
                            ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName())
            );
    }

    private PropertyDefinition isPropertyInsideAnnotationTypeProperties(
            List<PropertyDefinition> dbAnnotationTypeDefinitionProperties, PropertyDefinition property) {
        Optional<PropertyDefinition> optionalResult = dbAnnotationTypeDefinitionProperties.stream()
                .filter(prop -> prop.getName()
                        .equals(property.getName()))
                .findFirst();
        if (optionalResult.isPresent()){
            return optionalResult.get();
        }
        ResponseFormat responseFormat = componentsUtils.getResponseFormat(
                ActionStatus.PROPERTY_NOT_FOUND, property.getType(), property.getName());
        exceptionUtils.rollBackAndThrow(responseFormat);
        return null;
    }

    public Either<Boolean, ResponseFormat> iterateOverProperties(List<PropertyDefinition> properties){
        Either<Boolean, ResponseFormat> eitherResult = Either.left(true);
        String type = null;
        String innerType = null;
        for (PropertyDefinition property : properties) {
            if (!propertyOperation.isPropertyTypeValid(property)) {
                log.info("Invalid type for property {}", property);
                ResponseFormat responseFormat = componentsUtils.getResponseFormat(
                        ActionStatus.INVALID_PROPERTY_TYPE, property.getType(), property.getName());
                eitherResult = Either.right(responseFormat);
                break;
            }

            Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = applicationDataTypeCache.getAll();
            if (allDataTypes.isRight()) {
                JanusGraphOperationStatus status = allDataTypes.right().value();
                BeEcompErrorManager.getInstance().logInternalFlowError("AddPropertyToGroup", "Failed to validate property. Status is " + status, BeEcompErrorManager.ErrorSeverity.ERROR);
                return Either.right(componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponse(DaoStatusConverter.convertJanusGraphStatusToStorageStatus(status))));
            }

            type = property.getType();

            if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                ResponseFormat responseFormat = validateMapOrListPropertyType(property, allDataTypes.left().value());
                if(responseFormat != null)
                    break;
            }

            if (!propertyOperation.isPropertyDefaultValueValid(property, allDataTypes.left().value())) {
                log.info("Invalid default value for property {}", property);
                ResponseFormat responseFormat;
                if (type.equals(ToscaPropertyType.LIST.getType()) || type.equals(ToscaPropertyType.MAP.getType())) {
                    responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE,
                            property.getName(), type, innerType, property.getDefaultValue());
                } else {
                    responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_DEFAULT_VALUE,
                            property.getName(), type, property.getDefaultValue());
                }
                eitherResult = Either.right(responseFormat);
                break;

            }
        }
        return eitherResult;
    }

    private ResponseFormat validateMapOrListPropertyType(PropertyDefinition property, Map<String, DataTypeDefinition> allDataTypes) {
        ResponseFormat responseFormat = null;
        ImmutablePair<String, Boolean> propertyInnerTypeValid = propertyOperation
                .isPropertyInnerTypeValid(property, allDataTypes);
        String propertyInnerType = propertyInnerTypeValid.getLeft();
        if (!propertyInnerTypeValid.getRight().booleanValue()) {
            log.info("Invalid inner type for property {}", property);
            responseFormat = componentsUtils.getResponseFormat(
                    ActionStatus.INVALID_PROPERTY_INNER_TYPE, propertyInnerType, property.getName());
        }
        return responseFormat;
    }

}

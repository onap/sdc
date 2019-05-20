package org.openecomp.sdc.be.components.validation;

import com.google.common.collect.ImmutableSet;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.impl.utils.NodeFilterConstraintAction;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component("NodeFilterValidator")
public class NodeFilterValidator {

    private static final String SOURCE = "Source";
    public static final Set<String> comparableTypes = ImmutableSet.of(ToscaPropertyType.STRING.getType(),
            ToscaPropertyType.INTEGER.getType(), ToscaPropertyType.FLOAT.getType());
    public static final Set<String> schemableTypes =
            ImmutableSet.of(ToscaPropertyType.MAP.getType(), ToscaPropertyType.LIST.getType());
    public static final Set<String> comparableConstraintsOperators =
            ImmutableSet.of(ConstraintConvertor.GREATER_THAN_OPERATOR, ConstraintConvertor.LESS_THAN_OPERATOR);

    @Autowired
    protected ToscaOperationFacade toscaOperationFacade;

    @Autowired
    protected ComponentsUtils componentsUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFilterValidator.class);

    public Either<Boolean, ResponseFormat> validateComponentInstanceExist(Service service, String componentInstanceId) {
        if (service == null || StringUtils.isEmpty(componentInstanceId)) {
            LOGGER.debug("Input data cannot be empty");
            return getErrorResponse(ActionStatus.NODE_FILTER_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(service.getComponentInstances())) {
            LOGGER.debug("Component Instance list is empty");
            return getErrorResponse(ActionStatus.NODE_FILTER_NOT_FOUND);
        }
        boolean found =
                service.getComponentInstances().stream().anyMatch(ci -> ci.getUniqueId().equals(componentInstanceId));
        if (!found) {
            LOGGER.debug("Component Instance list is empty");
            return getErrorResponse(ActionStatus.NODE_FILTER_NOT_FOUND);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> getErrorResponse(ActionStatus actionStatus, String... variables) {
        ResponseFormat errorResponse = ResponseFormatManager.getInstance().getResponseFormat(actionStatus, variables);
        return Either.right(errorResponse);
    }

    public Either<Boolean, ResponseFormat> validateNodeFilter(CINodeFilterDataDefinition nodeFilter, String serviceId,
            String complonentInstanceId) {
        return Either.left(Boolean.TRUE);
    }


    public Either<Boolean, ResponseFormat> validateNodeFilter(Service parentComponent, String componentInstanceId,
            List<String> uiConstraints, NodeFilterConstraintAction action) {
        try {
            for (String uiConstraint : uiConstraints) {
                if (NodeFilterConstraintAction.ADD != action && NodeFilterConstraintAction.UPDATE != action) {
                    break;
                }
                UIConstraint constraint = new ConstraintConvertor().convert(uiConstraint);
                if (ConstraintConvertor.PROPERTY_CONSTRAINT.equals(constraint.getSourceType())) {
                    final Either<Boolean, ResponseFormat> booleanResponseFormatEither =
                            validatePropertyConstraint(parentComponent, componentInstanceId, constraint);
                    if (booleanResponseFormatEither.isRight()) {
                        return booleanResponseFormatEither;
                    }
                } else if (ConstraintConvertor.STATIC_CONSTRAINT.equals(constraint.getSourceType())) {
                    final Either<Boolean, ResponseFormat> booleanResponseFormatEither =
                            validateStaticValueAndOperator(parentComponent, componentInstanceId, constraint);
                    if (booleanResponseFormatEither.isRight()) {
                        return booleanResponseFormatEither;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Provided constraint" + uiConstraints, e);
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.CONSTRAINT_FORMAT_INCORRECT));
        }

        return Either.left(true);
    }

    private Either<Boolean, ResponseFormat> validatePropertyConstraint(Service parentComponent,
            String componentInstanceId, UIConstraint uiConstraint) {
        String source = SOURCE;
        Optional<ComponentInstance> brotherComponentInstance;

        List<? extends PropertyDefinition> sourcePropertyDefinition =
                parentComponent.getName().equals(uiConstraint.getSourceName()) ? parentComponent.getProperties() :
                        Collections.emptyList();


        if (sourcePropertyDefinition.isEmpty() && !parentComponent.getName().equals(uiConstraint.getSourceName())) {
            brotherComponentInstance = parentComponent.getComponentInstances().stream()
                                                      .filter(componentInstance -> uiConstraint.getSourceName()
                                                                                               .equals(componentInstance
                                                                                                               .getName()))
                                                      .findFirst();

            if (brotherComponentInstance.isPresent()) {
                final List<ComponentInstanceProperty> componentInstanceProperties =
                        parentComponent.getComponentInstancesProperties()
                                       .get(brotherComponentInstance.get().getUniqueId());
                sourcePropertyDefinition =
                        componentInstanceProperties == null ? new ArrayList<>() : componentInstanceProperties;
            }
        }

        if (!CollectionUtils.isEmpty(sourcePropertyDefinition)) {
            Optional<? extends PropertyDefinition> sourceSelectedProperty = sourcePropertyDefinition.stream()
                                                                                                    .filter(property -> uiConstraint
                                                                                                                                .getValue()
                                                                                                                                .equals(property.getName()))
                                                                                                    .findFirst();

            Optional<? extends PropertyDefinition> targetComponentInstanceProperty =
                    parentComponent.getComponentInstancesProperties().get(componentInstanceId).stream()
                                   .filter(property -> uiConstraint.getServicePropertyName().equals(property.getName()))
                                   .findFirst();

            source = !targetComponentInstanceProperty.isPresent() ? "Target" : SOURCE;
            if (sourceSelectedProperty.isPresent() && targetComponentInstanceProperty.isPresent()) {
                return validatePropertyData(uiConstraint, sourceSelectedProperty, targetComponentInstanceProperty);
            }
        }

        String missingProperty =
                source.equals(SOURCE) ? uiConstraint.getValue().toString() : uiConstraint.getServicePropertyName();

        return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.MAPPED_PROPERTY_NOT_FOUND, source, missingProperty));
    }

    private Either<Boolean, ResponseFormat> validatePropertyData(UIConstraint uiConstraint,
            Optional<? extends PropertyDefinition> sourceSelectedProperty,
            Optional<? extends PropertyDefinition> targetComponentInstanceProperty) {
        final PropertyDefinition sourcePropDefinition = sourceSelectedProperty.get();
        final String sourceType = sourcePropDefinition.getType();
        final PropertyDefinition targetPropDefinition = targetComponentInstanceProperty.get();
        final String targetType = targetPropDefinition.getType();
        if (sourceType.equals(targetType)) {
            if (schemableTypes.contains(sourceType)) {
                final SchemaDefinition sourceSchemaDefinition = sourcePropDefinition.getSchema();
                final SchemaDefinition targetSchemaDefinition = targetPropDefinition.getSchema();
                if (!sourceSchemaDefinition.equals(targetSchemaDefinition)) {
                    return Either.right(componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_SCHEMA_MISMATCH,
                            uiConstraint.getServicePropertyName(), uiConstraint.getValue().toString()));
                }
            }
            return Either.left(Boolean.TRUE);
        } else {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_PROPERTY_TYPE_MISMATCH,
                    uiConstraint.getServicePropertyName(), uiConstraint.getValue().toString()));
        }
    }

    private Either<Boolean, ResponseFormat> validateStaticValueAndOperator(Service parentComponent,
            String componentInstanceId, UIConstraint uiConstraint) {
        if (!(Objects.nonNull(uiConstraint) && uiConstraint.getValue() instanceof String)) {
            return Either.left(false);
        }
        Optional<ComponentInstanceProperty> componentInstanceProperty =
                parentComponent.getComponentInstancesProperties().get(componentInstanceId).stream()
                               .filter(property -> uiConstraint.getServicePropertyName().equals(property.getName()))
                               .findFirst();

        if (!componentInstanceProperty.isPresent()) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.SELECTED_PROPERTY_NOT_PRESENT,
                    uiConstraint.getServicePropertyName()));
        }
        if (comparableConstraintsOperators.contains(uiConstraint.getConstraintOperator()) && !comparableTypes.contains(
                componentInstanceProperty.get().getType())) {
            return Either.right(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_OPERATOR_PROVIDED,
                    uiConstraint.getServicePropertyName(), uiConstraint.getConstraintOperator()));
        }

        return isValidValueCheck(componentInstanceProperty.get().getType(), String.valueOf(uiConstraint.getValue()),
                uiConstraint.getServicePropertyName());
    }

    private Either<Boolean, ResponseFormat> isValidValueCheck(String type, String value, String propertyName) {

        ToscaPropertyType toscaPropertyType = ToscaPropertyType.isValidType(type);
        if (Objects.isNull(toscaPropertyType)) {
            return Either.right(
                    componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_PROPERTY_TYPE, type, propertyName));
        }
        if (toscaPropertyType.getValidator().isValid(value, null)) {
            return Either.left(Boolean.TRUE);
        }
        return Either.right(
                componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, type, propertyName, value));
    }


}



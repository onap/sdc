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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.dto.FilterConstraintDto;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.validation.FilterConstraintValidator;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

class NodeFilterValidatorTest {

    private static final String INNER_SERVICE = "innerService";
    private static final String PROPERTY_NAME = "Prop1";
    private static final String COMPONENT1_ID = "component1";
    private static final String PARENT_SERVICE_ID = "parentservice";
    private static final String COMPONENT2_ID = "component2";
    private ComponentsUtils componentsUtils;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private FilterConstraintValidator filterConstraintValidator;
    @InjectMocks
    private NodeFilterValidator nodeFilterValidator;
    private FilterConstraintDto baseFilterConstraintDto;

    protected static ToscaGetFunctionDataDefinition createToscaGetFunction(final String sourceName,
                                                                           final PropertySource propertySource,
                                                                           final ToscaGetFunctionType toscaGetFunctionType,
                                                                           final List<String> propertyPathFromSource,
                                                                           final List<Object> toscaIndexList) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(toscaGetFunctionType);
        toscaGetFunction.setPropertyPathFromSource(propertyPathFromSource);
        toscaGetFunction.setSourceName(sourceName);
        toscaGetFunction.setPropertySource(propertySource);
        toscaGetFunction.setPropertyName(propertyPathFromSource.get(0));
        toscaGetFunction.setToscaIndexList(toscaIndexList);
        return toscaGetFunction;
    }

    private static FilterConstraintDto buildFilterConstraintDto(final String propertyName, final FilterValueType valueType,
                                                                final ConstraintType constraintType,
                                                                final PropertyFilterTargetType targetType, Object value) {
        final var filterConstraintDto = new FilterConstraintDto();
        filterConstraintDto.setPropertyName(propertyName);
        filterConstraintDto.setValueType(valueType);
        filterConstraintDto.setOperator(constraintType);
        filterConstraintDto.setTargetType(targetType);
        filterConstraintDto.setValue(value);
        return filterConstraintDto;
    }

    @BeforeEach
    void setup() {
        componentsUtils = Mockito.mock(ComponentsUtils.class);
        MockitoAnnotations.openMocks(this);
        baseFilterConstraintDto = new FilterConstraintDto();
        baseFilterConstraintDto.setPropertyName(PROPERTY_NAME);
        baseFilterConstraintDto.setValueType(FilterValueType.STATIC);
        baseFilterConstraintDto.setOperator(ConstraintType.EQUAL);
        baseFilterConstraintDto.setTargetType(PropertyFilterTargetType.PROPERTY);
        baseFilterConstraintDto.setValue("value");
        when(applicationDataTypeCache.getAll(any())).thenReturn(Either.left(Map.of()));
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    void testValidateComponentInstanceExist() {
        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, "?", INNER_SERVICE)).thenReturn(expectedResponse);
        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateComponentInstanceExist(null, INNER_SERVICE);
        assertTrue(either.isRight());
        assertEquals(expectedResponse, either.right().value());

        Service service = createService("booleanIncorrect");
        when(componentsUtils.getResponseFormat(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND, service.getName(), "uniqueId"))
            .thenReturn(expectedResponse);
        either = nodeFilterValidator.validateComponentInstanceExist(service, "uniqueId");
        assertTrue(either.isRight());
        assertEquals(expectedResponse, either.right().value());

        List<ComponentInstance> list = new LinkedList<>();
        ComponentInstance instance = new ComponentInstance();
        instance.setUniqueId("uniqueId");
        list.add(instance);
        service.setComponentInstances(list);
        either = nodeFilterValidator.validateComponentInstanceExist(service, "uniqueId");
        assertTrue(either.isLeft());
    }

    @Test
    void testValidateNodeFilterStaticIncorrectPropertyTypeProvided() {
        final Service service = createService("booleanIncorrect");
        final FilterConstraintDto filterConstraintDto = buildFilterConstraintDto(PROPERTY_NAME, FilterValueType.STATIC, ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY, "true");
        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, filterConstraintDto);
        assertTrue(either.isRight());
        filterConstraintDto.setTargetType(PropertyFilterTargetType.CAPABILITY);
        either = nodeFilterValidator.validateFilter(service, INNER_SERVICE, filterConstraintDto);
        assertTrue(either.isRight());
    }

    @Test
    void testValidateComponentFilter() {
        Service service = createService("integer");
        final var filterConstraint1 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            createToscaGetFunction("test", PropertySource.INSTANCE, ToscaGetFunctionType.GET_PROPERTY, List.of("test2"), null)
        );
        Either<Boolean, ResponseFormat> actualValidationResult =
            nodeFilterValidator.validateSubstitutionFilter(service, Collections.singletonList(filterConstraint1));
        assertTrue(actualValidationResult.isRight());

        final var filterConstraint2 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of("Prop1"), null)
        );
        actualValidationResult =
            nodeFilterValidator.validateSubstitutionFilter(service, Collections.singletonList(filterConstraint2));
        assertTrue(actualValidationResult.isLeft());

        final var staticFilter1 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.STATIC,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            1
        );
        actualValidationResult = nodeFilterValidator.validateSubstitutionFilter(service, List.of(staticFilter1));
        assertTrue(actualValidationResult.isLeft());
        assertTrue(actualValidationResult.left().value());

        final var staticFilter2 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.STATIC,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            "true"
        );
        actualValidationResult = nodeFilterValidator.validateSubstitutionFilter(service, List.of(staticFilter2));
        assertTrue(actualValidationResult.isRight());

        service = createService(ToscaPropertyType.BOOLEAN.getType());
        final var staticFilter3 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.STATIC,
            ConstraintType.GREATER_THAN,
            PropertyFilterTargetType.PROPERTY,
            "3"
        );
        actualValidationResult = nodeFilterValidator.validateSubstitutionFilter(service, List.of(staticFilter3));
        assertTrue(actualValidationResult.isRight());

        final var staticFilter4 = buildFilterConstraintDto(
            "test",
            FilterValueType.STATIC,
            ConstraintType.GREATER_THAN,
            PropertyFilterTargetType.PROPERTY,
            "3"
        );
        actualValidationResult = nodeFilterValidator.validateSubstitutionFilter(service, Collections.singletonList(staticFilter4));
        assertTrue(actualValidationResult.isRight());
    }

    @Test
    void testValidateComponentFilterWithIndex() {
        Service service = createService("string", "schema");
        final var filterConstraint1 = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            createToscaGetFunction("test", PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME, "alist"), List.of("1"))
        );
        Map<String, DataTypeDefinition> data = new HashMap<>();
        DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
        List<PropertyDefinition> properties = new ArrayList<>();
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName("alist");
        propertyDefinition.setType("list");

        SchemaDefinition schemaDefinition = new SchemaDefinition();
        PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
        schemaProperty.setType("string");
        schemaDefinition.setProperty(schemaProperty);
        propertyDefinition.setSchema(schemaDefinition);

        properties.add(propertyDefinition);
        dataTypeDefinition.setProperties(properties);

        data.put("string", dataTypeDefinition);
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
        when(applicationDataTypeCache.getAll(null)).thenReturn(allDataTypes);

        Either<Boolean, ResponseFormat> actualValidationResult =
            nodeFilterValidator.validateSubstitutionFilter(service, Collections.singletonList(filterConstraint1));
        assertTrue(actualValidationResult.isLeft());
    }

    @Test
    void testValidateNodeFilterStaticIncorrectOperatorProvidedBoolean() {
        Service service = createService(ToscaPropertyType.BOOLEAN.getType());
        final FilterConstraintDto filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.STATIC,
            ConstraintType.GREATER_THAN,
            PropertyFilterTargetType.PROPERTY,
            "true"
        );
        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, ToscaPropertyType.BOOLEAN.getType(),
            filterConstraintDto.getPropertyName(), "\"true\"")
        ).thenReturn(expectedResponse);
        final Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(filterConstraintDto));
        assertTrue(validationResult.isRight());
        assertEquals(expectedResponse, validationResult.right().value());
    }

    @Test
    void testValidateNodeFilterStaticIncorrectValueProvidedBoolean() {
        final Service service = createService(ToscaPropertyType.BOOLEAN.getType());
        baseFilterConstraintDto.setValue("trues");

        final ResponseFormat responseFormat = new ResponseFormat();
        when(componentsUtils.getResponseFormat
            (ActionStatus.UNSUPPORTED_VALUE_PROVIDED, ToscaPropertyType.BOOLEAN.getType(), PROPERTY_NAME, "\"trues\"")
        ).thenReturn(responseFormat);
        final Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(validationResult.isRight());
        assertEquals(responseFormat, validationResult.right().value());
    }

    @Test
    void testValidateNodeFilterStaticIncorrectOperatorProvidedString() {
        Service service = createService(ToscaPropertyType.STRING.getType());
        baseFilterConstraintDto.setValue("true");
        baseFilterConstraintDto.setOperator(ConstraintType.GREATER_THAN);
        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(either.isLeft());
    }

    @Test
    void testValidateNodeFilterIntegerValueSuccess() {
        Service service = createService(ToscaPropertyType.INTEGER.getType());
        baseFilterConstraintDto.setValue(1);
        Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(validationResult.isLeft());
    }

    @Test
    void testValidateNodeFilterIntegerValueFail() {
        Service service = createService(ToscaPropertyType.INTEGER.getType());

        baseFilterConstraintDto.setValue(1.0);

        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, ToscaPropertyType.INTEGER.getType(),
            baseFilterConstraintDto.getPropertyName(), "1.0")
        ).thenReturn(expectedResponse);
        Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(validationResult.isRight());
        assertEquals(expectedResponse, validationResult.right().value());
    }

    @Test
    void testValidateNodeFilterFloatValueSuccess() {
        final Service service = createService(ToscaPropertyType.FLOAT.getType());
        baseFilterConstraintDto.setValue(1.0);
        final Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(validationResult.isLeft());
        assertTrue(validationResult.left().value());
    }

    @Test
    void testValidateNodeFilterFloatValueFail() {
        Service service = createService(ToscaPropertyType.FLOAT.getType());

        when(componentsUtils.getResponseFormat(ActionStatus.UNSUPPORTED_VALUE_PROVIDED, "param1")).thenReturn(new ResponseFormat());

        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(either.isRight());
    }

    @Test
    void testValidateNodeFilterStringValueSuccess() {
        Service service = createService(ToscaPropertyType.STRING.getType());
        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, INNER_SERVICE, List.of(baseFilterConstraintDto));

        assertTrue(either.isLeft());
    }

    @Test
    void testValidatePropertyConstraintBrotherSuccess() {
        Service service = createService(ToscaPropertyType.STRING.getType());
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(COMPONENT2_ID, PropertySource.INSTANCE, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );
        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto));

        assertTrue(either.isLeft());
        assertTrue(either.left().value());
    }

    @Test
    void testValidatePropertyConstraintParentSuccess() {
        final var service = createService(ToscaPropertyType.STRING.getType());
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );
        final Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto));

        assertTrue(validationResult.isLeft());
        assertTrue(validationResult.left().value());
    }

    @Test
    void testValidatePropertyConstraintBrotherPropertyTypeMismatch() {
        final Service service = createService(ToscaPropertyType.STRING.getType());
        service.getComponentInstancesProperties().get(COMPONENT2_ID).get(0).setType(ToscaPropertyType.INTEGER.getType());
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(COMPONENT2_ID, PropertySource.INSTANCE, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );

        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_PROPERTY_TYPE_MISMATCH,
            PROPERTY_NAME, ToscaPropertyType.INTEGER.getType(), PROPERTY_NAME, ToscaPropertyType.STRING.getType())
        ).thenReturn(expectedResponse);

        final Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto));

        assertTrue(either.isRight());
        assertEquals(expectedResponse, either.right().value());
    }

    @Test
    void testValidatePropertyConstraintParentPropertyTypeMismatch() {
        final Service service = createService(ToscaPropertyType.STRING.getType());
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(ToscaPropertyType.INTEGER.getType());
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );

        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_PROPERTY_TYPE_MISMATCH,
            PROPERTY_NAME, ToscaPropertyType.STRING.getType(), PROPERTY_NAME, ToscaPropertyType.INTEGER.getType())
        ).thenReturn(expectedResponse);

        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto));

        assertTrue(either.isRight());
        assertEquals(expectedResponse, either.right().value());
    }

    @Test
    void testValidatePropertyConstraintParentPropertyNotFound() {
        final Service service = createService(ToscaPropertyType.STRING.getType());
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");

        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(eq(ActionStatus.FILTER_PROPERTY_NOT_FOUND), any(), any()))
            .thenReturn(expectedResponse);

        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );
        Assertions.assertThrows(ComponentException.class,
            () -> nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto)));
    }

    @Test
    void testValidatePropertyConstraintBrotherPropertyNotFound() {
        Service service = createService(ToscaPropertyType.STRING.getType());
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setName("Prop2");
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );
        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.FILTER_PROPERTY_NOT_FOUND, "Target", PROPERTY_NAME))
            .thenReturn(expectedResponse);
        Assertions.assertThrows(ComponentException.class,
            () -> nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto)));
    }

    @Test
    void testValidatePropertyConstraintParentPropertySchemaMismatch() {
        final Service service = createService(ToscaPropertyType.LIST.getType(), ToscaPropertyType.STRING.getType());
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setType(ToscaPropertyType.LIST.getType());
        final var schemaProperty = new PropertyDataDefinition();
        schemaProperty.setType(ToscaPropertyType.INTEGER.getType());
        final var schemaDefinition = new SchemaDefinition();
        schemaDefinition.setProperty(schemaProperty);
        service.getComponentInstancesProperties().get(COMPONENT1_ID).get(0).setSchema(schemaDefinition);

        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );

        final ResponseFormat expectedResponse = new ResponseFormat();
        when(componentsUtils.getResponseFormat(ActionStatus.SOURCE_TARGET_SCHEMA_MISMATCH, PROPERTY_NAME, ToscaPropertyType.INTEGER.getType(),
            PROPERTY_NAME, ToscaPropertyType.STRING.getType())
        ).thenReturn(expectedResponse);

        Either<Boolean, ResponseFormat> either =
            nodeFilterValidator.validateFilter(service, COMPONENT1_ID, List.of(filterConstraintDto));

        assertTrue(either.isRight());
        assertEquals(expectedResponse, either.right().value());
    }

    @Test
    void testValidateNodeFilterForVfStaticValue() {
        Service service = createService(ToscaPropertyType.INTEGER.getType());
        addComponentInstanceToService(service, OriginTypeEnum.VF, "vfInstance", ToscaPropertyType.INTEGER.getType());
        baseFilterConstraintDto.setValue(1);
        Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, "vfInstance", List.of(baseFilterConstraintDto));

        assertTrue(validationResult.isLeft());
    }

    @Test
    void testValidateNodeFilterForVfToscaGetProperty() {
        Service service = createService(ToscaPropertyType.INTEGER.getType());
        addComponentInstanceToService(service, OriginTypeEnum.VF, "vfInstance", ToscaPropertyType.INTEGER.getType());
        final ToscaGetFunctionDataDefinition toscaGetFunction =
            createToscaGetFunction(PARENT_SERVICE_ID, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, List.of(PROPERTY_NAME), null);
        final var filterConstraintDto = buildFilterConstraintDto(
            PROPERTY_NAME,
            FilterValueType.GET_PROPERTY,
            ConstraintType.EQUAL,
            PropertyFilterTargetType.PROPERTY,
            toscaGetFunction
        );
        Either<Boolean, ResponseFormat> validationResult =
            nodeFilterValidator.validateFilter(service, "vfInstance", List.of(filterConstraintDto));

        assertTrue(validationResult.isLeft());
    }

    private void addComponentInstanceToService(Service service, OriginTypeEnum originTypeEnum, String instanceName, String type) {
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(instanceName);
        componentInstance.setName(instanceName);
        componentInstance.setOriginType(originTypeEnum);

        List<ComponentInstance> compInstances = new ArrayList<>();
        service.getComponentInstances().forEach(compInstance -> compInstances.add(compInstance));
        compInstances.add(componentInstance);
        service.setComponentInstances(compInstances);

        if (isInput(originTypeEnum)) {
            ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
            componentInstanceInput.setName(PROPERTY_NAME);
            componentInstanceInput.setType(type);
            if (service.getComponentInstancesInputs() == null) {
                service.setComponentInstancesInputs(new HashMap<>());
            }
            service.getComponentInstancesInputs().put(instanceName, Collections.singletonList(componentInstanceInput));
        } else {
            ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
            componentInstanceProperty.setName(PROPERTY_NAME);
            componentInstanceProperty.setType(type);
            if (service.getComponentInstancesProperties() == null) {
                service.setComponentInstancesProperties(new HashMap<>());
            }
            service.getComponentInstancesProperties().put(instanceName, Collections.singletonList(componentInstanceProperty));
        }
    }

    private boolean isInput(OriginTypeEnum instanceType) {
        return OriginTypeEnum.VF.equals(instanceType) || OriginTypeEnum.PNF.equals(instanceType) || OriginTypeEnum.CVFC.equals(instanceType) ||
            OriginTypeEnum.CR.equals(instanceType);
    }

    private Service createService(String type) {
        return createService(type, null);
    }

    private Service createService(String type, String schemaType) {
        Service service = new Service();
        service.setName(PARENT_SERVICE_ID);

        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(PROPERTY_NAME);
        propertyDefinition.setType(type);
        if (schemaType != null) {
            SchemaDefinition schemaDefinition = new SchemaDefinition();
            PropertyDataDefinition schemaProperty = new PropertyDataDefinition();
            schemaProperty.setType(schemaType);
            schemaDefinition.setProperty(schemaProperty);
            propertyDefinition.setSchema(schemaDefinition);
        }
        service.setProperties(Collections.singletonList(propertyDefinition));

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(COMPONENT1_ID);
        componentInstance.setName(COMPONENT1_ID);
        componentInstance.setOriginType(OriginTypeEnum.VFC);

        ComponentInstance componentInstance2 = new ComponentInstance();
        componentInstance2.setUniqueId(COMPONENT2_ID);
        componentInstance2.setName(COMPONENT2_ID);
        componentInstance2.setOriginType(OriginTypeEnum.VFC);

        ComponentInstance componentInstance3 = new ComponentInstance();
        componentInstance3.setUniqueId(INNER_SERVICE);
        componentInstance3.setName(INNER_SERVICE);
        componentInstance3.setOriginType(OriginTypeEnum.ServiceProxy);

        service.setComponentInstances(Arrays.asList(componentInstance, componentInstance2, componentInstance3));

        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(PROPERTY_NAME);
        componentInstanceProperty.setType(type);

        ComponentInstanceProperty componentInstanceProperty2 = new ComponentInstanceProperty();
        componentInstanceProperty2.setName(PROPERTY_NAME);
        componentInstanceProperty2.setType(type);

        Map<String, List<ComponentInstanceProperty>> componentInstancePropertyMap = new HashMap<>();
        componentInstancePropertyMap.put(componentInstance.getUniqueId(),
            Collections.singletonList(componentInstanceProperty));
        componentInstancePropertyMap.put(componentInstance2.getUniqueId(),
            Collections.singletonList(componentInstanceProperty2));
        componentInstancePropertyMap.put(INNER_SERVICE, Collections.singletonList(componentInstanceProperty));

        service.setComponentInstancesProperties(componentInstancePropertyMap);

        return service;
    }

}

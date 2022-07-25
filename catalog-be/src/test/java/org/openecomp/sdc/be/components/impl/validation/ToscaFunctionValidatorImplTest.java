/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.impl.validation;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ToscaFunctionExceptionSupplier;
import org.openecomp.sdc.be.components.impl.exceptions.ToscaGetFunctionExceptionSupplier;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaGetFunctionDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

class ToscaFunctionValidatorImplTest {

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    private ToscaFunctionValidatorImpl toscaFunctionValidator;

    @BeforeAll
    static void beforeAll() {
        initConfig();
    }

    private static void initConfig() {
        final ConfigurationSource configurationSource = new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-be"
        );
        new ConfigurationManager(configurationSource);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        toscaFunctionValidator = new ToscaFunctionValidatorImpl(applicationDataTypeCache);
    }

    @Test
    void testToscaGetFunctionValidation() {
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String resourceInstanceId = "resourceInstanceId";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String schemaType = "string";
        //creating instance list of string property with get_input value
        final ComponentInstanceProperty propertyGetInput = new ComponentInstanceProperty();
        propertyGetInput.setName("getInputProperty");
        propertyGetInput.setPropertyId(String.format("%s.%s", containerComponentId, "getInputProperty"));
        propertyGetInput.setValue(String.format("get_input: [\"%s\"]", inputName));
        propertyGetInput.setType("list");
        final SchemaDefinition listStringPropertySchema = createSchema(schemaType);
        propertyGetInput.setSchema(listStringPropertySchema);
        propertyGetInput.setToscaFunction(
            createGetToscaFunction(inputName, inputId, List.of(propertyGetInput.getName()), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );
        //creating instance map of string property with get_input value to a second level property:
        // get_input: ["property1", "subProperty1", "subProperty2"]
        final String getPropertyPropertyName = "getPropertyProperty";
        final List<String> containerPropertyPath = List.of("property1", "subProperty1", "subProperty2");
        final String containerPropertyId = String.format("%s.%s", containerComponentId, containerPropertyPath.get(0));
        final String mapToscaType = "map";
        final ComponentInstanceProperty propertyGetProperty = createComponentInstanceProperty(
            String.format("%s.%s", containerComponentId, getPropertyPropertyName),
            getPropertyPropertyName,
            mapToscaType,
            "string",
            String.format("\"get_property\": [\"%s\", \"%s\"]", PropertySource.SELF, String.join("\", \"", containerPropertyPath)),
            createGetToscaFunction(containerPropertyPath.get(containerPropertyPath.size() - 1), containerPropertyId,
                containerPropertyPath, PropertySource.SELF, ToscaGetFunctionType.GET_PROPERTY, containerComponentId, containerComponentName)
        );

        //creating component that has the instance properties
        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);
        //adding instance properties to the component
        final List<ComponentInstanceProperty> resourceInstanceProperties = List.of(propertyGetInput, propertyGetProperty);
        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(resourceInstanceId, resourceInstanceProperties);
        component.setComponentInstancesProperties(componentInstanceProps);

        //creating component input that will be gotten by the get_input instance property
        final var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType(propertyGetInput.getType());
        inputDefinition.setSchema(listStringPropertySchema);
        component.setInputs(List.of(inputDefinition));

        //creating component property that contains the sub property that will be gotten by the get_property instance property
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(containerPropertyPath.get(0));
        propertyDefinition.setUniqueId(containerPropertyId);
        final String property1Type = "property1.datatype";
        propertyDefinition.setType(property1Type);
        component.setProperties(List.of(propertyDefinition));
        //creating resource instance to be added to the component
        final ComponentInstance resourceInstance = createComponentInstance("resourceInstance", resourceInstanceId);
        component.setComponentInstances(List.of(resourceInstance));

        //creating data types for "map", and sub properties
        final Map<String, DataTypeDefinition> allDataTypesMap = new HashMap<>();
        allDataTypesMap.put(mapToscaType, new DataTypeDefinition());

        final String subProperty1Type = "subProperty1.datatype";
        allDataTypesMap.put(property1Type, createDataType(property1Type, Map.of(containerPropertyPath.get(1), subProperty1Type)));

        final var subProperty2Property = new PropertyDefinition();
        subProperty2Property.setName(containerPropertyPath.get(2));
        subProperty2Property.setType(propertyGetProperty.getType());
        subProperty2Property.setSchema(propertyGetProperty.getSchema());
        allDataTypesMap.put(subProperty1Type, createDataType(subProperty1Type, List.of(subProperty2Property)));

        when(applicationDataTypeCache.getAll(component.getModel())).thenReturn(Either.left(allDataTypesMap));
        //when/then
        assertDoesNotThrow(() -> toscaFunctionValidator.validate(propertyGetProperty, component));
        verify(applicationDataTypeCache).getAll(component.getModel());
    }

    @Test
    void testToscaGetPropertyOnInstanceValidation() {
        final String userId = "userId";
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String instanceUniqueId = String.format("%s.%s", containerComponentId, "instanceId");

        final List<String> parentPropertyPath = List.of("property1");
        final String containerPropertyId = String.format("%s.%s", containerComponentId, parentPropertyPath.get(0));
        final ComponentInstanceProperty getPropertyOnInstanceProperty = createComponentInstanceProperty(
            String.format("%s.%s", containerComponentId, "getPropertyOnInstanceProperty"),
            "getPropertyOnInstanceProperty",
            "string",
            null,
            String.format("\"get_property\": [\"%s\", \"%s\"]", PropertySource.INSTANCE, parentPropertyPath.get(0)),
            createGetToscaFunction(parentPropertyPath.get(0), containerPropertyId, parentPropertyPath, PropertySource.INSTANCE,
                ToscaGetFunctionType.GET_PROPERTY, instanceUniqueId, containerComponentName)
        );

        //creating component that has the instance properties
        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);
        component.setLastUpdaterUserId(userId);
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        //adding instance properties to the component
        final List<ComponentInstanceProperty> resourceInstanceProperties = List.of(getPropertyOnInstanceProperty);
        final Map<String, List<ComponentInstanceProperty>> componentInstanceProps = new HashMap<>();
        componentInstanceProps.put(instanceUniqueId, resourceInstanceProperties);
        component.setComponentInstancesProperties(componentInstanceProps);

        //creating resource property that will be get
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setName(parentPropertyPath.get(0));
        propertyDefinition.setUniqueId(containerPropertyId);
        final String property1Type = "string";
        propertyDefinition.setType(property1Type);
        //creating resource instance to be added to the component
        final ComponentInstance resourceInstance = createComponentInstance("resourceInstance", instanceUniqueId);
        resourceInstance.setProperties(List.of(propertyDefinition));
        component.setComponentInstances(List.of(resourceInstance));

        assertDoesNotThrow(() -> toscaFunctionValidator.validate(getPropertyOnInstanceProperty, component));
        verify(applicationDataTypeCache, never()).getAll(component.getModel());
    }

    @ParameterizedTest
    @MethodSource("getToscaFunctionForValidation")
    void validateToscaGetFunctionFailures(final ToscaGetFunctionDataDefinition toscaGetFunction,
                  final ByActionStatusComponentException expectedException) {
        final ComponentInstanceProperty propertyGetInput = new ComponentInstanceProperty();
        propertyGetInput.setName("anyName");
        propertyGetInput.setToscaFunction(toscaGetFunction);
        final Component component = new Service();
        final ComponentException actualException = assertThrows(ComponentException.class,
            () -> toscaFunctionValidator.validate(propertyGetInput, component));
        assertEquals(expectedException.getActionStatus(), actualException.getActionStatus());
        assertArrayEquals(expectedException.getParams(), actualException.getParams());
    }

    @Test
    void validate_propertyNotFoundTest() {
        //given
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "string";
        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            null,
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );

        final Component component = new Service();
        final InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setUniqueId(inputId + "1");
        component.setInputs(List.of(inputDefinition));

        //when/then
        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> toscaFunctionValidator.validate(propertyGetInput, component));

        final ByActionStatusComponentException expectedException =
            ToscaGetFunctionExceptionSupplier
                .propertyNotFoundOnTarget(inputName, PropertySource.SELF, ToscaGetFunctionType.GET_INPUT)
                .get();
        assertEquals(expectedException.getActionStatus(), actualException.getActionStatus());
        assertArrayEquals(expectedException.getParams(), actualException.getParams());
    }

    @Test
    void validate_schemaDivergeTest() {
        //given
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "list";
        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            "string",
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );

        final Component component = new Service();
        component.setUniqueId(containerComponentId);
        component.setName(containerComponentName);

        var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType(propertyType);
        inputDefinition.setSchema(createSchema("integer"));
        component.setInputs(List.of(inputDefinition));

        //when/then
        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> toscaFunctionValidator.validate(propertyGetInput, component));

        final ByActionStatusComponentException expectedException =
            ToscaGetFunctionExceptionSupplier
                .propertySchemaDiverge(propertyGetInput.getToscaFunction().getType(), inputDefinition.getSchemaType(),
                    propertyGetInput.getSchemaType())
                .get();
        assertEquals(expectedException.getActionStatus(), actualException.getActionStatus());
        assertArrayEquals(expectedException.getParams(), actualException.getParams());
    }

    @Test
    void validate_propertyTypeDivergeTest() {
        //given
        final String containerComponentId = "containerComponentId";
        final String containerComponentName = "containerComponentName";
        final String inputName = "myInputToGet";
        final String inputId = String.format("%s.%s", containerComponentId, inputName);
        final String propertyName = "getInputProperty";
        final String propertyId = String.format("%s.%s", containerComponentId, propertyName);
        final String propertyType = "string";

        final ComponentInstanceProperty propertyGetInput = createComponentInstanceProperty(
            propertyId,
            "getInputProperty",
            propertyType,
            "string",
            String.format("get_input: [\"%s\"]", inputName),
            createGetToscaFunction(inputName, inputId, List.of(propertyName), PropertySource.SELF, ToscaGetFunctionType.GET_INPUT,
                containerComponentId, containerComponentName)
        );

        final Component component = new Service();
        component.setName(containerComponentName);
        component.setUniqueId(containerComponentId);

        var inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setType("integer");
        component.setInputs(List.of(inputDefinition));

        //when/then
        final ByActionStatusComponentException actualException = assertThrows(ByActionStatusComponentException.class,
            () -> toscaFunctionValidator.validate(propertyGetInput, component));

        final ByActionStatusComponentException expectedException =
            ToscaGetFunctionExceptionSupplier
                .propertyTypeDiverge(propertyGetInput.getToscaFunction().getType(), inputDefinition.getType(), propertyGetInput.getType())
                .get();
        assertEquals(expectedException.getActionStatus(), actualException.getActionStatus());
        assertArrayEquals(expectedException.getParams(), actualException.getParams());
    }

    private static Stream<Arguments> getToscaFunctionForValidation() {
        final var toscaGetFunction1 = new ToscaGetFunctionDataDefinition();
        final ByActionStatusComponentException expectedResponse1 = ToscaFunctionExceptionSupplier
            .missingFunctionType().get();

        final var toscaGetFunction2 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction2.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        final ByActionStatusComponentException expectedResponse2 = ToscaGetFunctionExceptionSupplier
            .targetPropertySourceNotFound(toscaGetFunction2.getFunctionType()).get();

        final var toscaGetFunction3 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction3.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetFunction3.setPropertySource(PropertySource.SELF);
        final ByActionStatusComponentException expectedResponse3 = ToscaGetFunctionExceptionSupplier
            .targetSourcePathNotFound(toscaGetFunction3.getFunctionType()).get();

        final var toscaGetFunction4 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction4.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetFunction4.setPropertySource(PropertySource.SELF);
        toscaGetFunction4.setPropertyPathFromSource(List.of("sourcePath"));
        final ByActionStatusComponentException expectedResponse4 = ToscaGetFunctionExceptionSupplier
            .sourceNameNotFound(toscaGetFunction4.getPropertySource()).get();

        final var toscaGetFunction5 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction5.setFunctionType(ToscaGetFunctionType.GET_INPUT);
        toscaGetFunction5.setPropertySource(PropertySource.SELF);
        toscaGetFunction5.setPropertyPathFromSource(List.of("sourcePath"));
        toscaGetFunction5.setSourceName("sourceName");
        final ByActionStatusComponentException expectedResponse5 = ToscaGetFunctionExceptionSupplier
            .sourceIdNotFound(toscaGetFunction5.getPropertySource()).get();

        final var toscaGetFunction6 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction6.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
        toscaGetFunction6.setPropertySource(PropertySource.SELF);
        toscaGetFunction6.setPropertyPathFromSource(List.of("sourcePath"));
        toscaGetFunction6.setSourceName("sourceName");
        toscaGetFunction6.setSourceUniqueId("sourceUniqueId");
        final ByActionStatusComponentException expectedResponse6 = ToscaGetFunctionExceptionSupplier
            .propertyNameNotFound(toscaGetFunction6.getPropertySource()).get();

        final var toscaGetFunction7 = new ToscaGetFunctionDataDefinition();
        toscaGetFunction7.setFunctionType(ToscaGetFunctionType.GET_PROPERTY);
        toscaGetFunction7.setPropertySource(PropertySource.SELF);
        toscaGetFunction7.setPropertyPathFromSource(List.of("sourcePath"));
        toscaGetFunction7.setSourceName("sourceName");
        toscaGetFunction7.setSourceUniqueId("sourceUniqueId");
        toscaGetFunction7.setPropertyName("propertyName");
        final ByActionStatusComponentException expectedResponse7 = ToscaGetFunctionExceptionSupplier
            .propertyIdNotFound(toscaGetFunction7.getPropertySource()).get();

        return Stream.of(
            Arguments.of(toscaGetFunction1, expectedResponse1),
            Arguments.of(toscaGetFunction2, expectedResponse2),
            Arguments.of(toscaGetFunction3, expectedResponse3),
            Arguments.of(toscaGetFunction4, expectedResponse4),
            Arguments.of(toscaGetFunction5, expectedResponse5),
            Arguments.of(toscaGetFunction6, expectedResponse6),
            Arguments.of(toscaGetFunction7, expectedResponse7)
        );
    }

    private ComponentInstanceProperty createComponentInstanceProperty(final String uniqueId, final String name, final String type,
                                                                      final String schemaType, final String value,
                                                                      final ToscaGetFunctionDataDefinition toscaGetFunction) {
        final var componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(name);
        componentInstanceProperty.setUniqueId(uniqueId);
        componentInstanceProperty.setType(type);
        componentInstanceProperty.setValue(value);
        if (schemaType != null) {
            final SchemaDefinition schemaDefinition = createSchema(schemaType);
            componentInstanceProperty.setSchema(schemaDefinition);
        }
        if (toscaGetFunction != null) {
            componentInstanceProperty.setToscaFunction(toscaGetFunction);
        }

        return componentInstanceProperty;
    }

    private SchemaDefinition createSchema(final String schemaType) {
        final var schemaDefinition = new SchemaDefinition();
        final var schemaProperty = new PropertyDefinition();
        schemaProperty.setType(schemaType);
        schemaDefinition.setProperty(schemaProperty);
        return schemaDefinition;
    }

    private ToscaGetFunctionDataDefinition createGetToscaFunction(final String propertyName, final String propertyUniqueId,
                                                                  final List<String> propertyPathFromSource,
                                                                  final PropertySource propertySource, final ToscaGetFunctionType functionType,
                                                                  final String sourceUniqueId,
                                                                  final String sourceName) {
        final var toscaGetFunction = new ToscaGetFunctionDataDefinition();
        toscaGetFunction.setFunctionType(functionType);
        toscaGetFunction.setPropertyUniqueId(propertyUniqueId);
        toscaGetFunction.setPropertyName(propertyName);
        toscaGetFunction.setPropertyPathFromSource(propertyPathFromSource);
        toscaGetFunction.setPropertySource(propertySource);
        toscaGetFunction.setSourceName(sourceName);
        toscaGetFunction.setSourceUniqueId(sourceUniqueId);
        return toscaGetFunction;
    }

    private DataTypeDefinition createDataType(final String name, final Map<String, String> propertyNameAndTypeMap) {
        final var dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(name);
        if (MapUtils.isNotEmpty(propertyNameAndTypeMap)) {
            for (final Entry<String, String> propertyEntry : propertyNameAndTypeMap.entrySet()) {
                final var propertyDefinition = new PropertyDefinition();
                propertyDefinition.setName(propertyEntry.getKey());
                propertyDefinition.setType(propertyEntry.getValue());
                dataTypeDefinition.setProperties(List.of(propertyDefinition));
            }
        }
        return dataTypeDefinition;
    }

    private DataTypeDefinition createDataType(final String name, final List<PropertyDefinition> propertyList) {
        final var dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setName(name);
        if (CollectionUtils.isNotEmpty(propertyList)) {
            dataTypeDefinition.setProperties(propertyList);
        }
        return dataTypeDefinition;
    }

    private ComponentInstance createComponentInstance(String name, String uniqueId) {
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName(name);
        componentInstance.setUniqueId(uniqueId);
        return componentInstance;
    }

}
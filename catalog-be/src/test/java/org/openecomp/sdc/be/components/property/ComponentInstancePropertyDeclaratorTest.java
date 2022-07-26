/*
 * Copyright © 2016-2019 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.property.CapabilityTestUtils.createCapabilityDefinition;
import static org.openecomp.sdc.be.components.property.CapabilityTestUtils.createProperties;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

@ExtendWith(MockitoExtension.class)
class ComponentInstancePropertyDeclaratorTest extends PropertyDeclaratorTestBase {

    @InjectMocks
    private ComponentInstancePropertyDeclarator testInstance;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Mock
    private PropertyOperation propertyOperation;

    @Captor
    private ArgumentCaptor<Map<String, List<ComponentInstanceProperty>>> instancePropertiesCaptor;

    private static final String PROPERTY_ID = "propertyUid";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String SERVICE_ID = "serviceUid";
    private static final String SERVICE_NAME = "serviceName";

    @Test
    void declarePropertiesAsInputs_componentInstanceNotExist() {
        Component cmpt = new Resource();
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(cmpt, "someCmptInstId",
            Collections.emptyList());
        assertEquals(StorageOperationStatus.NOT_FOUND, createdInputs.right().value());
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    void declarePropertiesAsInputs_singleNonComplexProperty() {
        List<PropertyDataDefinition> properties = Collections.singletonList(prop1);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture()))
            .thenReturn(Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1",
            propsToDeclare);
        assertTrue(createdInputs.isLeft());
        List<InputDefinition> inputs = createdInputs.left().value();
        assertNotNull(inputs);
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        assertNotNull(capturedInstanceProperties);
        verifyCreatedInputs(properties, capturedInstanceProperties, inputs);
        verifyUpdatedProperties(properties, capturedInstanceProperties, inputs);
    }

    @Test
    void declareCapabilitiesPropertiesAsInputs() {
        prop1.setParentUniqueId("capUniqueId");
        List<PropertyDataDefinition> properties = Collections.singletonList(prop1);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor
            .capture())).thenReturn(Either.left(Collections.emptyMap()));

        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();

        List<ComponentInstanceProperty> capPropList = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = createProperties();
        capPropList.add(instanceProperty);
        capabilityDefinition.setProperties(capPropList);

        capabilityDefinition.setPath(Collections.singletonList("path"));
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));
        resource.setCapabilities(capabilityMap);

        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance
            .declarePropertiesAsInputs(resource, "inst1", propsToDeclare);
        assertTrue(createdInputs.isLeft());
    }

    @Test
    void testUnDeclarePropertiesAsInputs() throws Exception {
        Component component = new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setUniqueId("resourceId")
            .setName("resourceName").build();
        InputDefinition input = new InputDefinition();
        input.setUniqueId("ComponentInput1_uniqueId");
        input.setPropertyId("ComponentInput1_uniqueId");

        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = createProperties();

        List<GetInputValueDataDefinition> valueDataDefinitionList = new ArrayList<>();
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputId("ComponentInput1_uniqueId");
        getInputValueDataDefinition.setPropName("prop_name");
        valueDataDefinitionList.add(getInputValueDataDefinition);

        instanceProperty.setGetInputValues(valueDataDefinitionList);
        properties.add(instanceProperty);
        capabilityDefinition.setProperties(properties);
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));
        component.setCapabilities(capabilityMap);
        component.setInputs(Collections.singletonList(input));
        when(toscaOperationFacade.updateInstanceCapabilityProperty(any(Resource.class), any(),
            any(ComponentInstanceProperty.class), any(CapabilityDefinition.class))).thenReturn(StorageOperationStatus.OK);

        StorageOperationStatus result = testInstance.unDeclarePropertiesAsInputs(component, input);
        assertEquals(StorageOperationStatus.OK, result);
    }

    @Test
    void declarePropertiesAsInputs_multipleNonComplexProperty() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(
            Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1",
            propsToDeclare);

        List<InputDefinition> inputs = createdInputs.left().value();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        verifyCreatedInputs(properties, capturedInstanceProperties, inputs);
        verifyUpdatedProperties(properties, capturedInstanceProperties, inputs);
    }

    @Test
    void declarePropertiesAsInputs_singleComplexProperty() {
        PropertyDefinition innerProp1 = new PropertyDataDefinitionBuilder()
            .setName(INNER_PROP1)
            .setValue("true")
            .setType("boolean")
            .setUniqueId(complexProperty.getType() + ".datatype.ecomp_generated_naming")
            .build();
        PropertyDefinition innerProp2 = new PropertyDataDefinitionBuilder()
            .setName(INNER_PROP2)
            .setValue("abc")
            .setType("string")
            .setUniqueId(complexProperty.getType() + ".datatype.ecomp_generated_naming")
            .build();
        List<ComponentInstancePropInput> propsToDeclare = createComplexPropInputList(innerProp1, innerProp2);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(
            Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1",
            propsToDeclare);

        List<InputDefinition> inputs = createdInputs.left().value();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);

        verifyCreatedInputsFromComplexProperty(propsToDeclare, capturedInstanceProperties, inputs);
        verifyUpdatedComplexProperty(capturedInstanceProperties, inputs);
    }

    @Test
    void testCreateDeclaredProperty() {
        PropertyDefinition propertyDefinition = getPropertyForDeclaration();
        ComponentInstanceProperty declaredProperty = testInstance.createDeclaredProperty(propertyDefinition);

        assertNotNull(declaredProperty);
        assertEquals(propertyDefinition.getUniqueId(), declaredProperty.getUniqueId());
    }

    @Test
    void testUndeclareProperty() {
        Service service = new ServiceBuilder()
            .setUniqueId(SERVICE_ID)
            .setName(SERVICE_NAME)
            .build();

        InputDefinition inputToDelete = InputsBuilder
            .create()
            .setPropertyId(PROPERTY_ID)
            .setName(PROPERTY_NAME)
            .build();

        inputToDelete.setGetInputValues(getGetInputListForDeclaration());

        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty(getPropertyForDeclaration());
        List<ComponentInstanceProperty> componentInstanceProperties = new ArrayList<>();
        componentInstanceProperties.add(componentInstanceProperty);

        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(any(), any())).thenReturn(new LinkedList<>());

        StorageOperationStatus undeclareStatus =
            testInstance.unDeclarePropertiesAsInputs(service, inputToDelete);

        assertEquals(StorageOperationStatus.OK, undeclareStatus);
    }

    private List<GetInputValueDataDefinition> getGetInputListForDeclaration() {
        GetInputValueDataDefinition getInput = new GetInputValueDataDefinition();
        getInput.setInputId(PROPERTY_ID);
        getInput.setInputName(PROPERTY_NAME);
        getInput.setPropName(PROPERTY_NAME);
        List<GetInputValueDataDefinition> getInputList = new ArrayList<>();
        getInputList.add(getInput);
        return getInputList;
    }

    private PropertyDefinition getPropertyForDeclaration() {
        return new PropertyDataDefinitionBuilder()
            .setUniqueId(PROPERTY_ID)
            .build();
    }

    @Test
    void declarePropertiesAsListInput() {
        // construct arguments
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        InputDefinition input = new InputDefinition(new PropertyDataDefinitionBuilder()
            .setName("listinput")
            .setType("list")
            .setDescription("description")
            .setSchemaType("org.onap.datatype.listinput")
            .build());
        // mock returns
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(
            Either.left(Collections.emptyMap()));
        Either<InputDefinition, StorageOperationStatus> result = testInstance.declarePropertiesAsListInput(resource, "inst1", propsToDeclare, input);
        // validate result
        assertTrue(result.isLeft());
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        assertEquals(2, capturedInstanceProperties.size());
        Map<String, PropertyDataDefinition> propertiesMap =
            properties.stream().collect(Collectors.toMap(PropertyDataDefinition::getName, e -> e));
        for (ComponentInstanceProperty instanceProperty : capturedInstanceProperties) {
            assertTrue(propertiesMap.containsKey(instanceProperty.getName()));
            PropertyDataDefinition property = propertiesMap.get(instanceProperty.getName());
            assertEquals(property.getType(), instanceProperty.getType());
            assertTrue(instanceProperty.isGetInputProperty());
        }
    }

    @Test
    void declarePropertiesAsListInput_propertyOwnerNotFound() {
        // construct arguments
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        InputDefinition input = new InputDefinition(new PropertyDataDefinitionBuilder()
            .setName("listinput")
            .setType("list")
            .setDescription("description")
            .setSchemaType("org.onap.datatype.listinput")
            .build());
        Either<InputDefinition, StorageOperationStatus> result = testInstance.declarePropertiesAsListInput(resource, "inst2", propsToDeclare, input);
        // validate result
        assertTrue(result.isRight());
        assertEquals(StorageOperationStatus.NOT_FOUND, result.right().value());
    }

    @Test
    void unDeclarePropertiesAsListInputsTest() {
        InputDefinition inputToDelete = new InputDefinition();
        inputToDelete.setUniqueId(INPUT_ID);
        inputToDelete.setName(INPUT_ID);
        inputToDelete.setIsDeclaredListInput(true);

        Component component = createComponentWithListInput(INPUT_ID, "innerPropName");
        PropertyDefinition prop = new PropertyDataDefinitionBuilder()
            .setName("propName")
            .setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"))
            .setType("list")
            .setUniqueId("propName")
            .addGetInputValue(INPUT_ID)
            .build();
        component.setProperties(Collections.singletonList(prop));

        List<ComponentInstanceProperty> ciPropList = new ArrayList<>();
        ComponentInstanceProperty ciProp = new ComponentInstanceProperty();
        List<String> pathOfComponentInstances = new ArrayList<>();
        pathOfComponentInstances.add("pathOfComponentInstances");
        ciProp.setPath(pathOfComponentInstances);
        ciProp.setUniqueId("componentInstanceId");
        ciProp.setDefaultValue("default value");
        ciProp.setComponentInstanceId("componentInstanceId");
        ciProp.setComponentInstanceName("componentInstanceName");
        ciProp.setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"));
        ciPropList.add(ciProp);

        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(component, INPUT_ID)).thenReturn(ciPropList);
        when(propertyOperation.findDefaultValueFromSecondPosition(pathOfComponentInstances, ciProp.getUniqueId(), ciProp.getDefaultValue())).thenReturn(Either.left(ciProp.getDefaultValue()));
        when(toscaOperationFacade.updateComponentInstanceProperties(component, ciProp.getComponentInstanceId(), ciPropList)).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = testInstance.unDeclarePropertiesAsListInputs(component, inputToDelete);

        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
    }

    @Test
    void unDeclarePropertiesAsListInputsTest_whenNoListInput_returnOk() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
        List<ComponentInstanceProperty> resList = new ArrayList<>();
        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(resource, INPUT_ID)).thenReturn(resList);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        assertEquals(StorageOperationStatus.OK, status);
    }

    private void verifyUpdatedProperties(List<PropertyDataDefinition> properties, List<ComponentInstanceProperty> capturedInstanceProperties,
                                         List<InputDefinition> inputs) {
        assertEquals(properties.size(), capturedInstanceProperties.size());
        Map<String, ComponentInstanceProperty> updatedPropertiesByName = MapUtil.toMap(capturedInstanceProperties,
            ComponentInstanceProperty::getName);
        properties.forEach(prop -> verifyUpdatedInstanceProperty(prop, updatedPropertiesByName.get(prop.getName()), inputs));
    }

    private void verifyUpdatedComplexProperty(List<ComponentInstanceProperty> capturedInstanceProperties, List<InputDefinition> inputs) {
        assertEquals(1, capturedInstanceProperties.size());
        verifyUpdatedInstanceComplexProperty(capturedInstanceProperties.get(0), inputs);
    }

    private void verifyCreatedInputs(List<PropertyDataDefinition> originalPropsToDeclare, List<ComponentInstanceProperty> capturedUpdatedProperties,
                                     List<InputDefinition> inputs) {
        assertEquals(originalPropsToDeclare.size(), inputs.size());
        Map<String, InputDefinition> propertyIdToCreatedInput = MapUtil.toMap(inputs, InputDefinition::getPropertyId);
        originalPropsToDeclare.forEach(propToDeclare -> verifyCreatedInput(propToDeclare, propertyIdToCreatedInput.get(propToDeclare.getUniqueId())));
        capturedUpdatedProperties.forEach(
            updatedProperty -> verifyInputPropertiesList(updatedProperty, propertyIdToCreatedInput.get(updatedProperty.getUniqueId())));
    }

    private void verifyCreatedInputsFromComplexProperty(List<ComponentInstancePropInput> propsToDeclare,
                                                        List<ComponentInstanceProperty> capturedInstanceProperties, List<InputDefinition> inputs) {
        assertEquals(propsToDeclare.size(), inputs.size());
        Map<String, InputDefinition> inputsByName = MapUtil.toMap(inputs, InputDefinition::getName);
        propsToDeclare.forEach(propToDeclare -> verifyCreatedInputFromComplexProperty(propToDeclare, inputsByName));
        Map<String, List<InputDefinition>> propertyIdToCreatedInput = MapUtil.groupListBy(inputs, InputDefinition::getPropertyId);
        capturedInstanceProperties.forEach(updatedProperty -> verifyInputPropertiesListFromComplexProperty(updatedProperty,
            propertyIdToCreatedInput.get(updatedProperty.getUniqueId())));
    }

    private void verifyInputPropertiesListFromComplexProperty(ComponentInstanceProperty updatedProperty, List<InputDefinition> inputs) {
        inputs.forEach(input -> verifyInputPropertiesList(updatedProperty, input));
    }

    private void verifyCreatedInputFromComplexProperty(ComponentInstancePropInput parentProperty, Map<String, InputDefinition> inputsByName) {
        PropertyDefinition innerProperty = parentProperty.getInput();
        String expectedInputName = generateExpectedInputName(parentProperty, innerProperty);
        InputDefinition input = inputsByName.get(expectedInputName);
        assertEquals(innerProperty.getType(), input.getType());
        assertEquals(UniqueIdBuilder.buildPropertyUniqueId(RESOURCE_ID, input.getName()), input.getUniqueId());
        assertEquals(INSTANCE_ID, input.getInstanceUniqueId());

    }

    private void verifyInputPropertiesList(ComponentInstanceProperty updatedProperty, InputDefinition input) {
        assertEquals(1, input.getProperties().size());
        assertEquals(input.getProperties().get(0), updatedProperty);
        assertTrue(updatedProperty.getDefaultValue().contains(input.getName()));
    }

    private List<ComponentInstancePropInput> createComplexPropInputList(PropertyDefinition... innerProperties) {
        return Stream.of(innerProperties).map(this::createComplexPropInput).collect(Collectors.toList());
    }

    private ComponentInstancePropInput createComplexPropInput(PropertyDefinition innerProp) {
        ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput(new ComponentInstanceProperty(complexProperty));
        componentInstancePropInput.setInput(innerProp);
        componentInstancePropInput.setPropertiesName(complexProperty.getName() + "#" + innerProp.getName());
        return componentInstancePropInput;
    }

    private void verifyUpdatedInstanceProperty(PropertyDataDefinition originalProperty, ComponentInstanceProperty updatedProperty,
                                               List<InputDefinition> inputs) {
        assertEquals(generateGetInputValue(generateExpectedInputName(originalProperty)), updatedProperty.getValue());
        assertTrue(updatedProperty.isGetInputProperty());
        assertEquals(originalProperty.getName(), updatedProperty.getName());
        List<GetInputValueDataDefinition> getInputValues = updatedProperty.getGetInputValues();
        verifyGetInputValues(getInputValues, inputs);
    }

    private void verifyUpdatedInstanceComplexProperty(ComponentInstanceProperty updatedComplexProperty, List<InputDefinition> inputs) {
        assertEquals(generateComplexGetInputValue(inputs), updatedComplexProperty.getValue());
        assertTrue(updatedComplexProperty.isGetInputProperty());
        assertEquals(complexProperty.getName(), updatedComplexProperty.getName());
        List<GetInputValueDataDefinition> getInputValues = updatedComplexProperty.getGetInputValues();
        verifyGetInputValues(getInputValues, inputs);
    }

    private void verifyGetInputValues(List<GetInputValueDataDefinition> getInputValues, List<InputDefinition> inputs) {
        Map<String, InputDefinition> inputsByName = MapUtil.toMap(inputs, InputDefinition::getName);
        getInputValues.forEach(getInputVal -> {
            InputDefinition input = inputsByName.get(getInputVal.getInputName());
            assertEquals(getInputVal.getInputId(), input.getUniqueId());
        });
    }

    private String generateComplexGetInputValue(List<InputDefinition> createdInputs) {
        return String.format("{\"%s\":%s,\"%s\":%s}", INNER_PROP1, generateGetInputValue(createdInputs.get(0).getName()), INNER_PROP2,
            generateGetInputValue(createdInputs.get(1).getName()));
    }

    private String generateExpectedInputName(PropertyDataDefinition prop) {
        return INSTANCE_ID + "_" + prop.getName();
    }

    private String generateExpectedInputName(PropertyDefinition parentProp, PropertyDefinition innerProperty) {
        return INSTANCE_ID + "_" + parentProp.getName() + "_" + innerProperty.getName();
    }

    private void verifyCreatedInput(PropertyDataDefinition property, InputDefinition input) {
        assertEquals(property.getType(), input.getType());
        assertEquals(generateExpectedInputName(property), input.getName());
        assertEquals(UniqueIdBuilder.buildPropertyUniqueId(RESOURCE_ID, input.getName()), input.getUniqueId());
        assertEquals(property.getUniqueId(), input.getPropertyId());
        assertEquals(INSTANCE_ID, input.getInstanceUniqueId());
        assertNull(input.getValue());
        assertNull(input.getDefaultValue());
    }

    private Component createComponentWithListInput(String inputName, String propName) {
        InputDefinition input = InputsBuilder.create()
            .setName(inputName)
            .build();

        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setDefaultValue("defaultValue");
        input.setValue(generateGetInputValueAsListInput(inputName, propName));

        return new ResourceBuilder()
            .setUniqueId(RESOURCE_ID)
            .addInput(input)
            .build();
    }
}

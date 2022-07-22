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

package org.openecomp.sdc.be.components.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

@ExtendWith(MockitoExtension.class)
class GroupPropertyDeclaratorTest extends PropertyDeclaratorTestBase {

    private static final String GROUP_ID = "groupId";
    @InjectMocks
    private GroupPropertyDeclarator groupPropertyDeclarator;
    @Mock
    private GroupOperation groupOperation;
    @Mock
    private PropertyOperation propertyOperation;
    @Captor
    private ArgumentCaptor<List<PropertyDataDefinition>> updatedPropsCapture;
    private Resource resource;
    private InputDefinition input;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        resource = createResourceWithGroup();
        input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
    }

    @Test
    void testDeclarePropertiesAsInputs_groupNotExist() {
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = groupPropertyDeclarator.declarePropertiesAsInputs(resource,
            "nonExistingGroup", Collections.emptyList());
        assertEquals(StorageOperationStatus.NOT_FOUND, declareResult.right().value());
        verifyZeroInteractions(groupOperation);
    }

    @Test
    void testDeclarePropertiesAsInputs_failedToUpdateProperties() {
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = groupPropertyDeclarator.declarePropertiesAsInputs(resource, GROUP_ID,
            Collections.emptyList());
        assertEquals(StorageOperationStatus.GENERAL_ERROR, declareResult.right().value());
    }

    @Test
    void testDeclarePropertiesAsInputs() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = groupPropertyDeclarator.declarePropertiesAsInputs(resource, GROUP_ID,
            propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        assertEquals(2, inputs.size());
        verifyInputPropertiesList(inputs, updatedPropsCapture.getValue());
        //creation of inputs values is part of the DefaultPropertyDeclarator and is tested in the ComponentInstancePropertyDeclaratorTest class
    }

    @Test
    void testUnDeclareProperties_whenComponentHasNoGroups_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    void testUnDeclareProperties_whenNoPropertiesFromGroupMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(createResourceWithGroup(), input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    void whenFailingToUpdateDeclaredProperties_returnErrorStatus() {
        Resource resource = createResourceWithGroups(GROUP_ID);
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        assertTrue(groupDefinition.isPresent());
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        groupDefinition.get().setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, storageOperationStatus);
    }

    @Test
    void testUnDeclareProperties_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithGroups(GROUP_ID, "groupId2");
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        groupDefinition.get().setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);

        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertEquals(1, updatedProperties.size());
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertFalse(updatedProperty.isGetInputProperty());
        assertNull(updatedProperty.getValue());
        assertNull(getInputPropForInput.getDefaultValue());
        assertEquals(getInputPropForInput.getUniqueId(), updatedProperty.getUniqueId());
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_whenComponentHasNoGroups_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_whenNoPropertiesFromGroupMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(createResourceWithGroup(), input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    void whenFailingToUpdateDeclaredPropertiesAsListInputs_returnErrorStatus() {
        Resource resource = createResourceWithGroups(GROUP_ID);
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        assertTrue(groupDefinition.isPresent());
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        groupDefinition.get().setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, storageOperationStatus);
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithGroups(GROUP_ID, "groupId3");
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        groupDefinition.get().setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);

        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertEquals(1, updatedProperties.size());
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertFalse(updatedProperty.isGetInputProperty());
        assertNull(updatedProperty.getValue());
        assertNull(updatedProperty.getDefaultValue());
        assertEquals(getInputPropForInput.getUniqueId(), updatedProperty.getUniqueId());
    }

    private Resource createResourceWithGroup() {
        return createResourceWithGroups(GROUP_ID);
    }

    private Resource createResourceWithGroups(String... groups) {
        List<GroupDefinition> groupsDef = Stream.of(groups)
            .map(this::buildGroup)
            .collect(Collectors.toList());

        return new ResourceBuilder()
            .setUniqueId(RESOURCE_ID)
            .setGroups(groupsDef)
            .build();
    }

    private GroupDefinition buildGroup(String groupId) {
        return GroupDefinitionBuilder.create()
            .setUniqueId(groupId)
            .setName(groupId)
            .build();
    }

    private PropertyDataDefinition buildGetInputProperty(String inputId) {
        return new PropertyDataDefinitionBuilder()
            .addGetInputValue(inputId)
            .setUniqueId(GROUP_ID + "_" + inputId)
            .setDefaultValue("defaultValue")
            .setValue(generateGetInputValue(inputId))
            .build();
    }

}

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.PolicyOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

@ExtendWith(MockitoExtension.class)
//note that testing for most of the common logic is under the ComponentInstancePropertyDeclaratorTest
class PolicyPropertyDeclaratorTest extends PropertyDeclaratorTestBase {

    private static final String POLICY_ID = "policyId";
    @InjectMocks
    private PolicyPropertyDeclarator policyPropertyDeclarator;
    @Mock
    private PolicyOperation policyOperation;
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
        resource = createResourceWithPolicy();
        input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
    }

    @Test
    void testDeclarePropertiesAsInputs_policyNotExist() {
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = policyPropertyDeclarator.declarePropertiesAsInputs(resource,
            "nonExistingPolicy", Collections.emptyList());
        assertEquals(StorageOperationStatus.NOT_FOUND, declareResult.right().value());
        verifyZeroInteractions(policyOperation);
    }

    @Test
    void testDeclarePropertiesAsInputs_failedToUpdateProperties() {
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = policyPropertyDeclarator.declarePropertiesAsInputs(resource, POLICY_ID,
            Collections.emptyList());
        assertEquals(StorageOperationStatus.GENERAL_ERROR, declareResult.right().value());
    }

    @Test
    void testDeclarePropertiesAsInputs() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.OK);
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = policyPropertyDeclarator.declarePropertiesAsInputs(resource, POLICY_ID,
            propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        assertEquals(2, inputs.size());
        verifyInputPropertiesList(inputs, updatedPropsCapture.getValue());
        //creation of inputs values is part of the DefaultPropertyDeclarator and is tested in the ComponentInstancePropertyDeclaratorTest class
    }

    @Test
    void testUnDeclareProperties_whenComponentHasNoPolicies_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    void testUnDeclareProperties_whenNoPropertiesFromPolicyMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(createResourceWithPolicy(), input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    void whenFailingToUpdateDeclaredProperties_returnErrorStatus() {
        Resource resource = createResourceWithPolicies(POLICY_ID);
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        policyDefinition.setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, storageOperationStatus);
    }

    @Test
    void testUnDeclareProperties_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithPolicies(POLICY_ID, "policyId2");
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        policyDefinition.setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);

        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertEquals(1, updatedProperties.size());
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertFalse(updatedProperty.isGetInputProperty());
        assertNull(updatedProperty.getValue());
        assertNull(updatedProperty.getDefaultValue());
        assertEquals(getInputPropForInput.getUniqueId(), updatedProperty.getUniqueId());
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_whenComponentHasNoPolicies_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_whenNoPropertiesFromPolicyMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsListInputs(createResourceWithPolicy(), input);
        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    void whenFailingToUpdateDeclaredPropertiesAsListInputs_returnErrorStatus() {
        Resource resource = createResourceWithPolicies(POLICY_ID);
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        policyDefinition.setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertEquals(StorageOperationStatus.GENERAL_ERROR, storageOperationStatus);
    }

    @Test
    void testUnDeclarePropertiesAsListInputs_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithPolicies(POLICY_ID, "policyId3");
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        policyDefinition.setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(),
            getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(
            StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);

        assertEquals(StorageOperationStatus.OK, storageOperationStatus);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertEquals(1, updatedProperties.size());
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertFalse(updatedProperty.isGetInputProperty());
        assertNull(updatedProperty.getValue());
        assertNull(updatedProperty.getDefaultValue());
        assertEquals(getInputPropForInput.getUniqueId(), updatedProperty.getUniqueId());
    }

    private Resource createResourceWithPolicy() {
        return createResourceWithPolicies(POLICY_ID);
    }

    private Resource createResourceWithPolicies(String... policies) {
        List<PolicyDefinition> policiesDef = Stream.of(policies)
            .map(this::buildPolicy)
            .collect(Collectors.toList());

        return new ResourceBuilder()
            .setUniqueId(RESOURCE_ID)
            .setPolicies(policiesDef)
            .build();
    }

    private PolicyDefinition buildPolicy(String policyId) {
        return PolicyDefinitionBuilder.create()
            .setUniqueId(policyId)
            .setName(policyId)
            .build();
    }

    private PropertyDataDefinition buildGetInputProperty(String inputId) {
        return new PropertyDataDefinitionBuilder()
            .addGetInputValue(inputId)
            .setUniqueId(POLICY_ID + "_" + inputId)
            .setDefaultValue("defaultValue")
            .setValue(generateGetInputValue(inputId))
            .build();
    }


}


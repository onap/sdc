package org.openecomp.sdc.be.components.property;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import fj.data.Either;


@RunWith(MockitoJUnitRunner.class)
//note that testing for most of the common logic is under the ComponentInstancePropertyDeceleratorTest
public class PolicyPropertyDeceleratorTest extends PropertyDeceleratorTestBase{

    private static final String POLICY_ID = "policyId";
    private static final String RESOURCE_ID = "resourceId";
    private static final String INPUT_ID = "inputId";
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
    @Before
    public void setUp() throws Exception {
        super.setUp();
        resource = createResourceWithPolicy();
        input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
    }

    @Test
    public void testDeclarePropertiesAsInputs_policyNotExist() {
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = policyPropertyDeclarator.declarePropertiesAsInputs(resource, "nonExistingPolicy", Collections.emptyList());
        assertThat(declareResult.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    public void testDeclarePropertiesAsInputs_failedToUpdateProperties() {
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = policyPropertyDeclarator.declarePropertiesAsInputs(resource, POLICY_ID, Collections.emptyList());
        assertThat(declareResult.right().value()).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void testDeclarePropertiesAsInputs() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = policyPropertyDeclarator.declarePropertiesAsInputs(resource, POLICY_ID, propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        assertThat(inputs).hasSize(2);
        verifyInputPropertiesList(inputs, updatedPropsCapture.getValue());
        //creation of inputs values is part of the DefaultPropertyDecelerator and is tested in the ComponentInstancePropertyDeceleratorTest class
    }

    @Test
    public void testUnDeclareProperties_whenComponentHasNoPolicies_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    public void testUnDeclareProperties_whenNoPropertiesFromPolicyMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(createResourceWithPolicy(), input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(policyOperation);
    }

    @Test
    public void whenFailingToUpdateDeclaredProperties_returnErrorStatus() {
        Resource resource = createResourceWithPolicies(POLICY_ID);
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        policyDefinition.setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void testUnDeclareProperties_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithPolicies(POLICY_ID, "policyId2");
        PolicyDefinition policyDefinition = resource.getPolicies().get(POLICY_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        policyDefinition.setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(policyOperation.updatePolicyProperties(eq(resource), eq(POLICY_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = policyPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);

        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertThat(updatedProperties).hasSize(1);
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertThat(updatedProperty.isGetInputProperty()).isFalse();
        assertThat(updatedProperty.getValue()).isEmpty();
        assertThat(updatedProperty.getDefaultValue()).isEqualTo(getInputPropForInput.getDefaultValue());
        assertThat(updatedProperty.getUniqueId()).isEqualTo(getInputPropForInput.getUniqueId());
    }

    private Resource createResourceWithPolicy() {
        return createResourceWithPolicies(POLICY_ID);
    }

    private Resource createResourceWithPolicies(String ... policies) {
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
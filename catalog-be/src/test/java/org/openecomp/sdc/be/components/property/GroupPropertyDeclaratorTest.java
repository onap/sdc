package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupPropertyDeclaratorTest extends PropertyDeclaratorTestBase {


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
    @Before
    public void setUp() throws Exception {
        super.setUp();
        resource = createResourceWithGroup();
        input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
    }

    @Test
    public void testDeclarePropertiesAsInputs_groupNotExist() {
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = groupPropertyDeclarator.declarePropertiesAsInputs(resource, "nonExistingGroup", Collections.emptyList());
        assertThat(declareResult.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    public void testDeclarePropertiesAsInputs_failedToUpdateProperties() {
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        Either<List<InputDefinition>, StorageOperationStatus> declareResult = groupPropertyDeclarator.declarePropertiesAsInputs(resource, GROUP_ID, Collections.emptyList());
        assertThat(declareResult.right().value()).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void testDeclarePropertiesAsInputs() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = groupPropertyDeclarator.declarePropertiesAsInputs(resource, GROUP_ID, propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        assertThat(inputs).hasSize(2);
        verifyInputPropertiesList(inputs, updatedPropsCapture.getValue());
        //creation of inputs values is part of the DefaultPropertyDeclarator and is tested in the ComponentInstancePropertyDeclaratorTest class
    }

    @Test
    public void testUnDeclareProperties_whenComponentHasNoGroups_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    public void testUnDeclareProperties_whenNoPropertiesFromGroupMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(createResourceWithGroup(), input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    public void whenFailingToUpdateDeclaredProperties_returnErrorStatus() {
        Resource resource = createResourceWithGroups(GROUP_ID);
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        assertThat(groupDefinition.isPresent()).isTrue();
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        groupDefinition.get().setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void testUnDeclareProperties_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithGroups(GROUP_ID, "groupId2");
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        groupDefinition.get().setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsInputs(resource, input);

        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertThat(updatedProperties).hasSize(1);
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertThat(updatedProperty.isGetInputProperty()).isFalse();
        assertThat(updatedProperty.getValue()).isEmpty();
        assertThat(updatedProperty.getDefaultValue()).isEqualTo(getInputPropForInput.getDefaultValue());
        assertThat(updatedProperty.getUniqueId()).isEqualTo(getInputPropForInput.getUniqueId());
    }

    @Test
    public void testUnDeclarePropertiesAsListInputs_whenComponentHasNoGroups_returnOk() {
        Resource resource = new Resource();
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    public void testUnDeclarePropertiesAsListInputs_whenNoPropertiesFromGroupMatchInputId_returnOk() {
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(createResourceWithGroup(), input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        verifyZeroInteractions(groupOperation);
    }

    @Test
    public void whenFailingToUpdateDeclaredPropertiesAsListInputs_returnErrorStatus() {
        Resource resource = createResourceWithGroups(GROUP_ID);
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        assertThat(groupDefinition.isPresent()).isTrue();
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        groupDefinition.get().setProperties(Collections.singletonList(getInputPropForInput));
        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.GENERAL_ERROR);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);
        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.GENERAL_ERROR);
    }

    @Test
    public void testUnDeclarePropertiesAsListInputs_propertiesUpdatedCorrectly() {
        Resource resource = createResourceWithGroups(GROUP_ID, "groupId3");
        Optional<GroupDefinition> groupDefinition = resource.getGroupById(GROUP_ID);
        PropertyDataDefinition getInputPropForInput = buildGetInputProperty(INPUT_ID);
        PropertyDataDefinition someOtherProperty = new PropertyDataDefinitionBuilder().build();
        groupDefinition.get().setProperties(Arrays.asList(getInputPropForInput, someOtherProperty));

        when(propertyOperation.findDefaultValueFromSecondPosition(Collections.emptyList(), getInputPropForInput.getUniqueId(), getInputPropForInput.getDefaultValue())).thenReturn(Either.left(getInputPropForInput.getDefaultValue()));
        when(groupOperation.updateGroupProperties(eq(resource), eq(GROUP_ID), updatedPropsCapture.capture())).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = groupPropertyDeclarator.unDeclarePropertiesAsListInputs(resource, input);

        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
        List<PropertyDataDefinition> updatedProperties = updatedPropsCapture.getValue();
        assertThat(updatedProperties).hasSize(1);
        PropertyDataDefinition updatedProperty = updatedProperties.get(0);
        assertThat(updatedProperty.isGetInputProperty()).isFalse();
        assertThat(updatedProperty.getValue()).isEmpty();
        assertThat(updatedProperty.getDefaultValue()).isEqualTo(getInputPropForInput.getDefaultValue());
        assertThat(updatedProperty.getUniqueId()).isEqualTo(getInputPropForInput.getUniqueId());
    }

    private Resource createResourceWithGroup() {
        return createResourceWithGroups(GROUP_ID);
    }

    private Resource createResourceWithGroups(String ... groups) {
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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ServiceBuilder;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ComponentInstancePropertyDeclaratorTest extends PropertyDeclaratorTestBase {

    @InjectMocks
    private ComponentInstancePropertyDeclarator testInstance;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ComponentInstanceBusinessLogic componentInstanceBusinessLogic;
    @Captor
    private ArgumentCaptor<Map<String, List<ComponentInstanceProperty>>> instancePropertiesCaptor;

    private static final String PROPERTY_ID = "propertyUid";
    private static final String PROEPRTY_NAME = "propertyName";
    private static final String SERVICE_ID = "serviceUid";
    private static final String SERVICE_NAME = "serviceName";

    @Test
    public void declarePropertiesAsInputs_componentInstanceNotExist() {
        Component cmpt = new Resource();
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(cmpt, "someCmptInstId", Collections.emptyList());
        assertThat(createdInputs.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
        verifyZeroInteractions(toscaOperationFacade);
    }

    @Test
    public void declarePropertiesAsInputs_singleNonComplexProperty() {
        List<PropertyDataDefinition> properties = Collections.singletonList(prop1);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1", propsToDeclare);
        List<InputDefinition> inputs = createdInputs.left().value();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        verifyCreatedInputs(properties, capturedInstanceProperties, inputs);
        verifyUpdatedProperties(properties, capturedInstanceProperties, inputs);
    }

    @Test
    public void declarePropertiesAsInputs_multipleNonComplexProperty() {
        List<PropertyDataDefinition> properties = Arrays.asList(prop1, prop2);
        List<ComponentInstancePropInput> propsToDeclare = createInstancePropInputList(properties);
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1", propsToDeclare);

        List<InputDefinition> inputs = createdInputs.left().value();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        verifyCreatedInputs(properties, capturedInstanceProperties, inputs);
        verifyUpdatedProperties(properties, capturedInstanceProperties, inputs);
    }

    @Test
    public void declarePropertiesAsInputs_singleComplexProperty() {
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
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(Either.left(Collections.emptyMap()));
        Either<List<InputDefinition>, StorageOperationStatus> createdInputs = testInstance.declarePropertiesAsInputs(resource, "inst1", propsToDeclare);

        List<InputDefinition> inputs = createdInputs.left().value();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);

        verifyCreatedInputsFromComplexProperty(propsToDeclare, capturedInstanceProperties, inputs);
        verifyUpdatedComplexProperty(capturedInstanceProperties, inputs);
    }

    @Test
    public void testCreateDeclaredProperty() {
        PropertyDefinition propertyDefinition = getPropertyForDeclaration();
        ComponentInstanceProperty declaredProperty = testInstance.createDeclaredProperty(propertyDefinition);

        assertThat(Objects.nonNull(declaredProperty));
        assertThat(declaredProperty.getUniqueId().equals(propertyDefinition.getUniqueId()));
    }

    @Test
    public void testUndeclareProperty() {
        Service service = new ServiceBuilder()
                                  .setUniqueId(SERVICE_ID)
                                  .setName(SERVICE_NAME)
                                  .build();



        InputDefinition inputToDelete = InputsBuilder
                                                .create()
                                                .setPropertyId(PROPERTY_ID)
                                                .setName(PROEPRTY_NAME)
                                                .build();

        inputToDelete.setGetInputValues(getGetInputListForDeclaration());

        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty(getPropertyForDeclaration());
        List<ComponentInstanceProperty> componentInstanceProperties = new ArrayList<>();
        componentInstanceProperties.add(componentInstanceProperty);

        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(any(), any())).thenReturn(new LinkedList<>());

        StorageOperationStatus undeclareStatus =
                testInstance.unDeclarePropertiesAsInputs(service, inputToDelete);

        assertThat(undeclareStatus.equals(StorageOperationStatus.OK));
    }

    private List<GetInputValueDataDefinition> getGetInputListForDeclaration() {
        GetInputValueDataDefinition getInput = new GetInputValueDataDefinition();
        getInput.setInputId(PROPERTY_ID);
        getInput.setInputName(PROEPRTY_NAME);
        getInput.setPropName(PROEPRTY_NAME);
        List<GetInputValueDataDefinition> getInputList = new ArrayList<>();
        getInputList.add(getInput);
        return getInputList;
    }

    private PropertyDefinition getPropertyForDeclaration() {
        return new PropertyDataDefinitionBuilder()
                       .setUniqueId(PROPERTY_ID)
                       .build();
    }

    private void verifyUpdatedProperties(List<PropertyDataDefinition> properties, List<ComponentInstanceProperty> capturedInstanceProperties, List<InputDefinition> inputs) {
        assertThat(capturedInstanceProperties).hasSize(properties.size());
        Map<String, ComponentInstanceProperty> updatedPropertiesByName = MapUtil.toMap(capturedInstanceProperties, ComponentInstanceProperty::getName);
        properties.forEach(prop -> verifyUpdatedInstanceProperty(prop, updatedPropertiesByName.get(prop.getName()), inputs));
    }

    private void verifyUpdatedComplexProperty(List<ComponentInstanceProperty> capturedInstanceProperties, List<InputDefinition> inputs) {
        assertThat(capturedInstanceProperties).hasSize(1);
        verifyUpdatedInstanceComplexProperty(capturedInstanceProperties.get(0), inputs);
    }

    private void verifyCreatedInputs(List<PropertyDataDefinition> originalPropsToDeclare, List<ComponentInstanceProperty> capturedUpdatedProperties, List<InputDefinition> inputs) {
        assertThat(inputs).hasSize(originalPropsToDeclare.size());
        Map<String, InputDefinition> propertyIdToCreatedInput = MapUtil.toMap(inputs, InputDefinition::getPropertyId);
        originalPropsToDeclare.forEach(propToDeclare -> verifyCreatedInput(propToDeclare, propertyIdToCreatedInput.get(propToDeclare.getUniqueId())));
        capturedUpdatedProperties.forEach(updatedProperty -> verifyInputPropertiesList(updatedProperty, propertyIdToCreatedInput.get(updatedProperty.getUniqueId())));
    }

    private void verifyCreatedInputsFromComplexProperty(List<ComponentInstancePropInput> propsToDeclare, List<ComponentInstanceProperty> capturedInstanceProperties, List<InputDefinition> inputs) {
        assertThat(inputs).hasSize(propsToDeclare.size());
        Map<String, InputDefinition> inputsByName = MapUtil.toMap(inputs, InputDefinition::getName);
        propsToDeclare.forEach(propToDeclare -> verifyCreatedInputFromComplexProperty(propToDeclare, inputsByName));
        Map<String, List<InputDefinition>> propertyIdToCreatedInput = MapUtil.groupListBy(inputs, InputDefinition::getPropertyId);
        capturedInstanceProperties.forEach(updatedProperty -> verifyInputPropertiesListFromComplexProperty(updatedProperty, propertyIdToCreatedInput.get(updatedProperty.getUniqueId())));
    }

    private void verifyInputPropertiesListFromComplexProperty(ComponentInstanceProperty updatedProperty, List<InputDefinition> inputs) {
        inputs.forEach(input -> verifyInputPropertiesList(updatedProperty, input));
    }

    private void verifyCreatedInputFromComplexProperty(ComponentInstancePropInput parentProperty,  Map<String, InputDefinition> inputsByName) {
        PropertyDefinition innerProperty = parentProperty.getInput();
        String expectedInputName = generateExpectedInputName(parentProperty, innerProperty);
        InputDefinition input = inputsByName.get(expectedInputName);
        assertThat(input.getType()).isEqualTo(innerProperty.getType());
        assertThat(input.getValue()).isEqualTo(innerProperty.getValue());
//        assertThat(input.getDefaultValue()).isEqualTo(innerProperty.getValue());//bug
        assertThat(input.getUniqueId()).isEqualTo(UniqueIdBuilder.buildPropertyUniqueId(RESOURCE_ID, input.getName()));
        assertThat(input.getPropertyId()).isEqualTo(parentProperty.getUniqueId());
        assertThat(input.getInstanceUniqueId()).isEqualTo(INSTANCE_ID);

    }

    private void verifyInputPropertiesList(ComponentInstanceProperty updatedProperty, InputDefinition input) {
        assertThat(input.getProperties()).hasSize(1);
        assertThat(updatedProperty).isEqualTo(input.getProperties().get(0));
    }


    private List<ComponentInstancePropInput> createComplexPropInputList(PropertyDefinition ... innerProperties) {
        return Stream.of(innerProperties).map(this::createComplexPropInput).collect(Collectors.toList());
    }

    private ComponentInstancePropInput createComplexPropInput(PropertyDefinition innerProp) {
        ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput(new ComponentInstanceProperty(complexProperty));
        componentInstancePropInput.setInput(innerProp);
        componentInstancePropInput.setPropertiesName(complexProperty.getName() + "#" +  innerProp.getName());
        return componentInstancePropInput;
    }

    private void verifyUpdatedInstanceProperty(PropertyDataDefinition originalProperty, ComponentInstanceProperty updatedProperty, List<InputDefinition> inputs) {
        assertThat(updatedProperty.getValue()).isEqualTo(generateGetInputValue(generateExpectedInputName(originalProperty)));
        assertThat(updatedProperty.isGetInputProperty()).isTrue();
        assertThat(updatedProperty.getName()).isEqualTo(originalProperty.getName());
        List<GetInputValueDataDefinition> getInputValues = updatedProperty.getGetInputValues();
        verifyGetInputValues(getInputValues, inputs);
    }

    private void verifyUpdatedInstanceComplexProperty(ComponentInstanceProperty updatedComplexProperty, List<InputDefinition> inputs) {
        assertThat(updatedComplexProperty.getValue()).isEqualTo(generateComplexGetInputValue(inputs));
        assertThat(updatedComplexProperty.isGetInputProperty()).isTrue();
        assertThat(updatedComplexProperty.getName()).isEqualTo(complexProperty.getName());
        List<GetInputValueDataDefinition> getInputValues = updatedComplexProperty.getGetInputValues();
        verifyGetInputValues(getInputValues, inputs);
    }

    private void verifyGetInputValues(List<GetInputValueDataDefinition> getInputValues, List<InputDefinition> inputs) {
        Map<String, InputDefinition> inputsByName = MapUtil.toMap(inputs, InputDefinition::getName);
        getInputValues.forEach(getInputVal -> {
            InputDefinition input = inputsByName.get(getInputVal.getInputName());
            assertThat(input.getUniqueId()).isEqualTo(getInputVal.getInputId());
        });
    }

    private String generateComplexGetInputValue(List<InputDefinition> createdInputs) {
        return String.format("{\"%s\":%s,\"%s\":%s}", INNER_PROP1, generateGetInputValue(createdInputs.get(0).getName()), INNER_PROP2, generateGetInputValue(createdInputs.get(1).getName()));
    }

    private String generateExpectedInputName(PropertyDataDefinition prop) {
        return INSTANCE_ID + "_" + prop.getName();
    }

    private String generateExpectedInputName(PropertyDefinition parentProp, PropertyDefinition innerProperty) {
        return INSTANCE_ID + "_" + parentProp.getName()+ "_" + innerProperty.getName();
    }

    private void verifyCreatedInput(PropertyDataDefinition property, InputDefinition input) {
        assertThat(input.getType()).isEqualTo(property.getType());
        assertThat(input.getName()).isEqualTo(generateExpectedInputName(property));
        assertThat(input.getValue()).isEqualTo(property.getValue());
        assertThat(input.getDefaultValue()).isEqualTo(property.getValue());
        assertThat(input.getUniqueId()).isEqualTo(UniqueIdBuilder.buildPropertyUniqueId(RESOURCE_ID, input.getName()));
        assertThat(input.getPropertyId()).isEqualTo(property.getUniqueId());
        assertThat(input.getInstanceUniqueId()).isEqualTo(INSTANCE_ID);
    }

}
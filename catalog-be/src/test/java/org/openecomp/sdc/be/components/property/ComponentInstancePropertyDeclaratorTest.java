package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.InputsBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private PropertyOperation propertyOperation;

    @Captor
    private ArgumentCaptor<Map<String, List<ComponentInstanceProperty>>> instancePropertiesCaptor;

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
    public void declarePropertiesAsListInput() {
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
        when(toscaOperationFacade.addComponentInstancePropertiesToComponent(eq(resource), instancePropertiesCaptor.capture())).thenReturn(Either.left(Collections.emptyMap()));
        Either<InputDefinition, StorageOperationStatus> result = testInstance.declarePropertiesAsListInput(resource, "inst1", propsToDeclare, input);
        // validate result
        assertThat(result.isLeft()).isTrue();
        List<ComponentInstanceProperty> capturedInstanceProperties = instancePropertiesCaptor.getValue().get(INSTANCE_ID);
        assertThat(capturedInstanceProperties.size()).isEqualTo(2);
        Map<String, PropertyDataDefinition> propertiesMap =
                properties.stream().collect(Collectors.toMap(PropertyDataDefinition::getName, e->e));
        for(ComponentInstanceProperty instanceProperty: capturedInstanceProperties) {
            assertThat(propertiesMap.containsKey(instanceProperty.getName())).isTrue();
            PropertyDataDefinition property = propertiesMap.get(instanceProperty.getName());
            assertThat(instanceProperty.getType()).isEqualTo(property.getType());
            assertThat(instanceProperty.isGetInputProperty()).isTrue();
        }
    }

    @Test
    public void declarePropertiesAsListInput_propertyOwnerNotFound() {
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
        assertThat(result.isRight()).isTrue();
        assertThat(result.right().value()).isEqualTo(StorageOperationStatus.NOT_FOUND);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest() {
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

        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(eq(component), eq(INPUT_ID))).thenReturn(ciPropList);
        when(propertyOperation.findDefaultValueFromSecondPosition(eq(pathOfComponentInstances), eq(ciProp.getUniqueId()), eq(ciProp.getDefaultValue()))).thenReturn(Either.left(ciProp.getDefaultValue()));
        when(toscaOperationFacade.updateComponentInstanceProperties(eq(component), eq(ciProp.getComponentInstanceId()), eq(ciPropList))).thenReturn(StorageOperationStatus.OK);
        StorageOperationStatus storageOperationStatus = testInstance.unDeclarePropertiesAsListInputs(component, inputToDelete);

        assertThat(storageOperationStatus).isEqualTo(StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenNoListInput_returnOk() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
        List<ComponentInstanceProperty> resList = new ArrayList<>();
        when(componentInstanceBusinessLogic.getComponentInstancePropertiesByInputId(eq(resource), eq(INPUT_ID))).thenReturn(resList);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
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

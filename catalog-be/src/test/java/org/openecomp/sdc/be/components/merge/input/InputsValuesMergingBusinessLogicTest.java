package org.openecomp.sdc.be.components.merge.input;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class InputsValuesMergingBusinessLogicTest {

    public static final String INPUT_DEFUALT_TYPE = "string";
    public static final String INPUT1_ID = "input1";
    public static final String INPUT2_ID = "input2";
    public static final String INPUT3_ID = "input3";
    public static final String INPUT4_ID = "input4";
    private InputsValuesMergingBusinessLogic testInstance;

    @Before
    public void setUp() throws Exception {
        testInstance = new InputsValuesMergingBusinessLogic();
    }

    @Test
    public void testMergeInputs_inputsOfDifferentType_dontCopyOldValue() throws Exception {
        InputDefinition oldInput = createUserDefinedInputDefinition(INPUT1_ID, "oldVal1");

        InputDefinition newInput = createInputDefinition(INPUT1_ID, null);
        newInput.setType("int");


        Map<String, InputDefinition> updatedInputs = Collections.singletonMap(newInput.getName(), newInput);
        Map<String, InputDefinition> oldInputs = Collections.singletonMap(oldInput.getName(), oldInput);
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertNull(updatedInputs.get(INPUT1_ID).getDefaultValue());
    }

    @Test
    public void testMergeInputs_newInputsHaveNoValue_copyOldValues() throws Exception {
        InputDefinition oldInputWithCsarDefaultValue = createInputDefinition(INPUT1_ID, "oldVal1");
        InputDefinition oldInputWithUserDefinedValue = createUserDefinedInputDefinition(INPUT2_ID, "oldVal2");
        InputDefinition oldInputNotExistOnNew = createUserDefinedInputDefinition(INPUT3_ID, null);

        InputDefinition newInput1 = createInputDefinition(INPUT1_ID, "");
        InputDefinition newInput2 = createUserDefinedInputDefinition(INPUT2_ID, null);

        Map<String, InputDefinition> updatedInputs = mapInputsByName(Arrays.asList(newInput1, newInput2));
        Map<String, InputDefinition> oldInputs = mapInputsByName(Arrays.asList(oldInputWithCsarDefaultValue, oldInputWithUserDefinedValue, oldInputNotExistOnNew));
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertEquals(oldInputWithCsarDefaultValue.getDefaultValue(), updatedInputs.get(INPUT1_ID).getDefaultValue());
        assertEquals(oldInputWithUserDefinedValue.getDefaultValue(), updatedInputs.get(INPUT2_ID).getDefaultValue());
        assertNull(updatedInputs.get(INPUT3_ID));
    }

    @Test
    public void testMergeInputs_newInputsHaveValue_dontOverrideNewValue() throws Exception {
        InputDefinition oldInputWithCsarDefaultValue = createInputDefinition(INPUT1_ID, "oldVal1");
        InputDefinition oldInputWithUserDefinedValue = createUserDefinedInputDefinition(INPUT2_ID, "oldVal2");
        InputDefinition oldInputWithNoValue = createUserDefinedInputDefinition(INPUT3_ID, null);

        InputDefinition newInput1 = createInputDefinition(INPUT1_ID, "newVal1");
        InputDefinition newInput2 = createUserDefinedInputDefinition(INPUT2_ID, "newVal2");
        InputDefinition newInput3 = createUserDefinedInputDefinition(INPUT3_ID, "newVal3");
        InputDefinition newInput4 = createUserDefinedInputDefinition(INPUT4_ID, "newVal4");

        Map<String, InputDefinition> updatedInputs = mapInputsByName(Arrays.asList(newInput1, newInput2, newInput3, newInput4));
        Map<String, InputDefinition> oldInputs = mapInputsByName(Arrays.asList(oldInputWithCsarDefaultValue, oldInputWithUserDefinedValue, oldInputWithNoValue));
        testInstance.mergeComponentInputs(oldInputs, updatedInputs);

        assertEquals(updatedInputs.get(INPUT1_ID).getDefaultValue(), newInput1.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT2_ID).getDefaultValue(), newInput2.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT3_ID).getDefaultValue(), newInput3.getDefaultValue());
        assertEquals(updatedInputs.get(INPUT4_ID).getDefaultValue(), newInput4.getDefaultValue());
    }

    @Test
    public void getPrevoislyDeclaredInputsToMerge() throws Exception {
        PropertyDataDefinition declaredInputProp1 = new PropertyDataDefinitionBuilder().addGetInputValue(INPUT1_ID).addGetInputValue(INPUT3_ID).setUniqueId("prevDeclaredPropId").build();
        PropertyDataDefinition declaredInputProp2 = new PropertyDataDefinitionBuilder().addGetInputValue(INPUT4_ID).setUniqueId("prevDeclaredPropId2").build();

        Resource prevResource = new ResourceBuilder().addInput(INPUT1_ID).addInput(INPUT2_ID).addInput(INPUT3_ID).addInput(INPUT4_ID).build();

        Resource currentResource = new ResourceBuilder()
                .addInput(INPUT2_ID)
                .addInstanceProperty("inst1", new ComponentInstanceProperty(declaredInputProp1))
                .addInstanceInput("inst2", new ComponentInstanceInput(declaredInputProp2))
                .build();

        List<InputDefinition> previouslyDeclaredInputs = testInstance.getPreviouslyDeclaredInputsToMerge(prevResource, currentResource);
        assertEquals(3, previouslyDeclaredInputs.size());

        assertInput(previouslyDeclaredInputs.get(0), INPUT1_ID, declaredInputProp1.getUniqueId(), "inst1");
        assertInput(previouslyDeclaredInputs.get(1), INPUT3_ID, declaredInputProp1.getUniqueId(), "inst1");
        assertInput(previouslyDeclaredInputs.get(2), INPUT4_ID, declaredInputProp2.getUniqueId(), "inst2");
    }

    private void assertInput(InputDefinition inputDefinition, String expectedInputId, String expectedPropertyId, String expectedInstanceUniqueId) {
        assertEquals(expectedInputId, inputDefinition.getUniqueId());
        assertEquals(expectedPropertyId, inputDefinition.getPropertyId());
        assertEquals(inputDefinition.getInstanceUniqueId(), expectedInstanceUniqueId);
    }

    private Map<String, InputDefinition> mapInputsByName(List<InputDefinition> inputs) {
        return MapUtil.toMap(inputs, InputDefinition::getName);
    }

    private InputDefinition createInputDefinition(String name, String value) {
        InputDefinition inputDef = new InputDefinition();
        inputDef.setName(name);
        inputDef.setDefaultValue(value);
        inputDef.setType(INPUT_DEFUALT_TYPE);
        return inputDef;
    }

    private InputDefinition createUserDefinedInputDefinition(String name, String value) {
        InputDefinition inputDef = createInputDefinition(name, value);
        inputDef.setOwnerId("owner");
        return inputDef;
    }

    private void addInstanceProperty(Resource resource, ComponentInstanceProperty prop, String instanceId) {
        addInstancePropDefinition(resource.getComponentInstancesProperties(), prop, instanceId);
    }

    private void addInstanceInput(Resource resource, ComponentInstanceInput prop, String instanceId) {
        addInstancePropDefinition(resource.getComponentInstancesInputs(), prop, instanceId);
    }

    private <T extends PropertyDataDefinition> void addInstancePropDefinition(Map<String, List<T>> propsDefinitions, T propDef, String instanceId) {
        propsDefinitions.computeIfAbsent(instanceId, id -> new ArrayList<>()).add(propDef);
    }

    private Resource createResourceWithInputs(String ... inputsIds) {
        Resource resource = new Resource();
        List<InputDefinition> inputs = new ArrayList<>();
        for (String inputId : inputsIds) {
            InputDefinition inputDefinition = new InputDefinition();
            inputDefinition.setOwnerId("cs0008");
            inputDefinition.setUniqueId(inputId);
            inputs.add(inputDefinition);
        }
        resource.setInputs(inputs);
        return resource;
    }


    private ComponentInstanceProperty createGetInputComponentProperty(String ... declaredToInputId) {
        ComponentInstanceProperty prevDeclaredProperty = new ComponentInstanceProperty();
        for (String inputId : declaredToInputId) {
            addGetInputValueOnProp(inputId, prevDeclaredProperty);
        }

        return prevDeclaredProperty;
    }

    private void addGetInputValueOnProp(String declaredToInputId, PropertyDataDefinition declaredProperty) {
        GetInputValueDataDefinition getInputDef = new GetInputValueDataDefinition();
        getInputDef.setInputId(declaredToInputId);
        if (declaredProperty.getGetInputValues() == null) {
            declaredProperty.setGetInputValues(new ArrayList<>());
        }
        declaredProperty.getGetInputValues().add(getInputDef);
    }

    private ComponentInstanceInput createGetInputComponentInstanceInput(String declaredToInputId) {
        ComponentInstanceInput prevDeclaredProp = new ComponentInstanceInput();
        addGetInputValueOnProp(declaredToInputId, prevDeclaredProp);
        return prevDeclaredProp;
    }
}
package org.openecomp.sdc.be.components.merge.property;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

public class DataDefinitionsValuesMergingBusinessLogicTest {

    private static final String DEFAULT_PROP_TYPE = "string";

    @InjectMocks
    private DataDefinitionsValuesMergingBusinessLogic testInstance;

    @Mock
    private PropertyDataValueMergeBusinessLogic propertyDataValueMergeBusinessLogicMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergePropDataDefinition_propertiesNotOfSameType_dontMerge() throws Exception {
        PropertyDataDefinition oldProp1 = createPropertyDataDefinition("prop1", "oldVal1");

        PropertyDataDefinition newProp1 = createPropertyDataDefinition("prop1", null);
        newProp1.setType("int");

        testInstance.mergeInstanceDataDefinitions(Collections.singletonList(oldProp1), Collections.emptyList(), Collections.singletonList(newProp1), Collections.emptyList());
        Mockito.verifyZeroInteractions(propertyDataValueMergeBusinessLogicMock);
    }

    @Test
    public void mergePropDataDefinition_propertiesInnerTypesNotSame_dontMerge() throws Exception {
        PropertyDataDefinition oldProp1 = new PropertyDataDefinitionBuilder()
                .setName("prop1")
                .setType("list")
                .setSchemaType("string")
                .setValue("val1").build();

        PropertyDataDefinition newProp1 = new PropertyDataDefinitionBuilder()
                .setName("prop1")
                .setType("list")
                .setSchemaType("int")
                .setValue("val1").build();

        testInstance.mergeInstanceDataDefinitions(Collections.singletonList(oldProp1), Collections.emptyList(), Collections.singletonList(newProp1), Collections.emptyList());
        Mockito.verifyZeroInteractions(propertyDataValueMergeBusinessLogicMock);
    }

    @Test
    public void mergePropDataDefinition_getInputsToMerge() throws Exception {
        PropertyDataDefinition userDeclaredGetInput = createGetInputPropertyDataDefinition("prop1", "input1");
        PropertyDataDefinition nonUserDeclaredGetInput = createGetInputPropertyDataDefinition("prop2", "input2", "input3");

        PropertyDataDefinition newProp1 = createPropertyDataDefinition("prop1", "");
        PropertyDataDefinition newProp2 = createPropertyDataDefinition("prop2", null);

        InputDefinition oldDeclaredByUserInput1 = new InputDefinition();
        oldDeclaredByUserInput1.setName("input1");
        oldDeclaredByUserInput1.setInstanceUniqueId("instanceId");

        InputDefinition oldNotDeclaredByUserInput2 = new InputDefinition();
        oldNotDeclaredByUserInput2.setName("input2");

        InputDefinition oldNotDeclaredByUserInput3 = new InputDefinition();
        oldNotDeclaredByUserInput3.setName("input3");


        InputDefinition newInput3 = new InputDefinition();
        newInput3.setName("input3");


        List<PropertyDataDefinition> oldProps = Arrays.asList(userDeclaredGetInput, nonUserDeclaredGetInput);
        List<PropertyDataDefinition> newProps = Arrays.asList(newProp1, newProp2);

        List<InputDefinition> oldInputs = Arrays.asList(oldDeclaredByUserInput1, oldNotDeclaredByUserInput2, oldNotDeclaredByUserInput3);
        List<InputDefinition> newInputs = Collections.singletonList(newInput3);

        testInstance.mergeInstanceDataDefinitions(oldProps, oldInputs, newProps, newInputs);
        //get input prop was declared by user - ok to merge it although its input not exist (it will be added later)
        verify(propertyDataValueMergeBusinessLogicMock).mergePropertyValue(userDeclaredGetInput, newProp1, Collections.singletonList("input1"));
        //input 2 not exist in new version - dont merge it, input 3 exist in new version - ok to merge it
        verify(propertyDataValueMergeBusinessLogicMock).mergePropertyValue(nonUserDeclaredGetInput, newProp1, Collections.singletonList("input3"));
    }

    @Test
    public void mergePropDataDefinition_dontMergeOldPropsIfNotExistInNewVersion() throws Exception {
        PropertyDataDefinition oldProp = createPropertyDataDefinition("prop1", "oldVal1");
        PropertyDataDefinition newProp = createPropertyDataDefinition("prop2", null);
        testInstance.mergeInstanceDataDefinitions(Collections.singletonList(oldProp), Collections.emptyList(), Collections.singletonList(newProp), Collections.emptyList());
        Mockito.verifyZeroInteractions(propertyDataValueMergeBusinessLogicMock);
    }

    private PropertyDataDefinition createPropertyDataDefinition(String name, String value) {
        return new PropertyDataDefinitionBuilder()
                .setName(name)
                .setType(DEFAULT_PROP_TYPE)
                .setValue(value).build();
    }

    private PropertyDataDefinition createGetInputPropertyDataDefinition(String name, String ... inputsNames) {
        PropertyDataDefinitionBuilder propertyBuilder = new PropertyDataDefinitionBuilder()
                .setName(name)
                .setType(DEFAULT_PROP_TYPE);
        for (String inputName : inputsNames) {
            propertyBuilder.addGetInputValue(inputName);
        }
        return propertyBuilder.build();
    }

}
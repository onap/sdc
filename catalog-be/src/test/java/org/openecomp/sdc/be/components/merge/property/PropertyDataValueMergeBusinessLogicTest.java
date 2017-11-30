package org.openecomp.sdc.be.components.merge.property;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PropertyDataValueMergeBusinessLogicTest {

    @InjectMocks
    private PropertyDataValueMergeBusinessLogic testInstance;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergeProperties_emptyOldAndNewValues() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        testMergeProps(oldProp, newProp, null);
    }

    @Test
    public void mergeProperties_emptyOldValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "newVal");
        testMergeProps(oldProp, newProp, "newVal");
    }

    @Test
    public void mergeSimpleStringType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "val1");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        testMergeProps(oldProp, newProp, "val1");
    }

    @Test
    public void mergeSimpleStringType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "val1");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "newVal");
        testMergeProps(oldProp, newProp, "newVal");
    }

    @Test
    public void mergeSimpleIntType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, "44");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        testMergeProps(oldProp, newProp, "44");
    }

    @Test
    public void mergeSimpleIntType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, "44");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "45");
        testMergeProps(oldProp, newProp, "45");
    }

    @Test
    public void mergeSimpleBooleanType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "false");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, null);
        testMergeProps(oldProp, newProp, "false");
    }

    @Test
    public void mergeSimpleBooleanType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "false");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "true");
        testMergeProps(oldProp, newProp, "true");
    }

    @Test
    public void mergeSimpleListType_copyOldValuesByIndex() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"a\", \"b\", \"c\"]");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"x\", \"\"]");
        testMergeProps(oldProp, newProp, "[\"x\",\"b\",\"c\"]");
    }

    @Test
    public void mergeSimpleListType_jsonList() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "json", "[[\"a\", \"b\"], \"c\"]");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "json", "[[\"a\"], \"\"]");
        testMergeProps(oldProp, newProp, "[[\"a\"],\"c\"]");
    }

    @Test
    public void mergeComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "myType", null, "{\"mac_range_plan\":\"y\", \"mac_count_required\":{\"is_required\":true,\"count\":44}}");
        PropertyDataDefinition newProp = createProp("prop1", "myType", null, "{\"mac_count_required\":{\"is_required\":false, \"mac_address\":\"myAddress\"}}");
        testMergeProps(oldProp, newProp, "{\"mac_range_plan\":\"y\",\"mac_count_required\":{\"is_required\":false,\"mac_address\":\"myAddress\",\"count\":44}}");
    }

    @Test
    public void mergeListOfComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "list", "myType", "[{\"prop1\":\"val1\", \"prop2\":{\"prop3\":true,\"prop4\":44}}, " +
                                                                                       "{\"prop1\":\"val2\", \"prop2\":{\"prop3\":true}}]");
        PropertyDataDefinition newProp = createProp("prop1", "list", "myType", "[{\"prop2\":{\"prop3\":false}}]");

        Map<String, DataTypeDefinition> dataTypes = buildDataTypes();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        String expectedValue = "[{\"prop2\":{\"prop4\":44,\"prop3\":false},\"prop1\":\"\\\"val1\\\"\"}," +
                                "{\"prop2\":{\"prop3\":true},\"prop1\":\"\\\"val2\\\"\"}]";

        assertEquals(expectedValue, newProp.getValue());
    }

    @Test
    public void mergeMapType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "map", "string", "{\"prop1\":\"val1\", \"prop2\":\"val2\", \"prop3\":\"val3\"}");
        PropertyDataDefinition newProp = createProp("prop1", "map", "string", "{\"prop1\":\"newVal1\", \"prop2\":\"\"}");
        testMergeProps(oldProp, newProp, "{\"prop2\":\"val2\",\"prop1\":\"newVal1\",\"prop3\":\"val3\"}");
    }

    @Test
    public void mergeGetInputValue() throws Exception {
        PropertyDataDefinition oldProp = createGetInputProp("prop1", "string", null, "input1");
        PropertyDataDefinition newProp = createProp("prop1", "string", null, null);
        testMergeProps(oldProp, newProp, oldProp.getValue(), Collections.singletonList("input1"));
        assertGetInputValues(newProp, "input1");
    }

    @Test
    public void mergeGetInputValue_inputNotForMerging() throws Exception {
        PropertyDataDefinition oldProp = createGetInputProp("prop1", "string", null, "input1");
        PropertyDataDefinition newProp = createProp("prop1", "string", null, null);
        testMergeProps(oldProp, newProp,null, Collections.singletonList("input2"));
        assertTrue(newProp.getGetInputValues().isEmpty());
    }

    @Test
    public void mergeComplexGetInputValue_moreThanOneGetInput_copyOnlyInputsForMerging() throws Exception {
        PropertyDataDefinition oldProp = new PropertyDataDefinitionBuilder().addGetInputValue("input1").addGetInputValue("input2").setName("prop1").setType("myType").setValue("{\"mac_range_plan\":{\"get_input\": \"input1\"}, \"mac_count_required\":{\"is_required\":true,\"count\":{\"get_input\": \"input2\"}}}").build();
        PropertyDataDefinition newProp = createProp("prop1", "myType", null, "{\"mac_count_required\":{\"is_required\":true}}");
        testMergeProps(oldProp, newProp,"{\"mac_range_plan\":{},\"mac_count_required\":{\"is_required\":true,\"count\":{\"get_input\":\"input2\"}}}", Collections.singletonList("input2"));
        assertGetInputValues(newProp, "input2");
    }

    @Test
    public void mergeListValueWithMultipleGetInputs() throws Exception {
        PropertyDataDefinition oldProp = new PropertyDataDefinitionBuilder()
                .addGetInputValue("input1").addGetInputValue("input2").addGetInputValue("input3")
                .setName("prop1")
                .setType("list").setSchemaType("string")
                .setValue("[{\"get_input\": \"input2\"},{\"get_input\": \"input3\"},{\"get_input\": \"input1\"}]")
                .build();

        PropertyDataDefinition newProp = new PropertyDataDefinitionBuilder()
                .addGetInputValue("input3")
                .setName("prop1")
                .setType("list").setSchemaType("string")
                .setValue("[\"\", {\"get_input\": \"input3\"}]")
                .build();

        testMergeProps(oldProp, newProp,"[{},{\"get_input\":\"input3\"},{\"get_input\":\"input1\"}]", Arrays.asList("input3", "input1"));
        assertGetInputValues(newProp, "input3", "input1");
    }

    private void assertGetInputValues(PropertyDataDefinition newProp, String ... expectedInputNames) {
        assertTrue(newProp.isGetInputProperty());
        assertEquals(newProp.getGetInputValues().size(), expectedInputNames.length);
        for (int i = 0; i < expectedInputNames.length; i++) {
            String expectedInputName = expectedInputNames[i];
            GetInputValueDataDefinition getInputValueDataDefinition = newProp.getGetInputValues().get(i);
            assertEquals(getInputValueDataDefinition.getInputName(), expectedInputName);
        }
    }

    private void testMergeProps(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, String expectedValue) {
        testMergeProps(oldProp, newProp, expectedValue, Collections.emptyList());
    }

    private void testMergeProps(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, String expectedValue, List<String> getInputsToMerge) {
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(Collections.emptyMap()));
        testInstance.mergePropertyValue(oldProp, newProp, getInputsToMerge);
        assertEquals(expectedValue, newProp.getValue());
    }

    private PropertyDataDefinition createProp(String name, String type, String innerType, String val) {
        return new PropertyDataDefinitionBuilder()
                .setType(type)
                .setSchemaType(innerType)
                .setValue(val)
                .setName(name)
                .build();
    }

    private PropertyDataDefinition createGetInputProp(String name, String type, String innerType, String inputName) {
        String val = String.format("{\"get_input\":\"%s\"}", inputName);
        return new PropertyDataDefinitionBuilder()
                .setType(type)
                .setSchemaType(innerType)
                .setValue(val)
                .addGetInputValue(inputName)
                .setName(name)
                .build();

    }

    private Map<String, DataTypeDefinition> buildDataTypes() {
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");
        DataTypeDefinition myInnerType = new DataTypeDefinition();
        myInnerType.setName("myInnerType");

        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setName("prop1");

        PropertyDefinition prop2 = new PropertyDefinition();
        prop2.setName("prop2");
        prop2.setType("myInnerType");

        PropertyDefinition prop3 = new PropertyDefinition();
        prop3.setName("prop3");

        PropertyDefinition prop4 = new PropertyDefinition();
        prop4.setName("prop4");

        myType.setProperties(Arrays.asList(prop1, prop2));
        myInnerType.setProperties(Arrays.asList(prop3, prop4));

        return Stream.of(myType, myInnerType).collect(Collectors.toMap(DataTypeDefinition::getName, Function.identity()));
    }


}
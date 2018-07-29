package org.openecomp.sdc.be.components.merge.input;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;


public class DeclaredInputsResolverTest {

    private DeclaredInputsResolver testInstance;
    private Resource prevResource, currResource;

    @Before
    public void setUp() throws Exception {
        testInstance = new DeclaredInputsResolver();
        prevResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input2")
                .addInput("input3")
                .addInput("input4")
                .build();

        currResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input3")
                .build();
    }

    @Test
    public void whenPropertiesMapIsEmpty_returnEmptyList() {
        List<InputDefinition> previouslyDeclaredInputsToMerge = testInstance.getPreviouslyDeclaredInputsToMerge(prevResource, currResource, emptyMap());
        assertThat(previouslyDeclaredInputsToMerge).isEmpty();
    }

    @Test
    public void whenPrevResourceHasNoInputs_returnEmptyList() {
        List<InputDefinition> previouslyDeclaredInputsToMerge = testInstance.getPreviouslyDeclaredInputsToMerge(new Resource(), currResource, emptyMap());
        assertThat(previouslyDeclaredInputsToMerge).isEmpty();
    }

    @Test
    public void whenAllPropertiesNotReferencingInput_returnEmptyList() {
        PropertyDataDefinition prop1 = createProperty("prop1");
        PropertyDataDefinition prop2 = createProperty("prop2");
        Map<String, List<PropertyDataDefinition>> props = ImmutableMap.of("inst1", singletonList(prop1), "inst2", singletonList(prop2));
        List<InputDefinition> previouslyDeclaredInputsToMerge = testInstance.getPreviouslyDeclaredInputsToMerge(prevResource, currResource, props);
        assertThat(previouslyDeclaredInputsToMerge).isEmpty();
    }

    @Test
    public void doNotReturnReferencedInputIfAlreadyExistInNewResource() {
        PropertyDataDefinition prop1 = createPropertyReferencingInput("prop1", "input1");
        PropertyDataDefinition prop2 = createPropertyReferencingInput("prop2", "input3");
        Map<String, List<PropertyDataDefinition>> props = ImmutableMap.of("inst1", singletonList(prop1), "inst2", singletonList(prop2));
        List<InputDefinition> previouslyDeclaredInputsToMerge = testInstance.getPreviouslyDeclaredInputsToMerge(prevResource, currResource, props);
        assertThat(previouslyDeclaredInputsToMerge).isEmpty();
    }

    @Test
    public void returnAllInputsReferencedByPropertyAndNotExistInNewResource() {
        PropertyDataDefinition prop1 = createPropertyReferencingInput("prop1", "input1");
        PropertyDataDefinition prop2 = createPropertyReferencingInput("prop2", "input2");
        PropertyDataDefinition prop3 = createPropertyReferencingInput("prop3", "input3");
        PropertyDataDefinition prop4 = createPropertyReferencingInput("prop4", "input4");
        PropertyDataDefinition prop5 = createProperty("prop5");
        Map<String, List<PropertyDataDefinition>> props = ImmutableMap.of("inst1", asList(prop1, prop3), "inst2", singletonList(prop2), "group1", asList(prop4, prop5));
        List<InputDefinition> previouslyDeclaredInputsToMerge = testInstance.getPreviouslyDeclaredInputsToMerge(prevResource, currResource, props);
        assertThat(previouslyDeclaredInputsToMerge)
                .extracting("name")
                .containsExactlyInAnyOrder("input2", "input4");
    }

    private PropertyDataDefinition createPropertyReferencingInput(String propName, String referencingInputName) {
        return new PropertyDataDefinitionBuilder()
                .setName(propName)
                .addGetInputValue(referencingInputName)
                .build();
    }

    private PropertyDataDefinition createProperty(String propName) {
        return new PropertyDataDefinitionBuilder()
                .setName(propName)
                .build();
    }



}
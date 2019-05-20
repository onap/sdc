package org.openecomp.sdc.be.components.merge.input;

import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseComponentInputsMerge {

    @Mock
    protected ToscaOperationFacade toscaOperationFacade;

    @Mock
    InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogic;

    @Mock
    DeclaredInputsResolver declaredInputsResolver;

    @Captor
    ArgumentCaptor<Map<String, List<PropertyDataDefinition>>> getInputPropertiesCaptor;

    Resource prevResource, currResource;
    protected static final String RESOURCE_ID = "newResourceId";

    public void setUp() throws Exception {
        prevResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input2")
                .build();

        currResource = new ResourceBuilder()
                .addInstanceProperty("inst1", "prop1")
                .addInstanceProperty("inst1", "prop2")
                .addInstanceInput("inst2", "prop3")
                .addGroupProperty("group1", "prop1")
                .addPolicyProperty("policy1", "prop2")
                .addInput("input1")
                .setUniqueId(RESOURCE_ID)
                .build();
    }

    void verifyCallToMergeComponentInputs(Resource oldResource, List<InputDefinition> inputsToMerge) {
        Map<String, InputDefinition> oldInputsByName = oldResource.getInputs().stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        Map<String, InputDefinition> inputsToMergeByName = inputsToMerge.stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        verify(inputsValuesMergingBusinessLogic).mergeComponentInputs(oldInputsByName, inputsToMergeByName);
    }
}

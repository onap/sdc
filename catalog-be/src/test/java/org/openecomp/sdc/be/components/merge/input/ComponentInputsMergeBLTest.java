package org.openecomp.sdc.be.components.merge.input;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;

import fj.data.Either;

public class ComponentInputsMergeBLTest {

    @InjectMocks
    private ComponentInputsMergeBL testInstance;

    @Mock
    private InputsValuesMergingBusinessLogic inputsValuesMergingBusinessLogicMock;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergeComponentInputs() {
        Resource oldResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input2")
                .build();

        Resource newResource = new Resource();

        List<InputDefinition> inputsToMerge = ObjectGenerator.buildInputs("input1", "input2", "input3");

        when(toscaOperationFacade.updateInputsToComponent(inputsToMerge, newResource.getUniqueId())).thenReturn(Either.left(inputsToMerge));
        ActionStatus actionStatus = testInstance.mergeComponentInputs(oldResource, newResource, inputsToMerge);
        assertEquals(ActionStatus.OK, actionStatus);
        verifyCallToMergeComponentInputs(oldResource, inputsToMerge);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mergeAndRedeclareComponentInputs() throws Exception {
        Resource oldResource = new ResourceBuilder()
                .addInput("input1")
                .addInput("input2")
                .build();

        Resource newResource = ObjectGenerator.buildBasicResource();
        List<InputDefinition> inputsToMerge = ObjectGenerator.buildInputs("input1", "input2", "input3");
        List<InputDefinition> inputsToRedeclare = ObjectGenerator.buildInputs("input4");
        List<InputDefinition> expectedInputsToUpdate = ListUtils.union(inputsToMerge, inputsToRedeclare);
        when(inputsValuesMergingBusinessLogicMock.getPreviouslyDeclaredInputsToMerge(oldResource, newResource)).thenReturn(inputsToRedeclare);
        when(toscaOperationFacade.updateInputsToComponent(expectedInputsToUpdate, newResource.getUniqueId())).thenReturn(Either.left(inputsToMerge));
        ActionStatus actionStatus = testInstance.mergeAndRedeclareComponentInputs(oldResource, newResource, inputsToMerge);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void redeclareResourceInputsForInstance() throws Exception {
        List<InputDefinition> oldInputs = ObjectGenerator.buildInputs("input1", "input2");
        Resource newResource = ObjectGenerator.buildBasicResource();
        List<InputDefinition> inputsToRedeclare = ObjectGenerator.buildInputs("input1");
        when(inputsValuesMergingBusinessLogicMock.getPreviouslyDeclaredInputsToMerge(oldInputs, newResource, "inst1")).thenReturn(inputsToRedeclare);
        when(toscaOperationFacade.updateInputsToComponent(inputsToRedeclare, newResource.getUniqueId())).thenReturn(Either.left(inputsToRedeclare));
        ActionStatus actionStatus = testInstance.redeclareComponentInputsForInstance(oldInputs, newResource, "inst1");
    }

    private void verifyCallToMergeComponentInputs(Resource oldResource, List<InputDefinition> inputsToMerge) {
        Map<String, InputDefinition> oldInputsByName = oldResource.getInputs().stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        Map<String, InputDefinition> inputsToMergeByName = inputsToMerge.stream().collect(Collectors.toMap(InputDefinition::getName, Function.identity()));
        verify(inputsValuesMergingBusinessLogicMock).mergeComponentInputs(oldInputsByName, inputsToMergeByName);
    }

}
package org.openecomp.sdc.be.components.merge;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.utils.ObjectGenerator.buildResourceWithInputs;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;

import fj.data.Either;

public class GlobalTypesMergeBusinessLogicTest {

    @InjectMocks
    private GlobalTypesMergeBusinessLogic testInstance;

    @Mock
    private ComponentInputsMergeBL resourceInputsMergeBLMock;

    @Mock
    private GlobalInputsFilteringBusinessLogic globalInputsFilteringBusinessLogic;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergeInstancePropsAndInputs_mergeOnlyNewResourceGenericGlobalTypes() {
        Resource oldResource = buildResourceWithInputs("input1", "input2");
        Resource newResource = buildResourceWithInputs("input1", "input2", "global1", "global2");
        List<InputDefinition> globalInputs = Arrays.asList(newResource.getInputs().get(2), newResource.getInputs().get(3));
        when(globalInputsFilteringBusinessLogic.filterGlobalInputs(newResource)).thenReturn(Either.left(globalInputs));
        when(resourceInputsMergeBLMock.mergeComponentInputs(oldResource, newResource, globalInputs)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.OK, actionStatus);

    }

    @Test
    public void mergeInstancePropsAndInputs_failedToFilterGlobalInputs() throws Exception {
        Resource oldResource = buildResourceWithInputs("input1", "input2");
        Resource newResource = buildResourceWithInputs("input1", "input2", "global1", "global2");
        when(globalInputsFilteringBusinessLogic.filterGlobalInputs(newResource)).thenReturn(Either.right(ActionStatus.GENERAL_ERROR));
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(actionStatus, ActionStatus.GENERAL_ERROR);
        verifyZeroInteractions(resourceInputsMergeBLMock);
    }


}
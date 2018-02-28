package org.openecomp.sdc.be.components.merge.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.merge.instance.ComponentsMergeCommand;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceDataMergeBusinessLogicTest {

    @InjectMocks
    private ResourceDataMergeBusinessLogic testInstance;

    @Mock
    private ComponentsMergeCommand commandA;

    @Mock
    private ComponentsMergeCommand commandB;

    @Mock
    private ComponentsMergeCommand commandC;

    @Before
    public void setUp() throws Exception {
        testInstance = new ResourceDataMergeBusinessLogic(Arrays.asList(commandA, commandB, commandC));
    }

    @Test
    public void mergeResources_allMergeClassesAreCalled() {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(commandA.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(commandB.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(commandC.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void mergeResources_mergeCommandFailed_dontCallOtherMergeMethods() {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(commandA.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verify(commandA).description();
        verifyZeroInteractions(commandB, commandC);
    }

}
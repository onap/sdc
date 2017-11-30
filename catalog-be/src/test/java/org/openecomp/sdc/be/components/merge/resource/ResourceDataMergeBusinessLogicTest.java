package org.openecomp.sdc.be.components.merge.resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.merge.input.ComponentInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstanceInputsMergeBL;
import org.openecomp.sdc.be.components.merge.property.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ResourceDataMergeBusinessLogicTest {

    @InjectMocks
    private ResourceDataMergeBusinessLogic testInstance;

    @Mock
    private ComponentInstanceInputsMergeBL instanceInputsValueMergeBLMock;

    @Mock
    private ComponentInstancePropertiesMergeBL instancePropertiesValueMergeBLMock;

    @Mock
    private ComponentInputsMergeBL inputsValueMergeBLMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void mergeResourceInputs_allMergeClassesAreCalled() throws Exception {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(instancePropertiesValueMergeBLMock.mergeComponentInstancesProperties(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(instanceInputsValueMergeBLMock.mergeComponentInstancesInputs(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(inputsValueMergeBLMock.mergeAndRedeclareComponentInputs(oldResource, newResource, Collections.emptyList())).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void mergeResourceInputs_failToMergeProperties_dontCallOtherMergeMethods() throws Exception {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(instancePropertiesValueMergeBLMock.mergeComponentInstancesProperties(oldResource, newResource)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verifyZeroInteractions(instanceInputsValueMergeBLMock, inputsValueMergeBLMock);
    }

    @Test
    public void mergeResourceInputs_failToMergeInstanceInputs_dontCallOtherMergeMethods() throws Exception {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(instancePropertiesValueMergeBLMock.mergeComponentInstancesProperties(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(instanceInputsValueMergeBLMock.mergeComponentInstancesInputs(oldResource, newResource)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verifyZeroInteractions(inputsValueMergeBLMock);
    }

    @Test
    public void mergeResourceInputs_failedToMergeInputs() throws Exception {
        Resource oldResource = ObjectGenerator.buildBasicResource();
        Resource newResource = ObjectGenerator.buildBasicResource();
        when(instancePropertiesValueMergeBLMock.mergeComponentInstancesProperties(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(instanceInputsValueMergeBLMock.mergeComponentInstancesInputs(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(inputsValueMergeBLMock.mergeAndRedeclareComponentInputs(oldResource, newResource, Collections.emptyList())).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }
}
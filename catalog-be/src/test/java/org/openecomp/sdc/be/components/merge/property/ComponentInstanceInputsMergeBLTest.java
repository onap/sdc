package org.openecomp.sdc.be.components.merge.property;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

public class ComponentInstanceInputsMergeBLTest {

    public static final String INSTANCE1 = "instance1";
    public static final String INSTANCE2 = "instance2";
    @InjectMocks
    private ComponentInstanceInputsMergeBL testInstance;

    @Mock
    private DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    private Resource oldResource, newResource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ComponentInstance instance1 = new ComponentInstanceBuilder().setId(INSTANCE1).setName(INSTANCE1).build();
        ComponentInstance instance2 = new ComponentInstanceBuilder().setId(INSTANCE2).setName(INSTANCE2).build();

        oldResource = new ResourceBuilder()
                .addInstanceInput(INSTANCE1, "property1")
                .addInstanceInput(INSTANCE1, "property2")
                .addInstanceInput(INSTANCE2, "property3")
                .addComponentInstance(instance1)
                .addComponentInstance(instance2)
                .addInput("input1")
                .addInput("input2").build();

        newResource = new ResourceBuilder()
                .addInstanceInput(INSTANCE1, "property11")
                .addInstanceInput(INSTANCE1, "property12")
                .addInstanceInput(INSTANCE2, "property13")
                .addComponentInstance(instance1)
                .addComponentInstance(instance2)
                .addInput("input11")
                .addInput("input12").build();
    }

    @Test
    public void mergeInstancesInputs() throws Exception {
        when(toscaOperationFacade.updateComponentInstanceInputsToComponent(newResource.getComponentInstancesInputs(), newResource.getUniqueId())).thenReturn(Either.left(Collections.emptyMap()));
        ActionStatus actionStatus = testInstance.mergeComponentInstancesInputs(oldResource, newResource);
        assertEquals(actionStatus, ActionStatus.OK);
        verifyMergeBLCalled(oldResource, newResource);
    }

    @Test
    public void mergeInstancesInputs_failure() throws Exception {
        when(toscaOperationFacade.updateComponentInstanceInputsToComponent(newResource.getComponentInstancesInputs(), newResource.getUniqueId())).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        verifyNoMoreInteractions(toscaOperationFacade, propertyValuesMergingBusinessLogic);
        ActionStatus actionStatus = testInstance.mergeComponentInstancesInputs(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }

    @Test
    public void mergeInstanceProps() throws Exception {
        List<ComponentInstanceInput> newInstanceInputs = newResource.safeGetComponentInstanceInput(INSTANCE1);
        List<ComponentInstanceInput> oldInstInputs = oldResource.safeGetComponentInstanceInput(INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceInputs(newResource, INSTANCE1, newInstanceInputs))
                .thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceInputs(oldInstInputs, oldResource.getInputs(), newResource, INSTANCE1);
        assertEquals(actionStatus, ActionStatus.OK);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstInputs, oldResource.getInputs(), newInstanceInputs, newResource.getInputs());
    }

    @Test
    public void mergeInstanceProps_failure() throws Exception {
        List<ComponentInstanceInput> newInstanceInputs = newResource.safeGetComponentInstanceInput(INSTANCE1);
        List<ComponentInstanceInput> oldInstInputs = oldResource.safeGetComponentInstanceInput(INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceInputs(newResource, INSTANCE1, newInstanceInputs))
                .thenReturn(StorageOperationStatus.GENERAL_ERROR);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceInputs(oldInstInputs, oldResource.getInputs(), newResource, INSTANCE1);
        assertEquals(actionStatus, ActionStatus.GENERAL_ERROR);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstInputs, oldResource.getInputs(), newInstanceInputs, newResource.getInputs());
    }

    private void verifyMergeBLCalled(Resource oldResource, Resource newResource) {
        List<ComponentInstanceInput> instance1oldInputs = oldResource.getComponentInstancesInputs().get(INSTANCE1);
        List<ComponentInstanceInput> instance1newInputs = newResource.getComponentInstancesInputs().get(INSTANCE1);
        List<ComponentInstanceInput> instance2oldInputs = oldResource.getComponentInstancesInputs().get(INSTANCE2);
        List<ComponentInstanceInput> instance2newInputs = newResource.getComponentInstancesInputs().get(INSTANCE2);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance1oldInputs, oldResource.getInputs(), instance1newInputs, newResource.getInputs());
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance2oldInputs, oldResource.getInputs(), instance2newInputs, newResource.getInputs());
    }

}
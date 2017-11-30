package org.openecomp.sdc.be.components.merge.property;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.merge.property.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.components.merge.property.DataDefinitionsValuesMergingBusinessLogic;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ComponentInstancePropertiesMergeBLTest {

    private static final String INSTANCE1 = "instance1";
    private static final String INSTANCE2 = "instance2";

    @InjectMocks
    private ComponentInstancePropertiesMergeBL testInstance;

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
        oldResource = new ResourceBuilder()
                .addInstanceProperty(INSTANCE1, "property1")
                .addInstanceProperty(INSTANCE1, "property2")
                .addInstanceProperty(INSTANCE2, "property3")
                .addInput("input1")
                .addInput("input2").build();

        newResource = new ResourceBuilder()
                .addInstanceProperty(INSTANCE1, "property11")
                .addInstanceProperty(INSTANCE1, "property12")
                .addInstanceProperty(INSTANCE2, "property13")
                .addInput("input11")
                .addInput("input12").build();
    }

    @Test
    public void mergeInstancesPropsAndInputs_mergeInstanceProps() throws Exception {
        when(toscaOperationFacade.updateComponentInstancePropsToComponent(newResource.getComponentInstancesProperties(), newResource.getUniqueId()))
                                 .thenReturn(Either.left(Collections.emptyMap()));
        ActionStatus actionStatus = testInstance.mergeComponentInstancesProperties(oldResource, newResource);
        assertEquals(actionStatus, ActionStatus.OK);
        verifyMergeBLCalled(oldResource, newResource);
    }

    @Test
    public void mergeInstancesProps_failure() throws Exception {
        when(toscaOperationFacade.updateComponentInstancePropsToComponent(newResource.getComponentInstancesProperties(), newResource.getUniqueId()))
                                 .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        verifyNoMoreInteractions(toscaOperationFacade, propertyValuesMergingBusinessLogic);
        ActionStatus actionStatus = testInstance.mergeComponentInstancesProperties(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }

    @Test
    public void mergeInstanceProps() throws Exception {
        List<ComponentInstanceProperty> newInstanceProps = newResource.safeGetComponentInstanceProperties(INSTANCE1);
        List<ComponentInstanceProperty> oldInstProps = oldResource.safeGetComponentInstanceProperties(INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceProperties(newResource, INSTANCE1, newInstanceProps))
                                 .thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceProperties(oldInstProps, oldResource.getInputs(), newResource, INSTANCE1);
        assertEquals(actionStatus, ActionStatus.OK);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstProps, oldResource.getInputs(), newInstanceProps, newResource.getInputs());
    }

    @Test
    public void mergeInstanceProps_failure() throws Exception {
        List<ComponentInstanceProperty> newInstanceProps = newResource.safeGetComponentInstanceProperties(INSTANCE1);
        List<ComponentInstanceProperty> oldInstProps = oldResource.safeGetComponentInstanceProperties(INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceProperties(newResource, INSTANCE1, newInstanceProps))
                .thenReturn(StorageOperationStatus.GENERAL_ERROR);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceProperties(oldInstProps, oldResource.getInputs(), newResource, INSTANCE1);
        assertEquals(actionStatus, ActionStatus.GENERAL_ERROR);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstProps, oldResource.getInputs(), newInstanceProps, newResource.getInputs());
    }

    private void verifyMergeBLCalled(Resource oldResource, Resource newResource) {
        List<ComponentInstanceProperty> instance1oldProps = oldResource.getComponentInstancesProperties().get(INSTANCE1);
        List<ComponentInstanceProperty> instance1newProps = newResource.getComponentInstancesProperties().get(INSTANCE1);
        List<ComponentInstanceProperty> instance2oldProps = oldResource.getComponentInstancesProperties().get(INSTANCE2);
        List<ComponentInstanceProperty> instance2newProps = newResource.getComponentInstancesProperties().get(INSTANCE2);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance1oldProps, oldResource.getInputs(), instance1newProps, newResource.getInputs());
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance2oldProps, oldResource.getInputs(), instance2newProps, newResource.getInputs());
    }

}
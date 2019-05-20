package org.openecomp.sdc.be.components.merge.property;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstancePropertiesMergeBL;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ComponentInstancePropertiesMergeBLTest {

    private static final String INSTANCE1 = "instance1";
    private static final String INSTANCE2 = "instance2";
    private static final String OLD_INSTANCE1 = "old.instance1";
    private static final String OLD_INSTANCE2 = "old.instance2";
    private static final String NEW_INSTANCE1 = "new.instance1";
    private static final String NEW_INSTANCE2 = "new.instance2";

    @InjectMocks
    private ComponentInstancePropertiesMergeBL testInstance;

    @Mock
    private DataDefinitionsValuesMergingBusinessLogic propertyValuesMergingBusinessLogic;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    private Resource oldResource;
    private Resource newResource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        oldResource = new ResourceBuilder()
                .addComponentInstance(INSTANCE1, OLD_INSTANCE1)
                .addComponentInstance(INSTANCE2, OLD_INSTANCE2)
                .addInstanceProperty(OLD_INSTANCE1, "property1")
                .addInstanceProperty(OLD_INSTANCE1, "property2")
                .addInstanceProperty(OLD_INSTANCE2, "property3")
                .addInput("input1")
                .addInput("input2").build();

        newResource = new ResourceBuilder()
                .addComponentInstance(INSTANCE1, NEW_INSTANCE1)
                .addComponentInstance(INSTANCE2, NEW_INSTANCE2)
                .addInstanceProperty(NEW_INSTANCE1, "property11")
                .addInstanceProperty(NEW_INSTANCE1, "property12")
                .addInstanceProperty(NEW_INSTANCE2, "property13")
                .addInput("input11")
                .addInput("input12").build();
    }

    @Test
    public void mergeInstancesPropsAndInputs_mergeInstanceProps() throws Exception {
        when(toscaOperationFacade.updateComponentInstancePropsToComponent(newResource.getComponentInstancesProperties(), newResource.getUniqueId()))
                                 .thenReturn(Either.left(Collections.emptyMap()));
        ActionStatus actionStatus = testInstance.mergeComponents(oldResource, newResource);
        assertEquals(ActionStatus.OK, actionStatus);
        verifyMergeBLCalled(oldResource, newResource);
    }

    @Test
    public void mergeInstancesProps_failure() throws Exception {
        when(toscaOperationFacade.updateComponentInstancePropsToComponent(newResource.getComponentInstancesProperties(), newResource.getUniqueId()))
                                 .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        verifyNoMoreInteractions(toscaOperationFacade, propertyValuesMergingBusinessLogic);
        ActionStatus actionStatus = testInstance.mergeComponents(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }

    @Test
    public void mergeInstanceProps() throws Exception {
        List<ComponentInstanceProperty> newInstanceProps = newResource.safeGetComponentInstanceProperties(NEW_INSTANCE1);
        List<ComponentInstanceProperty> oldInstProps = oldResource.safeGetComponentInstanceProperties(OLD_INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceProperties(newResource, NEW_INSTANCE1, newInstanceProps))
                                 .thenReturn(StorageOperationStatus.OK);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceProperties(oldInstProps, oldResource.getInputs(), newResource, NEW_INSTANCE1);
        assertEquals(ActionStatus.OK, actionStatus);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstProps, oldResource.getInputs(), newInstanceProps, newResource.getInputs());
    }

    @Test
    public void mergeInstanceProps_failure() throws Exception {
        List<ComponentInstanceProperty> newInstanceProps = newResource.safeGetComponentInstanceProperties(NEW_INSTANCE1);
        List<ComponentInstanceProperty> oldInstProps = oldResource.safeGetComponentInstanceProperties(OLD_INSTANCE1);
        when(toscaOperationFacade.updateComponentInstanceProperties(newResource, NEW_INSTANCE1, newInstanceProps))
                .thenReturn(StorageOperationStatus.GENERAL_ERROR);
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeComponentInstanceProperties(oldInstProps, oldResource.getInputs(), newResource, NEW_INSTANCE1);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(oldInstProps, oldResource.getInputs(), newInstanceProps, newResource.getInputs());
    }

    private void verifyMergeBLCalled(Resource oldResource, Resource newResource) {
        List<ComponentInstanceProperty> instance1oldProps = oldResource.getComponentInstancesProperties().get(OLD_INSTANCE1);
        List<ComponentInstanceProperty> instance1newProps = newResource.getComponentInstancesProperties().get(NEW_INSTANCE1);
        List<ComponentInstanceProperty> instance2oldProps = oldResource.getComponentInstancesProperties().get(OLD_INSTANCE2);
        List<ComponentInstanceProperty> instance2newProps = newResource.getComponentInstancesProperties().get(NEW_INSTANCE2);
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance1oldProps, oldResource.getInputs(), instance1newProps, newResource.getInputs());
        verify(propertyValuesMergingBusinessLogic).mergeInstanceDataDefinitions(instance2oldProps, oldResource.getInputs(), instance2newProps, newResource.getInputs());
    }

}
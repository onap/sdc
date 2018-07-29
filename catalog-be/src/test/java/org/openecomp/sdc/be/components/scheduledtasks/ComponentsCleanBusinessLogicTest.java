package org.openecomp.sdc.be.components.scheduledtasks;

import com.google.common.collect.Lists;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentsCleanBusinessLogicTest {

    @Mock
    private ResourceBusinessLogic resourceBusinessLogic;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private ComponentsUtils componentsUtils;

    @InjectMocks
    private ComponentsCleanBusinessLogic componentsCleanBL = new ComponentsCleanBusinessLogic();

    @Before
    public void setUp() {
        mockResourceDeleting();
        mockServiceDeleting();
    }

    @Test
    public void deleteAll() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Resource);
        cleanList.add(NodeTypeEnum.Service);
        when(graphLockOperation.lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER),
                eq(NodeTypeEnum.Component))).thenReturn(StorageOperationStatus.OK);
        componentsCleanBL.cleanComponents(cleanList);
        verify(resourceBusinessLogic).deleteMarkedComponents();
        verify(serviceBusinessLogic).deleteMarkedComponents();
        verify(graphLockOperation).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    @Test
    public void deleteResourceWhenOperationAlreadyLocked() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Resource);
        componentsCleanBL.cleanComponents(cleanList, true);
        verify(resourceBusinessLogic).deleteMarkedComponents();
        verify(graphLockOperation, times(0)).lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any());
        verify(graphLockOperation, times(0)).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    @Test
    public void deleteResource() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Resource);
        when(graphLockOperation.lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER),
                eq(NodeTypeEnum.Component))).thenReturn(StorageOperationStatus.OK);
        componentsCleanBL.cleanComponents(cleanList);
        verify(resourceBusinessLogic).deleteMarkedComponents();
        verify(graphLockOperation).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    @Test
    public void deleteServiceWhenOperationAlreadyLocked() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Service);
        componentsCleanBL.cleanComponents(cleanList, true);
        verify(serviceBusinessLogic).deleteMarkedComponents();
        verify(graphLockOperation, times(0)).lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any());
        verify(graphLockOperation, times(0)).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }


    @Test
    public void deleteResourceIsNotCalledDueToCleanupLock() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();

        cleanList.add(NodeTypeEnum.Resource);
        when(graphLockOperation.lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER),
                eq(NodeTypeEnum.Component))).thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);
        Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents = componentsCleanBL.cleanComponents(cleanList);

        assertThat(cleanedComponents.get(NodeTypeEnum.Resource)).isNotNull();
        verify(resourceBusinessLogic, times(0)).deleteMarkedComponents();
        verify(graphLockOperation, times(0)).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }


    @Test
    public void deleteService() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Service);
        when(graphLockOperation.lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER),
                eq(NodeTypeEnum.Component))).thenReturn(StorageOperationStatus.OK);
        componentsCleanBL.cleanComponents(cleanList);
        verify(serviceBusinessLogic).deleteMarkedComponents();
        verify(graphLockOperation).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    @Test
    public void deleteServiceIsNotCalledDueToCleanupLock() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.Service);
        when(graphLockOperation.lockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER),
                eq(NodeTypeEnum.Component))).thenReturn(StorageOperationStatus.FAILED_TO_LOCK_ELEMENT);

        Map<NodeTypeEnum, Either<List<String>, ResponseFormat>> cleanedComponents = componentsCleanBL.cleanComponents(cleanList);

        assertThat(cleanedComponents.get(NodeTypeEnum.Service)).isNotNull();
        verify(serviceBusinessLogic, times(0)).deleteMarkedComponents();
        verify(graphLockOperation, times(0)).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    @Test
    public void deleteWrongElement() {
        List<NodeTypeEnum> cleanList = new ArrayList<>();
        cleanList.add(NodeTypeEnum.User);
        componentsCleanBL.cleanComponents(cleanList);
        verify(resourceBusinessLogic, times(0)).deleteMarkedComponents();
        verify(serviceBusinessLogic, times(0)).deleteMarkedComponents();
        verify(graphLockOperation, times(0)).unlockComponentByName(eq(ComponentsCleanBusinessLogic.DELETE_LOCKER), any(),
                eq(NodeTypeEnum.Component));
    }

    private void mockResourceDeleting() {
        when(resourceBusinessLogic.deleteMarkedComponents()).thenReturn(Either.left(Lists.newArrayList()));
    }

    private void mockServiceDeleting() {
        when(serviceBusinessLogic.deleteMarkedComponents()).thenReturn(Either.left(Lists.newArrayList()));
    }

}


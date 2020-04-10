package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;

import static org.mockito.Mockito.*;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.log.wrappers.Logger;

public class ComponentLockerTest {

    private static final String USER_ID = "userId";

    @Mock
    private GraphLockOperation graphLockOperation;

    @InjectMocks
    private ComponentLocker testInstance;

    // initiaize
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void lock_Component() {
        Component component= new Service();
        component.setComponentType(ComponentTypeEnum.SERVICE);
        component.setUniqueId(USER_ID);
        when(graphLockOperation.lockComponent(USER_ID, component.getComponentType().getNodeType())).thenReturn(StorageOperationStatus.OK);
        testInstance.lock(component);
        verify(graphLockOperation, times(1)).lockComponent(USER_ID, component.getComponentType().getNodeType());
    }

    @Test(expected = StorageException.class)
    public void lock_Component_Exception() {
        Component component= new Service();
        component.setComponentType(ComponentTypeEnum.SERVICE);
        component.setUniqueId(USER_ID);
        when(graphLockOperation.lockComponent(USER_ID, component.getComponentType().getNodeType())).thenReturn(StorageOperationStatus.INVALID_VALUE);
        testInstance.lock(component);
    }

}

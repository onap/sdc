/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Fujitsu Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GraphLockOperation;
import org.openecomp.sdc.be.model.Service;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class ComponentLockerTest {

    private static final String USER_ID = "userId";

    @Mock
    private GraphLockOperation graphLockOperation;

    @InjectMocks
    private ComponentLocker testInstance;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
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

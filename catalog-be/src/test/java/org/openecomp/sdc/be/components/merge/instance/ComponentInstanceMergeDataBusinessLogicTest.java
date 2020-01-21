/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.instance;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ComponentInstanceMergeDataBusinessLogicTest {

    @InjectMocks
    private ComponentInstanceMergeDataBusinessLogic testInstance;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Mock
    private ComponentInstanceMergeInterface componentInstanceMergeInterfaceMock1;

    @Mock
    private ComponentInstanceMergeInterface componentInstanceMergeInterfaceMock2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testInstance.setComponentInstancesMergeBLs(Arrays.asList(componentInstanceMergeInterfaceMock1, componentInstanceMergeInterfaceMock2));
    }

    @Test
    public void saveAllDataBeforeDeleting_allInstanceMergeInterfacesAreCalled() throws Exception {
        Component container = new Service();
        ComponentInstance instance = new ComponentInstance();
        Component instanceOriginResource = new Resource();
        DataForMergeHolder dataForMergeHolder = testInstance.saveAllDataBeforeDeleting(container, instance, instanceOriginResource);
        verify(componentInstanceMergeInterfaceMock1).saveDataBeforeMerge(dataForMergeHolder, container, instance, instanceOriginResource);
        verify(componentInstanceMergeInterfaceMock2).saveDataBeforeMerge(dataForMergeHolder, container, instance, instanceOriginResource);
    }

    @Test
    public void mergeComponentUserOrigData_allInstanceMergeInterfacesAreCalled() throws Exception {
        ArgumentCaptor<ComponentParametersView> componentsFilterCapture = ArgumentCaptor.forClass(ComponentParametersView.class);
        Service persistedService = new Service();
        User user = new User();
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        when(toscaOperationFacade.getToscaElement(Mockito.eq("newContainerId"), componentsFilterCapture.capture())).thenReturn(Either.left(persistedService));
        when(componentInstanceMergeInterfaceMock1.mergeDataAfterCreate(user, dataHolder, persistedService, "instId")).thenReturn(persistedService);
        when(componentInstanceMergeInterfaceMock2.mergeDataAfterCreate(user, dataHolder, persistedService, "instId")).thenReturn(persistedService);
        Component mergeResult = testInstance.mergeComponentUserOrigData(user, dataHolder, new Service(), "newContainerId", "instId");
        assertEquals(persistedService, mergeResult);
        assertComponentFilter(componentsFilterCapture.getValue());
    }

    @Test(expected = ComponentException.class)
    public void mergeComponentUserOrigData_failToGetPersistedComponent_doNotTryToMerge() throws Exception {
        User user = new User();
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        ResponseFormat rf = new ResponseFormat();
        Resource container = new ResourceBuilder().setComponentType(ComponentTypeEnum.SERVICE).build();
        when(toscaOperationFacade.getToscaElement(Mockito.eq("newContainerId"), Mockito.any(ComponentParametersView.class))).thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.GENERAL_ERROR, ComponentTypeEnum.SERVICE)).thenReturn(ActionStatus.GENERAL_ERROR);
        when(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)).thenReturn(rf);
        testInstance.mergeComponentUserOrigData(user, dataHolder, container, "newContainerId", "instId");
    }

    @Test(expected = ComponentException.class)
    public void mergeComponentUserOrigData_failOnOneMerge_doNotCallOtherMerge() throws Exception {
        Service persistedService = new Service();
        User user = new User();
        DataForMergeHolder dataHolder = new DataForMergeHolder();
        ResponseFormat rf = new ResponseFormat();
        when(toscaOperationFacade.getToscaElement(Mockito.eq("newContainerId"), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(persistedService));
        when(componentInstanceMergeInterfaceMock1.mergeDataAfterCreate(user, dataHolder, persistedService, "instId")).thenThrow(new ByActionStatusComponentException(ActionStatus.GENERAL_ERROR));
        testInstance.mergeComponentUserOrigData(user, dataHolder, new Service(), "newContainerId", "instId");
    }

    private void assertComponentFilter(ComponentParametersView value) {
        assertFalse(value.isIgnoreComponentInstances());
        assertFalse(value.isIgnoreComponentInstancesProperties());
        assertFalse(value.isIgnoreComponentInstancesInputs());
        assertFalse(value.isIgnoreArtifacts());
        assertFalse(value.isIgnoreComponentInstancesInterfaces());
    }



}

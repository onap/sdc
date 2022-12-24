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

package org.openecomp.sdc.be.components.impl.instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentInstanceChangeOperationOrchestratorTest {

    private static final Resource CONTAINER = new Resource();
    private static final ComponentInstance NEW_VERSION = new ComponentInstance();
    private static final ComponentInstance PREV_VERSION = new ComponentInstance();
    private static final String DELETED_INS_ID = "id";
    private ComponentInstanceChangeOperationOrchestrator testInstance;
    @Mock
    private OnComponentInstanceChangeOperation componentInstanceChangeOperation1;
    @Mock
    private OnComponentInstanceChangeOperation componentInstanceChangeOperation2;
    @Mock
    private OnComponentInstanceChangeOperation componentInstanceChangeOperation3;

    @Before
    public void setUp() throws Exception {
        testInstance = new ComponentInstanceChangeOperationOrchestrator(asList(componentInstanceChangeOperation1, componentInstanceChangeOperation2, componentInstanceChangeOperation3));
    }

    @Test
    public void doPostChangeVersionOperations_whenFirstPostOperationFails_doNotRunFollowingOperations() {
        when(componentInstanceChangeOperation1.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(CONTAINER, PREV_VERSION, NEW_VERSION);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyNoInteractions(componentInstanceChangeOperation2, componentInstanceChangeOperation3);
    }

    @Test
    public void doPostChangeVersionOperations_whenAnyPostOperationFails_doNotRunFollowingOperations() {
        when(componentInstanceChangeOperation1.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(CONTAINER, PREV_VERSION, NEW_VERSION);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyNoInteractions(componentInstanceChangeOperation3);
    }

    @Test
    public void doPostChangeVersionOperations_whenLastPostOperationFails_returnTheFailureResult() {
        when(componentInstanceChangeOperation1.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation3.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(CONTAINER, PREV_VERSION, NEW_VERSION);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void doPostChangeVersionOperations_whenAllOperationsSucceeds_returnOk() {
        when(componentInstanceChangeOperation1.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation3.onChangeVersion(CONTAINER, PREV_VERSION, NEW_VERSION)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.doPostChangeVersionOperations(CONTAINER, PREV_VERSION, NEW_VERSION);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }

    @Test
    public void doOnDeleteInstanceOperations_whenFirstPostOperationFails_doNotRunFollowingOperations() {
        when(componentInstanceChangeOperation1.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doOnDeleteInstanceOperations(CONTAINER, DELETED_INS_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyNoInteractions(componentInstanceChangeOperation2, componentInstanceChangeOperation3);
    }

    @Test
    public void doOnDeleteInstanceOperations_whenAnyPostOperationFails_doNotRunFollowingOperations() {
        when(componentInstanceChangeOperation1.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doOnDeleteInstanceOperations(CONTAINER, DELETED_INS_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
        verifyNoInteractions(componentInstanceChangeOperation3);
    }

    @Test
    public void doOnDeleteInstanceOperations_whenLastPostOperationFails_returnTheFailureResult() {
        when(componentInstanceChangeOperation1.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation3.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.doOnDeleteInstanceOperations(CONTAINER, DELETED_INS_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.GENERAL_ERROR);
    }

    @Test
    public void doOnDeleteInstanceOperations_whenAllOperationsSucceeds_returnOk() {
        when(componentInstanceChangeOperation1.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation2.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        when(componentInstanceChangeOperation3.onDelete(CONTAINER, DELETED_INS_ID)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.doOnDeleteInstanceOperations(CONTAINER, DELETED_INS_ID);
        assertThat(actionStatus).isEqualTo(ActionStatus.OK);
    }
}

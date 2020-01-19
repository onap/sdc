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

package org.openecomp.sdc.be.components.merge.resource;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.merge.ComponentsMergeCommand;
import org.openecomp.sdc.be.components.utils.ObjectGenerator;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Resource;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResourceDataMergeBusinessLogicTest {

    @InjectMocks
    private ResourceDataMergeBusinessLogic testInstance;

    @Mock
    private MergeCommandsFactory mergeCommandsFactory;

    @Mock
    private ComponentsMergeCommand commandA;

    @Mock
    private ComponentsMergeCommand commandB;

    @Mock
    private ComponentsMergeCommand commandC;

    private Resource oldResource, newResource;

    @Before
    public void setUp() throws Exception {
        oldResource = ObjectGenerator.buildBasicResource();
        newResource = ObjectGenerator.buildBasicResource();
        when(mergeCommandsFactory.getMergeCommands(oldResource, newResource)).thenReturn(Either.left(asList(commandA, commandB, commandC)));
    }

    @Test
    public void whenCommandsFactoryFails_propagateTheFailure() {
        when(mergeCommandsFactory.getMergeCommands(oldResource, newResource)).thenReturn(Either.right(ActionStatus.GENERAL_ERROR));
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verifyZeroInteractions(commandA, commandB, commandC);
    }

    @Test
    public void mergeResources_allMergeClassesAreCalled() {
        when(commandA.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(commandB.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        when(commandC.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void mergeResources_mergeCommandFailed_dontCallOtherMergeMethods() {
        when(commandA.mergeComponents(oldResource, newResource)).thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.mergeResourceEntities(oldResource, newResource);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
        verify(commandA).description();
        verifyZeroInteractions(commandB, commandC);
    }

}

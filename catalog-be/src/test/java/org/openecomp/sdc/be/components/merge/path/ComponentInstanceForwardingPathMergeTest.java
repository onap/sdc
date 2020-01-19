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

package org.openecomp.sdc.be.components.merge.path;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.merge.instance.ComponentInstanceForwardingPathMerge;
import org.openecomp.sdc.be.components.path.BaseForwardingPathVersionChangeTest;
import org.openecomp.sdc.be.impl.ForwardingPathUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

public class ComponentInstanceForwardingPathMergeTest extends BaseForwardingPathVersionChangeTest {

    @InjectMocks
    private ComponentInstanceForwardingPathMerge testInstance;

    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    private User user;

    @Before
    public void setUpData() {
        MockitoAnnotations.initMocks(this);
        user = new User();
        user.setUserId("44");
        user.setRole(UserRoleEnum.ADMIN.getName());
    }

    @Test
    public void testIgnoreMergeSinceItIsNotService() {

        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
        assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());
        Component componentResponseFormatEither = testInstance
            .mergeDataAfterCreate(user, dataHolder, newNodeAC, "3344");
        assertNotNull(componentResponseFormatEither);
        assertTrue(componentResponseFormatEither != null);
        assertEquals(newNodeAC, componentResponseFormatEither);
    }

    @Test
    public void mergeShouldDelete() {
        Set<String> forwardingPathNamesToDeleteOnComponentInstanceDeletion = new ForwardingPathUtils()
            .findForwardingPathNamesToDeleteOnComponentInstanceDeletion(service, nodeACI.getUniqueId());
        nodeACI.getCapabilities().clear();
        newNodeAC.getCapabilities().clear();
        Set<String> returnValue = forwardingPathNamesToDeleteOnComponentInstanceDeletion;
        when(serviceBusinessLogic.deleteForwardingPaths(any(), any(), any(), anyBoolean()))
            .thenReturn(returnValue);
        when(toscaOperationFacade.getToscaFullElement(any())).thenReturn(Either.left(newNodeAC));

        // Change internal ci, just like change version do
        service.getComponentInstances().remove(nodeACI);
        service.getComponentInstances().add(newNodeACI);

        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
        assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());
        Component componentResponseFormatEither = testInstance
            .mergeDataAfterCreate(user, dataHolder, service, newNodeA);
        assertNotNull(componentResponseFormatEither);
        assertEquals(0, ((Service) componentResponseFormatEither).getForwardingPaths().size());
    }

    @Test
    public void mergeShouldUpdate() {
          when(serviceBusinessLogic.updateForwardingPath(any(), any(), any(), anyBoolean()))
              .then(invocationOnMock -> service);
           when(toscaOperationFacade.getToscaFullElement(any())).thenReturn(Either.left(newNodeAC));
          testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
          assertEquals(nodeACI.getName(), dataHolder.getOrigComponentInstId());

          // Change internal ci, just like change version do
          service.getComponentInstances().remove(nodeACI);
          service.getComponentInstances().add(newNodeACI);

          Component component = testInstance.mergeDataAfterCreate(user, dataHolder, service, newNodeA);
          assertNotNull(component);
    }

    @Test
    public void handleNullCapailities() {
        nodeACI.setCapabilities(null);
        testInstance.saveDataBeforeMerge(dataHolder, service, nodeACI, newNodeAC);
    }
}

/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.impl.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.ComponentLocker;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

@RunWith(MockitoJUnitRunner.class)
public class ComponentLockAspectTest {

    private static final String COMPONENT_ID = "componentID";

    @Mock
    private ComponentLocker componentLocker;
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private Signature signature;

    @Test
    public void shouldLockProceedAndUnlockComponent() throws Throwable {
        ComponentLockAspect componentLockAspect = new ComponentLockAspect(componentLocker);
        Mockito.when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        componentLockAspect.lock(proceedingJoinPoint, COMPONENT_ID, ComponentTypeEnum.RESOURCE);
        InOrder orderVerifier = Mockito.inOrder(componentLocker, proceedingJoinPoint);
        orderVerifier.verify(proceedingJoinPoint).getSignature();
        orderVerifier.verify(componentLocker).lock(COMPONENT_ID, ComponentTypeEnum.RESOURCE.getNodeType());
        orderVerifier.verify(proceedingJoinPoint).proceed();
        orderVerifier.verify(componentLocker).unlock(COMPONENT_ID, ComponentTypeEnum.RESOURCE.getNodeType());
    }
}
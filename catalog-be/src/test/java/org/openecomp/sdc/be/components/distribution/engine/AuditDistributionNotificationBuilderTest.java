/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.distribution.engine;

import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;

public class AuditDistributionNotificationBuilderTest {
    
    
     public class SelfReturningAnswer implements Answer<Object>{

        public Object answer(InvocationOnMock invocation) throws Throwable {
            Object mock = invocation.getMock();
            if( invocation.getMethod().getReturnType().isInstance( mock )){
                return mock;
            }
            else{
                return RETURNS_DEFAULTS.answer(invocation);
            }
        }
    }
     
 
     private CambriaErrorResponse status;
     private Service service;
     private User modifier;
     
    @Test
    public void testBuilder() {

        AuditDistributionNotificationBuilder mockBuilder =
                mock(AuditDistributionNotificationBuilder.class, new SelfReturningAnswer());

        when(mockBuilder.setTopicName("topicName").setDistributionId("distributionId").setStatus(status)
                .setService(service).setEnvId("envId").setModifier(modifier).setWorkloadContext("workloadContext")
                .setTenant("tenant")).thenReturn(mockBuilder);
        assert mockBuilder.setTopicName("topicName").setDistributionId("distributionId").setStatus(status)
                .setService(service).setEnvId("envId").setModifier(modifier).setWorkloadContext("workloadContext")
                .setTenant("tenant") == mockBuilder;
    }
}

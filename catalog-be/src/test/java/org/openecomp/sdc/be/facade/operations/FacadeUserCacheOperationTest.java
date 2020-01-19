/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.facade.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.user.UserMessage;
import org.openecomp.sdc.be.user.UserOperationEnum;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FacadeUserCacheOperationTest {
    @Mock
    private DmaapProducer msProducer;
    @Captor
    private ArgumentCaptor<UserMessage> messageCaptor;
    
    private UserOperation userCacheOperation;
    
    @Before
    public void setUp() {
        userCacheOperation = new UserOperation(msProducer);
    }

    @Test
    public void testUpdate() {
        userCacheOperation.updateUserCache(UserOperationEnum.CREATE, "id", "role");
        Mockito.verify(msProducer).pushMessage(messageCaptor.capture());
        
        UserMessage message = messageCaptor.getValue();
        
        assertThat(message.getOperation()).isEqualTo(UserOperationEnum.CREATE);
        assertThat(message.getUserId()).isEqualTo("id");
        assertThat(message.getRole()).isEqualTo("role");
    }

}

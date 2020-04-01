/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Samsung Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.catalog.api.IStatus;
import org.openecomp.sdc.be.catalog.enums.ChangeTypeEnum;
import org.openecomp.sdc.be.catalog.impl.ComponentMessage;
import org.openecomp.sdc.be.catalog.impl.DmaapProducer;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;

@RunWith(MockitoJUnitRunner.class)
public class CatalogOperationTest {
    @Mock
    private DmaapProducer msProducer;
    @Mock
    private Resource component;
    @Captor
    private ArgumentCaptor<ComponentMessage> messageCaptor;

    private CatalogOperation catalogOperation;

    @Before
    public void setUp() {
        catalogOperation = new CatalogOperation(msProducer);
    }

    @Test
    public void updateCatalogTest() {
        when(component.getLifecycleState()).thenReturn(LifecycleStateEnum.CERTIFIED);
        when(component.getResourceType()).thenReturn(ResourceTypeEnum.ABSTRACT);
        when(component.getLastUpdateDate()).thenReturn(System.currentTimeMillis());
        when(component.getLastUpdaterUserId()).thenReturn("mock-id");
        when(component.getCategories()).thenReturn(null);
        when(msProducer.pushMessage(any(ComponentMessage.class))).thenReturn(IStatus.getSuccessStatus());

        catalogOperation.updateCatalog(ChangeTypeEnum.LIFECYCLE, component);

        Mockito.verify(msProducer).pushMessage(messageCaptor.capture());
        ComponentMessage message = messageCaptor.getValue();
        assertThat(message.getChangeType()).isEqualTo(ChangeTypeEnum.LIFECYCLE);
        assertThat(message.getResourceType()).isEqualTo(ResourceTypeEnum.ABSTRACT.name());
        assertThat(message.getLifecycleState()).isEqualTo(LifecycleStateEnum.CERTIFIED.name());
    }
}
/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.impl.MonitoringDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.common.monitoring.MonitoringEvent;
import org.openecomp.sdc.exception.ResponseFormat;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MonitoringBusinessLogicTest {

    private MonitoringEvent event;

    @InjectMocks
    MonitoringBusinessLogic monitoringBusinessLogic;

    @Mock
    private MonitoringDao monitoringDao;

    @Mock
    private ComponentsUtils componentsUtils;

    @Before
    public void setUp() throws Exception {
        monitoringBusinessLogic = new MonitoringBusinessLogic();
        MockitoAnnotations.initMocks(this);
        event = new MonitoringEvent();
    }

    @Test
    public void testLogMonitoringEvent_returnsSuccessful() {
        Mockito.when(monitoringDao.addRecord(any(MonitoringEvent.class))).thenReturn(ActionStatus.OK);
        Assert.assertTrue(monitoringBusinessLogic.logMonitoringEvent(event).isLeft());
    }

    @Test
    public void testLogMonitoringEvent_returnsError() {
        Mockito.when(monitoringDao.addRecord(any(MonitoringEvent.class))).thenReturn(ActionStatus.GENERAL_ERROR);
        Mockito.when(componentsUtils.getResponseFormat(any(ActionStatus.class))).thenReturn(new ResponseFormat());
        Assert.assertTrue(monitoringBusinessLogic.logMonitoringEvent(event).isRight());
    }

    @Test
    public void testGetEsPort(){
        when(monitoringDao.getEsPort()).thenReturn("10");
        String port = monitoringBusinessLogic.getEsPort();
        assertEquals("10", port);
    }

    @Test
    public void testGetHost(){
        Mockito.when(monitoringDao.getEsHost()).thenReturn("['127.0.0.1', '[::1]']");
        Assert.assertEquals("127.0.0.1", monitoringBusinessLogic.getEsHost());
    }
}
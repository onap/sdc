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
package org.openecomp.sdc.be.auditing.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.openecomp.sdc.test.utils.TestConfigurationProvider;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class AuditingManagerTest {

    private static final String MSG = "msg";
    private String msg = "Any message";
    private AuditingManager auditingManager;

    @Mock
    private AuditingGenericEvent auditEvent;
    @Mock
    private AuditCassandraDao cassandraDao;
    @Mock
    private AuditEventFactory eventFactory;
    @Mock
    private ConfigurationProvider config;

    @Before
    public void setUp() throws Exception {
        auditingManager = new AuditingManager(cassandraDao, new TestConfigurationProvider());
        Mockito.when(eventFactory.getLogMessage()).thenReturn(msg);
        Mockito.when(eventFactory.getDbEvent()).thenReturn(auditEvent);
        Mockito.when(cassandraDao.saveRecord(auditEvent)).thenReturn(CassandraOperationStatus.OK);
    }

    @Test
    public void testShouldAuditEvent() {
        String result = auditingManager.auditEvent(eventFactory);
        assertThat(result, is(msg));
        Mockito.verify(cassandraDao).saveRecord(auditEvent);
    }

    @Test
    public void testShouldNotAuditEvent() {
        Configuration testConfig = new Configuration();
        testConfig.setDisableAudit(true);
        Mockito.when(config.getConfiguration()).thenReturn(testConfig);

        AuditingManager auditingManager2 = new AuditingManager(cassandraDao, config);
        assertNull(auditingManager2.auditEvent(eventFactory));
        Mockito.verify(config, Mockito.times(1)).getConfiguration();
    }

    @Test
    public void testShouldAuditEventWithLog() {
        LoggerSdcAudit logger = Mockito.mock(LoggerSdcAudit.class);
        String result = auditingManager.auditEvent(eventFactory, logger);
        assertThat(result, is(msg));
        Mockito.verify(cassandraDao).saveRecord(auditEvent);
    }
}
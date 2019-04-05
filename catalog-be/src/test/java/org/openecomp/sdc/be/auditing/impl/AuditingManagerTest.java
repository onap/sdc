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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditingManager.ConfigurationProvider;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;

@RunWith(MockitoJUnitRunner.class)
public class AuditingManagerTest {

    private static final String MSG = "msg";
    private String msg = "Any message";
    private AuditingManager auditingManager;

    @Mock
    private AuditingGenericEvent auditEvent;
    @Mock
    private AuditingDao auditingDao;
    @Mock
    private AuditCassandraDao cassandraDao;
    @Mock
    private AuditEventFactory eventFactory;

    @Before
    public void setUp() throws Exception {
        auditingManager = new AuditingManager(auditingDao, cassandraDao, new TestConfigurationProvider());
        Mockito.when(eventFactory.getLogMessage()).thenReturn(msg);
        Mockito.when(eventFactory.getDbEvent()).thenReturn(auditEvent);
        Mockito.when(eventFactory.getAuditingEsType()).thenReturn(MSG);
        Mockito.when(cassandraDao.saveRecord(auditEvent)).thenReturn(CassandraOperationStatus.OK);
        Mockito.when(auditingDao.addRecord(auditEvent, MSG)).thenReturn(
            ActionStatus.OK);
    }

    @Test
    public void testShouldAuditEvent() {
        String result = auditingManager.auditEvent(eventFactory);
        assertThat(result, is(msg));
        Mockito.verify(cassandraDao).saveRecord(auditEvent);
        Mockito.verify(auditingDao).addRecord(auditEvent, MSG);
    }

    class TestConfigurationProvider implements ConfigurationProvider {
        public Configuration getConfiguration() {
            return Mockito.mock(Configuration.class);
        }
    }
}
/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuditingManager {

    private static final Logger log = Logger.getLogger(AuditingManager.class.getName());

    private final AuditingDao auditingDao;
    private final AuditCassandraDao cassandraDao;
    private final ConfigurationProvider configurationProvider;

    @Autowired
    public AuditingManager(AuditingDao auditingDao, AuditCassandraDao cassandraDao, ConfigurationProvider configurationProvider) {
        this.auditingDao = auditingDao;
        this.cassandraDao = cassandraDao;
        this.configurationProvider = configurationProvider;
    }

    public String auditEvent(AuditEventFactory factory) {
        if (configurationProvider.getConfiguration().isDisableAudit()) {
            return null;
        }
        String msg = factory.getLogMessage();
        logAuditEvent(msg);

        //TODO - remove this method after we got rid of ES
        saveEventToElasticSearch(factory);
        saveEventToCassandra(factory.getDbEvent());
        return msg;
    }

    private void saveEventToCassandra(AuditingGenericEvent event) {
        CassandraOperationStatus result = cassandraDao.saveRecord(event);
        if (!result.equals(CassandraOperationStatus.OK)) {
            log.warn("Failed to persist to cassandra auditing event: {}", result.name());
        }
    }

    private void saveEventToElasticSearch(AuditEventFactory factory) {
        ActionStatus addRecordStatus = auditingDao.addRecord(factory.getDbEvent(), factory.getAuditingEsType());
        if (!addRecordStatus.equals(ActionStatus.OK)) {
            log.warn("Failed to persist auditing event: {}", addRecordStatus.name());
        }
    }

    private void logAuditEvent(final String formattedString) {
        log.trace("logAuditEvent - start");
        log.debug(formattedString);
        LogFieldsMdcHandler.getInstance()
                .setAuditMessage(formattedString);
        if (!LoggerSdcAudit.isFlowBeingTakenCare()){
            log.debug("MOVED FROM AUDIT LOG: {}", formattedString);
        }
        log.trace("logAuditEvent - end");
    }

}

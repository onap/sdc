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

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.log.wrappers.LoggerSdcAudit;
import org.springframework.stereotype.Component;
import org.slf4j.MarkerFactory;

@Component
public class AuditingManager {

    private static final Logger log = Logger.getLogger(AuditingManager.class.getName());

    private final AuditCassandraDao cassandraDao;
    private final ConfigurationProvider configurationProvider;

    public AuditingManager(AuditCassandraDao cassandraDao, ConfigurationProvider configurationProvider) {
        this.cassandraDao = cassandraDao;
        this.configurationProvider = configurationProvider;
    }

    public String auditEvent(AuditEventFactory factory) {
        if (configurationProvider.getConfiguration().isDisableAudit()) {
            return null;
        }
        String msg = factory.getLogMessage();
        logAuditEvent(msg);

        saveEventToCassandra(factory.getDbEvent());
        return msg;
    }

    public String auditEvent(AuditEventFactory factory, LoggerSdcAudit audit) {
        String msg = auditEvent(factory);
        logAuditEvent(msg, audit, factory.getDbEvent().getRequestId());
        return msg;
    }

    private void logAuditEvent(String msg, LoggerSdcAudit audit, String requestId) {
        if(audit != null) {
            audit.logEntry(LogLevel.INFO, Severity.OK, msg,
                    MarkerFactory.getMarker(ONAPLogConstants.Markers.ENTRY.getName()), requestId);
        }
    }

    private void saveEventToCassandra(AuditingGenericEvent event) {
        CassandraOperationStatus result = cassandraDao.saveRecord(event);
        if (result != CassandraOperationStatus.OK) {
            log.warn("Failed to persist to cassandra auditing event: {}", result.name());
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

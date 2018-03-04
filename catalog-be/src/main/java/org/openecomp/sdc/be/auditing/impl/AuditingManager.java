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
 */

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.resources.data.auditing.AuditRecordFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Map.Entry;

@Component
public class AuditingManager {

    private static final Logger log = LoggerFactory.getLogger(AuditingManager.class);
    private final AuditingDao auditingDao;
    private final AuditCassandraDao cassandraDao;

    public AuditingManager(AuditingDao auditingDao, AuditCassandraDao cassandraDao) {
        this.auditingDao = auditingDao;
        this.cassandraDao = cassandraDao;
    }

    // TODO remove after completing refactoring
    public String auditEvent(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        String msg = "";
        try {
            boolean disableAudit = ConfigurationManager.getConfigurationManager().getConfiguration().isDisableAudit();
            if (disableAudit) {
                return null;
            }
            // Adding UUID from thread local
            auditingFields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID, ThreadLocalsHolder.getUuid());

            Object status = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
            auditingFields.put(AuditingFieldsKeysEnum.AUDIT_STATUS, String.valueOf(status));

            // normalizing empty string values - US471661
            normalizeEmptyAuditStringValues(auditingFields);

            // Format modifier
            formatModifier(auditingFields);

            // Format user
            formatUser(auditingFields);

            // Logging the event
            msg = AuditingLogFormatUtil.logAuditEvent(auditingFields);

            // Determining the type of the auditing data object
            AuditingActionEnum actionEnum = AuditingActionEnum.getActionByName((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION));
            log.info("audit event {} of type {}", actionEnum.getName(), actionEnum.getAuditingEsType());
            ActionStatus addRecordStatus = auditingDao.addRecord(auditingFields, actionEnum.getAuditingEsType());
            if (!addRecordStatus.equals(ActionStatus.OK)) {
                log.warn("Failed to persist auditing event: {}", addRecordStatus);
            }

            AuditingGenericEvent recordForCassandra = AuditRecordFactory.createAuditRecord(auditingFields);
            if (recordForCassandra != null) {
                saveEventToCassandra(recordForCassandra);
            }

        } catch (Exception e) {
            // Error during auditing shouldn't terminate flow
            log.warn("Error during auditEvent: {}", e);
        }
        return msg;
    }


    public String auditEvent(AuditEventFactory factory) {
        if (ConfigurationManager.getConfigurationManager().getConfiguration().isDisableAudit()) {
            return null;
        }
        AuditingGenericEvent event = factory.getDbEvent();
        // Logging the event
        //TODO - change this call after EELF Audit stuff merge
        String msg = factory.getLogMessage();
        AuditingLogFormatUtil.logAuditEvent(msg);

        //TODO - remove this method after we got rid of ES
        saveEventToElasticSearch(factory, event);
        saveEventToCassandra(event);
        return msg;
    }

    private void saveEventToCassandra(AuditingGenericEvent event) {
        CassandraOperationStatus result = cassandraDao.saveRecord(event);
        if (!result.equals(CassandraOperationStatus.OK)) {
            log.warn("Failed to persist to cassandra auditing event: {}", result.name());
        }
    }

    private void saveEventToElasticSearch(AuditEventFactory factory, AuditingGenericEvent event) {
        ActionStatus addRecordStatus = auditingDao.addRecord(event, factory.getAuditingEsType());
        if (!addRecordStatus.equals(ActionStatus.OK)) {
            log.warn("Failed to persist auditing event: {}", addRecordStatus.name());
        }
    }


    private void formatUser(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        if (auditingFields.get(AuditingFieldsKeysEnum.AUDIT_USER_UID) != null) {
            String userDetails = (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_USER_UID);

            String user = AuditingLogFormatUtil.getUser(userDetails);
            auditingFields.put(AuditingFieldsKeysEnum.AUDIT_USER_UID, user);
        }
    }

    private void formatModifier(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        String modifier = AuditingLogFormatUtil.getModifier((String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME), (String) auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID));
        auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID, modifier);
        auditingFields.remove(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME);
    }

    private void normalizeEmptyAuditStringValues(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        for (Entry<AuditingFieldsKeysEnum, Object> auditingEntry : auditingFields.entrySet()) {
            if (auditingEntry.getKey().getValueClass().equals(String.class)) {
                String auditingValue = (String) auditingEntry.getValue();
                boolean isEmpty = false;
                if (auditingValue != null) {
                    String trimmedValue = auditingValue.trim();
                    if ((trimmedValue.equals(Constants.EMPTY_STRING)) || trimmedValue.equals(Constants.NULL_STRING) || trimmedValue.equals(Constants.DOUBLE_NULL_STRING)) {
                        isEmpty = true;
                    }
                } else {// is null
                    isEmpty = true;
                }
                // Normalizing to ""
                if (isEmpty) {
                    auditingEntry.setValue(Constants.EMPTY_STRING);
                }
            }
        }
    }
}

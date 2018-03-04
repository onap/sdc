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

package org.openecomp.sdc.be.resources.data.auditing;

import java.text.SimpleDateFormat;
import java.util.*;

import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.CONSUMER_EVENT_TYPE)
public class ConsumerEvent extends AuditingGenericEvent {
    private static String CONSUMER_EVENT_TEMPLATE = "action=\"%s\" timestamp=\"%s\" "
            + "modifier=\"%s\" ecompUser=\"%s\" status=\"%s\" desc=\"%s\"";

    @PartitionKey
    protected UUID timebaseduuid;

    @ClusteringColumn
    protected Date timestamp1;

    @Column(name = "request_id")
    protected String requestId;
    @Column
    protected String action;
    @Column
    protected String status;

    @Column(name = "description")
    protected String desc;

    @Column
    private String modifier;

    @Column(name = "ecomp_user")
    private String ecompUser;

    public ConsumerEvent() {
        super();
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public ConsumerEvent(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        this();
        Object value;
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID);
        if (value != null) {
            setRequestId((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);
        if (value != null) {
            setAction((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_STATUS);
        if (value != null) {
            setStatus((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_DESC);
        if (value != null) {
            setDesc((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID);
        if (value != null) {
            setModifier((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ECOMP_USER);
        if (value != null) {
            setEcompUser((String) value);
        }
    }

    public ConsumerEvent(String action, CommonAuditData commonAuditData, String ecompUser, String modifier) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.modifier = modifier;
        this.ecompUser = ecompUser;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKeysEnum.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());

        fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKeysEnum.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKeysEnum.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKeysEnum.AUDIT_ECOMP_USER.getDisplayName(), getEcompUser());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getEcompUser() {
        return ecompUser;
    }

    public void setEcompUser(String ecompUser) {
        this.ecompUser = ecompUser;
    }

    public UUID getTimebaseduuid() {
        return timebaseduuid;
    }

    public void setTimebaseduuid(UUID timebaseduuid) {
        this.timebaseduuid = timebaseduuid;
    }

    public Date getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(Date timestamp1) {
        this.timestamp1 = timestamp1;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "ConsumerEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId="
                + requestId + ", action=" + action + ", status=" + status + ", desc=" + desc + ", modifier=" + modifier
                + ", ecompUser=" + ecompUser + "]";
    }

}

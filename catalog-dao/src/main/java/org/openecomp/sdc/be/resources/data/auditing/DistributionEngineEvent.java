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

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

@Getter
@Setter
@ToString
@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.DISTRIBUTION_ENGINE_EVENT_TYPE)
public class DistributionEngineEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn
    protected Date timestamp1;
    @Column(name = "request_id")
    protected String requestId;
    @Column(name = "service_instance_id")
    protected String serviceInstanceId;
    @Column
    protected String action;
    @Column
    protected String status;
    @Column(name = "description")
    protected String desc;
    @Column(name = "consumer_id")
    private String consumerId;
    @Column(name = "DSTATUS_TOPIC")
    private String dstatusTopic;
    @Column(name = "DNOTIF_TOPIC")
    private String dnotifTopic;
    @Column(name = "d_env")
    private String environmentName;
    @Column
    private String role;
    @Column(name = "api_key")
    private String apiKey;

    //Required to be public as it is used by Cassandra driver on get operation
    public DistributionEngineEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public DistributionEngineEvent(String action, CommonAuditData commonAuditData, String consumerId, DistributionTopicData distributionTopicData,
                                   String apiKey, String envName, String role) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.status = commonAuditData.getStatus();
        //if no desc, keep distr desc
        this.desc = commonAuditData.getDescription();
        this.consumerId = consumerId;
        this.dstatusTopic = distributionTopicData.getStatusTopic();
        this.dnotifTopic = distributionTopicData.getNotificationTopic();
        this.apiKey = apiKey;
        this.environmentName = envName;
        this.role = role;
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Date timestamp1) {
        this.timestamp1 = timestamp1;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), getConsumerId());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_API_KEY.getDisplayName(), getApiKey());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME.getDisplayName(), getEnvironmentName());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_ROLE.getDisplayName(), getRole());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME.getDisplayName(), getDstatusTopic());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME.getDisplayName(), getDnotifTopic());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }
}

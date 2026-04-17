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
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Getter
@Setter
@Entity(defaultKeyspace = AuditingTypesConstants.AUDIT_KEYSPACE)
@CqlName(AuditingTypesConstants.DISTRIBUTION_NOTIFICATION_EVENT_TYPE)
public class DistributionNotificationEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn
    @Setter(AccessLevel.NONE)
    protected Instant timestamp1;
    @CqlName("request_id")
    protected String requestId;
    @CqlName("service_instance_id")
    protected String serviceInstanceId;
    @CqlName("action")
    protected String action;
    @CqlName("status")
    protected String status;
    @CqlName("description")
    protected String desc;
    @CqlName("resource_name")
    private String resourceName;
    @CqlName("resource_type")
    private String resourceType;
    @CqlName("curr_version")
    private String currVersion;
    @CqlName("modifier")
    private String modifier;
    @CqlName("curr_state")
    private String currState;
    @CqlName("topic_name")
    private String topicName;
    @CqlName("did")
    private String did;
    @CqlName("env_id")
    private String envId;
    @CqlName("vnf_workload_context")
    private String vnfWorkloadContext;
    @CqlName("tenant")
    private String tenant;

    public DistributionNotificationEvent() {
        timestamp1 = Instant.now();
        timebaseduuid = Uuids.timeBased();
    }

    public DistributionNotificationEvent(String action, CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                         ResourceVersionInfo resourceVersionInfo, String did, String modifier, String topicName,
                                         OperationalEnvAuditData opEnvFields) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.did = did;
        this.modifier = modifier;
        this.currState = resourceVersionInfo.getState();
        this.currVersion = resourceVersionInfo.getVersion();
        this.resourceName = resourceCommonInfo.getResourceName();
        this.resourceType = resourceCommonInfo.getResourceType();
        this.topicName = topicName;
        this.envId = opEnvFields.getEnvId();
        this.vnfWorkloadContext = opEnvFields.getVnfWorkloadContext();
        this.tenant = opEnvFields.getTenant();
    }

    public void setTimestamp1(String timestamp) {
        this.timestamp1 = parseDateFromString(timestamp);
    }

    public void setTimestamp1(Instant timestamp) {
        this.timestamp1 = timestamp;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_REQUEST_ID.getDisplayName(), getRequestId());
        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_ENVIRONMENT_ID.getDisplayName(), getEnvId());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_VNF_WORKLOAD_CONTEXT.getDisplayName(), getVnfWorkloadContext());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_TENANT.getDisplayName(), getTenant());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID.getDisplayName(), getDid());
        fields.put(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE.getDisplayName(), getCurrState());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION.getDisplayName(), getCurrVersion());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_TOPIC_NAME.getDisplayName(), getTopicName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(Date.from(timestamp1)));
    }

    @Override
    public String toString() {
        return "DistributionNotificationEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", requestId=" + requestId
            + ", serviceInstanceId=" + serviceInstanceId + ", action=" + action + ", status=" + status + ", desc=" + desc + ", resourceName="
            + resourceName + ", resourceType=" + resourceType + ", currVersion=" + currVersion + ", modifier=" + modifier + ", currState=" + currState
            + ", topicName=" + topicName + ", did=" + did + ", envId=" + envId + ", vnfWorkloadContext=" + vnfWorkloadContext + ", tenant=" + tenant
            + "]";
    }
}

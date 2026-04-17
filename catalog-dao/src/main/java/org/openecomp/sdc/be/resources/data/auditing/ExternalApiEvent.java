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
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;


/**
 * This class Represents the Audit for External API
 */
@Getter
@Setter
@Entity(defaultKeyspace = AuditingTypesConstants.AUDIT_KEYSPACE)
@CqlName(AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE)
public class ExternalApiEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn()
    protected Instant timestamp1;
    @CqlName("action")
    protected String action;
    @CqlName("status")
    protected String status;
    @CqlName("description")
    protected String desc;
    @CqlName("service_instance_id")
    protected String serviceInstanceId;
    @CqlName("invariant_uuid")
    protected String invariantUuid;
    @CqlName("prev_version")
    protected String prevVersion;
    @CqlName("prev_state")
    protected String prevState;
    @CqlName("curr_state")
    protected String currState;
    @CqlName("consumer_id")
    private String consumerId;
    @CqlName("resource_url")
    private String resourceURL;
    @CqlName("resource_name")
    private String resourceName;
    @CqlName("resource_type")
    private String resourceType;
    @CqlName("modifier")
    private String modifier;
    @CqlName("curr_version")
    private String currVersion;
    @CqlName("prev_artifact_uuid")
    private String prevArtifactUuid;
    @CqlName("curr_artifact_uuid")
    private String currArtifactUuid;
    @CqlName("artifact_data")
    private String artifactData;

    //Required to be public as it is used by Cassandra driver on get operation
    public ExternalApiEvent() {
        timestamp1 = Instant.now();
        timebaseduuid = Uuids.timeBased();
    }

    public ExternalApiEvent(String action, CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo, DistributionData distributionData,
                            ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String modifier, String invariantUuid,
                            String artifactData) {
        this();
        this.action = action;
        this.status = commonAuditData.getStatus();
        this.desc = commonAuditData.getDescription();
        this.requestId = commonAuditData.getRequestId();
        this.consumerId = distributionData.getConsumerId();
        this.resourceURL = distributionData.getResourceUrl();
        this.resourceName = resourceCommonInfo.getResourceName();
        this.resourceType = resourceCommonInfo.getResourceType();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.invariantUuid = invariantUuid;
        this.modifier = modifier;
        this.prevVersion = prevParams.getVersion();
        this.prevState = prevParams.getState();
        this.prevArtifactUuid = prevParams.getArtifactUuid();
        this.currVersion = currParams.getVersion();
        this.currState = currParams.getState();
        this.currArtifactUuid = currParams.getArtifactUuid();
        this.artifactData = artifactData;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_SERVICE_INSTANCE_ID.getDisplayName(), getServiceInstanceId());
        fields.put(AuditingFieldsKey.AUDIT_INVARIANT_UUID.getDisplayName(), getInvariantUuid());
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_STATUS.getDisplayName(), getStatus());
        fields.put(AuditingFieldsKey.AUDIT_DESC.getDisplayName(), getDesc());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(Date.from(timestamp1)));
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName(), getConsumerId());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_RESOURCE_URL.getDisplayName(), getResourceURL());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
        fields.put(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_PREV_VERSION.getDisplayName(), getPrevVersion());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION.getDisplayName(), getCurrVersion());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_PREV_STATE.getDisplayName(), getPrevState());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE.getDisplayName(), getCurrState());
        fields.put(AuditingFieldsKey.AUDIT_PREV_ARTIFACT_UUID.getDisplayName(), getPrevArtifactUuid());
        fields.put(AuditingFieldsKey.AUDIT_CURR_ARTIFACT_UUID.getDisplayName(), getCurrArtifactUuid());
        fields.put(AuditingFieldsKey.AUDIT_ARTIFACT_DATA.getDisplayName(), getArtifactData());
    }

    @Override
    public String toString() {
        return "ExternalApiEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", action=" + action + ", status=" + status
            + ", desc=" + desc + ", consumerId=" + consumerId + ", resourceURL=" + resourceURL + ", resourceName=" + resourceName + ", resourceType="
            + resourceType + ", serviceInstanceId=" + serviceInstanceId + ", invariantUuid=" + invariantUuid + ", modifier=" + modifier
            + ", prevVersion=" + prevVersion + ", currVersion=" + currVersion + ", prevState=" + prevState + ", currState=" + currState
            + ", prevArtifactUuid=" + prevArtifactUuid + ", currArtifactUuid=" + currArtifactUuid + ", artifactData=" + artifactData + "]";
    }
}

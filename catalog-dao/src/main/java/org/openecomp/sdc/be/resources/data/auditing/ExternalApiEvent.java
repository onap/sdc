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
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * This class Represents the Audit for External API 
 *
 */
@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.EXTERNAL_API_EVENT_TYPE)
public class ExternalApiEvent extends AuditingGenericEvent {
    @PartitionKey
    protected UUID timebaseduuid;

    @ClusteringColumn()
    protected Date timestamp1;

    @Column
    protected String action;
    @Column
    protected String status;

    @Column(name = "description")
    protected String desc;

    @Column(name = "consumer_id")
    private String consumerId;

    @Column(name = "resource_url")
    private String resourceURL;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "service_instance_id")
    protected String serviceInstanceId;

    @Column(name = "invariant_uuid")
    protected String invariantUuid;

    @Column(name = "modifier")
    private String modifier;

    @Column(name = "prev_version")
    protected String prevVersion;

    @Column(name = "curr_version")
    private String currVersion;

    @Column(name = "prev_state")
    protected String prevState;

    @Column(name = "curr_state")
    protected String currState;

    @Column(name = "prev_artifact_uuid")
    private String prevArtifactUuid;

    @Column(name = "curr_artifact_uuid")
    private String currArtifactUuid;

    @Column(name = "artifact_data")
    private String artifactData;

    //Required to be public as it is used by Cassandra driver on get operation
    public ExternalApiEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public ExternalApiEvent(String action, CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                            DistributionData distributionData, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                            String modifier, String invariantUuid, String artifactData) {
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
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
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

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getInvariantUuid() {
        return invariantUuid;
    }

    public void setInvariantUuid(String invariantUuid) {
        this.invariantUuid = invariantUuid;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getPrevArtifactUuid() {
        return prevArtifactUuid;
    }

    public void setPrevArtifactUuid(String prevArtifactUuid) {
        this.prevArtifactUuid = prevArtifactUuid;
    }

    public String getCurrArtifactUuid() {
        return currArtifactUuid;
    }

    public void setCurrArtifactUuid(String currArtifactUuid) {
        this.currArtifactUuid = currArtifactUuid;
    }

    public String getArtifactData() {
        return artifactData;
    }

    public void setArtifactData(String artifactData) {
        this.artifactData = artifactData;
    }

    @Override
    public String toString() {
        return "ExternalApiEvent [timebaseduuid=" + timebaseduuid + ", timestamp1=" + timestamp1 + ", action=" + action
                + ", status=" + status + ", desc=" + desc + ", consumerId=" + consumerId + ", resourceURL="
                + resourceURL + ", resourceName=" + resourceName + ", resourceType=" + resourceType
                + ", serviceInstanceId=" + serviceInstanceId + ", invariantUuid=" + invariantUuid + ", modifier=" + modifier
                + ", prevVersion=" + prevVersion+ ", currVersion=" + currVersion
                + ", prevState=" + prevState + ", currState=" + currState
                + ", prevArtifactUuid="
                + prevArtifactUuid + ", currArtifactUuid=" + currArtifactUuid + ", artifactData=" + artifactData + "]";
    }

    public String getPrevVersion() {
        return prevVersion;
    }

    public void setPrevVersion(String prevVersion) {
        this.prevVersion = prevVersion;
    }

    public String getCurrVersion() {
        return currVersion;
    }

    public void setCurrVersion(String currVersion) {
        this.currVersion = currVersion;
    }

    public String getPrevState() {
        return prevState;
    }

    public void setPrevState(String prevState) {
        this.prevState = prevState;
    }

    public String getCurrState() {
        return currState;
    }

    public void setCurrState(String currState) {
        this.currState = currState;
    }
}

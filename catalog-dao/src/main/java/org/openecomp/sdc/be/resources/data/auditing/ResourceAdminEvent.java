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
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

@Getter
@Setter
@ToString
@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.RESOURCE_ADMIN_EVENT_TYPE)
public class ResourceAdminEvent extends AuditingGenericEvent {

    @PartitionKey
    protected UUID timebaseduuid;
    @ClusteringColumn
    protected Date timestamp1;
    @Column
    protected String action;
    @Column(name = "resource_type")
    protected String resourceType;
    @Column(name = "prev_version")
    protected String prevVersion;
    @Column(name = "prev_state")
    protected String prevState;
    @Column(name = "curr_state")
    protected String currState;
    @Column(name = "request_id")
    protected String requestId;
    @Column(name = "service_instance_id")
    protected String serviceInstanceId;
    @Column
    protected String status;
    @Column(name = "description")
    protected String desc;
    @Column
    protected String modifier;
    @Column(name = "prev_artifact_UUID")
    protected String prevArtifactUUID;
    @Column(name = "curr_artifact_UUID")
    protected String currArtifactUUID;
    @Column(name = "artifact_data")
    protected String artifactData;
    @Column
    protected String did;
    @Column(name = "dprev_status")
    protected String dprevStatus;
    @Column(name = "dcurr_status")
    protected String dcurrStatus;
    @Column(name = "tosca_node_type")
    protected String toscaNodeType;
    @Column
    protected String comment;
    @Column(name = "invariant_UUID")
    protected String invariantUUID;
    @Column(name = "resource_name")
    private String resourceName;
    @Column(name = "curr_version")
    private String currVersion;

    public ResourceAdminEvent() {
        timestamp1 = new Date();
        timebaseduuid = UUIDs.timeBased();
    }

    public ResourceAdminEvent(String action, CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams,
                              ResourceVersionInfo currParams, String invariantUuid, String modifier, String artifactData, String comment, String did,
                              String toscaNodeType) {
        this();
        this.action = action;
        this.requestId = commonAuditData.getRequestId();
        this.desc = commonAuditData.getDescription();
        this.status = commonAuditData.getStatus();
        this.serviceInstanceId = commonAuditData.getServiceInstanceId();
        this.currState = currParams.getState();
        this.currVersion = currParams.getVersion();
        this.currArtifactUUID = currParams.getArtifactUuid();
        this.prevState = prevParams.getState();
        this.prevVersion = prevParams.getVersion();
        this.prevArtifactUUID = prevParams.getArtifactUuid();
        this.resourceName = resourceCommonInfo.getResourceName();
        this.resourceType = resourceCommonInfo.getResourceType();
        this.comment = comment;
        this.dcurrStatus = currParams.getDistributionStatus();
        this.dprevStatus = prevParams.getDistributionStatus();
        this.artifactData = artifactData;
        this.modifier = modifier;
        this.invariantUUID = invariantUuid;
        this.did = did;
        this.toscaNodeType = toscaNodeType;
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
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_TYPE.getDisplayName(), getResourceType());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_PREV_VERSION.getDisplayName(), getPrevVersion());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_PREV_STATE.getDisplayName(), getPrevState());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_NAME.getDisplayName(), getResourceName());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_VERSION.getDisplayName(), getCurrVersion());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_CURR_STATE.getDisplayName(), getCurrState());
        fields.put(AuditingFieldsKey.AUDIT_MODIFIER_UID.getDisplayName(), getModifier());
        fields.put(AuditingFieldsKey.AUDIT_PREV_ARTIFACT_UUID.getDisplayName(), getPrevArtifactUUID());
        fields.put(AuditingFieldsKey.AUDIT_CURR_ARTIFACT_UUID.getDisplayName(), getCurrArtifactUUID());
        fields.put(AuditingFieldsKey.AUDIT_ARTIFACT_DATA.getDisplayName(), getArtifactData());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_COMMENT.getDisplayName(), getComment());
        fields.put(AuditingFieldsKey.AUDIT_DISTRIBUTION_ID.getDisplayName(), getDid());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_DCURR_STATUS.getDisplayName(), getDcurrStatus());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_DPREV_STATUS.getDisplayName(), getDprevStatus());
        fields.put(AuditingFieldsKey.AUDIT_RESOURCE_TOSCA_NODE_TYPE.getDisplayName(), getToscaNodeType());
        fields.put(AuditingFieldsKey.AUDIT_INVARIANT_UUID.getDisplayName(), getInvariantUUID());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), timestamp1.getTime());
    }
}

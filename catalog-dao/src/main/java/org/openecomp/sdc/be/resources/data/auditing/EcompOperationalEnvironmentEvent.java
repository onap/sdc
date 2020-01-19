/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.ECOMP_OPERATIONAL_ENV_EVENT_TYPE)
public class EcompOperationalEnvironmentEvent  extends AuditingGenericEvent {

    @PartitionKey
    @Column(name = "operational_environment_id")
    protected String operationalEnvironmentId;

    @ClusteringColumn
    protected Date timestamp1;

    @Column
    protected String action;

    @Column(name = "operational_environment_action")
    protected String operationalEnvironmentAction;

    @Column(name = "operational_environment_name")
    protected String operationalEnvironmentName;

    @Column(name = "operational_environment_type")
    protected String operationalEnvironmentType;

    @Column(name = "tenant_context")
    protected String tenantContext;


    //Required to be public as it is used by Cassandra driver on get operation
    public EcompOperationalEnvironmentEvent() {
        timestamp1 = new Date();
    }

    public EcompOperationalEnvironmentEvent(String action, String operationalEnvironmentId, String operationalEnvironmentName,
                                            String operationalEnvironmentType, String operationalEnvironmentAction, String tenantContext) {
        this();
        this.action = action;
        this.operationalEnvironmentId = operationalEnvironmentId;
        this.operationalEnvironmentType = operationalEnvironmentType;
        this.operationalEnvironmentName = operationalEnvironmentName;
        this.operationalEnvironmentAction = operationalEnvironmentAction;
        this.tenantContext = tenantContext;
    }

    public String getOperationalEnvironmentId() {
        return operationalEnvironmentId;
    }

    public void setOperationalEnvironmentId(String operationalEnvironmentId) {
        this.operationalEnvironmentId = operationalEnvironmentId;
    }

    public String getOperationalEnvironmentAction() {
        return operationalEnvironmentAction;
    }

    public void setOperationalEnvironmentAction(String operationalEnvironmentAction) {
        this.operationalEnvironmentAction = operationalEnvironmentAction;
    }

    public String getOperationalEnvironmentName() {
        return operationalEnvironmentName;
    }

    public void setOperationalEnvironmentName(String operationalEnvironmentName) {
        this.operationalEnvironmentName = operationalEnvironmentName;
    }

    public String getOperationalEnvironmentType() {
        return operationalEnvironmentType;
    }

    public void setOperationalEnvironmentType(String operationalEnvironmentType) {
        this.operationalEnvironmentType = operationalEnvironmentType;
    }

    public String getTenantContext() {
        return tenantContext;
    }

    public void setTenantContext(String tenantContext) {
        this.tenantContext = tenantContext;
    }

    public Date getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(Date timestamp) {
        this.timestamp1 = timestamp;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void fillFields() {
        fields.put(AuditingFieldsKey.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKey.AUDIT_OPERATIONAL_ENVIRONMENT_ID.getDisplayName(), getOperationalEnvironmentId());
        fields.put(AuditingFieldsKey.AUDIT_OPERATIONAL_ENVIRONMENT_NAME.getDisplayName(), getOperationalEnvironmentName());
        fields.put(AuditingFieldsKey.AUDIT_OPERATIONAL_ENVIRONMENT_TYPE.getDisplayName(), getOperationalEnvironmentType());
        fields.put(AuditingFieldsKey.AUDIT_OPERATIONAL_ENVIRONMENT_ACTION.getDisplayName(), getOperationalEnvironmentAction());
        fields.put(AuditingFieldsKey.AUDIT_TENANT_CONTEXT.getDisplayName(), getTenantContext());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }

    @Override
    public String toString() {
        return "EcompOperationalEnvironmentEvent [timestamp1=" + timestamp1 + ", action = " + action
                + ", operational_environment_id=" + operationalEnvironmentId + ", operational_environment_name=" + operationalEnvironmentName
                + ", operational_environment_type=" + operationalEnvironmentType + ", operational_environment_action=" + operationalEnvironmentAction
                + ", tenant_context=" + tenantContext + "]";
    }

}

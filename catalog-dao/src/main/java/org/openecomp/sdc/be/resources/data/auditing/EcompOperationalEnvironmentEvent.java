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


import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;


@Getter
@Setter
@ToString
@Entity(defaultKeyspace = AuditingTypesConstants.AUDIT_KEYSPACE)
@CqlName(AuditingTypesConstants.ECOMP_OPERATIONAL_ENV_EVENT_TYPE)
public class EcompOperationalEnvironmentEvent extends AuditingGenericEvent {

    @PartitionKey
    @CqlName("operational_environment_id")
    protected String operationalEnvironmentId;
    @ClusteringColumn
    protected Instant timestamp1;
    @CqlName("action")
    protected String action;
    @CqlName("operational_environment_action")
    protected String operationalEnvironmentAction;
    @CqlName("operational_environment_name")
    protected String operationalEnvironmentName;
    @CqlName("operational_environment_type")
    protected String operationalEnvironmentType;
    @CqlName("tenant_context")
    protected String tenantContext;

    //Required to be public as it is used by Cassandra driver on get operation
    public EcompOperationalEnvironmentEvent() {
        timestamp1 = Instant.now();
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
        fields.put(AuditingFieldsKey.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(Date.from(timestamp1)));
    }
}

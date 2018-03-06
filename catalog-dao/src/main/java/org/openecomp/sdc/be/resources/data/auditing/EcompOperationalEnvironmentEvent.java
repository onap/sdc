package org.openecomp.sdc.be.resources.data.auditing;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@Table(keyspace = AuditingTypesConstants.AUDIT_KEYSPACE, name = AuditingTypesConstants.ECOMP_OPERATIONAL_ENV_EVENT_TYPE)
public class EcompOperationalEnvironmentEvent  extends AuditingGenericEvent {

    @PartitionKey
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


    public EcompOperationalEnvironmentEvent() {
        super();
        timestamp1 = new Date();
    }

    public EcompOperationalEnvironmentEvent(Map<AuditingFieldsKeysEnum, Object> auditingFields) {
        this();
        Object value;

        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_ACTION);
        if (value != null) {
            setAction((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ID);
        if (value != null) {
            setOperationalEnvironmentId((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_NAME);
        if (value != null) {
            setOperationalEnvironmentName((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_TYPE);
        if (value != null) {
            setOperational_environment_type((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ACTION);
        if (value != null) {
            setOperationalEnvironmentAction((String) value);
        }
        value = auditingFields.get(AuditingFieldsKeysEnum.AUDIT_TENANT_CONTEXT);
        if (value != null) {
            setTenantContext((String) value);
        }
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

    public void setOperational_environment_type(String operationalEnvironmentType) {
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
        fields.put(AuditingFieldsKeysEnum.AUDIT_ACTION.getDisplayName(), getAction());
        fields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ID.getDisplayName(), getOperationalEnvironmentId());
        fields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_NAME.getDisplayName(), getOperationalEnvironmentName());
        fields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_TYPE.getDisplayName(), getOperationalEnvironmentType());
        fields.put(AuditingFieldsKeysEnum.AUDIT_OPERATIONAL_ENVIRONMENT_ACTION.getDisplayName(), getOperationalEnvironmentAction());
        fields.put(AuditingFieldsKeysEnum.AUDIT_TENANT_CONTEXT.getDisplayName(), getTenantContext());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP.getDisplayName(), simpleDateFormat.format(timestamp1));
    }

    @Override
    public String toString() {
        return "EcompOperationalEnvironmentEvent [timestamp1=" + timestamp1 + ", action = " + action
                + ", operational_environment_id=" + operationalEnvironmentId + ", operational_environment_name=" + operationalEnvironmentName
                + ", operational_environment_type=" + operationalEnvironmentType + ", operational_environment_action=" + operationalEnvironmentAction
                + ", tenant_context=" + tenantContext + "]";
    }

}

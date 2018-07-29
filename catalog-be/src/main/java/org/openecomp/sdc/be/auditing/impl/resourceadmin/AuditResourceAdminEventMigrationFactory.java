package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditResourceAdminEventMigrationFactory extends AuditResourceAdminEventFactory {

    public AuditResourceAdminEventMigrationFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                   ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid,
                                                   String modifier, String artifactData, String comment, String did,
                                                   String toscaNodeType, String timestamp) {
        super(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier,
                artifactData, comment, did, toscaNodeType, timestamp);
    }

    @Override
    public String getLogPattern() {
        return "";
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[0];
    }
}

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditArtifactResourceAdminEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditCertificationResourceAdminEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditCreateUpdateResourceAdminEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditDistStateChangeRequestResourceAdminEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditDistStateChangeResourceAdminEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditImportResourceAdminEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditResourceEventFactoryMananger {


    public static AuditBaseEventFactory createResourceEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceAuditData prevParams,
                                                                   ResourceAuditData currParams, String resourceType, String resourceName, String invariantUuid,
                                                                   User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        AuditBaseEventFactory factory = null;

        switch (action) {
            case IMPORT_RESOURCE:
                factory = new AuditImportResourceAdminEventFactory(commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did, toscaNodeType);
                break;
            case CREATE_RESOURCE:
            case UPDATE_RESOURCE_METADATA:
                factory = new AuditCreateUpdateResourceAdminEventFactory(action, commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did, org.openecomp.sdc.common.api.Constants.EMPTY_STRING);
                break;
            case CHECKIN_RESOURCE:
            case CHECKOUT_RESOURCE:
            case UNDO_CHECKOUT_RESOURCE:
            case CERTIFICATION_REQUEST_RESOURCE:
            case START_CERTIFICATION_RESOURCE:
            case CERTIFICATION_SUCCESS_RESOURCE:
            case FAIL_CERTIFICATION_RESOURCE:
            case CANCEL_CERTIFICATION_RESOURCE:
                factory = new AuditCertificationResourceAdminEventFactory(action, commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did);
                break;
            case ARTIFACT_UPLOAD:
            case ARTIFACT_DELETE:
            case ARTIFACT_METADATA_UPDATE:
            case ARTIFACT_PAYLOAD_UPDATE:
            case ARTIFACT_DOWNLOAD:
                factory = new AuditArtifactResourceAdminEventFactory(action, commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did);
                break;
            case DISTRIBUTION_STATE_CHANGE_REQUEST:
                factory = new AuditDistStateChangeRequestResourceAdminEventFactory(commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did);
                break;
            case DISTRIBUTION_STATE_CHANGE_APPROV:
            case DISTRIBUTION_STATE_CHANGE_REJECT:
                factory = new AuditDistStateChangeResourceAdminEventFactory(action, commonFields, prevParams, currParams,
                        resourceType, resourceName, invariantUuid,
                        modifier, artifactData, comment, did);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return factory;
    }
}

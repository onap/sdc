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
package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditResourceEventFactoryManager {

    /**
     * Utility classes, which are collections of static members, are not meant to be instantiated
     */
    private AuditResourceEventFactoryManager() {
    }

    public static AuditEventFactory createResourceEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                                               ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams,
                                                               ResourceVersionInfo currParams, String invariantUuid, User modifier,
                                                               String artifactData, String comment, String did, String toscaNodeType) {
        AuditBaseEventFactory factory = null;
        switch (action) {
            case IMPORT_RESOURCE:
                factory = new AuditImportResourceAdminEventFactory(commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier,
                    artifactData, comment, did, toscaNodeType);
                break;
            case CREATE_RESOURCE:
            case CREATE_SERVICE:
            case UPDATE_RESOURCE_METADATA:
            case ARCHIVE_COMPONENT:
            case RESTORE_COMPONENT:
            case UPDATE_SERVICE_METADATA:
                factory = new AuditCreateUpdateResourceAdminEventFactory(action, commonFields, resourceCommonInfo, prevParams, currParams,
                    invariantUuid, modifier, artifactData, comment, did, org.openecomp.sdc.common.api.Constants.EMPTY_STRING);
                break;
            case CHECKIN_RESOURCE:
            case CHECKOUT_RESOURCE:
            case UNDO_CHECKOUT_RESOURCE:
            case CERTIFICATION_REQUEST_RESOURCE:
            case START_CERTIFICATION_RESOURCE:
            case CERTIFICATION_SUCCESS_RESOURCE:
            case FAIL_CERTIFICATION_RESOURCE:
            case CANCEL_CERTIFICATION_RESOURCE:
            case UPDATE_SERVICE_REFERENCE:
            case VF_UPGRADE_SERVICES:
                factory = new AuditCertificationResourceAdminEventFactory(action, commonFields, resourceCommonInfo, prevParams, currParams,
                    invariantUuid, modifier, artifactData, comment, did);
                break;
            case ARTIFACT_UPLOAD:
            case ARTIFACT_DELETE:
            case ARTIFACT_METADATA_UPDATE:
            case ARTIFACT_PAYLOAD_UPDATE:
            case ARTIFACT_DOWNLOAD:
                factory = new AuditArtifactResourceAdminEventFactory(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid,
                    modifier, artifactData, comment, did);
                break;
            case DISTRIBUTION_STATE_CHANGE_REQUEST:
                factory = new AuditDistStateChangeRequestResourceAdminEventFactory(commonFields, resourceCommonInfo, prevParams, currParams,
                    invariantUuid, modifier, artifactData, comment, did);
                break;
            case DISTRIBUTION_STATE_CHANGE_APPROV:
            case DISTRIBUTION_STATE_CHANGE_REJECT:
                factory = new AuditDistStateChangeResourceAdminEventFactory(action, commonFields, resourceCommonInfo, prevParams, currParams,
                    invariantUuid, modifier, artifactData, comment, did);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return factory;
    }
}

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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.info;

import org.openecomp.sdc.common.datastructure.AuditingFieldsKey;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;

public class DistributionStatusInfo {

    private String omfComponentID;
    private String timestamp;
    private String url;
    private String status;
    private String errorReason;

    DistributionStatusInfo() {
    }

    public DistributionStatusInfo(ESTimeBasedEvent distributionStatusEvent) {
        omfComponentID = String.valueOf(distributionStatusEvent.getFields().get(AuditingFieldsKey.AUDIT_DISTRIBUTION_CONSUMER_ID.getDisplayName()));
        timestamp = String.valueOf(distributionStatusEvent.getFields().get(AuditingFieldsKey.AUDIT_DISTRIBUTION_STATUS_TIME.getDisplayName()));
        url = String.valueOf(distributionStatusEvent.getFields().get(AuditingFieldsKey.AUDIT_DISTRIBUTION_RESOURCE_URL.getDisplayName()));
        status = String.valueOf(distributionStatusEvent.getFields().get(AuditingFieldsKey.AUDIT_STATUS.getDisplayName()));
        errorReason = String.valueOf(distributionStatusEvent.getFields().get(AuditingFieldsKey.AUDIT_DESC.getDisplayName()));
    }

    public DistributionStatusInfo(String omfComponentID, String timestamp, String url, String status) {
        this.omfComponentID = omfComponentID;
        this.timestamp = timestamp;
        this.url = url;
        this.status = status;
    }

    public String getOmfComponentID() {
        return omfComponentID;
    }

    public void setOmfComponentID(String omfComponentID) {
        this.omfComponentID = omfComponentID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

}

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
package org.openecomp.sdc.be.resources.data.auditing.model;

public class CommonAuditData {

    private String description;
    private String requestId;
    private String serviceInstanceId;
    private String status;

    private CommonAuditData() {
        //for builder
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public static class Builder {

        private final CommonAuditData instance;

        private Builder() {
            instance = new CommonAuditData();
        }

        public Builder description(String description) {
            instance.description = description;
            return this;
        }

        public Builder status(int status) {
            instance.status = String.valueOf(status);
            return this;
        }

        public Builder status(String status) {
            instance.status = status;
            return this;
        }

        public Builder requestId(String requestId) {
            instance.requestId = requestId;
            return this;
        }

        public Builder serviceInstanceId(String serviceInstanceId) {
            instance.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public CommonAuditData build() {
            return instance;
        }
    }
}

/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;

public class AuditDistributionNotificationBuilder {

    private String topicName;
    private String distributionId;
    private CambriaErrorResponse status;
    private Service service;
    private String envId;
    private User modifier;
    private String workloadContext;
    private String tenant;

    public AuditDistributionNotificationBuilder() {}

    public String getTopicName() {
        return topicName;
    }

    public AuditDistributionNotificationBuilder setTopicName(String topicName) {
        this.topicName = topicName;
        return this;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public AuditDistributionNotificationBuilder setDistributionId(String distributionId) {
        this.distributionId = distributionId;
        return this;
    }

    public CambriaErrorResponse getStatus() {
        return status;
    }

    public AuditDistributionNotificationBuilder setStatus(CambriaErrorResponse status) {
        this.status = status;
        return this;
    }

    public Service getService() {
        return service;
    }

    public AuditDistributionNotificationBuilder setService(Service service) {
        this.service = service;
        return this;
    }

    public String getEnvId() {
        return envId;
    }

    public AuditDistributionNotificationBuilder setEnvId(String envId) {
        this.envId = envId;
        return this;
    }

    public User getModifier() {
        return modifier;
    }

    public AuditDistributionNotificationBuilder setModifier(User modifier) {
        this.modifier = modifier;
        return this;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public AuditDistributionNotificationBuilder setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public AuditDistributionNotificationBuilder setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }
}

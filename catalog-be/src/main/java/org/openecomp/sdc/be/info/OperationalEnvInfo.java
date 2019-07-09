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

package org.openecomp.sdc.be.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.io.IOException;

public final class OperationalEnvInfo {
    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    @JsonIgnore
    private static final Logger logger = Logger.getLogger(OperationalEnvInfo.class);
    
    @JsonProperty("operational-environment-id")
    private String operationalEnvId;

    @JsonProperty("operational-environment-name")
    private String operationalEnvName;

    @JsonProperty("operational-environment-type")
    private String operationalEnvType;

    @JsonProperty("operational-environment-status")
    private String operationalEnvStatus;

    @JsonProperty("tenant-context")
    private String tenantContext;

    @JsonProperty("workload-context")
    private String workloadContext;

    @JsonProperty("resource-version")
    private String resourceVersion;

    @JsonProperty("relationship-list")
    private RelationshipList relationships;

    public String getOperationalEnvId() {
        return operationalEnvId;
    }
    
    public void setOperationalEnvId(String operationalEnvId) {
        this.operationalEnvId = operationalEnvId;
    }

    public String getOperationalEnvName() {
        return operationalEnvName;
    }

    public void setOperationalEnvName(String operationalEnvName) {
        this.operationalEnvName = operationalEnvName;
    }

    public String getOperationalEnvType() {
        return operationalEnvType;
    }

    public void setOperationalEnvType(String operationalEnvType) {
        this.operationalEnvType = operationalEnvType;
    }

    public String getOperationalEnvStatus() {
        return operationalEnvStatus;
    }

    public void setOperationalEnvStatus(String operationalEnvStatus) {
        this.operationalEnvStatus = operationalEnvStatus;
    }

    public String getTenantContext() {
        return tenantContext;
    }

    public void setTenantContext(String tenantContext) {
        this.tenantContext = tenantContext;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public RelationshipList getRelationships() {
        return relationships;
    }

    public void setRelationships(RelationshipList relationships) {
        this.relationships = relationships;
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            logger.debug("Convert object to string failed with exception. ", e);
            return StringUtils.EMPTY;
        }
    }
    
    public static OperationalEnvInfo createFromJson(String json) throws IOException {
        return objectMapper.readValue(json, OperationalEnvInfo.class);
    }
    
}

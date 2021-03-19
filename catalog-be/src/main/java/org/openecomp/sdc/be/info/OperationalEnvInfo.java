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
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;

@Getter
@Setter
public final class OperationalEnvInfo {

    @JsonIgnore
    private static final Logger logger = Logger.getLogger(OperationalEnvInfo.class);
    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();
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

    public static OperationalEnvInfo createFromJson(String json) throws IOException {
        return objectMapper.readValue(json, OperationalEnvInfo.class);
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.debug("Convert object to string failed with exception. ", e);
            return StringUtils.EMPTY;
        }
    }
}

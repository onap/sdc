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
 */

package org.openecomp.sdc.be.info;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceVersionInfo {
    private String version;
    private String url;

    public ServiceVersionInfo(String serviceName, String serviceVersion, String context) {
        super();
        this.version = serviceVersion;
        StringBuilder sb = new StringBuilder(context);
        sb.append("services/").append(serviceName).append("/").append(serviceVersion);
        url = sb.toString();
    }


    private String artifactUuid;
    private String state;
    private String distributionStatus;


    public static Builder newBuilder() {
        return new Builder();
    }

    public String getArtifactUuid() {
        return artifactUuid;
    }
    public String getState() {
        return state;
    }
    public String getDistributionStatus() { return distributionStatus; }


    public static class Builder {
        private final ServiceVersionInfo instance;

        private Builder() {
            instance = new ServiceVersionInfo();
        }

        public Builder artifactUuid(String artifactUuid) {
            instance.artifactUuid = artifactUuid;
            return this;
        }

        public Builder state(String state) {
            instance.state = state;
            return this;
        }

        public Builder version(String version) {
            instance.version = version;
            return this;
        }

        public Builder distributionStatus(String distributionStatus) {
            instance.distributionStatus = distributionStatus;
            return this;
        }

        public ServiceVersionInfo build() {
            return instance;
        }
    }
}

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
package org.openecomp.sdc.be.resources.data;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;


@Getter
@Setter
@Entity(defaultKeyspace = "sdcrepository")
@CqlName("operationalEnvironment")
public class OperationalEnvironmentEntry {

    @PartitionKey(0)
    @CqlName("environment_id")
    private String environmentId;
    @CqlName("tenant")
    private String tenant;
    @CqlName("is_production")
    private Boolean isProduction;
    @CqlName("ecomp_workload_context")
    private String ecompWorkloadContext;
    @CqlName("dmaap_ueb_address")
    private Set<String> dmaapUebAddress;
    @CqlName("ueb_api_key")
    private String uebApikey;
    @CqlName("ueb_secret_key")
    private String uebSecretKey;
    @CqlName("status")
    private String status;
    @CqlName("last_modified")
    private Instant lastModified;

    public void setStatus(EnvironmentStatusEnum status) {
        this.status = status.getName();
    }

    public void setStatus(String status) {
        //log if status doesn't exists in EnvironmentStatusEnum
        this.status = status;
    }

    public void addDmaapUebAddress(String address) {
        if (this.dmaapUebAddress == null) {
            this.dmaapUebAddress = new HashSet<>();
        }
        dmaapUebAddress.add(address);
    }

    @Override
    public String toString() {
        return "OperationalEnvironmentEntry [environmentId=" + environmentId + ", tenant=" + tenant + ", isProduction=" + isProduction
            + ", ecompWorkloadContext=" + ecompWorkloadContext + ", dmaapUebAddress=" + dmaapUebAddress + ", uebApikey=" + uebApikey + ", status="
            + status + ", lastModified=" + lastModified + "]";
    }
}

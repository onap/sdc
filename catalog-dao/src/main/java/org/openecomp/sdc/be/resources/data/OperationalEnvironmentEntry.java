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

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Table(keyspace = "sdcrepository", name = "operationalEnvironment")
public class OperationalEnvironmentEntry {

    @PartitionKey(0)
    @Column(name = "environment_id")
    private String environmentId;

    @Column(name = "tenant")
    private String tenant;

    @Column(name = "is_production")
    private Boolean isProduction;

    @Column(name = "ecomp_workload_context")
    private String ecompWorkloadContext;

    @Column(name = "dmaap_ueb_address")
    private Set<String> dmaapUebAddress;

    @Column(name = "ueb_api_key")
    private String uebApikey;

    @Column(name = "ueb_secret_key")
    private String uebSecretKey;

    @Column(name = "status")
    private String status;

    @Column(name = "last_modified")
    private Date lastModified;


    public void setStatus(EnvironmentStatusEnum status) {
        this.status = status.getName();
    }

    public void addDmaapUebAddress(String address) {
        if ( this.dmaapUebAddress == null )
            this.dmaapUebAddress = new HashSet<>();
        dmaapUebAddress.add(address);
    }

    @Override
	public String toString() {
		return "OperationalEnvironmentEntry [environmentId=" + environmentId + ", tenant=" + tenant + ", isProduction="
				+ isProduction + ", ecompWorkloadContext=" + ecompWorkloadContext + ", dmaapUebAddress="
				+ dmaapUebAddress + ", uebApikey=" + uebApikey + ", status=" + status
				+ ", lastModified=" + lastModified + "]";
	}


}

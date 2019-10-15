/**
 * Copyright (c) 2019 Vodafone Group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.Data;

@Data
@Table(keyspace = "zusammen_dox", name = "vtp_results")
public class VtpResultsEntity {

    @PartitionKey
    @Column(name = "id")
    private String id;

    @Column(name = "vsp_id")
    private String vspId;


    @Column(name = "vsp_version")
    private String vspVersion;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "endpoint_name")
    private String endPointName;

    public VtpResultsEntity() {
        // Don't delete! Default constructor is required by DataStax driver
    }
}

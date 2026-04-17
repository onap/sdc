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


import java.util.Date;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(defaultKeyspace = "sdcrepository")
@CqlName("migrationTasks")
public class MigrationTaskEntry {

    @PartitionKey(0)
    @CqlName("major_version")
    private Long majorVersion;
    @ClusteringColumn
    @CqlName("minor_version")
    private Long minorVersion;
    @CqlName("timestamp")
    private Date timestamp;
    @CqlName("task_name")
    private String taskName;
    @CqlName("execution_time")
    private double executionTime;
    @CqlName("task_status")
    private String taskStatus;
    @CqlName("msg")
    private String message;
    @CqlName("description")
    private String description;
}

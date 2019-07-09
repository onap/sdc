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

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Date;

@Table(keyspace = "sdcrepository", name = "migrationTasks")
public class MigrationTaskEntry {

    @PartitionKey(0)
    @Column(name = "major_version")
    private Long majorVersion;

    @ClusteringColumn
    @Column(name = "minor_version")
    private Long minorVersion;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "execution_time")
    private double executionTime;

    @Column(name = "task_status")
    private String taskStatus;

    @Column(name = "msg")
    private String message;

    @Column(name = "description")
    private String description;

    public void setMajorVersion(Long majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void setMinorVersion(Long minorVersion) {
        this.minorVersion = minorVersion;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public Long getMajorVersion() {
        return majorVersion;
    }

    public Long getMinorVersion() {
        return minorVersion;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public String getMessage() {
        return message;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

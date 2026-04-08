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
package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.core.PagingIterable;

import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;

@Dao
public interface AuditDao {

    // ***** distributionstatusevent table
    @Query("SELECT * FROM sdcaudit.distributionstatusevent WHERE DID = :did")
    PagingIterable<DistributionStatusEvent> getListOfDistributionStatuses(@CqlName("did") String did);

    @Query("SELECT * FROM sdcaudit.distributionstatusevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId ALLOW FILTERING")
    PagingIterable<DistributionStatusEvent> getDistributionStatusByServiceInstanceId(@CqlName("serviceInstanceId") String serviceInstanceId);

    // ***** resourceadminevent table
    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DRequest' ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getServiceDistributionStatus(@CqlName("serviceInstanceId") String serviceInstanceId);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId")
    PagingIterable<ResourceAdminEvent> getByServiceInstanceId(@CqlName("serviceInstanceId") String serviceInstanceId);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND PREV_VERSION = :prevVersion ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getAuditByServiceIdAndPrevVersion(
            @CqlName("serviceInstanceId") String serviceInstanceId,
            @CqlName("prevVersion") String prevVersion);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE DID = :did AND ACTION = :action ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getDistributionRequest(
            @CqlName("did") String did,
            @CqlName("action") String action);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND CURR_VERSION = :currVersion ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getAuditByServiceIdAndCurrVersion(
            @CqlName("serviceInstanceId") String serviceInstanceId,
            @CqlName("currVersion") String currVersion);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'ArchiveComponent' ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getArchiveAuditByServiceInstanceId(@CqlName("serviceInstanceId") String serviceInstanceId);

    @Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'RestoreComponent' ALLOW FILTERING")
    PagingIterable<ResourceAdminEvent> getRestoreAuditByServiceInstanceId(@CqlName("serviceInstanceId") String serviceInstanceId);

    // ***** distributiondeployevent table
    @Query("SELECT * FROM sdcaudit.distributiondeployevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DResult' ALLOW FILTERING")
    PagingIterable<DistributionDeployEvent> getServiceDistributionDeploy(@CqlName("serviceInstanceId") String serviceInstanceId);

    @Query("SELECT * FROM sdcaudit.distributiondeployevent WHERE DID = :did AND ACTION = :action AND STATUS = :status ALLOW FILTERING")
    PagingIterable<DistributionDeployEvent> getDistributionDeployByStatus(
            @CqlName("did") String did,
            @CqlName("action") String action,
            @CqlName("status") String status);

    // ***** distributionnotificationevent table
    @Query("SELECT * FROM sdcaudit.distributionnotificationevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DNotify' ALLOW FILTERING")
    PagingIterable<DistributionNotificationEvent> getServiceDistributionNotify(@CqlName("serviceInstanceId") String serviceInstanceId);

    @Query("SELECT * FROM sdcaudit.distributionnotificationevent WHERE DID = :did AND ACTION = :action ALLOW FILTERING")
    PagingIterable<DistributionNotificationEvent> getDistributionNotify(
            @CqlName("did") String did,
            @CqlName("action") String action);

    @Insert
    void saveDistributionStatusEvent(DistributionStatusEvent event);        

    @Insert
    void saveResourceAdminEvent(ResourceAdminEvent event);

    @Insert
    void saveDistributionDeployEvent(DistributionDeployEvent event);

    @Insert
    void saveDistributionNotificationEvent(DistributionNotificationEvent event);
}

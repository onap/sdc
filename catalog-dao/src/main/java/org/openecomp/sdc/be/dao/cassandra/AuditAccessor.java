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

import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface AuditAccessor {

	// ***** distributionstatusevent table
	@Query("SELECT * FROM sdcaudit.distributionstatusevent WHERE DID = :did AND ACTION = 'DStatus' ALLOW FILTERING")
	Result<DistributionStatusEvent> getListOfDistributionStatuses(@Param("did") String did);

	// ***** resourceadminevent table
	@Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DRequest' ALLOW FILTERING")
	Result<ResourceAdminEvent> getServiceDistributionStatus(@Param("serviceInstanceId") String serviceInstanceId);

	@Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId ")
	Result<ResourceAdminEvent> getByServiceInstanceId(@Param("serviceInstanceId") String serviceInstanceId);

	@Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND PREV_VERSION = :prevVersion ALLOW FILTERING")
	Result<ResourceAdminEvent> getAuditByServiceIdAndPrevVersion(@Param("serviceInstanceId") String serviceInstanceId,
			@Param("prevVersion") String prevVersion);

	@Query("SELECT * FROM sdcaudit.resourceadminevent WHERE DID = :did AND ACTION = :action ALLOW FILTERING")
	Result<ResourceAdminEvent> getDistributionRequest(@Param("did") String did, @Param("action") String action);

	@Query("SELECT * FROM sdcaudit.resourceadminevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND CURR_VERSION = :currVersion ALLOW FILTERING")
	Result<ResourceAdminEvent> getAuditByServiceIdAndCurrVersion(@Param("serviceInstanceId") String serviceInstanceId,
			@Param("currVersion") String currVersion);

	// ***** distributiondeployevent table
	@Query("SELECT * FROM sdcaudit.distributiondeployevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DResult' ALLOW FILTERING")
	Result<DistributionDeployEvent> getServiceDistributionDeploy(@Param("serviceInstanceId") String serviceInstanceId);

	@Query("SELECT * FROM sdcaudit.distributiondeployevent WHERE DID = :did AND ACTION = :action AND STATUS = :status ALLOW FILTERING")
	Result<DistributionDeployEvent> getDistributionDeployByStatus(@Param("did") String did,
			@Param("action") String action, @Param("status") String status);

	// ***** distributionnotificationevent table
	@Query("SELECT * FROM sdcaudit.distributionnotificationevent WHERE SERVICE_INSTANCE_ID = :serviceInstanceId AND ACTION = 'DNotify' ALLOW FILTERING")
	Result<DistributionNotificationEvent> getServiceDistributionNotify(
			@Param("serviceInstanceId") String serviceInstanceId);

	@Query("SELECT * FROM sdcaudit.distributionnotificationevent WHERE DID = :did AND ACTION = :action ALLOW FILTERING")
	Result<DistributionNotificationEvent> getDistributionNotify(@Param("did") String did,
			@Param("action") String action);

}

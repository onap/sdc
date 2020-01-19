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

package org.openecomp.sdc.be.dao.cassandra.schema;

import org.openecomp.sdc.be.dao.cassandra.schema.tables.*;

public enum Table {

    ARTIFACT(new ArtifactTableDescription()),
    USER_ADMIN_EVENT(new UserAdminEventTableDescription()),
    USER_ACCESS_EVENT(new UserAccessEventTableDescription()),
    RESOURCE_ADMIN_EVENT(new ResAdminEventTableDescription()),
    DISTRIBUTION_DOWNLOAD_EVENT(new DistribDownloadEventTableDesc()),
    DISTRIBUTION_ENGINE_EVENT(new DistribEngineEventTableDesc()),
    DISTRIBUTION_NOTIFICATION_EVENT(new DistribNotifEventTableDesc()),
    DISTRIBUTION_STATUS_EVENT(new DistribStatusEventTableDesc()),
    DISTRIBUTION_DEPLOY_EVENT(new DistribDeployEventTableDesc()),
    DISTRIBUTION_GET_UEB_CLUSTER_EVENT(new GetUebClusterEventTableDesc()),
    AUTH_EVENT(new AuthEventTableDescription()),
    CATEGORY_EVENT(new CategoryEventTableDescription()),
    GET_USERS_LIST_EVENT(new GetUsersListEventTableDesc()),
    GET_CATEGORY_HIERARCHY_EVENT(new GetCatHierEventTableDesc()),
    EXTERNAL_API_EVENT(new ExternalApiEventTableDesc()),
    SDC_SCHEMA_FILES(new SdcSchemaFilesTableDescription()),
    SDC_REPO(new MigrationTasksTableDescription()),
    SDC_OPERATIONAL_ENVIRONMENT(new OperationalEnvironmentsTableDescription()),
    AUDIT_ECOMP_OPERATIONAL_ENVIRONMENT(new EcompOperationalEnvironmentEventTableDesc()),
    FEATURE_TOGGLE_STATE(new FeatureToggleEventTableDesc());

	ITableDescription tableDescription;

	Table(ITableDescription tableDescription) {
		this.tableDescription = tableDescription;
	}

	public ITableDescription getTableDescription() {
		return tableDescription;
	}

}

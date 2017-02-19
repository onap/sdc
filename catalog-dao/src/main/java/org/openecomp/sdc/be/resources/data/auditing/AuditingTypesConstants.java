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

package org.openecomp.sdc.be.resources.data.auditing;

public interface AuditingTypesConstants {

	public static final String ARTIFACT_KEYSPACE = "sdcartifact";
	public static final String AUDIT_KEYSPACE = "sdcaudit";
	public static final String COMPONENT_KEYSPACE = "sdccomponent";
	public static final String TITAN_KEYSPACE = "titan";

	public static final String USER_ADMIN_EVENT_TYPE = "useradminevent";
	public static final String USER_ACCESS_EVENT_TYPE = "useraccessevent";
	public static final String RESOURCE_ADMIN_EVENT_TYPE = "resourceadminevent";
	public static final String DISTRIBUTION_DOWNLOAD_EVENT_TYPE = "distributiondownloadevent";

	public static final String DISTRIBUTION_ENGINE_EVENT_TYPE = "distributionengineevent";
	public static final String DISTRIBUTION_NOTIFICATION_EVENT_TYPE = "distributionnotificationevent";
	public static final String DISTRIBUTION_STATUS_EVENT_TYPE = "distributionstatusevent";
	public static final String DISTRIBUTION_DEPLOY_EVENT_TYPE = "distributiondeployevent";
	public static final String DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE = "auditinggetuebclusterevent";
	public static final String DISTRIBUTION_GET_VALID_ARTIFACT_TYPES_EVENT_TYPE = "auditinggetvalidartifacttypesevent";

	public static final String AUTH_EVENT_TYPE = "authevent";
	public static final String CONSUMER_EVENT_TYPE = "consumerevent";
	public static final String CATEGORY_EVENT_TYPE = "categoryevent";
	public static final String GET_USERS_LIST_EVENT_TYPE = "getuserslistevent";
	public static final String GET_CATEGORY_HIERARCHY_EVENT_TYPE = "getcategoryhierarchyevent";
	public static final String EXTERNAL_API_EVENT_TYPE = "externalapievent";

}

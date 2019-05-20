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

    String ARTIFACT_KEYSPACE = "sdcartifact";
    String REPO_KEYSPACE = "sdcrepository";
    String AUDIT_KEYSPACE = "sdcaudit";
    String COMPONENT_KEYSPACE = "sdccomponent";
    String janusGraph_KEYSPACE = "janusgraph";

    String USER_ADMIN_EVENT_TYPE = "useradminevent";
    String USER_ACCESS_EVENT_TYPE = "useraccessevent";
    String RESOURCE_ADMIN_EVENT_TYPE = "resourceadminevent";
    String DISTRIBUTION_DOWNLOAD_EVENT_TYPE = "distributiondownloadevent";

    String DISTRIBUTION_ENGINE_EVENT_TYPE = "distributionengineevent";
    String DISTRIBUTION_NOTIFICATION_EVENT_TYPE = "distributionnotificationevent";
    String DISTRIBUTION_STATUS_EVENT_TYPE = "distributionstatusevent";
    String DISTRIBUTION_DEPLOY_EVENT_TYPE = "distributiondeployevent";
    String DISTRIBUTION_GET_UEB_CLUSTER_EVENT_TYPE = "auditinggetuebclusterevent";
    //TODO remove if not in use
    String DISTRIBUTION_GET_VALID_ARTIFACT_TYPES_EVENT_TYPE = "auditinggetvalidartifacttypesevent";

    String ECOMP_OPERATIONAL_ENV_EVENT_TYPE = "ecompopenvironmentevent";
    String AUTH_EVENT_TYPE = "authevent";
    String CONSUMER_EVENT_TYPE = "consumerevent";
    String CATEGORY_EVENT_TYPE = "categoryevent";
    String GET_USERS_LIST_EVENT_TYPE = "getuserslistevent";
    String GET_CATEGORY_HIERARCHY_EVENT_TYPE = "getcategoryhierarchyevent";
    String EXTERNAL_API_EVENT_TYPE = "externalapievent";
    String ENVIRONMENT_ENGINE_EVENT_TYPE = "environmentengineevent";

}

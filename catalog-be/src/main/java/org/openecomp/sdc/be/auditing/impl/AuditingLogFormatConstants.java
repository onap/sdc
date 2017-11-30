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

package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

class AuditingLogFormatConstants {

	private AuditingLogFormatConstants() {}

	static final AuditingFieldsKeysEnum[] DISTRIBUTION_REGISTRATION_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME};
	
	static final AuditingFieldsKeysEnum[] DISTRIBUTION_DOWNLOAD_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] GET_UEB_CLUSTER_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_DESC
	};
	
	static final AuditingFieldsKeysEnum[] DISTRIBUTION_DEPLOY_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] DISTRIBUTION_STATUS_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_STATUS_TIME,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC		
	};
	
	static final AuditingFieldsKeysEnum[] DISTRIBUTION_NOTIFY_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] ADD_REMOVE_TOPIC_KEY_ACL_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ROLE,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_API_KEY,
		AuditingFieldsKeysEnum.AUDIT_STATUS
	};
	
	static final AuditingFieldsKeysEnum[] CREATE_TOPIC_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ENVRIONMENT_NAME,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_TOPIC_NAME,
		AuditingFieldsKeysEnum.AUDIT_STATUS
	};
	
	static final AuditingFieldsKeysEnum[] ACTIVATE_DISTRIBUTION_ARRAY ={
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_ID,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] CHANGE_DISTRIBUTION_STATUS_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_DPREV_STATUS,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_DCURR_STATUS,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_COMMENT
	};
	
	static final AuditingFieldsKeysEnum[] CREATE_RESOURCE_TEMPLATE_SUFFIX_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] CREATE_RESOURCE_TEMPLATE_PREFIX_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE

	};
	
	static final AuditingFieldsKeysEnum[] USER_ACCESS_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_USER_UID,
		AuditingFieldsKeysEnum.AUDIT_STATUS, 
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] USER_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_USER_UID,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] AUTH_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_AUTH_URL,
		AuditingFieldsKeysEnum.AUDIT_AUTH_USER, 
		AuditingFieldsKeysEnum.AUDIT_AUTH_STATUS,
		AuditingFieldsKeysEnum.AUDIT_AUTH_REALM
	};
	
	static final  AuditingFieldsKeysEnum[] ECOMP_USER_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_ECOMP_USER, 
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};

	static final  AuditingFieldsKeysEnum[] CATEGORY_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_CATEGORY_NAME,
		AuditingFieldsKeysEnum.AUDIT_SUB_CATEGORY_NAME,
		AuditingFieldsKeysEnum.AUDIT_GROUPING_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final  AuditingFieldsKeysEnum[] GET_USERS_LIST_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_USER_DETAILS, 
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final  AuditingFieldsKeysEnum[] GET_CATEGORY_HIERARCHY_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION, 
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_DETAILS, 
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	static final AuditingFieldsKeysEnum[] USER_ADMIN_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_USER_BEFORE,
		AuditingFieldsKeysEnum.AUDIT_USER_AFTER,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	static final AuditingFieldsKeysEnum[] EXTERNAL_GET_ASSET_LIST_TEMPLATE_ARRAY = {
			AuditingFieldsKeysEnum.AUDIT_ACTION,
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
			AuditingFieldsKeysEnum.AUDIT_STATUS,
			AuditingFieldsKeysEnum.AUDIT_DESC
	};
	static final AuditingFieldsKeysEnum[] EXTERNAL_GET_ASSET_TEMPLATE_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	static final AuditingFieldsKeysEnum[] EXTERNAL_DOWNLOAD_ARTIFACT_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	static final AuditingFieldsKeysEnum[] EXTERNAL_CRUD_API_ARTIFACT_ARRAY = {
		AuditingFieldsKeysEnum.AUDIT_ACTION,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
		AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID,
		AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
		AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
		AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID,
		AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID,
		AuditingFieldsKeysEnum.AUDIT_ARTIFACT_DATA,
		AuditingFieldsKeysEnum.AUDIT_STATUS,
		AuditingFieldsKeysEnum.AUDIT_DESC
	};
	
	static final AuditingFieldsKeysEnum[] EXTERNAL_CRUD_API_ARRAY = {
			AuditingFieldsKeysEnum.AUDIT_ACTION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, 
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
			AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
			
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,
			
			AuditingFieldsKeysEnum.AUDIT_PREV_ARTIFACT_UUID,
			AuditingFieldsKeysEnum.AUDIT_CURR_ARTIFACT_UUID,
			AuditingFieldsKeysEnum.AUDIT_STATUS,
			AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
			AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID,
			AuditingFieldsKeysEnum.AUDIT_DESC	
	};
	
	static final AuditingFieldsKeysEnum[] EXTERNAL_LYFECYCLE_API_ARRAY = {
			AuditingFieldsKeysEnum.AUDIT_ACTION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_NAME,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_TYPE,
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_CONSUMER_ID, 
			AuditingFieldsKeysEnum.AUDIT_DISTRIBUTION_RESOURCE_URL,
			AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME,
			AuditingFieldsKeysEnum.AUDIT_MODIFIER_UID,
			
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_VERSION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_VERSION,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_PREV_STATE,
			AuditingFieldsKeysEnum.AUDIT_RESOURCE_CURR_STATE,
			
			AuditingFieldsKeysEnum.AUDIT_SERVICE_INSTANCE_ID,
			AuditingFieldsKeysEnum.AUDIT_INVARIANT_UUID,
			AuditingFieldsKeysEnum.AUDIT_STATUS,
			AuditingFieldsKeysEnum.AUDIT_DESC	
	};
}

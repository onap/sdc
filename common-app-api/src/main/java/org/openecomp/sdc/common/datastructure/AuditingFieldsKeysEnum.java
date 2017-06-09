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

package org.openecomp.sdc.common.datastructure;

import java.util.Date;

public enum AuditingFieldsKeysEnum {
	// General
	AUDIT_TIMESTAMP(Date.class, "TIMESTAMP"), 
	AUDIT_ACTION(String.class, "ACTION"), 
	AUDIT_MODIFIER_NAME(String.class, "MODIFIER_ID"), 
	AUDIT_MODIFIER_UID(String.class, "MODIFIER"), 
	AUDIT_STATUS(String.class, "STATUS"), 
	AUDIT_DESC(String.class, "DESC"), 
	AUDIT_SERVICE_INSTANCE_ID(String.class, "SERVICE_INSTANCE_ID"), 
	AUDIT_INVARIANT_UUID(String.class, "INVARIANT_UUID"), 
	AUDIT_REQUEST_ID(String.class, "REQUEST_ID"),

	// Users administration
	AUDIT_USER_UID(String.class, "USER"), 
	AUDIT_USER_BEFORE(String.class, "USER_BEFORE"), 
	AUDIT_USER_AFTER(String.class, "USER_AFTER"), 
	AUDIT_USER_DETAILS(String.class, "DETAILS"),

	// Resource administration
	AUDIT_RESOURCE_NAME(String.class, "RESOURCE_NAME"), 
	AUDIT_RESOURCE_TYPE(String.class, "RESOURCE_TYPE"), 
	AUDIT_RESOURCE_CURR_VERSION(String.class, "CURR_VERSION"), 
	AUDIT_RESOURCE_PREV_VERSION(String.class, "PREV_VERSION"), 
	AUDIT_RESOURCE_CURR_STATE(String.class, "CURR_STATE"), 
	AUDIT_RESOURCE_PREV_STATE(String.class, "PREV_STATE"), 
	AUDIT_RESOURCE_COMMENT(String.class, "COMMENT"), 
	AUDIT_ARTIFACT_DATA(String.class, "ARTIFACT_DATA"), 
	AUDIT_PREV_ARTIFACT_UUID(String.class, "PREV_ARTIFACT_UUID"), 
	AUDIT_CURR_ARTIFACT_UUID(String.class, "CURR_ARTIFACT_UUID"), 
	AUDIT_RESOURCE_DPREV_STATUS(String.class, "DPREV_STATUS"), 
	AUDIT_RESOURCE_DCURR_STATUS(String.class, "DCURR_STATUS"), 
	AUDIT_RESOURCE_TOSCA_NODE_TYPE(String.class, "TOSCA_NODE_TYPE"),
	AUDIT_RESOURCE_URL(String.class, "RESOURCE_URL"),

	// Distribution Engine
	AUDIT_DISTRIBUTION_ENVRIONMENT_NAME(String.class, "D_ENV"), 
	AUDIT_DISTRIBUTION_TOPIC_NAME(String.class, "TOPIC_NAME"),
    AUDIT_DISTRIBUTION_NOTIFICATION_TOPIC_NAME(String.class, "DNOTIF_TOPIC"), 
    AUDIT_DISTRIBUTION_STATUS_TOPIC_NAME(String.class, "DSTATUS_TOPIC"),
    AUDIT_DISTRIBUTION_ROLE(String.class, "ROLE"), 
    AUDIT_DISTRIBUTION_ID(String.class, "DID"), 
    AUDIT_DISTRIBUTION_API_KEY(String.class, "API_KEY"), 
    AUDIT_DISTRIBUTION_CONSUMER_ID(String.class, "CONSUMER_ID"), 
    AUDIT_DISTRIBUTION_RESOURCE_URL(String.class, "RESOURCE_URL"), 
    AUDIT_DISTRIBUTION_STATUS_TIME(String.class, "STATUS_TIME"), 
    AUDIT_DISTRIBUTION_STATUS_DESC(String.class, "STATUS_DESC"),

	// category
	AUDIT_CATEGORY_NAME(String.class, "CATEGORY_NAME"), 
	AUDIT_SUB_CATEGORY_NAME(String.class, "SUB_CATEGORY_NAME"), 
	AUDIT_GROUPING_NAME(String.class, "GROUPING_NAME"), 
	AUDIT_DETAILS(String.class, "DETAILS"),

	// authentication
	AUDIT_AUTH_URL(String.class, "URL"), 
	AUDIT_AUTH_USER(String.class, "USER"), 
	AUDIT_AUTH_STATUS(String.class, "AUTH_STATUS"), 
	AUDIT_AUTH_REALM(String.class, "REALM"),
	AUDIT_ECOMP_USER(String.class, "ECOMP_USER");

	private Class<?> clazz;
	private String displayName;

	AuditingFieldsKeysEnum(Class<?> clazz, String displayName) {
		this.clazz = clazz;
		this.displayName = displayName;
	}

	public Class<?> getValueClass() {
		return this.clazz;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}

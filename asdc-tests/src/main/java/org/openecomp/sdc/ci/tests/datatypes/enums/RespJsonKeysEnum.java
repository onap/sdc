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

package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum RespJsonKeysEnum {

	IS_ABSTRACT("abstract"), 
	UNIQUE_ID("uniqueId"), 
	RESOURCE_NAME("name"), 
	RESOURCE_VERSION("version"), 
	TAGS("tags"), 
	LIFE_CYCLE_STATE("lifecycleState"), 
	DERIVED_FROM("derivedFrom"), 
	RESOURCE_DESC("description"), 
	VENDOR_NAME("vendorName"), 
	VENDOR_RELEASE("vendorRelease"), 
	CONTACT_ID("contactId"), 
	ICON("icon"), 
	HIGHEST_VERSION("highestVersion"), 
	CREATOR_USER_ID("creatorUserId"), 
	CREATOR_FULL_NAME("creatorFullName"), 
	LAST_UPDATER_ATT_UID("lastUpdaterUserId"), 
	LAST_UPDATER_FULL_NAME("lastUpdaterFullName"), 
	ARTIFACTS("artifacts"), 
	DESCRIPTION("description"), 
	UUID("uuid"), 
	COST("cost"), 
	LICENSE_TYPE("licenseType"), 
	RESOURCE_TYPE("resourceType"), 
	CATEGORIES("categories");
	
	private String respJsonKeyName;

	private RespJsonKeysEnum(String respJsonKeyName) {
		this.respJsonKeyName = respJsonKeyName;
	}

	public String getRespJsonKeyName() {
		return respJsonKeyName;
	}

}

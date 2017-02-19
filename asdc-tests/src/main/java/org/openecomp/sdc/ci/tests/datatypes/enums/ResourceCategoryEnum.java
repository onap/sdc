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

public enum ResourceCategoryEnum {

	NETWORK_L2_3_ROUTERS("Network L2-3", "Router"), NETWORK_L2_3_GETEWAY("Network L2-3","Gateway"), NETWORK_L2_3_WAN_CONNECTORS("Network L2-3", "WAN Connectors"), NETWORK_L2_3_LAN_CONNECTORS("Network L2-3", "LAN Connectors"), 
	NETWORK_L2_3_INFRASTRUCTURE("Network L2-3", "Infrastructure"), NETWORK_L4("Network L4+", "Common Network Resources"), APPLICATION_L4_BORDER("Application L4+", "Border Element"), 
	APPLICATION_L4_APP_SERVER("Application L4+", "Application Server"), APPLICATION_L4_WEB_SERVERS("Application L4+", "Web Server"), APPLICATION_L4_CALL_CONTROL("Application L4+","Call Control"), 
	APPLICATION_L4_MEDIA_SERVER("Application L4+", "Media Servers"), APPLICATION_L4_LOAD_BALANCER("Application L4+", "Load Balancer"), APPLICATION_L4_DATABASE("Application L4+","Database"), 
	APPLICATION_L4_FIREWALL("Application L4+", "Firewall"), GENERIC_INFRASTRUCTURE("Generic", "Infrastructure"), GENERIC_ABSTRACT("Generic", "Abstract"), GENERIC_NETWORK_ELEMENTS("Generic","Network Elements"), 
	GENERIC_DATABASE("Generic", "Database"), NETWORK_CONNECTIVITY_CON_POINT("Network Connectivity", "Connection Points"), NETWORK_CONNECTIVITY_VIRTUAL_LINK("Network Connectivity","Virtual Links");

	private String category;
	private String subCategory;

	ResourceCategoryEnum(String category, String subCategory) {
		this.category = category;
		this.subCategory = subCategory;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

}

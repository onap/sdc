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

package org.openecomp.sdc.ci.tests.datatypes;

public enum ResourceCategoriesNameEnum {

	GENERIC("checkbox-resourcenewcategory.generic"), NETWORK_CONNECTIVITY("checkbox-resourcenewcategory.networkconnectivity"), NETWORK_ELEMENTS("checkbox-resourcenewcategory.generic.networkelements"), 
	ABSTRACT("checkbox-resourcenewcategory.generic.abstract"), DATABASE_GENERIC("checkbox-resourcenewcategory.generic.database"), INFRASTRUCTURE("checkbox-resourcenewcategory.generic.infrastructure"), 
	VIRTUAL_LINKS("checkbox-resourcenewcategory.networkconnectivity.virtuallinks"), CONNECTION_POINTS("checkbox-resourcenewcategory.networkconnectivity.connectionpoints"), NETWORKL4("checkbox-resourcenewcategory.networkl4+"), 
	COMMON_NETWORK_RESOURCES("checkbox-resourcenewcategory.networkl4+.commonnetworkresources"), APPLICATIONL4("checkbox-resourcenewcategory.applicationl4+"), WEB_SERVER("checkbox-resourcenewcategory.applicationl4+.webserver"), 
	APPLICATION_SERVER("checkbox-resourcenewcategory.applicationl4+.applicationserver"), CALL_CONTROL("checkbox-resourcenewcategory.applicationl4+.callcontrol"), BORDER_ELEMENT("checkbox-resourcenewcategory.applicationl4+.borderelement"), 
	MEDIA_SERVERS("checkbox-resourcenewcategory.applicationl4+.mediaservers"), DATABASE("checkbox-resourcenewcategory.applicationl4+.database"), FIREWALL("checkbox-resourcenewcategory.applicationl4+.firewall"), 
	LOAD_BALANCER("checkbox-resourcenewcategory.applicationl4+.loadbalancer"), NETWORK_L23("checkbox-resourcenewcategory.networkl2-3"), Router("checkbox-resourcenewcategory.networkl2-3.router"), 
	WAN_Connectors("checkbox-resourcenewcategory.networkl2-3.wanconnectors"), LAN_CONNECTORS("checkbox-resourcenewcategory.networkl2-3.lanconnectors"), GATEWAY("checkbox-resourcenewcategory.networkl2-3.gateway"), 
	INFRASTRUCTUREL23("checkbox-resourcenewcategory.networkl2-3.infrastructure");

	private String value;

	public String getValue() {
		return value;
	}

	private ResourceCategoriesNameEnum(String value) {
		this.value = value;
	}

}

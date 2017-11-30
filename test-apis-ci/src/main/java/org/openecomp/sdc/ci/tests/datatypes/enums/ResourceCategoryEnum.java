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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum ResourceCategoryEnum {

	NETWORK_L2_3_ROUTERS("Network L2-3", "Router", "resourceNewCategory.network l2-3", "resourceNewCategory.network l2-3.router"), 
	NETWORK_L2_3_GETEWAY("Network L2-3","Gateway", "resourceNewCategory.network l2-3", "resourceNewCategory.network l2-3.gateway"), 
	NETWORK_L2_3_WAN_CONNECTORS("Network L2-3", "WAN Connectors", "resourceNewCategory.network l2-3", "resourceNewCategory.network l2-3.lan connectors"), 
	NETWORK_L2_3_LAN_CONNECTORS("Network L2-3", "LAN Connectors", "resourceNewCategory.network l2-3", "resourceNewCategory.network l2-3.wan connectors"), 
	NETWORK_L2_3_INFRASTRUCTURE("Network L2-3", "Infrastructure", "resourceNewCategory.network l2-3", "resourceNewCategory.network l2-3.infrastructure"),
	
	NETWORK_L4("Network L4+", "Common Network Resources", "resourceNewCategory.network l4+", "resourceNewCategory.network l4+.common network resources"), 
	
	APPLICATION_L4_BORDER("Application L4+", "Border Element", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.border element"), 
	APPLICATION_L4_APP_SERVER("Application L4+", "Application Server", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.application server"), 
	APPLICATION_L4_WEB_SERVERS("Application L4+", "Web Server", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.web server"), 
	APPLICATION_L4_CALL_CONTROL("Application L4+","Call Control", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.call control"), 
	APPLICATION_L4_MEDIA_SERVER("Application L4+", "Media Servers", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.media servers"), 
	APPLICATION_L4_LOAD_BALANCER("Application L4+", "Load Balancer", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.load balancer"), 
	APPLICATION_L4_DATABASE("Application L4+","Database", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.database"), 
	APPLICATION_L4_FIREWALL("Application L4+", "Firewall", "resourceNewCategory.application l4+", "resourceNewCategory.application l4+.firewall"), 
	
	GENERIC_INFRASTRUCTURE("Generic", "Infrastructure", "resourceNewCategory.generic", "resourceNewCategory.generic.infrastructure"), 
	GENERIC_ABSTRACT("Generic", "Abstract", "resourceNewCategory.generic", "resourceNewCategory.generic.abstract"), 
	GENERIC_NETWORK_ELEMENTS("Generic","Network Elements", "resourceNewCategory.generic", "resourceNewCategory.generic.network elements"), 
	GENERIC_DATABASE("Generic", "Database", "resourceNewCategory.generic", "resourceNewCategory.generic.database"),
	GENERIC_RULES("Generic", "Rules", "resourceNewCategory.generic", "resourceNewCategory.generic.rules"),
	
	NETWORK_CONNECTIVITY_CON_POINT("Network Connectivity", "Connection Points", "resourceNewCategory.network connectivity", "resourceNewCategory.network connectivity.connection points"), 
	NETWORK_CONNECTIVITY_VIRTUAL_LINK("Network Connectivity","Virtual Links", "resourceNewCategory.network connectivity", "resourceNewCategory.network connectivity.virtual links"),
	
	TEMPLATE_MONITORING_TEMPLATE("Template", "Monitoring Template", "resourceNewCategory.template", "resourceNewCategory.template.monitoring template"), 
	
	ALLOTTED_RESOURCE("Allotted Resource", "Allotted Resource", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.allotted resource"),
	ALLOTTED_RESOURCE_SERVICE_ADMIN("Allotted Resource", "Service Admin", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.service admin"),
	ALLOTTED_RESOURCE_CONTRAIL_ROUTE("Allotted Resource", "Contrail Route", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.contrail route"),
	ALLOTTED_RESOURCE_TUNNEL_XCONNECT("Allotted Resource", "Tunnel XConnect", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.tunnel xconnect"),
	ALLOTTED_RESOURCE_IP_MUX_DEMUX("Allotted Resource", "IP Mux Demux", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.ip mux demux"),
	ALLOTTED_RESOURCE_SECURITY_ZONE("Allotted Resource", "Security Zone", "resourceNewCategory.allotted resource", "resourceNewCategory.allotted resource.security zone"),
	
	DCAE_COMPONENT_MICROSERVICE("DCAE Component", "Microservice", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.microservice"),
	DCAE_COMPONENT_DATABASE("DCAE Component", "Database", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.database"),
	DCAE_COMPONENT_POLICY("DCAE Component", "policy", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.policy"),
	DCAE_COMPONENT_SOURCE("DCAE Component", "Source", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.source"),
	DCAE_COMPONENT_ANALYSTICS("DCAE Component", "Analytics", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.analytics"),
	DCAE_COMPONENT_UTILITY("DCAE Component", "Utility", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.utility"),
	DCAE_COMPONENT_COLLECTOR("DCAE Component", "Collector", "resourceNewCategory.dcae component", "resourceNewCategory.dcae component.collector"),
	;
	
	private String category;
	private String subCategory;
	private String categoryUniqeId;
	private String subCategoryUniqeId;

	ResourceCategoryEnum(String category, String subCategory) {
		this.category = category;
		this.subCategory = subCategory;
	}
	
	private ResourceCategoryEnum(String category, String subCategory, String categoryUniqeId, String subCategoryUniqeId) {
		this.category = category;
		this.subCategory = subCategory;
		this.categoryUniqeId = categoryUniqeId;
		this.subCategoryUniqeId = subCategoryUniqeId;
	}

	public String getCategoryUniqeId() {
		return categoryUniqeId;
	}

	public void setCategoryUniqeId(String categoryUniqeId) {
		this.categoryUniqeId = categoryUniqeId;
	}

	public String getSubCategoryUniqeId() {
		return subCategoryUniqeId;
	}

	public void setSubCategoryUniqeId(String subCategoryUniqeId) {
		this.subCategoryUniqeId = subCategoryUniqeId;
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


	public static ResourceCategoryEnum findEnumNameByValues(String category, String subCategory){
		for(ResourceCategoryEnum resourceCategoryEnum : ResourceCategoryEnum.values()) {
			if(resourceCategoryEnum.getCategory().equals(category) && resourceCategoryEnum.getSubCategory().equals(subCategory)){
				return resourceCategoryEnum;
			}
		}
		return null;
	}


	/**
	 * @return random category enum except allotted category
	 */
	public static ResourceCategoryEnum getRandomElement() {
		Random random = new Random();
		ResourceCategoryEnum resourceCategoryEnum = ResourceCategoryEnum.values()[random.nextInt(ResourceCategoryEnum.values().length)];

		if(!resourceCategoryEnum.toString().startsWith("ALLOTTED")){
			return resourceCategoryEnum;
		}else{
			return getRandomElement();
		}
	}


	/**
	 * @return random  allotted category enum
	 */
	public static ResourceCategoryEnum getRandomAllottedElement() {
		Random random = new Random();
		ResourceCategoryEnum resourceCategoryEnum = ResourceCategoryEnum.values()[random.nextInt(ResourceCategoryEnum.values().length)];

		if(resourceCategoryEnum.toString().startsWith("ALLOTTED")){
			return resourceCategoryEnum;
		}else{
			return getRandomAllottedElement();
		}
	}

}

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

import java.util.ArrayList;

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class ServiceReqDetails extends ComponentReqDetails {
	
	protected String serviceType = "MyServiceType";
	protected String serviceRole = "MyServiceRole";
	protected String namingPolicy = "MyServiceNamingPolicy";
	protected Boolean ecompGeneratedNaming = true;

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	public String getNamingPolicy() {
		return namingPolicy;
	}

	public void setNamingPolicy(String namingPolicy) {
		this.namingPolicy = namingPolicy;
	}

	public Boolean getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

	public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
	}

	public ServiceReqDetails(String serviceName, String category, ArrayList<String> tags, String description,
			String contactId, String icon) {
		this.name = serviceName;
		// this.category = category;
		this.tags = tags;
		this.description = description;
		this.contactId = contactId;
		this.icon = icon;
		projectCode = "12345";
		CategoryDefinition categoryDefinition = new CategoryDefinition();
		categoryDefinition.setName(category);
		categories = new ArrayList<>();
		categories.add(categoryDefinition);

	}

	public ServiceReqDetails(Service service) {
		this.contactId = service.getContactId();
		this.categories = service.getCategories();
		this.creatorUserId = service.getCreatorUserId();
		this.creatorFullName = service.getCreatorFullName();
		this.description = service.getDescription();
		this.icon = service.getIcon();
		this.name = service.getName();
		this.projectCode = service.getProjectCode();
		this.tags = service.getTags();
		this.uniqueId = service.getUniqueId();
		this.UUID = service.getUUID();
		this.version = service.getVersion();

	}

	public ServiceReqDetails() {
		contactId = "aa1234";
		projectCode = "12345";
	}

	public ServiceReqDetails(ServiceReqDetails a, String newServiceName) {
		a.setName(newServiceName);
	}

	@Override
	public String toString() {
		return "ServiceDetails [name=" + name + ", category=" + getCategory() + ", tags=" + tags + ", description="
				+ description + ", contactId=" + contactId + ", icon=" + icon + "]";
	}

	public ServiceReqDetails(ServiceReqDetails aService) {
		this(aService.getName(), aService.getCategory(), (ArrayList<String>) aService.getTags(),
				aService.getDescription(), aService.getContactId(), aService.getIcon());
		uniqueId = aService.getUniqueId();
		version = aService.getVersion();
	}

	public String getCategory() {
		if (categories != null && categories.size() >= 1) {
			return categories.get(0).getName();
		}
		return null;
	}

}

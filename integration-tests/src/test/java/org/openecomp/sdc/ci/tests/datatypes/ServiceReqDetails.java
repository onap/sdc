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

import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceInstantiationType;

import java.util.ArrayList;
import java.util.Collections;

public class ServiceReqDetails extends ComponentReqDetails {
	
	protected String serviceType = "MyServiceType";
	protected String serviceRole = "MyServiceRole";
	protected String namingPolicy = "MyServiceNamingPolicy";
	protected Boolean ecompGeneratedNaming = true;
	protected String instantiationType = ServiceInstantiationType.A_LA_CARTE.getValue();
	protected String serviceFunction = "";

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	public String getInstantiationType() {
		return instantiationType;
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

	public String getServiceFunction() {
		return serviceFunction;
	}

	public void setServiceFunction(String serviceFunction) {
		this.serviceFunction = serviceFunction;
	}

	public ServiceReqDetails(String serviceName, String category, ArrayList<String> tags, String description,
			String contactId, String icon, String instantiationType) {
		this.name = serviceName;
		this.tags = tags;
		this.description = description;
		this.contactId = contactId;
		this.icon = icon;
		projectCode = "12345";
		serviceFunction = "serviceFunction name";
		CategoryDefinition categoryDefinition = new CategoryDefinition();
		categoryDefinition.setName(category);
		categoryDefinition.setIcons(Collections.singletonList(icon));
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
		return "ServiceDetails [name = " + name + ", category = " + getCategory() + ", tags = " + tags + ", description = "
				+ description + ", contactId = " + contactId + ", icon = " + icon + ", instantiation type = " + getInstantiationType() + "]";
	}

	public ServiceReqDetails(ServiceReqDetails aService) {
		this(aService.getName(), aService.getCategory(), (ArrayList<String>) aService.getTags(),
				aService.getDescription(), aService.getContactId(), aService.getIcon(), aService.getInstantiationType());
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

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

package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class UiServiceMetadata extends UiComponentMetadata {
	
	private String distributionStatus;
	private Boolean ecompGeneratedNaming;
	private String namingPolicy;
	private String serviceType;
	private String serviceRole;
	private String environmentContext;

	public UiServiceMetadata(List<CategoryDefinition> categories, ServiceMetadataDataDefinition metadata) {
		super(categories, metadata);
		this.distributionStatus = metadata.getDistributionStatus();
		this.ecompGeneratedNaming = metadata.isEcompGeneratedNaming();
		this.namingPolicy = metadata.getNamingPolicy();
		this.serviceType = metadata.getServiceType();
		this.serviceRole = metadata.getServiceRole();
		this.environmentContext = metadata.getEnvironmentContext();
	}	
	
	public String getDistributionStatus() {
		return distributionStatus;
	}

	public void setDistributionStatus(String distributionStatus) {
		this.distributionStatus = distributionStatus;
	}
	
	public Boolean getEcompGeneratedNaming() {
		return ecompGeneratedNaming;
	}

	public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
		this.ecompGeneratedNaming = ecompGeneratedNaming;
	}

	public String getNamingPolicy() {
		return namingPolicy;
	}

	public void setNamingPolicy(String namingPolicy) {
		this.namingPolicy = namingPolicy;
	}
	
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

	public String getEnvironmentContext() { return environmentContext; }

	public void setEnvironmentContext(String environmentContext) { this.environmentContext = environmentContext; }
}

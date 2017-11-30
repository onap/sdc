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

package org.openecomp.sdc.be.model;

import java.util.Map;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElementTypeEnum;

public class Service extends Component {

	private static final long serialVersionUID = -8819935942700578059L;

	public Service() {
		super(new ServiceMetadataDefinition());
		this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.SERVICE);
		this.setToscaType(ToscaElementTypeEnum.TopologyTemplate.getValue());
	}

	public Service(ComponentMetadataDefinition serviceMetadataDefinition) {
		super(serviceMetadataDefinition);
		this.getComponentMetadataDefinition().getMetadataDataDefinition().setComponentType(ComponentTypeEnum.SERVICE);
		this.setToscaType(ToscaElementTypeEnum.TopologyTemplate.getValue());
	}

	private Map<String, ArtifactDefinition> serviceApiArtifacts;


	public Map<String, ArtifactDefinition> getServiceApiArtifacts() {
		return serviceApiArtifacts;
	}

	public void setServiceApiArtifacts(Map<String, ArtifactDefinition> serviceApiArtifacts) {
		this.serviceApiArtifacts = serviceApiArtifacts;
	}

	public String getProjectCode() {
		return getServiceMetadataDefinition().getProjectCode();
	}

	public void setProjectCode(String projectName) {
		getServiceMetadataDefinition().setProjectCode(projectName);
	}

	public DistributionStatusEnum getDistributionStatus() {
		String distributionStatus = getServiceMetadataDefinition().getDistributionStatus();
		if (distributionStatus != null) {
			return DistributionStatusEnum.valueOf(distributionStatus);
		} else {
			return null;
		}
	}

	public void setDistributionStatus(DistributionStatusEnum distributionStatus) {
		if (distributionStatus != null)
			getServiceMetadataDefinition().setDistributionStatus(distributionStatus.name());
	}

	public void setEcompGeneratedNaming(Boolean ecompGeneratedNaming) {
		getServiceMetadataDefinition().setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	public Boolean isEcompGeneratedNaming() {
		return getServiceMetadataDefinition().isEcompGeneratedNaming();
	}

	public void setNamingPolicy(String namingPolicy) {
		getServiceMetadataDefinition().setNamingPolicy(namingPolicy);
	}
	
	public String getNamingPolicy() {
		return getServiceMetadataDefinition().getNamingPolicy();
	}

	public String getEnvironmentContext() { return getServiceMetadataDefinition().getEnvironmentContext();  }

	public void setEnvironmentContext(String environmentContext) {
		getServiceMetadataDefinition().setEnvironmentContext(environmentContext);
	}

	public void setServiceType(String serviceType){
		getServiceMetadataDefinition().setServiceType(serviceType);
	}
	
	public String getServiceType(){
		return getServiceMetadataDefinition().getServiceType();
	}
	
	public void setServiceRole(String serviceRole){
		getServiceMetadataDefinition().setServiceRole(serviceRole);
	}
	
	public String getServiceRole(){
		return getServiceMetadataDefinition().getServiceRole();
	}

	private ServiceMetadataDataDefinition getServiceMetadataDefinition() {
		return (ServiceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition();
	}


	@Override
	public String toString() {
		return "Service [componentMetadataDefinition=" + getComponentMetadataDefinition()
		// + ", resourceInstances=" + resourceInstances + ",
		// resourceInstancesRelations=" + resourceInstancesRelations + ",
		// resourceInstancesRelations="
		// + resourceInstancesRelations
				+ " ]";
	}

	@Override
	public void setSpecificComponetTypeArtifacts(Map<String, ArtifactDefinition> specificComponentTypeArtifacts) {
		setServiceApiArtifacts(specificComponentTypeArtifacts);
	}
}

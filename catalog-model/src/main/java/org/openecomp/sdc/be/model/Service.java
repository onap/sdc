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

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class Service extends Component {
	public Service() {
		super(new ServiceMetadataDefinition());
		componentType = ComponentTypeEnum.SERVICE;
	}

	public Service(ComponentMetadataDefinition serviceMetadataDefinition) {
		super(serviceMetadataDefinition);
		componentType = ComponentTypeEnum.SERVICE;
	}

	private Map<String, ArtifactDefinition> serviceApiArtifacts;

	private List<AdditionalInformationDefinition> additionalInformation;

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

	private ServiceMetadataDataDefinition getServiceMetadataDefinition() {
		return (ServiceMetadataDataDefinition) getComponentMetadataDefinition().getMetadataDataDefinition();
	}

	public List<AdditionalInformationDefinition> getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(List<AdditionalInformationDefinition> additionalInformation) {
		this.additionalInformation = additionalInformation;
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

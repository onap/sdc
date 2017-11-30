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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.List;

public class NotificationDataImpl implements INotificationData {

	private String distributionID;
	private String serviceName;
	private String serviceVersion;
	private String serviceUUID;
	private String serviceDescription;
	private String serviceInvariantUUID;
	private List<JsonContainerResourceInstance> resources;
	private List<ArtifactInfoImpl> serviceArtifacts;
	private String workloadContext;

	@Override
	public String getDistributionID() {
		return distributionID;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public String getServiceVersion() {
		return serviceVersion;
	}

	@Override
	public String getServiceUUID() {
		return serviceUUID;
	}

	public void setDistributionID(String distributionID) {
		this.distributionID = distributionID;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public void setServiceUUID(String serviceUUID) {
		this.serviceUUID = serviceUUID;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	@Override
	public String getWorkloadContext() { return workloadContext; }

	@Override
	public void setWorkloadContext(String workloadContext) { this.workloadContext = workloadContext;	}

	@Override
	public String toString() {
		return "NotificationDataImpl{" +
				"distributionID='" + distributionID + '\'' +
				", serviceName='" + serviceName + '\'' +
				", serviceVersion='" + serviceVersion + '\'' +
				", serviceUUID='" + serviceUUID + '\'' +
				", serviceDescription='" + serviceDescription + '\'' +
				", serviceInvariantUUID='" + serviceInvariantUUID + '\'' +
				", resources=" + resources +
				", serviceArtifacts=" + serviceArtifacts +
				", workloadContext='" + workloadContext + '\'' +
				'}';
	}

	@Override
	public List<JsonContainerResourceInstance> getResources() {
		return resources;
	}

	@Override
	public void setResources(List<JsonContainerResourceInstance> resources) {
		this.resources = resources;

	}

	@Override
	public List<ArtifactInfoImpl> getServiceArtifacts() {
		// TODO Auto-generated method stub
		return serviceArtifacts;
	}

	@Override
	public void setServiceArtifacts(List<ArtifactInfoImpl> serviceArtifacts) {
		this.serviceArtifacts = serviceArtifacts;

	}

	@Override
	public String getServiceInvariantUUID() {
		return serviceInvariantUUID;
	}

	@Override
	public void setServiceInvariantUUID(String serviceInvariantUUID) {
		this.serviceInvariantUUID = serviceInvariantUUID;
	}

}

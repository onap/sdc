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

package org.openecomp.sdc.be.datatypes.components;

import java.io.Serializable;

public class ServiceMetadataDataDefinition extends ComponentMetadataDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7661001892509435120L;

	private String distributionStatus;

	public ServiceMetadataDataDefinition() {
		super();
	}

	public ServiceMetadataDataDefinition(ServiceMetadataDataDefinition other) {
		super(other);
	}

	public String getDistributionStatus() {
		return distributionStatus;
	}

	public void setDistributionStatus(String distributionStatus) {
		this.distributionStatus = distributionStatus;
	}

	@Override
	public String toString() {
		return "ServiceMetadataDataDefinition [ distributionStatus=" + distributionStatus + ", parent="
				+ super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((distributionStatus == null) ? 0 : distributionStatus.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ComponentMetadataDataDefinition)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass())
			return false;
		ServiceMetadataDataDefinition other = (ServiceMetadataDataDefinition) obj;
		if (distributionStatus == null) {
			if (other.distributionStatus != null)
				return false;
		} else if (!distributionStatus.equals(other.distributionStatus))
			return false;
		return super.equals(obj);
	}

}

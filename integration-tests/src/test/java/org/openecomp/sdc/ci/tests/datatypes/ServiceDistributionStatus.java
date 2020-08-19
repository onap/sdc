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

public class ServiceDistributionStatus {
	
	private String distributionID;
	private String timestamp;
	private String userId;
	private String deployementStatus;
	
	public ServiceDistributionStatus() {
		super();
	}

	public ServiceDistributionStatus(String distributionID, String timestamp, String userId, String deployementStatus) {
		super();
		this.distributionID = distributionID;
		this.timestamp = timestamp;
		this.userId = userId;
		this.deployementStatus = deployementStatus;
	}

	@Override
	public String toString() {
		return "ServiceDistributionStatus [distributionID=" + distributionID + ", timestamp=" + timestamp + ", userId=" + userId + ", deployementStatus=" + deployementStatus + "]";
	}

	public String getDistributionID() {
		return distributionID;
	}

	public void setDistributionID(String distributionID) {
		this.distributionID = distributionID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDeployementStatus() {
		return deployementStatus;
	}

	public void setDeployementStatus(String deployementStatus) {
		this.deployementStatus = deployementStatus;
	}

}

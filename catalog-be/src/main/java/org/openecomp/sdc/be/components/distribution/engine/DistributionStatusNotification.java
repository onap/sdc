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

public class DistributionStatusNotification {

	String distributionID;
	String consumerID;
	long timestamp;
	String artifactURL;
	DistributionStatusNotificationEnum status;
	String errorReason;

	public String getDistributionID() {
		return distributionID;
	}

	public void setDistributionID(String distributionId) {
		this.distributionID = distributionId;
	}

	public String getConsumerID() {
		return consumerID;
	}

	public void setConsumerID(String consumerId) {
		this.consumerID = consumerId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getArtifactURL() {
		return artifactURL;
	}

	public void setArtifactURL(String artifactURL) {
		this.artifactURL = artifactURL;
	}

	public DistributionStatusNotificationEnum getStatus() {
		return status;
	}

	public void setStatus(DistributionStatusNotificationEnum status) {
		this.status = status;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	@Override
	public String toString() {
		return "DistributionStatusNotification [distributionId=" + distributionID + ", consumerId=" + consumerID + ", timestamp=" + timestamp + ", artifactURL=" + artifactURL + ", status=" + status + ", errorReason=" + errorReason + "]";
	}
}

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

package org.openecomp.sdc.ci.tests.executeOnUGN.distributionClient;

import java.util.ArrayList;
import java.util.List;

public class ClientConfiguration {

	private String asdcAddress;
	private String user;
	private String password;
	private Integer pollingInterval;
	private Integer pollingTimeout;
	private List<String> relevantArtifactTypes;
	private String consumerGroup;
	private String environmentName;
	private String consumerID;

	public ClientConfiguration() {

		super();

		this.asdcAddress = "localhost:8443";
		this.consumerID = "mso-123456";
		this.consumerGroup = "mso-group";
		this.environmentName = "PROD";
		this.password = "password";
		this.pollingInterval = 20;
		this.pollingTimeout = 20;
		this.relevantArtifactTypes = new ArrayList<String>();
		this.relevantArtifactTypes.add("SHELL");
		this.user = "mso-user";
	}

	public String getAsdcAddress() {
		return asdcAddress;
	}

	public void setAsdcAddress(String asdcAddress) {
		this.asdcAddress = asdcAddress;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(Integer pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public int getPollingTimeout() {
		return pollingTimeout;
	}

	public void setPollingTimeout(Integer pollingTimeout) {
		this.pollingTimeout = pollingTimeout;
	}

	public List<String> getRelevantArtifactTypes() {
		return relevantArtifactTypes;
	}

	public void setRelevantArtifactTypes(List<String> relevantArtifactTypes) {
		this.relevantArtifactTypes = relevantArtifactTypes;
	}

	public String getConsumerGroup() {
		return consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getComsumerID() {
		return consumerID;
	}

	public void setComsumerID(String comsumerID) {
		this.consumerID = comsumerID;
	}

	public ClientConfiguration(String asdcAddress, String user, String password, Integer pollingInterval,
			Integer pollingTimeout, List<String> relevantArtifactTypes, String consumerGroup, String environmentName,
			String comsumerID) {
		super();
		this.asdcAddress = asdcAddress;
		this.user = user;
		this.password = password;
		this.pollingInterval = pollingInterval;
		this.pollingTimeout = pollingTimeout;
		this.relevantArtifactTypes = relevantArtifactTypes;
		this.consumerGroup = consumerGroup;
		this.environmentName = environmentName;
		this.consumerID = comsumerID;
	}

}

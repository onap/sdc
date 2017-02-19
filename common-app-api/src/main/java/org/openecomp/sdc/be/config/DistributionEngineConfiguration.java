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

package org.openecomp.sdc.be.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class DistributionEngineConfiguration extends BasicConfiguration {

	private List<String> uebServers;

	private String distributionNotifTopicName;

	private String distributionStatusTopicName;

	private Integer initRetryIntervalSec;

	private Integer initMaxIntervalSec;

	private ComponentArtifactTypesConfig distribNotifServiceArtifactTypes;

	private ComponentArtifactTypesConfig distribNotifResourceArtifactTypes;

	// private List<String> distribNotifServiceInfoArtifactTypes;

	// private List<String> distribNotifResourceLifecycleArtifactTypes;

	private String uebPublicKey;

	private String uebSecretKey;

	private List<String> environments;

	private DistributionStatusTopicConfig distributionStatusTopic;

	private CreateTopicConfig createTopic;

	private boolean startDistributionEngine;

	private DistributionNotificationTopicConfig distributionNotificationTopic;

	private Integer defaultArtifactInstallationTimeout = 60;

	public static class DistribNotifServiceArtifacts {

		Map<String, Object> service;
		Map<String, Object> resource;

		public Map<String, Object> getService() {
			return service;
		}

		public void setService(Map<String, Object> service) {
			this.service = service;
		}

		public Map<String, Object> getResource() {
			return resource;
		}

		public void setResource(Map<String, Object> resource) {
			this.resource = resource;
		}

	}

	public static class NotifArtifactTypes {

		List<String> info;
		List<String> lifecycle;

		public List<String> getInfo() {
			return info;
		}

		public void setInfo(List<String> info) {
			this.info = info;
		}

		public List<String> getLifecycle() {
			return lifecycle;
		}

		public void setLifecycle(List<String> lifecycle) {
			this.lifecycle = lifecycle;
		}

	}

	public static class NotifArtifactTypesResource {

		List<ArtifcatTypeEnum> lifecycle;

	}

	public static enum ArtifcatTypeEnum {

		MURANO_PKG("MURANO-PKG"), HEAT("HEAT"), DG_XML("DG_XML");

		String value;

		private ArtifcatTypeEnum(String value) {
			this.value = value;
		}

		public String getValue() {

			return value;
		}
	}

	public List<String> getUebServers() {
		return uebServers;
	}

	public void setUebServers(List<String> uebServers) {
		this.uebServers = uebServers;
	}

	public String getDistributionNotifTopicName() {
		return distributionNotifTopicName;
	}

	public void setDistributionNotifTopicName(String distributionNotifTopicName) {
		this.distributionNotifTopicName = distributionNotifTopicName;
	}

	public String getDistributionStatusTopicName() {
		return distributionStatusTopicName;
	}

	public void setDistributionStatusTopicName(String distributionStatusTopicName) {
		this.distributionStatusTopicName = distributionStatusTopicName;
	}

	public Integer getInitRetryIntervalSec() {
		return initRetryIntervalSec;
	}

	public void setInitRetryIntervalSec(Integer initRetryIntervalSec) {
		this.initRetryIntervalSec = initRetryIntervalSec;
	}

	public ComponentArtifactTypesConfig getDistribNotifServiceArtifactTypes() {
		return distribNotifServiceArtifactTypes;
	}

	public void setDistribNotifServiceArtifactTypes(ComponentArtifactTypesConfig distribNotifServiceArtifactTypes) {
		this.distribNotifServiceArtifactTypes = distribNotifServiceArtifactTypes;
	}

	public ComponentArtifactTypesConfig getDistribNotifResourceArtifactTypes() {
		return distribNotifResourceArtifactTypes;
	}

	public void setDistribNotifResourceArtifactTypes(ComponentArtifactTypesConfig distribNotifResourceArtifactTypes) {
		this.distribNotifResourceArtifactTypes = distribNotifResourceArtifactTypes;
	}

	public String getUebPublicKey() {
		return uebPublicKey;
	}

	public void setUebPublicKey(String uebPublicKey) {
		this.uebPublicKey = uebPublicKey;
	}

	public String getUebSecretKey() {
		return uebSecretKey;
	}

	public void setUebSecretKey(String uebSecretKey) {
		this.uebSecretKey = uebSecretKey;
	}

	public List<String> getEnvironments() {
		return environments;
	}

	public void setEnvironments(List<String> environments) {

		Set<String> set = new HashSet<String>();
		if (environments != null) {
			set.addAll(environments);
			this.environments = new ArrayList<String>(set);
		} else {
			this.environments = null;
		}

	}

	public DistributionStatusTopicConfig getDistributionStatusTopic() {
		return distributionStatusTopic;
	}

	public void setDistributionStatusTopic(DistributionStatusTopicConfig distributionStatusTopic) {
		this.distributionStatusTopic = distributionStatusTopic;
	}

	public Integer getInitMaxIntervalSec() {
		return initMaxIntervalSec;
	}

	public void setInitMaxIntervalSec(Integer initMaxIntervalSec) {
		this.initMaxIntervalSec = initMaxIntervalSec;
	}

	public CreateTopicConfig getCreateTopic() {
		return createTopic;
	}

	public void setCreateTopic(CreateTopicConfig createTopic) {
		this.createTopic = createTopic;
	}

	public boolean isStartDistributionEngine() {
		return startDistributionEngine;
	}

	public void setStartDistributionEngine(boolean startDistributionEngine) {
		this.startDistributionEngine = startDistributionEngine;
	}

	public DistributionNotificationTopicConfig getDistributionNotificationTopic() {
		return distributionNotificationTopic;
	}

	public void setDistributionNotificationTopic(DistributionNotificationTopicConfig distributionNotificationTopic) {
		this.distributionNotificationTopic = distributionNotificationTopic;
	}

	public int getDefaultArtifactInstallationTimeout() {
		return defaultArtifactInstallationTimeout;
	}

	public void setDefaultArtifactInstallationTimeout(int defaultArtifactInstallationTimeout) {
		this.defaultArtifactInstallationTimeout = defaultArtifactInstallationTimeout;
	}

	public static class CreateTopicConfig {

		private Integer partitionCount;
		private Integer replicationCount;

		public Integer getPartitionCount() {
			return partitionCount;
		}

		public void setPartitionCount(Integer partitionCount) {
			this.partitionCount = partitionCount;
		}

		public Integer getReplicationCount() {
			return replicationCount;
		}

		public void setReplicationCount(Integer replicationCount) {
			this.replicationCount = replicationCount;
		}

		@Override
		public String toString() {
			return "CreateTopicConfig [partitionCount=" + partitionCount + ", replicationCount=" + replicationCount + "]";
		}

	}

	public static class EnvironmentConfig {

		private String name;
		private List<String> uebServers;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getUebServers() {
			return uebServers;
		}

		public void setUebServers(List<String> uebServers) {
			this.uebServers = uebServers;
		}

		@Override
		public String toString() {
			return "EnvironmentConfig [name=" + name + ", uebServers=" + uebServers + "]";
		}

	}

	public static class DistributionStatusTopicConfig {

		private Integer pollingIntervalSec;
		private Integer fetchTimeSec;
		private String consumerGroup;
		private String consumerId;

		public Integer getPollingIntervalSec() {
			return pollingIntervalSec;
		}

		public void setPollingIntervalSec(Integer pollingIntervalSec) {
			this.pollingIntervalSec = pollingIntervalSec;
		}

		public Integer getFetchTimeSec() {
			return fetchTimeSec;
		}

		public void setFetchTimeSec(Integer fetchTimeSec) {
			this.fetchTimeSec = fetchTimeSec;
		}

		public String getConsumerGroup() {
			return consumerGroup;
		}

		public void setConsumerGroup(String consumerGroup) {
			this.consumerGroup = consumerGroup;
		}

		public String getConsumerId() {
			return consumerId;
		}

		public void setConsumerId(String consumerId) {
			this.consumerId = consumerId;
		}

		@Override
		public String toString() {
			return "DistributionStatusTopicConfig [pollingIntervalSec=" + pollingIntervalSec + ", fetchTimeSec=" + fetchTimeSec + ", consumerGroup=" + consumerGroup + ", consumerId=" + consumerId + "]";
		}

	}

	public static class DistributionNotificationTopicConfig {

		private Integer maxWaitingAfterSendingSeconds;
		private Integer maxThreadPoolSize;
		private Integer minThreadPoolSize;

		public Integer getMaxWaitingAfterSendingSeconds() {
			return maxWaitingAfterSendingSeconds;
		}

		public void setMaxWaitingAfterSendingSeconds(Integer maxWaitingAfterSendingSeconds) {
			this.maxWaitingAfterSendingSeconds = maxWaitingAfterSendingSeconds;
		}

		public Integer getMaxThreadPoolSize() {
			return maxThreadPoolSize;
		}

		public void setMaxThreadPoolSize(Integer maxThreadPoolSize) {
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		public Integer getMinThreadPoolSize() {
			return minThreadPoolSize;
		}

		public void setMinThreadPoolSize(Integer minThreadPoolSize) {
			this.minThreadPoolSize = minThreadPoolSize;
		}

		@Override
		public String toString() {
			return "DistributionNotificationTopicConfig [maxWaitingAfterSendingSeconds=" + maxWaitingAfterSendingSeconds + ", maxThreadPoolSize=" + maxThreadPoolSize + ", minThreadPoolSize=" + minThreadPoolSize + "]";
		}

	}

	public static class ComponentArtifactTypesConfig {

		private List<String> info;
		private List<String> lifecycle;

		public List<String> getInfo() {
			return info;
		}

		public void setInfo(List<String> info) {
			this.info = info;
		}

		public List<String> getLifecycle() {
			return lifecycle;
		}

		public void setLifecycle(List<String> lifecycle) {
			this.lifecycle = lifecycle;
		}

		@Override
		public String toString() {
			return "ArtifactTypesConfig [info=" + info + ", lifecycle=" + lifecycle + "]";
		}

	}

}

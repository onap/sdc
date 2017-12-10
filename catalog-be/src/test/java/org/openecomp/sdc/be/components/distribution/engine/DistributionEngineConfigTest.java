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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class DistributionEngineConfigTest {

	@Before
	public void setup() {
		ExternalConfiguration.setAppName("catalog-be");
		ExternalConfiguration.setConfigDir("src/test/resources/config");
		ExternalConfiguration.listenForChanges();

		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), ExternalConfiguration.getConfigDir() + File.separator + ExternalConfiguration.getAppName());

		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
	}

	@Test
	public void validateMissingEnvironments() {

		DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();

		String uebPublicKey = "uebPublicKey";
		String uebSecretKey = "uebSecretKey";

		DistributionEngine distributionEngine = new DistributionEngine();

		List<String> environments = new ArrayList<String>();
		environments.add("PROD");
		deConfiguration.setEnvironments(environments);

		List<String> servers = new ArrayList<String>();
		servers.add("server1:80");
		servers.add("server2:8080");

		CreateTopicConfig createTopic = new CreateTopicConfig();
		createTopic.setPartitionCount(1);
		createTopic.setReplicationCount(1);
		deConfiguration.setCreateTopic(createTopic);

		ComponentArtifactTypesConfig distribNotifResourceArtifactTypes = new ComponentArtifactTypesConfig();
		deConfiguration.setDistribNotifResourceArtifactTypes(distribNotifResourceArtifactTypes);

		ComponentArtifactTypesConfig distribNotifServiceArtifactTypes = new ComponentArtifactTypesConfig();
		deConfiguration.setDistribNotifServiceArtifactTypes(distribNotifServiceArtifactTypes);

		deConfiguration.setDistributionNotifTopicName("distributionNotifTopicName");
		deConfiguration.setDistributionStatusTopicName("statusTopic");

		DistributionStatusTopicConfig distributionStatusTopic = new DistributionStatusTopicConfig();
		distributionStatusTopic.setConsumerGroup("asdc-group");
		distributionStatusTopic.setConsumerId("asdc-id");
		distributionStatusTopic.setFetchTimeSec(20);
		distributionStatusTopic.setPollingIntervalSec(20);
		deConfiguration.setDistributionStatusTopic(distributionStatusTopic);

		deConfiguration.setUebServers(servers);
		deConfiguration.setUebPublicKey(uebPublicKey);
		deConfiguration.setUebSecretKey(uebSecretKey);
		deConfiguration.setInitMaxIntervalSec(8);
		deConfiguration.setInitRetryIntervalSec(3);

		boolean isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setUebServers(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setUebServers(servers);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setEnvironments(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setEnvironments(environments);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setUebPublicKey(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setUebPublicKey(uebPublicKey);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setUebSecretKey(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setUebSecretKey(uebPublicKey);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setDistributionNotifTopicName(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setDistributionNotifTopicName(uebPublicKey);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setDistributionStatusTopicName(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setDistributionStatusTopicName(uebPublicKey);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setInitMaxIntervalSec(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setInitMaxIntervalSec(8);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

		deConfiguration.setInitRetryIntervalSec(null);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertFalse("check empty configuration", isValid);

		deConfiguration.setInitRetryIntervalSec(8);
		isValid = distributionEngine.validateConfiguration(deConfiguration);
		assertTrue("check empty configuration", isValid);

	}

}

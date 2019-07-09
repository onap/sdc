/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.common.http.config.ExternalServiceConfig;

import java.util.List;


public class DistributionEngineConfigurationTest {

	private DistributionEngineConfiguration createTestSubject() {
		return new DistributionEngineConfiguration();
	}

	
	@Test
	public void testGetUebServers() throws Exception {
		DistributionEngineConfiguration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebServers();
	}

	
	@Test
	public void testSetUebServers() throws Exception {
		DistributionEngineConfiguration testSubject;
		List<String> uebServers = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setUebServers(uebServers);
	}

	
	@Test
	public void testGetDistributionNotifTopicName() throws Exception {
		DistributionEngineConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionNotifTopicName();
	}

	
	@Test
	public void testSetDistributionNotifTopicName() throws Exception {
		DistributionEngineConfiguration testSubject;
		String distributionNotifTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionNotifTopicName(distributionNotifTopicName);
	}

	
	@Test
	public void testGetDistributionStatusTopicName() throws Exception {
		DistributionEngineConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatusTopicName();
	}

	
	@Test
	public void testSetDistributionStatusTopicName() throws Exception {
		DistributionEngineConfiguration testSubject;
		String distributionStatusTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatusTopicName(distributionStatusTopicName);
	}

	
	@Test
	public void testGetInitRetryIntervalSec() throws Exception {
		DistributionEngineConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInitRetryIntervalSec();
	}

	
	@Test
	public void testSetInitRetryIntervalSec() throws Exception {
		DistributionEngineConfiguration testSubject;
		Integer initRetryIntervalSec = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setInitRetryIntervalSec(initRetryIntervalSec);
	}

	
	@Test
	public void testGetDistribNotifServiceArtifactTypes() throws Exception {
		DistributionEngineConfiguration testSubject;
		ComponentArtifactTypesConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistribNotifServiceArtifactTypes();
	}

	
	@Test
	public void testSetDistribNotifServiceArtifactTypes() throws Exception {
		DistributionEngineConfiguration testSubject;
		ComponentArtifactTypesConfig distribNotifServiceArtifactTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistribNotifServiceArtifactTypes(distribNotifServiceArtifactTypes);
	}

	
	@Test
	public void testGetDistribNotifResourceArtifactTypes() throws Exception {
		DistributionEngineConfiguration testSubject;
		ComponentArtifactTypesConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistribNotifResourceArtifactTypes();
	}

	
	@Test
	public void testSetDistribNotifResourceArtifactTypes() throws Exception {
		DistributionEngineConfiguration testSubject;
		ComponentArtifactTypesConfig distribNotifResourceArtifactTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistribNotifResourceArtifactTypes(distribNotifResourceArtifactTypes);
	}

	
	@Test
	public void testGetUebPublicKey() throws Exception {
		DistributionEngineConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebPublicKey();
	}

	
	@Test
	public void testSetUebPublicKey() throws Exception {
		DistributionEngineConfiguration testSubject;
		String uebPublicKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebPublicKey(uebPublicKey);
	}

	
	@Test
	public void testGetUebSecretKey() throws Exception {
		DistributionEngineConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebSecretKey();
	}

	
	@Test
	public void testSetUebSecretKey() throws Exception {
		DistributionEngineConfiguration testSubject;
		String uebSecretKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebSecretKey(uebSecretKey);
	}

	
	@Test
	public void testGetEnvironments() throws Exception {
		DistributionEngineConfiguration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironments();
	}

	
	
	@Test
	public void testSetEnvironments() throws Exception {
		DistributionEngineConfiguration testSubject;
		List<String> environments = null;

		// test 1
		testSubject = createTestSubject();
		environments = null;
		testSubject.setEnvironments(environments);
	}

	
	@Test
	public void testGetDistributionStatusTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		DistributionStatusTopicConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatusTopic();
	}

	
	@Test
	public void testSetDistributionStatusTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		DistributionStatusTopicConfig distributionStatusTopic = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatusTopic(distributionStatusTopic);
	}

	
	@Test
	public void testGetInitMaxIntervalSec() throws Exception {
		DistributionEngineConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInitMaxIntervalSec();
	}

	
	@Test
	public void testSetInitMaxIntervalSec() throws Exception {
		DistributionEngineConfiguration testSubject;
		Integer initMaxIntervalSec = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setInitMaxIntervalSec(initMaxIntervalSec);
	}

	
	@Test
	public void testGetCreateTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		CreateTopicConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreateTopic();
	}

	
	@Test
	public void testSetCreateTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		CreateTopicConfig createTopic = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreateTopic(createTopic);
	}

	
	@Test
	public void testIsStartDistributionEngine() throws Exception {
		DistributionEngineConfiguration testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isStartDistributionEngine();
	}

	
	@Test
	public void testSetStartDistributionEngine() throws Exception {
		DistributionEngineConfiguration testSubject;
		boolean startDistributionEngine = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setStartDistributionEngine(startDistributionEngine);
	}

	
	@Test
	public void testGetDistributionNotificationTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		DistributionNotificationTopicConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionNotificationTopic();
	}

	
	@Test
	public void testSetDistributionNotificationTopic() throws Exception {
		DistributionEngineConfiguration testSubject;
		DistributionNotificationTopicConfig distributionNotificationTopic = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionNotificationTopic(distributionNotificationTopic);
	}

	
	@Test
	public void testGetDefaultArtifactInstallationTimeout() throws Exception {
		DistributionEngineConfiguration testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultArtifactInstallationTimeout();
	}

	
	@Test
	public void testSetDefaultArtifactInstallationTimeout() throws Exception {
		DistributionEngineConfiguration testSubject;
		int defaultArtifactInstallationTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultArtifactInstallationTimeout(defaultArtifactInstallationTimeout);
	}

	
	@Test
	public void testIsUseHttpsWithDmaap() throws Exception {
		DistributionEngineConfiguration testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isUseHttpsWithDmaap();
	}

	
	@Test
	public void testSetUseHttpsWithDmaap() throws Exception {
		DistributionEngineConfiguration testSubject;
		boolean useHttpsWithDmaap = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setUseHttpsWithDmaap(useHttpsWithDmaap);
	}


	
	@Test
	public void testGetAaiConfig() throws Exception {
	DistributionEngineConfiguration testSubject;ExternalServiceConfig result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getAaiConfig();
	}


	
	@Test
	public void testGetAllowedTimeBeforeStaleSec() throws Exception {
	DistributionEngineConfiguration testSubject;Integer result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getAllowedTimeBeforeStaleSec();
	}


	
	@Test
	public void testGetCurrentArtifactInstallationTimeout() throws Exception {
	DistributionEngineConfiguration testSubject;int result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getCurrentArtifactInstallationTimeout();
	}


	
	@Test
	public void testGetCurrentArtifactInstallationTimeout_1() throws Exception {
	DistributionEngineConfiguration testSubject;int result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getCurrentArtifactInstallationTimeout();
	}


	
	@Test
	public void testSetCurrentArtifactInstallationTimeout() throws Exception {
	DistributionEngineConfiguration testSubject;int currentArtifactInstallationTimeout = 0;
	
	
	// default test
	testSubject=createTestSubject();testSubject.setCurrentArtifactInstallationTimeout(currentArtifactInstallationTimeout);
	}


	
	@Test
	public void testGetOpEnvRecoveryIntervalSec() throws Exception {
	DistributionEngineConfiguration testSubject;Integer result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.getOpEnvRecoveryIntervalSec();
	}


	
	@Test
	public void testSetOpEnvRecoveryIntervalSec() throws Exception {
	DistributionEngineConfiguration testSubject;Integer opEnvRecoveryIntervalSec = 0;
	
	
	// default test
	testSubject=createTestSubject();testSubject.setOpEnvRecoveryIntervalSec(opEnvRecoveryIntervalSec);
	}


	
	@Test
	public void testGetAllowedTimeBeforeStaleSec_1() throws Exception {
	DistributionEngineConfiguration testSubject;Integer result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getAllowedTimeBeforeStaleSec();
	}


	
	@Test
	public void testSetAllowedTimeBeforeStaleSec() throws Exception {
	DistributionEngineConfiguration testSubject;Integer allowedTimeBeforeStaleSec = 0;
	
	
	// default test
	testSubject=createTestSubject();testSubject.setAllowedTimeBeforeStaleSec(allowedTimeBeforeStaleSec);
	}


	
	@Test
	public void testGetAaiConfig_1() throws Exception {
	DistributionEngineConfiguration testSubject;ExternalServiceConfig result;
	
	// default test 1
	testSubject=createTestSubject();result=testSubject.getAaiConfig();
	}


	
	@Test
	public void testSetAaiConfig() throws Exception {
	DistributionEngineConfiguration testSubject;ExternalServiceConfig aaiConfig = null;
	
	
	// default test
	testSubject=createTestSubject();testSubject.setAaiConfig(aaiConfig);
	}


	
	@Test
	public void testGetMsoConfig() throws Exception {
	DistributionEngineConfiguration testSubject;ExternalServiceConfig result;
	
	// default test
	testSubject=createTestSubject();result=testSubject.getMsoConfig();
	}


	
	@Test
	public void testSetMsoConfig() throws Exception {
	DistributionEngineConfiguration testSubject;ExternalServiceConfig msoConfig = null;
	
	
	// default test
	testSubject=createTestSubject();testSubject.setMsoConfig(msoConfig);
	}
	
	private ComponentArtifactTypesConfig createTestSubject2() {
		return new DistributionEngineConfiguration.ComponentArtifactTypesConfig();
	}

	
	@Test
	public void testGetInfo() throws Exception {
		ComponentArtifactTypesConfig testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject2();
		result = testSubject.getInfo();
	}

	
	@Test
	public void testSetInfo() throws Exception {
		ComponentArtifactTypesConfig testSubject;
		List<String> info = null;

		// default test
		testSubject = createTestSubject2();
		testSubject.setInfo(info);
	}

	
	@Test
	public void testGetLifecycle() throws Exception {
		ComponentArtifactTypesConfig testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject2();
		result = testSubject.getLifecycle();
	}

	
	@Test
	public void testSetLifecycle() throws Exception {
		ComponentArtifactTypesConfig testSubject;
		List<String> lifecycle = null;

		// default test
		testSubject = createTestSubject2();
		testSubject.setLifecycle(lifecycle);
	}

	
	@Test
	public void testToString() throws Exception {
		ComponentArtifactTypesConfig testSubject;
		String result;

		// default test
		testSubject = createTestSubject2();
		result = testSubject.toString();
	}
	
	
}

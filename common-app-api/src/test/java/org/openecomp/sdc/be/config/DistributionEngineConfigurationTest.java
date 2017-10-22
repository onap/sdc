package org.openecomp.sdc.be.config;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.ComponentArtifactTypesConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionNotificationTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;


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
}
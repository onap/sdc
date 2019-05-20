package org.openecomp.sdc.be.components.health;

import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;
import java.util.List;

public class HealthCheckBusinessLogicTest extends BeConfDependentTest{

	private HealthCheckBusinessLogic createTestSubject() {
		HealthCheckBusinessLogic healthCheckBusinessLogic = new HealthCheckBusinessLogic();
		DmaapHealth dmaapHealth = new DmaapHealth();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "dmaapHealth", dmaapHealth);
		DistributionEngineClusterHealth distributionEngineClusterHealth = new DistributionEngineClusterHealth();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "distributionEngineClusterHealth", distributionEngineClusterHealth);
		SwitchoverDetector switchoverDetector = new SwitchoverDetector();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "switchoverDetector", switchoverDetector);
		List<HealthCheckInfo> prevBeHealthCheckInfos = new LinkedList<>(); 
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "prevBeHealthCheckInfos", prevBeHealthCheckInfos);
		return healthCheckBusinessLogic;
	}

	@Test
	public void testInit() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.init();
	}

	@Test
	public void testIsDistributionEngineUp() throws Exception {
		HealthCheckBusinessLogic testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDistributionEngineUp();
	}

	@Test
	public void testGetBeHealthCheckInfosStatus() throws Exception {
		HealthCheckBusinessLogic testSubject;
		Pair<Boolean, List<HealthCheckInfo>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeHealthCheckInfosStatus();
	}

	@Test
	public void testGetBeHealthCheckInfos() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> result;
		
		
		
		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getBeHealthCheckInfos");
	}

	@Test
	public void testGetEsHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getEsHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetBeHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getBeHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetDmaapHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getDmaapHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetJanusGraphHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJanusGraphHealthCheck(healthCheckInfos);
	}

	@Test
	public void testGetCassandraHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getCassandraHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetDistributionEngineCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getDistributionEngineCheck", healthCheckInfos);
	}

	@Test
	public void testGetAmdocsHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getAmdocsHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetDcaeHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getDcaeHealthCheck", healthCheckInfos);
	}

	@Test
	public void testGetHostedComponentsBeHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String componentName = "mock";
		String healthCheckUrl = "mock";
		HealthCheckInfo result;

		// test 1
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck",
				componentName, healthCheckUrl);

		// test 2
		testSubject = createTestSubject();
		healthCheckUrl = "";
		result = Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck",
				componentName, healthCheckUrl);
	}

	@Test
	public void testDestroy() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "destroy");
	}

	@Test
	public void testLogAlarm() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String componentChangedMsg = "mock";

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "logAlarm", componentChangedMsg);
	}

	@Test
	public void testGetSiteMode() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSiteMode();
	}

	@Test
	public void testAnyStatusChanged() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> beHealthCheckInfos = null;
		List<HealthCheckInfo> prevBeHealthCheckInfos = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		beHealthCheckInfos = null;
		prevBeHealthCheckInfos = null;
		result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
		Assert.assertEquals(false, result);

		// test 2
		testSubject = createTestSubject();
		prevBeHealthCheckInfos = null;
		beHealthCheckInfos = null;
		result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
		Assert.assertEquals(false, result);

		// test 3
		testSubject = createTestSubject();
		beHealthCheckInfos = null;
		prevBeHealthCheckInfos = null;
		result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
		Assert.assertEquals(false, result);

		// test 4
		testSubject = createTestSubject();
		prevBeHealthCheckInfos = null;
		beHealthCheckInfos = null;
		result = testSubject.anyStatusChanged(beHealthCheckInfos, prevBeHealthCheckInfos);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testBuildOnBoardingHealthCheckUrl() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildOnBoardingHealthCheckUrl");
	}

	@Test
	public void testBuildDcaeHealthCheckUrl() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildDcaeHealthCheckUrl");
	}
}
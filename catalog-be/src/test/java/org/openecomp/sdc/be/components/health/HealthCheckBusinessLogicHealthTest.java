/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.health;

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.openecomp.sdc.be.catalog.impl.DmaapProducerHealth;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_DCAE;
import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_ON_BOARDING;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckBusinessLogicHealthTest extends BeConfDependentTest {
	private DmaapProducerHealth dmaapProducerHealth = mock(DmaapProducerHealth.class);
	private HealthCheckInfo dmaapProducerHealthCheckInfo = mock(HealthCheckInfo.class);

	private SwitchoverDetector switchoverDetector;

	private HealthCheckBusinessLogic createTestSubject() {

		HealthCheckBusinessLogic healthCheckBusinessLogic = new HealthCheckBusinessLogic();
		DmaapHealth dmaapHealth = new DmaapHealth();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "dmaapHealth", dmaapHealth);
		PortalHealthCheckBuilder portalHealthCheckBuilder = new PortalHealthCheckBuilder();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "portalHealthCheck", portalHealthCheckBuilder);
		DistributionEngineClusterHealth distributionEngineClusterHealth = new DistributionEngineClusterHealth();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "distributionEngineClusterHealth",
				distributionEngineClusterHealth);
		SwitchoverDetector switchoverDetector = new SwitchoverDetector();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "switchoverDetector", switchoverDetector);
		List<HealthCheckInfo> prevBeHealthCheckInfos = new LinkedList<>();
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "prevBeHealthCheckInfos", prevBeHealthCheckInfos);
		ReflectionTestUtils.setField(healthCheckBusinessLogic, "dmaapProducerHealth", dmaapProducerHealth);
		return healthCheckBusinessLogic;
	}



	@Before
	public void beforeTest() {
		when(dmaapProducerHealth.getHealthCheckInfo())
				.thenReturn(dmaapProducerHealthCheckInfo);
	}

	@Test
	public void testInit() throws Exception {
		HealthCheckBusinessLogic testSubject = createTestSubject();
		testSubject.init();
	}

	@Test
	public void testIsDistributionEngineUp() throws Exception {
		HealthCheckBusinessLogic testSubject;
		// default test
		testSubject = createTestSubject();
		testSubject.isDistributionEngineUp();
	}

	@Test
	public void testGetBeHealthCheckInfosStatus() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.getBeHealthCheckInfosStatus();
	}

	@Test
	public void testGetBeHealthCheckInfos() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getBeHealthCheckInfos");
	}

	@Test
	public void testGetDmaapHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getDmaapHealthCheck");
	}

	@Test
	public void testGetJanusGraphHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
//		testSubject.getJanusGraphHealthCheck(healthCheckInfos);
		healthCheckInfos.add(testSubject.getJanusGraphHealthCheck());
	}

	@Test
	public void testGetCassandraHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getCassandraHealthCheck");
	}

	@Test
	public void testGetDistributionEngineCheck() throws Exception {
//		HealthCheckBusinessLogic testSubject;
		DistributionEngineClusterHealth testSubject = new DistributionEngineClusterHealth();

		// default test
		Deencapsulation.invoke(testSubject, "getHealthCheckInfo");
	}

	@Test
	public void testGetAmdocsHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		String url = testSubject.buildOnBoardingHealthCheckUrl();
		Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck", HC_COMPONENT_ON_BOARDING, url);
	}

	@Test
	public void testGetPortalHealthCheckSuccess() throws Exception {
		PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
		String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
		int timeout = 3000;
		doReturn(200).when(testSubject).getStatusCode(eq(healthCheckURL), eq(timeout));
		testSubject.init();
		testSubject.runTask();
		HealthCheckInfo hci = testSubject.getHealthCheckInfo();
		Assert.assertEquals("PORTAL", hci.getHealthCheckComponent());
		Assert.assertEquals(HealthCheckInfo.HealthCheckStatus.UP, hci.getHealthCheckStatus());
		Assert.assertEquals("OK", hci.getDescription());
	}

	@Test
	public void testGetPortalHealthCheckFailureMissingConfig() throws Exception{
		PortalHealthCheckBuilder testSubject = new PortalHealthCheckBuilder();
		testSubject.init(null);
		HealthCheckInfo hci = testSubject.getHealthCheckInfo();
		Assert.assertEquals("PORTAL", hci.getHealthCheckComponent());
		Assert.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
		Assert.assertEquals("PORTAL health check configuration is missing", hci.getDescription());
	}


	@Test
	public void testGetPortalHealthCheckFailureErrorResponse() throws HttpExecuteException, CipherUtilException {
		PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
		String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
		int timeout = 3000;
//		when(testSubject.getStatusCode(healthCheckURL,timeout)).thenReturn(404);
		doReturn(404).when(testSubject).getStatusCode(eq(healthCheckURL), eq(timeout));
		testSubject.init(testSubject.getConfiguration());
		testSubject.runTask();
		HealthCheckInfo hci = testSubject.getHealthCheckInfo();
		Assert.assertEquals("PORTAL", hci.getHealthCheckComponent());
		Assert.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
		Assert.assertEquals("PORTAL responded with 404 status code", hci.getDescription());
	}

	@Test
	public void testGetPortalHealthCheckFailureNoResponse() throws HttpExecuteException, CipherUtilException {
		PortalHealthCheckBuilder testSubject = spy(PortalHealthCheckBuilder.class);
		String healthCheckURL = testSubject.buildPortalHealthCheckUrl();
		int timeout = 3000;
//		when(testSubject.getStatusCode(healthCheckURL, timeout)).thenThrow(HttpExecuteException.class);
		doThrow(HttpExecuteException.class).when(testSubject).getStatusCode(eq(healthCheckURL), eq(timeout));
		testSubject.init(testSubject.getConfiguration());
		testSubject.runTask();
		HealthCheckInfo hci = testSubject.getHealthCheckInfo();
		Assert.assertEquals("PORTAL", hci.getHealthCheckComponent());
		Assert.assertEquals(HealthCheckInfo.HealthCheckStatus.DOWN, hci.getHealthCheckStatus());
		Assert.assertEquals("PORTAL is not available", hci.getDescription());
	}

	@Test
	public void testGetDcaeHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		List<HealthCheckInfo> healthCheckInfos = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		String url = testSubject.buildDcaeHealthCheckUrl();
		Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck", HC_COMPONENT_DCAE, url);
	}

	@Test
	public void testGetHostedComponentsBeHealthCheck() throws Exception {
		HealthCheckBusinessLogic testSubject;
		String componentName = "mock";
		String healthCheckUrl = "mock";
		// test 1
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck", componentName, healthCheckUrl);

		// test 2
		testSubject = createTestSubject();
		healthCheckUrl = "";
		Deencapsulation.invoke(testSubject, "getHostedComponentsBeHealthCheck", componentName, healthCheckUrl);
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

		// default test
		testSubject = createTestSubject();
		testSubject.getSiteMode();
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

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "buildOnBoardingHealthCheckUrl");
	}

	@Test
	public void testBuildDcaeHealthCheckUrl() throws Exception {
		HealthCheckBusinessLogic testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "buildDcaeHealthCheckUrl");
	}
}
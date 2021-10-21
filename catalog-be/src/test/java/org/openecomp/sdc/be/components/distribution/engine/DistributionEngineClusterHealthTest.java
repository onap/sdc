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

package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth.HealthCheckScheduledTask;
import org.openecomp.sdc.common.api.HealthCheckInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DistributionEngineClusterHealthTest extends BeConfDependentTest{

	private DistributionEngineClusterHealth createTestSubject() {
		return new DistributionEngineClusterHealth();
	}

	@Test
	public void testStartHealthCheckTask() throws Exception {
		DistributionEngineClusterHealth testSubject;
		Map<String, AtomicBoolean> envNamePerStatus = null;
		boolean startTask = false;

		// default test
		testSubject = createTestSubject();
		testSubject.startHealthCheckTask(envNamePerStatus, startTask);
	}

	@Test
	public void testStartHealthCheckTask_1() {
	    final DistributionEngineClusterHealth distributionEngineClusterHealth = new DistributionEngineClusterHealth();
	    final Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
	    distributionEngineClusterHealth.init("myKey");
	    distributionEngineClusterHealth.startHealthCheckTask(envNamePerStatus);
	}
	
	@Test
	public void testHealthCheckScheduledTaskRun() throws Exception {
		DistributionEngineClusterHealth testSubject;
		Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
		envNamePerStatus.put("mock", new AtomicBoolean(true));
		// default test
		testSubject = createTestSubject();
		testSubject.startHealthCheckTask(envNamePerStatus, false);
		HealthCheckScheduledTask healthCheckScheduledTask = testSubject. new HealthCheckScheduledTask(new LinkedList<>());
		LinkedList<UebHealthCheckCall> healthCheckCalls = new LinkedList<>();
		UebHealthCheckCall hcc = new UebHealthCheckCall("mock", "mock");
		healthCheckCalls.add(hcc);
		healthCheckScheduledTask.healthCheckCalls = healthCheckCalls;
		
		healthCheckScheduledTask.run();
	}
	
	@Test
	public void testGetHealthCheckInfo() throws Exception {
		DistributionEngineClusterHealth testSubject;
		HealthCheckInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckInfo();
	}

	@Test
	public void testSetHealthCheckUebIsDisabled() throws Exception {
		DistributionEngineClusterHealth testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setHealthCheckUebIsDisabled();
	}

	@Test
	public void testSetHealthCheckUebConfigurationError() throws Exception {
		DistributionEngineClusterHealth testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setHealthCheckUebConfigurationError();
	}

	@Test
	public void testSetHealthCheckOkAndReportInCaseLastStateIsDown() throws Exception {
		DistributionEngineClusterHealth testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setHealthCheckOkAndReportInCaseLastStateIsDown();
	}
}

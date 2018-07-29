package org.openecomp.sdc.be.components.distribution.engine;

import mockit.Deencapsulation;
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
	public void testInit() throws Exception {
		DistributionEngineClusterHealth testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "init");
	}

	@Test
	public void testDestroy() throws Exception {
		DistributionEngineClusterHealth testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "destroy");
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
	public void testStartHealthCheckTask_1() throws Exception {
		DistributionEngineClusterHealth testSubject;
		Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		testSubject.init();
		testSubject.startHealthCheckTask(envNamePerStatus);
	}

	@Test
	public void testHealthCheckScheduledTask() throws Exception {
		DistributionEngineClusterHealth testSubject;
		Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		HealthCheckScheduledTask healthCheckScheduledTask = testSubject. new HealthCheckScheduledTask(new LinkedList<>());
		LinkedList<UebHealthCheckCall> healthCheckCalls = new LinkedList<>();
		UebHealthCheckCall hcc = new UebHealthCheckCall("mock", "mock");
		healthCheckCalls.add(hcc);
		healthCheckScheduledTask.healthCheckCalls = healthCheckCalls;
		
		Deencapsulation.invoke(healthCheckScheduledTask, "queryUeb");
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
		
		Deencapsulation.invoke(healthCheckScheduledTask, "run");
	}
	
	@Test
	public void testHealthCheckScheduledTaskRun_2() throws Exception {
		DistributionEngineClusterHealth testSubject;
		Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
		envNamePerStatus.put("mock", new AtomicBoolean(false));
		// default test
		testSubject = createTestSubject();
		testSubject.startHealthCheckTask(envNamePerStatus, false);
		HealthCheckScheduledTask healthCheckScheduledTask = testSubject. new HealthCheckScheduledTask(new LinkedList<>());
		LinkedList<UebHealthCheckCall> healthCheckCalls = new LinkedList<>();
		UebHealthCheckCall hcc = new UebHealthCheckCall("mock", "mock");
		healthCheckCalls.add(hcc);
		healthCheckScheduledTask.healthCheckCalls = healthCheckCalls;
		
		Deencapsulation.invoke(healthCheckScheduledTask, "run");
	}
	
	@Test
	public void testLogAlarm() throws Exception {
		DistributionEngineClusterHealth testSubject;
		boolean lastHealthState = false;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "logAlarm", new Object[] { lastHealthState });
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
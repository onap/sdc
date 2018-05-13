package org.openecomp.sdc.be.components.distribution.engine;

import java.net.URISyntaxException;

import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.common.api.HealthCheckInfo;

import mockit.Deencapsulation;

public class DmaapHealthTest extends BeConfDependentTest{

	private DmaapHealth createTestSubject() {
		return new DmaapHealth();
	}

	@Test
	public void testInit() throws Exception {
		DmaapHealth testSubject;
		DmaapHealth result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.init();
	}

	@Test
	public void testDestroy() throws Exception {
		DmaapHealth testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "destroy");
	}

	@Test
	public void testStartHealthCheckTask() throws Exception {
		DmaapHealth testSubject;
		boolean startTask = false;

		// default test
		testSubject = createTestSubject();
		testSubject.startHealthCheckTask(startTask);
	}

	@Test
	public void testReport() throws Exception {
		DmaapHealth testSubject;
		Boolean isUp = false;

		// default test
		testSubject = createTestSubject();
		testSubject.report(isUp);
	}

	@Test
	public void testLogAlarm() throws Exception {
		DmaapHealth testSubject;
		boolean lastHealthState = false;

		// default test
		testSubject = createTestSubject();
		testSubject.logAlarm(lastHealthState);
	}

	@Test
	public void testGetConfiguration() throws Exception {
		DmaapHealth testSubject;
		DmaapConsumerConfiguration result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConfiguration();
	}

	@Test
	public void testGetHealthCheckInfo() throws Exception {
		DmaapHealth testSubject;
		HealthCheckInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckInfo();
	}

	@Test(expected=URISyntaxException.class)
	public void testGetUrlHost() throws Exception {
		String qualifiedHost = "";
		String result;

		// default test
		result = DmaapHealth.getUrlHost(qualifiedHost);
	}
	
	@Test
	public void testGetUrlHost_2() throws Exception {
		String qualifiedHost = "www.mock.com";
		String result;

		// default test
		result = DmaapHealth.getUrlHost(qualifiedHost);
	}
}
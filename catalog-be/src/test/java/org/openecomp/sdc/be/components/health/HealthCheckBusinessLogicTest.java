package org.openecomp.sdc.be.components.health;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.api.HealthCheckInfo;

public class HealthCheckBusinessLogicTest {

	private HealthCheckBusinessLogic createTestSubject() {
		return new HealthCheckBusinessLogic();
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

}
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.switchover.detector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.config.Configuration.SwitchoverDetectorConfig;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector.SwitchoverDetectorGroup;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector.SwitchoverDetectorScheduledTask;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector.SwitchoverDetectorState;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SwitchoverDetectorTest {

	private static final String SITEMODE = "SITEMODE";
	@Mock
	private SwitchoverDetectorConfig config;
	@Mock
	private SwitchoverDetectorScheduledTask task;
	@Mock
	private ScheduledExecutorService switchoverDetectorScheduler;

	@Test
	public void shouldSwitchoverDetectorStateGotCorrectStates() {
		assertEquals(SwitchoverDetectorState.ACTIVE.getState(), "active");
		assertEquals(SwitchoverDetectorState.STANDBY.getState(), "standby");
		assertEquals(SwitchoverDetectorState.UNKNOWN.getState(), "unknown");
	}

	@Test
	public void shouldSwitchoverDetectorGroupGotCorrectGroup() {
		assertEquals(SwitchoverDetectorGroup.BE_SET.getGroup(), "beSet");
		assertEquals(SwitchoverDetectorGroup.FE_SET.getGroup(), "feSet");
	}

	@Test
	public void shouldSwitchoverDetectorSetSiteMode() {
		SwitchoverDetector switchoverDetector = new SwitchoverDetector();
		switchoverDetector.setSiteMode(SITEMODE);
		assertEquals(switchoverDetector.getSiteMode(), SITEMODE);
	}

	@Test
	public void shouldStartSwitchoverDetectorTask() {
		SwitchoverDetector switchoverDetector = new SwitchoverDetector();
		switchoverDetector.setSwitchoverDetectorConfig(config);
		switchoverDetector.switchoverDetectorScheduledTask = task;
		switchoverDetector.switchoverDetectorScheduler = switchoverDetectorScheduler;
		switchoverDetector.startSwitchoverDetectorTask();
		Mockito.verify(switchoverDetectorScheduler).scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS);
	}
}

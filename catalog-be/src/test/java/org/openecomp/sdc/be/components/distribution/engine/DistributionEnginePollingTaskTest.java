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

import mockit.Deencapsulation;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

public class DistributionEnginePollingTaskTest extends BeConfDependentTest {

	@Mock
	private ComponentsUtils componentsUtils;

	private DistributionEnginePollingTask createTestSubject() {
		componentsUtils = Mockito.mock(ComponentsUtils.class);
		DistributionEngineConfiguration distributionEngineConfiguration = configurationManager
				.getDistributionEngineConfiguration();

		return new DistributionEnginePollingTask(distributionEngineConfiguration,
				new DistributionCompleteReporterMock(), componentsUtils, new DistributionEngineClusterHealth(),
				new OperationalEnvironmentEntry());
	}

	@Test
	public void testStartTask() throws Exception {
		DistributionEnginePollingTask testSubject;
		String topicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.startTask(topicName);
	}

	@Test
	public void testStopTask() throws Exception {
		DistributionEnginePollingTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.stopTask();
	}

	@Test
	public void testDestroy() throws Exception {
		DistributionEnginePollingTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.destroy();
	}

	@Test
	public void testRun() throws Exception {
		DistributionEnginePollingTask testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.run();
	}
	
	@Test
	public void testHandleDistributionNotificationMsg() throws Exception {
		DistributionEnginePollingTask testSubject;
		DistributionStatusNotification notification = new DistributionStatusNotification();
		notification.setDistributionID("mock");
		notification.setConsumerID("mock");
		notification.setArtifactURL("mock");
		notification.setTimestamp(435435);
		notification.setStatus(DistributionStatusNotificationEnum.ALREADY_DEPLOYED);
		notification.setErrorReason("mock");
		
		
		
		// default test
		testSubject = createTestSubject();
		Mockito.doNothing().when(componentsUtils).auditDistributionStatusNotification( Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), 
				Mockito.anyString(), Mockito.anyString());
		Deencapsulation.invoke(testSubject, "handleDistributionNotificationMsg",
				notification);
	}

	@Test
	public void testShutdownExecutor() throws Exception {
		DistributionEnginePollingTask testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "shutdownExecutor");
	}

	private class DistributionCompleteReporterMock implements DistributionCompleteReporter {

		@Override
		public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
			// TODO Auto-generated method stub

		}

	}
}

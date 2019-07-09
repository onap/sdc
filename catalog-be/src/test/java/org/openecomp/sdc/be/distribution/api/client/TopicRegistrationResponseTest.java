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

package org.openecomp.sdc.be.distribution.api.client;

import org.junit.Test;

public class TopicRegistrationResponseTest {

	private TopicRegistrationResponse createTestSubject() {
		return new TopicRegistrationResponse();
	}

	@Test
	public void testSetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String distrNotificationTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistrNotificationTopicName(distrNotificationTopicName);
	}

	@Test
	public void testSetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String distrStatusTopicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistrStatusTopicName(distrStatusTopicName);
	}

	@Test
	public void testGetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrNotificationTopicName();
	}

	@Test
	public void testGetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrStatusTopicName();
	}
}

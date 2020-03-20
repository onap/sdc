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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TopicRegistrationResponseTest {
	private static final String NOTIF_TOPIC_NAME = "notif-mock-topic-name";
	private static final String STATUS_TOPIC_NAME = "status-mock-topic-name";

	private TopicRegistrationResponse createTestSubject() {
		return new TopicRegistrationResponse();
	}

	@Test
	public void testSetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		// default test
		testSubject = createTestSubject();
		testSubject.setDistrNotificationTopicName(NOTIF_TOPIC_NAME);
		assertThat(testSubject).hasFieldOrPropertyWithValue("distrNotificationTopicName", NOTIF_TOPIC_NAME);
	}

	@Test
	public void testSetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistrStatusTopicName(STATUS_TOPIC_NAME);
		assertThat(testSubject).hasFieldOrPropertyWithValue("distrStatusTopicName", STATUS_TOPIC_NAME);
	}

	@Test
	public void testGetDistrNotificationTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrNotificationTopicName();
		assertThat(result).isNull();
	}

	@Test
	public void testGetDistrStatusTopicName() throws Exception {
		TopicRegistrationResponse testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrStatusTopicName();
		assertThat(result).isNull();
	}
}

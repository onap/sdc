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

import java.util.LinkedList;
import java.util.List;

public class RegistrationRequestTest {

	private RegistrationRequest createTestSubject() {
		return new RegistrationRequest("", "", false);
	}
	
	@Test
	public void testConstructor() throws Exception {
		List<String> distEnvEndPoints = new LinkedList<>();
		new RegistrationRequest("mock", "mock", distEnvEndPoints , false);
	}
	
	@Test
	public void testGetApiPublicKey() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiPublicKey();
	}

	@Test
	public void testGetDistrEnvName() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrEnvName();
	}

	@Test
	public void testGetIsConsumerToSdcDistrStatusTopic() throws Exception {
		RegistrationRequest testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsConsumerToSdcDistrStatusTopic();
	}

	@Test
	public void testGetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistEnvEndPoints();
	}

	@Test
	public void testSetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> distEnvEndPoints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistEnvEndPoints(distEnvEndPoints);
	}
}

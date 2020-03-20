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

import java.util.LinkedList;
import java.util.List;

public class RegistrationRequestTest {
	private static final String API_KEY = "mock-api-key";
	private static final String DISTR_ENV_NAME = "mock-distr-env-name";

	private RegistrationRequest createTestSubject() {
		return new RegistrationRequest(API_KEY, DISTR_ENV_NAME, false);
	}
	
	@Test
	public void testConstructor() throws Exception {
		List<String> distEnvEndPoints = new LinkedList<>();
		RegistrationRequest request = new RegistrationRequest(API_KEY, DISTR_ENV_NAME, distEnvEndPoints , false);
		assertThat(request)
				.isInstanceOf(RegistrationRequest.class)
				.hasFieldOrPropertyWithValue("apiPublicKey", API_KEY)
				.hasFieldOrPropertyWithValue("distrEnvName", DISTR_ENV_NAME)
				.hasFieldOrPropertyWithValue("distEnvEndPoints", distEnvEndPoints);
	}
	
	@Test
	public void testGetApiPublicKey() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiPublicKey();
		assertThat(result).isEqualTo(API_KEY);
	}

	@Test
	public void testGetDistrEnvName() throws Exception {
		RegistrationRequest testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistrEnvName();
		assertThat(result).isEqualTo(DISTR_ENV_NAME);
	}

	@Test
	public void testGetIsConsumerToSdcDistrStatusTopic() throws Exception {
		RegistrationRequest testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsConsumerToSdcDistrStatusTopic();
		assertThat(result).isFalse();
	}

	@Test
	public void testGetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistEnvEndPoints();
		assertThat(result).isNull();
	}

	@Test
	public void testSetDistEnvEndPoints() throws Exception {
		RegistrationRequest testSubject;
		List<String> distEnvEndPoints = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setDistEnvEndPoints(distEnvEndPoints);
		assertThat(testSubject).hasFieldOrPropertyWithValue("distEnvEndPoints", distEnvEndPoints);
	}
}

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
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.HashSet;
import java.util.List;

public class EnvironmentMessageBusDataTest {

	private EnvironmentMessageBusData createTestSubject() {
		return new EnvironmentMessageBusData();
	}

	@Test
	public void testConstructor() throws Exception {
		OperationalEnvironmentEntry operationalEnvironment = new OperationalEnvironmentEntry();
		operationalEnvironment.setDmaapUebAddress(new HashSet<>());
		new EnvironmentMessageBusData(operationalEnvironment);
	} 
	
	@Test
	public void testGetTenant() throws Exception {
		EnvironmentMessageBusData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenant();
	}

	@Test
	public void testSetTenant() throws Exception {
		EnvironmentMessageBusData testSubject;
		String tenant = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTenant(tenant);
	}

	@Test
	public void testGetDmaaPuebEndpoints() throws Exception {
		EnvironmentMessageBusData testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDmaaPuebEndpoints();
	}

	@Test
	public void testSetDmaaPuebEndpoints() throws Exception {
		EnvironmentMessageBusData testSubject;
		List<String> dmaaPuebEndpoints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDmaaPuebEndpoints(dmaaPuebEndpoints);
	}

	@Test
	public void testGetUebPublicKey() throws Exception {
		EnvironmentMessageBusData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebPublicKey();
	}

	@Test
	public void testSetUebPublicKey() throws Exception {
		EnvironmentMessageBusData testSubject;
		String uebPublicKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebPublicKey(uebPublicKey);
	}

	@Test
	public void testGetUebPrivateKey() throws Exception {
		EnvironmentMessageBusData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebPrivateKey();
	}

	@Test
	public void testSetUebPrivateKey() throws Exception {
		EnvironmentMessageBusData testSubject;
		String uebPrivateKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebPrivateKey(uebPrivateKey);
	}

	@Test
	public void testGetEnvId() throws Exception {
		EnvironmentMessageBusData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvId();
	}

	@Test
	public void testSetEnvId() throws Exception {
		EnvironmentMessageBusData testSubject;
		String envId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvId(envId);
	}
}

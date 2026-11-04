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

import static org.junit.Assert.assertEquals;

public class EnvironmentMessageBusDataTest {

	private EnvironmentMessageBusData createTestSubject() {
		return new EnvironmentMessageBusData();
	}

	@Test
	public void testConstructorCopiesEnvIdAndTenantOfAnEntryWithoutUebAddresses() {
		OperationalEnvironmentEntry operationalEnvironment = new OperationalEnvironmentEntry();
		operationalEnvironment.setEnvironmentId("AUTO");
		operationalEnvironment.setTenant("tenant");
		EnvironmentMessageBusData messageBusData = new EnvironmentMessageBusData(operationalEnvironment);
		assertEquals("AUTO", messageBusData.getEnvId());
		assertEquals("tenant", messageBusData.getTenant());
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

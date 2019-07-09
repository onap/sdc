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

package org.openecomp.sdc.asdctool.simulator.tenant;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

public class OperationalEnvironmentTest {

	private OperationalEnvironment createTestSubject() {
		return new OperationalEnvironment();
	}

	@Test
	public void testGetLastModified() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastModified();
	}

	@Test
	public void testSetLastModified() throws Exception {
		OperationalEnvironment testSubject;
		String lastModified = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastModified(lastModified);
	}

	@Test
	public void testGetEnvironmentId() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentId();
	}

	@Test
	public void testSetEnvironmentId() throws Exception {
		OperationalEnvironment testSubject;
		String environmentId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironmentId(environmentId);
	}

	@Test
	public void testGetTenant() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenant();
	}

	@Test
	public void testSetTenant() throws Exception {
		OperationalEnvironment testSubject;
		String tenant = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTenant(tenant);
	}

	@Test
	public void testGetIsProduction() throws Exception {
		OperationalEnvironment testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsProduction();
	}

	@Test
	public void testSetIsProduction() throws Exception {
		OperationalEnvironment testSubject;
		Boolean production = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsProduction(production);
	}

	@Test
	public void testGetEcompWorkloadContext() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompWorkloadContext();
	}

	@Test
	public void testSetEcompWorkloadContext() throws Exception {
		OperationalEnvironment testSubject;
		String ecompWorkloadContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompWorkloadContext(ecompWorkloadContext);
	}

	@Test
	public void testGetStatus() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		OperationalEnvironment testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testSetStatus_1() throws Exception {
		OperationalEnvironment testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(EnvironmentStatusEnum.COMPLETED);
	}

	@Test
	public void testGetDmaapUebAddress() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDmaapUebAddress();
	}

	@Test
	public void testSetDmaapUebAddress() throws Exception {
		OperationalEnvironment testSubject;
		String dmaapUebAddress = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDmaapUebAddress(dmaapUebAddress);
	}

	@Test
	public void testGetUebApikey() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebApikey();
	}

	@Test
	public void testSetUebApikey() throws Exception {
		OperationalEnvironment testSubject;
		String uebApikey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebApikey(uebApikey);
	}

	@Test
	public void testGetUebSecretKey() throws Exception {
		OperationalEnvironment testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebSecretKey();
	}

	@Test
	public void testSetUebSecretKey() throws Exception {
		OperationalEnvironment testSubject;
		String uebSecretKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebSecretKey(uebSecretKey);
	}
}

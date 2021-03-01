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

package org.openecomp.sdc.common.api;

import org.junit.Test;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;

import java.util.List;

public class HealthCheckInfoTest {

	private HealthCheckInfo createTestSubject() {
		return new HealthCheckInfo("", HealthCheckStatus.UP, "", "");
	}

	@Test
	public void testGetHealthCheckComponent() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckComponent();
	}

	@Test
	public void testGetHealthCheckStatus() throws Exception {
		HealthCheckInfo testSubject;
		HealthCheckStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckStatus();
	}

	@Test
	public void testGetComponentsInfo() throws Exception {
		HealthCheckInfo testSubject;
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsInfo();
	}

	@Test
	public void testSetComponentsInfo() throws Exception {
		HealthCheckInfo testSubject;
		List<HealthCheckInfo> componentsInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsInfo(componentsInfo);
	}

	@Test
	public void testGetVersion() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	@Test
	public void testSetVersion() throws Exception {
		HealthCheckInfo testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	@Test
	public void testGetDescription() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testToString() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

}

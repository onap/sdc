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
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.common.api.HealthCheckInfo;

import java.net.URISyntaxException;

public class DmaapHealthTest extends BeConfDependentTest{

	private DmaapHealth createTestSubject() {
		return new DmaapHealth();
	}

	@Test
	public void testInit() throws Exception {
		DmaapHealth testSubject;
		DmaapHealth result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.init();
	}

	@Test
	public void testReport() throws Exception {
		DmaapHealth testSubject;
		Boolean isUp = false;

		// default test
		testSubject = createTestSubject();
		testSubject.report(isUp);
	}

	@Test
	public void testGetHealthCheckInfo() throws Exception {
		DmaapHealth testSubject;
		HealthCheckInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckInfo();
	}

	@Test(expected=URISyntaxException.class)
	public void testGetUrlHost() throws Exception {
		String qualifiedHost = "";
		String result;

		// default test
		result = DmaapHealth.getUrlHost(qualifiedHost);
	}
	
	@Test
	public void testGetUrlHost_2() throws Exception {
		String qualifiedHost = "www.mock.com";
		String result;

		// default test
		result = DmaapHealth.getUrlHost(qualifiedHost);
	}
}

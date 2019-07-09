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

public class UebHealthCheckCallTest extends BeConfDependentTest {

	private UebHealthCheckCall createTestSubject() {
		return new UebHealthCheckCall("mock", "mock");
	}

	@Test
	public void testCall() throws Exception {
		UebHealthCheckCall testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.call();
	}

	@Test
	public void testGetServer() throws Exception {
		UebHealthCheckCall testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServer();
	}

	@Test
	public void testGetCambriaHandler() throws Exception {
		UebHealthCheckCall testSubject;
		CambriaHandler result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCambriaHandler();
	}

	@Test
	public void testSetCambriaHandler() throws Exception {
		UebHealthCheckCall testSubject;
		CambriaHandler cambriaHandler = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCambriaHandler(cambriaHandler);
	}
}

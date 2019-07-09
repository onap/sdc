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

package org.openecomp.sdc.be.config;

import java.util.List;

import org.junit.Test;


public class CleanComponentsConfigurationTest {

	private CleanComponentsConfiguration createTestSubject() {
		return new CleanComponentsConfiguration();
	}

	
	@Test
	public void testGetCleanIntervalInMinutes() throws Exception {
		CleanComponentsConfiguration testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCleanIntervalInMinutes();
	}

	


	
	@Test
	public void testGetComponentsToClean() throws Exception {
		CleanComponentsConfiguration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsToClean();
	}

	
	@Test
	public void testSetComponentsToClean() throws Exception {
		CleanComponentsConfiguration testSubject;
		List<String> componentsToClean = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsToClean(componentsToClean);
	}
}

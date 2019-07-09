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

package org.openecomp.sdc.be.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CapReqDefTest {

	private CapReqDef createTestSubject() {
		return new CapReqDef();
	}

	@Test
	public void testCtor() throws Exception {
		new CapReqDef(new HashMap<>(), new HashMap<>());
	}
	
	@Test
	public void testGetCapabilities() throws Exception {
		CapReqDef testSubject;
		Map<String, List<CapabilityDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	
	@Test
	public void testGetRequirements() throws Exception {
		CapReqDef testSubject;
		Map<String, List<RequirementDefinition>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	
	@Test
	public void testSetCapabilities() throws Exception {
		CapReqDef testSubject;
		Map<String, List<CapabilityDefinition>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	
	@Test
	public void testSetRequirements() throws Exception {
		CapReqDef testSubject;
		Map<String, List<RequirementDefinition>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}
}

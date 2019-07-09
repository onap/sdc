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

import java.util.List;
import java.util.Map;

public class UploadComponentInstanceInfoTest {

	private UploadComponentInstanceInfo createTestSubject() {
		return new UploadComponentInstanceInfo();
	}

	@Test
	public void testGetProperties() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadPropInfo>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadPropInfo>> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testGetName() throws Exception {
		UploadComponentInstanceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		UploadComponentInstanceInfo testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetType() throws Exception {
		UploadComponentInstanceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		UploadComponentInstanceInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetCapabilities() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadCapInfo>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilities();
	}

	@Test
	public void testSetCapabilities() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadCapInfo>> capabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilities(capabilities);
	}

	@Test
	public void testGetRequirements() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadReqInfo>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirements();
	}

	@Test
	public void testSetRequirements() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, List<UploadReqInfo>> requirements = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirements(requirements);
	}

	@Test
	public void testGetCapabilitiesNamesToUpdate() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilitiesNamesToUpdate();
	}

	@Test
	public void testSetCapabilitiesNamesToUpdate() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, String> capabilitiesNamesToUpdate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilitiesNamesToUpdate(capabilitiesNamesToUpdate);
	}

	@Test
	public void testGetRequirementsNamesToUpdate() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequirementsNamesToUpdate();
	}

	@Test
	public void testSetRequirementsNamesToUpdate() throws Exception {
		UploadComponentInstanceInfo testSubject;
		Map<String, String> requirementsNamesToUpdate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequirementsNamesToUpdate(requirementsNamesToUpdate);
	}
}

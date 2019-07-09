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

import java.util.Map;

public class ParsedToscaYamlInfoTest {

	private ParsedToscaYamlInfo createTestSubject() {
		return new ParsedToscaYamlInfo();
	}

	@Test
	public void testGetInstances() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, UploadComponentInstanceInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstances();
	}

	@Test
	public void testSetInstances() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, UploadComponentInstanceInfo> instances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInstances(instances);
	}

	@Test
	public void testGetGroups() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, GroupDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}

	@Test
	public void testSetGroups() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, GroupDefinition> groups = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGroups(groups);
	}

	@Test
	public void testGetInputs() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, InputDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	@Test
	public void testSetInputs() throws Exception {
		ParsedToscaYamlInfo testSubject;
		Map<String, InputDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	@Test
	public void testToString() throws Exception {
		ParsedToscaYamlInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

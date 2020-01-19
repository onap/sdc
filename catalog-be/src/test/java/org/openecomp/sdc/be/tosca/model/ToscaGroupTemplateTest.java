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

package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ToscaGroupTemplateTest {

	private ToscaGroupTemplate createTestSubject() {
		return new ToscaGroupTemplate("", new ToscaMetadata(), new HashMap<>());
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaGroupTemplate testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaGroupTemplate testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetMembers() throws Exception {
		ToscaGroupTemplate testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMembers();
	}

	
	@Test
	public void testSetMembers() throws Exception {
		ToscaGroupTemplate testSubject;
		List<String> members = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMembers(members);
	}

	
	@Test
	public void testGetMetadata() throws Exception {
		ToscaGroupTemplate testSubject;
		IToscaMetadata result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadata();
	}

	
	@Test
	public void testSetMetadata() throws Exception {
		ToscaGroupTemplate testSubject;
		IToscaMetadata metadata = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMetadata(metadata);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaGroupTemplate testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaGroupTemplate testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}

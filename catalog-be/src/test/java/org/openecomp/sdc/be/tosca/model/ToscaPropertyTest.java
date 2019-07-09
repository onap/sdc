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


public class ToscaPropertyTest {

	private ToscaProperty createTestSubject() {
		return new ToscaProperty();
	}

	
	@Test
	public void testGetEntry_schema() throws Exception {
		ToscaProperty testSubject;
		EntrySchema result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEntry_schema();
	}

	
	@Test
	public void testSetEntry_schema() throws Exception {
		ToscaProperty testSubject;
		EntrySchema entry_schema = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEntry_schema(entry_schema);
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaProperty testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetDefaultp() throws Exception {
		ToscaProperty testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultp();
	}

	
	@Test
	public void testSetDefaultp() throws Exception {
		ToscaProperty testSubject;
		Object defaultp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultp(defaultp);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ToscaProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ToscaProperty testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetRequired() throws Exception {
		ToscaProperty testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequired();
	}

	
	@Test
	public void testSetRequired() throws Exception {
		ToscaProperty testSubject;
		Boolean required = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequired(required);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		ToscaProperty testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		ToscaProperty testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}
}

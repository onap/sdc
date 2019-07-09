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

package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.Map;


public class InterfaceDataDefinitionTest {

	private InterfaceDataDefinition createTestSubject() {
		return new InterfaceDataDefinition();
	}

	@Test
	public void testOverloadConstructors() throws Exception {
		InterfaceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
		
		new InterfaceDataDefinition(testSubject);
		new InterfaceDataDefinition("","");
	}

	@Test
	public void testGetUniqueId() throws Exception {
		InterfaceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}


	@Test
	public void testSetUniqueId() throws Exception {
		InterfaceDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}


	@Test
	public void testGetType() throws Exception {
		InterfaceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}


	@Test
	public void testSetType() throws Exception {
		InterfaceDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}


	@Test
	public void testGetCreationDate() throws Exception {
		InterfaceDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationDate();
	}


	@Test
	public void testSetCreationDate() throws Exception {
		InterfaceDataDefinition testSubject;
		Long creationDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationDate(creationDate);
	}


	@Test
	public void testGetLastUpdateDate() throws Exception {
		InterfaceDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdateDate();
	}


	@Test
	public void testSetLastUpdateDate() throws Exception {
		InterfaceDataDefinition testSubject;
		Long lastUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdateDate(lastUpdateDate);
	}


	@Test
	public void testGetDescription() throws Exception {
		InterfaceDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}


	@Test
	public void testSetDescription() throws Exception {
		InterfaceDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}


	@Test
	public void testGetOperations() throws Exception {
		InterfaceDataDefinition testSubject;
		Map<String, OperationDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperations();
	}


	@Test
	public void testSetOperations() throws Exception {
		InterfaceDataDefinition testSubject;
		Map<String, OperationDataDefinition> operations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperations(operations);
	}
}

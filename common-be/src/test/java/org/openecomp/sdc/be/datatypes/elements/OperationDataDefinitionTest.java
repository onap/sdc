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


public class OperationDataDefinitionTest {

	private OperationDataDefinition createTestSubject() {
		return new OperationDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		OperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new OperationDataDefinition(testSubject);
		new OperationDataDefinition("mock");
	}

	@Test
	public void testGetUniqueId() throws Exception {
		OperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		OperationDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}


	@Test
	public void testGetCreationDate() throws Exception {
		OperationDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationDate();
	}


	@Test
	public void testSetCreationDate() throws Exception {
		OperationDataDefinition testSubject;
		Long creationDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationDate(creationDate);
	}


	@Test
	public void testGetLastUpdateDate() throws Exception {
		OperationDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdateDate();
	}


	@Test
	public void testSetLastUpdateDate() throws Exception {
		OperationDataDefinition testSubject;
		Long lastUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdateDate(lastUpdateDate);
	}


	@Test
	public void testGetDescription() throws Exception {
		OperationDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}


	@Test
	public void testSetDescription() throws Exception {
		OperationDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}


	@Test
	public void testGetImplementation() throws Exception {
		OperationDataDefinition testSubject;
		ArtifactDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImplementation();
	}


	@Test
	public void testSetImplementation() throws Exception {
		OperationDataDefinition testSubject;
		ArtifactDataDefinition implementation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setImplementation(implementation);
	}


	@Test
	public void testGetInputs() throws Exception {
		OperationDataDefinition testSubject;
		ListDataDefinition<OperationInputDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}


	@Test
	public void testSetInputs() throws Exception {
		OperationDataDefinition testSubject;
		ListDataDefinition<OperationInputDefinition> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}
}

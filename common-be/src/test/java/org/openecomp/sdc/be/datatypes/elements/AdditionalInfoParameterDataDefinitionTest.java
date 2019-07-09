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

import java.util.List;


public class AdditionalInfoParameterDataDefinitionTest {

	private AdditionalInfoParameterDataDefinition createTestSubject() {
		return new AdditionalInfoParameterDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = new AdditionalInfoParameterDataDefinition(testSubject);
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetCreationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationTime();
	}

	
	@Test
	public void testSetCreationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long creationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationTime(creationTime);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Long modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetLastCreatedCounter() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastCreatedCounter();
	}

	
	@Test
	public void testSetLastCreatedCounter() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		Integer lastCreatedCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastCreatedCounter(lastCreatedCounter);
	}

	
	@Test
	public void testGetParameters() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		List<AdditionalInfoParameterInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParameters();
	}

	
	@Test
	public void testSetParameters() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		List<AdditionalInfoParameterInfo> parameters = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setParameters(parameters);
	}

	
	@Test
	public void testToString() throws Exception {
		AdditionalInfoParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

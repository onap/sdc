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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class AdditionalInfoParameterDataTest {

	private AdditionalInfoParameterData createTestSubject() {
		return new AdditionalInfoParameterData();
	}

	@Test
	public void testCtor() throws Exception {
		new AdditionalInfoParameterData(new HashMap<>());
		new AdditionalInfoParameterData(new AdditionalInfoParameterDataDefinition(), new HashMap<>(), new HashMap<>());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		AdditionalInfoParameterData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetAdditionalInfoParameterDataDefinition() throws Exception {
		AdditionalInfoParameterData testSubject;
		AdditionalInfoParameterDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAdditionalInfoParameterDataDefinition();
	}

	
	@Test
	public void testSetAdditionalInfoParameterDataDefinition() throws Exception {
		AdditionalInfoParameterData testSubject;
		AdditionalInfoParameterDataDefinition additionalInfoParameterDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAdditionalInfoParameterDataDefinition(additionalInfoParameterDataDefinition);
	}

	
	@Test
	public void testGetParameters() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParameters();
	}

	
	@Test
	public void testSetParameters() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> parameters = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setParameters(parameters);
	}

	
	@Test
	public void testGetIdToKey() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdToKey();
	}

	
	@Test
	public void testSetIdToKey() throws Exception {
		AdditionalInfoParameterData testSubject;
		Map<String, String> idToKey = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIdToKey(idToKey);
	}

	
	@Test
	public void testToString() throws Exception {
		AdditionalInfoParameterData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

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
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class InterfaceDefinitionTest {

	private InterfaceDefinition createTestSubject() {
		return new InterfaceDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new InterfaceDefinition(new InterfaceDataDefinition());
		new InterfaceDefinition("mock", "mock", new HashMap<>());
	}
	
	@Test
	public void testIsDefinition() throws Exception {
		InterfaceDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDefinition();
	}

	@Test
	public void testSetDefinition() throws Exception {
		InterfaceDefinition testSubject;
		boolean definition = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefinition(definition);
	}

	@Test
	public void testGetOperationsMap() throws Exception {
		InterfaceDefinition testSubject;
		Map<String, Operation> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperationsMap();
	}



	@Test
	public void testToString() throws Exception {
		InterfaceDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

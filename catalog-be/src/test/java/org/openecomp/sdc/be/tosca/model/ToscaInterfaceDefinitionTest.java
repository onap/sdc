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

import java.util.Map;

public class ToscaInterfaceDefinitionTest {

	private ToscaInterfaceDefinition createTestSubject() {
		return new ToscaInterfaceDefinition();
	}

	@Test
	public void testGetType() throws Exception {
		ToscaInterfaceDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		ToscaInterfaceDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetOperations() throws Exception {
		ToscaInterfaceDefinition testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperations();
	}

	@Test
	public void testSetOperations() throws Exception {
		ToscaInterfaceDefinition testSubject;
		Map<String, Object> toscaOperations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperations(toscaOperations);
	}
}

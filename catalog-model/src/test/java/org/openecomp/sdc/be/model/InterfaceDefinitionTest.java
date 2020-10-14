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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;


class InterfaceDefinitionTest {

	@Test
	void testCtor() throws Exception {
		new InterfaceDefinition(new InterfaceDataDefinition());
		new InterfaceDefinition("mock", "mock", new HashMap<>());
	}
	
	@Test
	void testIsDefinition() {
		final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
		assertFalse(interfaceDefinition.isDefinition());
	}

	@Test
	void testGetOperationsMap() {
		final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
		assertNotNull(interfaceDefinition.getOperationsMap());
		assertTrue(interfaceDefinition.getOperationsMap().isEmpty());
	}

	@Test
	void testHasOperation() {
		final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
		final Map<String, Operation> operationMap = new HashMap<>();
		final Set<String> operationSet = new HashSet<>();
		operationSet.add("operation1");
		operationSet.add("operation2");
		operationSet.add("operation3");
		operationSet.forEach(operation -> operationMap.put(operation, new Operation()));
		interfaceDefinition.setOperationsMap(operationMap);

		operationSet.forEach(operation -> assertThat(String.format("Should contain operation: %s", operation),
			interfaceDefinition.hasOperation(operation), is(true)));
	}
}

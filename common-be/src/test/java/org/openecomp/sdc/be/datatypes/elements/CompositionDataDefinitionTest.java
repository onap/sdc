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


public class CompositionDataDefinitionTest {

	private CompositionDataDefinition createTestSubject() {
		return new CompositionDataDefinition();
	}

	
	@Test
	public void testGetComponentInstances() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, ComponentInstanceDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstances();
	}

	
	@Test
	public void testSetComponentInstances() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, ComponentInstanceDataDefinition> componentInstances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstances(componentInstances);
	}

	
	@Test
	public void testGetRelations() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, RelationshipInstDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelations();
	}

	
	@Test
	public void testSetRelations() throws Exception {
		CompositionDataDefinition testSubject;
		Map<String, RelationshipInstDataDefinition> relations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelations(relations);
	}

	
	@Test
	public void testAddInstance() throws Exception {
		CompositionDataDefinition testSubject;
		String key = "";
		ComponentInstanceDataDefinition instance = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addInstance(key, instance);
	}

	
	@Test
	public void testAddRelation() throws Exception {
		CompositionDataDefinition testSubject;
		String key = "";
		RelationshipInstDataDefinition relation = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addRelation(key, relation);
	}
}

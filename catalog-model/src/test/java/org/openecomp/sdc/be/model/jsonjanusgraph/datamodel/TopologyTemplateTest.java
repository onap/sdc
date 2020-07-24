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

package org.openecomp.sdc.be.model.jsonjanusgraph.datamodel;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.*;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class TopologyTemplateTest {

	private TopologyTemplate createTestSubject() {
		return new TopologyTemplate();
	}

	@Test
	public void testGetComponentInstances() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ComponentInstanceDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstances();
	}

	@Test
	public void testSetComponentInstances() throws Exception {
		TopologyTemplate testSubject;
		Map<String, ComponentInstanceDataDefinition> instances = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstances(instances);
	}

	@Test
	public void testGetRelations() throws Exception {
		TopologyTemplate testSubject;
		Map<String, RelationshipInstDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelations();
	}
}

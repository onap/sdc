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
import org.openecomp.sdc.be.datatypes.elements.RelationshipInstDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class RelationshipTypeDataTest {

	private RelationshipTypeData createTestSubject() {
		return new RelationshipTypeData();
	}

	@Test
	public void testCtor() throws Exception {
		new RelationshipTypeData(new HashMap<>());
		new RelationshipTypeData(new RelationshipInstDataDefinition());
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		RelationshipTypeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetRelationshipTypeDataDefinition() throws Exception {
		RelationshipTypeData testSubject;
		RelationshipInstDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationshipTypeDataDefinition();
	}

	
	@Test
	public void testSetRelationshipTypeDataDefinition() throws Exception {
		RelationshipTypeData testSubject;
		RelationshipInstDataDefinition relationshipTypeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationshipTypeDataDefinition(relationshipTypeDataDefinition);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		RelationshipTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testToString() throws Exception {
		RelationshipTypeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

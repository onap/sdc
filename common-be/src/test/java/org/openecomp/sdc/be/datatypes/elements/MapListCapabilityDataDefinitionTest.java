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

import org.apache.commons.collections.map.HashedMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapListCapabilityDataDefinitionTest {

	private MapListCapabilityDataDefinition createTestSubject() {
		return new MapListCapabilityDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		MapListCapabilityDataDefinition testSubject;
		Map<String, ListCapabilityDataDefinition> result;

		// default test
		new MapListCapabilityDataDefinition(new HashedMap());
		new MapListCapabilityDataDefinition(createTestSubject());
	}
	
	@Test
	public void testAdd() throws Exception {
		MapListCapabilityDataDefinition testSubject = createTestSubject();
		testSubject.add("", null);
		testSubject.add("key2", null);
		testSubject.add("key2", new CapabilityDataDefinition());
		assertEquals(2, testSubject.getMapToscaDataDefinition().size());
		assertEquals(2, testSubject.getMapToscaDataDefinition().get("key2").getListToscaDataDefinition().size());
	}
}

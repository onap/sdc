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
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.elements.ProductMetadataDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class ProductMetadataDataTest {

	private ProductMetadataData createTestSubject() {
		return new ProductMetadataData();
	}

	@Test
	public void testCtor() throws Exception {
		new ProductMetadataData(new ProductMetadataDataDefinition());
		new ProductMetadataData(new GraphPropertiesDictionaryExtractor(new HashMap<>()));
	}
	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ProductMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ProductMetadataData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		ProductMetadataData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

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

package org.openecomp.sdc.be.dao.graph.datatype;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.neo4j.GraphEdgeLabels;

import java.util.HashMap;
import java.util.Map;

public class GraphEdgeTest {

	private GraphEdge createTestSubject() {
		return new GraphEdge();
	}
	
	@Test
	public void testCtor() throws Exception {
		new GraphEdge(GraphEdgeLabels.ADDITIONAL_INFORMATION, new HashMap<>());
	}

	@Test
	public void testGetEdgeType() throws Exception {
		GraphEdge testSubject;
		GraphEdgeLabels result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEdgeType();
	}

	@Test
	public void testSetEdgeType() throws Exception {
		GraphEdge testSubject;
		GraphEdgeLabels edgeType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEdgeType(edgeType);
	}

	@Test
	public void testGetProperties() throws Exception {
		GraphEdge testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		GraphEdge testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	@Test
	public void testHashCode() throws Exception {
		GraphEdge testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		GraphEdge testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
		
		result = testSubject.equals(new Object());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		GraphEdge testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

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

package org.openecomp.sdc.asdctool;

import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.janusgraph.core.JanusGraph;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class UtilsTest {

	@Test
	public void testBuildOkResponse() throws Exception {
		int status = 0;
		Object entity = null;
		Map<String, String> additionalHeaders = null;
		Response result;

		// test with mock entity
		Object mockEntity = new Object();
		result = Utils.buildOkResponse(status, entity, additionalHeaders);
		Assert.assertNotNull(result);

		// test with mock headers
		Map<String, String> mockAdditionalHeaders = new HashMap<>();
		mockAdditionalHeaders.put("stam", "stam");
		result = Utils.buildOkResponse(status, mockEntity, mockAdditionalHeaders);
		Assert.assertNotNull(result);
	}

	@Test
	public void testOpenGraph() throws Exception {
		Configuration conf = null;
		JanusGraph result;

		// default test with null
		result = Utils.openGraph(conf);
	}

	@Test
	public void testVertexLeftContainsRightProps() throws Exception {
		Map<String, Object> leftProps = new HashMap<>();
		Map<String, Object> rightProps = null;
		boolean result;

		// test 1 with null
		rightProps = null;
		result = Utils.vertexLeftContainsRightProps(leftProps, rightProps);
		Assert.assertEquals(true, result);

		// test 2 with mocks
		Map<String, Object> mockLeftProps = new HashMap<>();
		mockLeftProps.put("stam", new Object());
		Map<String, Object> mockRightProps = new HashMap<>();
		mockRightProps.put("stam", new Object());
		result = Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps);
		Assert.assertEquals(false, result);

		// test 3 with mocks
		Object mockObject = new Object();
		mockLeftProps = new HashMap<>();
		mockLeftProps.put("stam", mockObject);
		mockRightProps = new HashMap<>();
		mockRightProps.put("stam", mockObject);
		result = Utils.vertexLeftContainsRightProps(mockLeftProps, mockRightProps);
		Assert.assertEquals(true, result);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSetProperties() throws Exception {
		Element element = null;
		Map<String, Object> properties = null;

		// test 1
		properties = null;
		Utils.setProperties(element, properties);
		
		// test 2
		properties = new HashMap<>();
		properties.put("stam", new Object());
		Utils.setProperties(element, properties);
	}

	@Test(expected=NullPointerException.class)
	public void testGetProperties() throws Exception {
		Element element = null;
		Map<String, Object> result;

		// default test
		result = Utils.getProperties(element);
	}
}

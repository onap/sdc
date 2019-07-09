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

import org.junit.Assert;
import org.junit.Test;

public class ForwardingPathElementDataDefinitionTest {

	private ForwardingPathElementDataDefinition createTestSubject() {
		return new ForwardingPathElementDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new ForwardingPathElementDataDefinition(testSubject);
		new ForwardingPathElementDataDefinition("mock", "mock", "mock", "mock", "mock", "mock");
	}
	
	@Test
	public void testGetFromNode() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromNode();
	}

	@Test
	public void testSetFromNode() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String fromNode = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFromNode(fromNode);
	}

	@Test
	public void testGetToNode() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToNode();
	}

	@Test
	public void testSetToNode() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String toNode = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToNode(toNode);
	}

	@Test
	public void testGetFromCP() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromCP();
	}

	@Test
	public void testSetFromCP() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String fromCP = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFromCP(fromCP);
	}

	@Test
	public void testGetToCP() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToCP();
	}

	@Test
	public void testSetToCP() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String toCP = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToCP(toCP);
	}

	@Test
	public void testGetToCPOriginId() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToCPOriginId();
	}

	@Test
	public void testSetToCPOriginId() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String toCPOriginId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToCPOriginId(toCPOriginId);
	}

	@Test
	public void testGetFromCPOriginId() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFromCPOriginId();
	}

	@Test
	public void testSetFromCPOriginId() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String fromCPOriginId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFromCPOriginId(fromCPOriginId);
	}

	@Test
	public void testEquals() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		Object o = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(o);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testHashCode() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testToString() throws Exception {
		ForwardingPathElementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

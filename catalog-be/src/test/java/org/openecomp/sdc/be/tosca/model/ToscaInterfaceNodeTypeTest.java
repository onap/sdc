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

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ToscaInterfaceNodeTypeTest {

	private ToscaInterfaceNodeType createTestSubject() {
		return new ToscaInterfaceNodeType();
	}

	@Test
	public void testGetDerived_from() throws Exception {
		ToscaInterfaceNodeType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerived_from();
	}

	@Test
	public void testSetDerived_from() throws Exception {
		ToscaInterfaceNodeType testSubject;
		String derived_from = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDerived_from(derived_from);
	}

	@Test
	public void testGetDescription() throws Exception {
		ToscaInterfaceNodeType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		ToscaInterfaceNodeType testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testEquals() throws Exception {
		ToscaInterfaceNodeType testSubject;
		Object o = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		o = null;
		result = testSubject.equals(o);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);

	}

	@Test
	public void testHashCode() throws Exception {
		ToscaInterfaceNodeType testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testGetOperations() throws Exception {
		ToscaInterfaceNodeType testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOperations();
	}

	@Test
	public void testSetOperations() throws Exception {
		ToscaInterfaceNodeType testSubject;
		Map<String, Object> operations = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOperations(operations);
	}
}

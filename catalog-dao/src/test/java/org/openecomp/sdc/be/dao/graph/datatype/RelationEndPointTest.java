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
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class RelationEndPointTest {

	private RelationEndPoint createTestSubject() {
		return new RelationEndPoint(NodeTypeEnum.AdditionalInfoParameters, "", null);
	}

	@Test
	public void testGetLabel() throws Exception {
		RelationEndPoint testSubject;
		NodeTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLabel();
	}

	@Test
	public void testSetLabel() throws Exception {
		RelationEndPoint testSubject;
		NodeTypeEnum label = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLabel(label);
	}

	@Test
	public void testGetIdName() throws Exception {
		RelationEndPoint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdName();
	}

	@Test
	public void testSetIdName() throws Exception {
		RelationEndPoint testSubject;
		String idName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setIdName(idName);
	}

	@Test
	public void testGetIdValue() throws Exception {
		RelationEndPoint testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdValue();
	}

	@Test
	public void testSetIdValue() throws Exception {
		RelationEndPoint testSubject;
		Object idValue = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIdValue(idValue);
	}

	@Test
	public void testToString() throws Exception {
		RelationEndPoint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testHashCode() throws Exception {
		RelationEndPoint testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		RelationEndPoint testSubject;
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
}

/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.datamodel;

import org.junit.Test;

import java.util.Set;

public class NameIdPairTest {

	private NameIdPair createTestSubject() {
		return new NameIdPair("", "");
	}

	@Test
	public void testGetName() throws Exception {
		NameIdPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testConstructorWith3Parameters() throws Exception {
		NameIdPair testSubject;
		String result;

		// default test
		testSubject =  new NameIdPair("", "","");
		result = testSubject.getOwnerId();
	}

	@Test
	public void testSetName() throws Exception {
		NameIdPair testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetId() throws Exception {
		NameIdPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	@Test
	public void testSetId() throws Exception {
		NameIdPair testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	@Test
	public void testGetOwnerId() throws Exception {
		NameIdPair testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		testSubject.put("ownerId", "mock");
		result = testSubject.getOwnerId();
	}

	@Test
	public void testSetOwnerId() throws Exception {
		NameIdPair testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerId(ownerId);
	}

	@Test
	public void testGetWrappedData() throws Exception {
		NameIdPair testSubject;
		Set<NameIdPairWrapper> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getWrappedData();
	}

	@Test
	public void testSetWrappedData() throws Exception {
		NameIdPair testSubject;
		Set<NameIdPairWrapper> data = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setWrappedData(data);
	}

	@Test
	public void testAddWrappedData() throws Exception {
		NameIdPair testSubject;
		NameIdPairWrapper nameIdPairWrapper = null;

		// default test
		testSubject = createTestSubject();
		testSubject.addWrappedData(nameIdPairWrapper);
	}

	@Test
	public void testEquals() throws Exception {
		NameIdPair testSubject;
		Object o = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(o);
	}

	@Test
	public void testHashCode() throws Exception {
		NameIdPair testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testCreate() throws Exception {
		String name = "";
		String id = "";
		NameIdPair result;

		// default test
		result = NameIdPair.create(name, id);
	}
}

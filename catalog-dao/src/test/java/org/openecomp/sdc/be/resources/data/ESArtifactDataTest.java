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

import java.nio.ByteBuffer;

public class ESArtifactDataTest {

	private ESArtifactData createTestSubject() {
		return new ESArtifactData();
	}
	
	@Test
	public void testCtor() throws Exception {
		new ESArtifactData("mock");
		new ESArtifactData("mock", new byte[0]);
	}
	
	@Test
	public void testGetDataAsArray() throws Exception {
		ESArtifactData testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataAsArray();
	}

	@Test
	public void testSetDataAsArray() throws Exception {
		ESArtifactData testSubject;
		byte[] data = new byte[] { ' ' };

		// test 1
		testSubject = createTestSubject();
		data = null;
		testSubject.setDataAsArray(data);

		// test 2
		testSubject = createTestSubject();
		data = new byte[] { ' ' };
		testSubject.setDataAsArray(data);
	}

	@Test
	public void testGetData() throws Exception {
		ESArtifactData testSubject;
		ByteBuffer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getData();
	}

	@Test
	public void testSetData() throws Exception {
		ESArtifactData testSubject;
		ByteBuffer data = null;

		// test 1
		testSubject = createTestSubject();
		data = null;
		testSubject.setData(data);
	}

	@Test
	public void testGetId() throws Exception {
		ESArtifactData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	@Test
	public void testSetId() throws Exception {
		ESArtifactData testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}
}

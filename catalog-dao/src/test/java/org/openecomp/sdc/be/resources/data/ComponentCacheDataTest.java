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
import java.util.Date;


public class ComponentCacheDataTest {

	private ComponentCacheData createTestSubject() {
		return new ComponentCacheData();
	}
	
	@Test
	public void testCtor() throws Exception {
		new ComponentCacheData("mock");
		new ComponentCacheData("mock", new byte[0]);
		new ComponentCacheData("mock", new byte[0], new Date(), "mock", true, true);
	}
	
	@Test
	public void testGetDataAsArray() throws Exception {
		ComponentCacheData testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataAsArray();
	}

	
	@Test
	public void testSetDataAsArray() throws Exception {
		ComponentCacheData testSubject;
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
		ComponentCacheData testSubject ;
		
		testSubject = createTestSubject();
		
		testSubject.getData();
	}

	@Test
	public void testSetData() throws Exception {
		ComponentCacheData testSubject ;
		
		testSubject = createTestSubject();
		
		ByteBuffer data = ByteBuffer.allocate(0);
		testSubject.setData(data);
	}
	
	@Test
	public void testGetId() throws Exception {
		ComponentCacheData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	
	@Test
	public void testSetId() throws Exception {
		ComponentCacheData testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		ComponentCacheData testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		ComponentCacheData testSubject;
		Date modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetType() throws Exception {
		ComponentCacheData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ComponentCacheData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetIsDirty() throws Exception {
		ComponentCacheData testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsDirty();
	}

	
	@Test
	public void testSetIsDirty() throws Exception {
		ComponentCacheData testSubject;
		boolean isDirty = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsDirty(isDirty);
	}

	
	@Test
	public void testGetIsZipped() throws Exception {
		ComponentCacheData testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsZipped();
	}

	
	@Test
	public void testSetIsZipped() throws Exception {
		ComponentCacheData testSubject;
		boolean isZipped = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsZipped(isZipped);
	}
}

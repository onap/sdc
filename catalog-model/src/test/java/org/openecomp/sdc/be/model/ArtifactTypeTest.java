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

package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactTypeTest {

	private ArtifactType createTestSubject() {
		return new ArtifactType();
	}

	@Test
	public void testGetName() throws Exception {
		ArtifactType testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		ArtifactType testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testHashCode() throws Exception {
		ArtifactType testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		ArtifactType testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		obj = new Object();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		testSubject.setName("mock");
		ArtifactType createTestSubject = createTestSubject();
		createTestSubject.setName("mock");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(true, result);
		createTestSubject.setName("mock2");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(false, result);
	}
}

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

package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

public class ResourceArtifactInfoImplTest {

	private ResourceArtifactInfoImpl createTestSubject() {
		return new ResourceArtifactInfoImpl();
	}

	@Test
	public void testGetResourceName() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	@Test
	public void testSetResourceName() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	@Test
	public void testGetResourceVersion() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVersion();
	}

	@Test
	public void testSetResourceVersion() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVersion(resourceVersion);
	}

	@Test
	public void testGetResourceUUID() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceUUID();
	}

	@Test
	public void testSetResourceUUID() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String resourceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceUUID(resourceUUID);
	}

	@Test
	public void testToString() throws Exception {
		ResourceArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

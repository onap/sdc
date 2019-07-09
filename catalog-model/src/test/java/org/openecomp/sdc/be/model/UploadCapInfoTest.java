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

import java.util.List;

import org.junit.Test;


public class UploadCapInfoTest {

	private UploadCapInfo createTestSubject() {
		return new UploadCapInfo();
	}

	
	@Test
	public void testGetNode() throws Exception {
		UploadCapInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		UploadCapInfo testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetValidSourceTypes() throws Exception {
		UploadCapInfo testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	
	@Test
	public void testSetValidSourceTypes() throws Exception {
		UploadCapInfo testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		UploadCapInfo testSubject;
		List<UploadPropInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		UploadCapInfo testSubject;
		List<UploadPropInfo> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}

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
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.LinkedList;
import java.util.List;

public class JsonContainerResourceInstanceTest {

	private JsonContainerResourceInstance createTestSubject() {
		return new JsonContainerResourceInstance(new ComponentInstance(), "", null);
	}

	@Test
	public void testConstructor() throws Exception {
		ComponentInstance componentInstance = new ComponentInstance();
		new JsonContainerResourceInstance(componentInstance, new LinkedList<>());
		componentInstance.setOriginType(OriginTypeEnum.Configuration);
		new JsonContainerResourceInstance(componentInstance, new LinkedList<>());
	}
	
	@Test
	public void testGetResourceInstanceName() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInstanceName();
	}

	@Test
	public void testSetResourceInstanceName() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resourceInstanceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInstanceName(resourceInstanceName);
	}

	@Test
	public void testGetResourceName() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	@Test
	public void testSetResourceName() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	@Test
	public void testGetResourceVersion() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVersion();
	}

	@Test
	public void testSetResourceVersion() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVersion(resourceVersion);
	}

	@Test
	public void testGetResoucreType() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResoucreType();
	}

	@Test
	public void testSetResoucreType() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resoucreType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResoucreType(resoucreType);
	}

	@Test
	public void testGetResourceUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceUUID();
	}

	@Test
	public void testSetResourceUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resourceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceUUID(resourceUUID);
	}

	@Test
	public void testGetArtifacts() throws Exception {
		JsonContainerResourceInstance testSubject;
		List<ArtifactInfoImpl> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	@Test
	public void testSetArtifacts() throws Exception {
		JsonContainerResourceInstance testSubject;
		List<ArtifactInfoImpl> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	@Test
	public void testGetResourceInvariantUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInvariantUUID();
	}

	@Test
	public void testSetResourceInvariantUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String resourceInvariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInvariantUUID(resourceInvariantUUID);
	}

	@Test
	public void testGetResourceCustomizationUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceCustomizationUUID();
	}

	@Test
	public void testSetResourceCustomizationUUID() throws Exception {
		JsonContainerResourceInstance testSubject;
		String customizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceCustomizationUUID(customizationUUID);
	}

	@Test
	public void testGetCategory() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	@Test
	public void testSetCategory() throws Exception {
		JsonContainerResourceInstance testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	@Test
	public void testGetSubcategory() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubcategory();
	}

	@Test
	public void testSetSubcategory() throws Exception {
		JsonContainerResourceInstance testSubject;
		String subcategory = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubcategory(subcategory);
	}

	@Test
	public void testToString() throws Exception {
		JsonContainerResourceInstance testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

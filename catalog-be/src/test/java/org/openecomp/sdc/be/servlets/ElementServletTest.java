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

package org.openecomp.sdc.be.servlets;

import static org.mockito.Mockito.mock;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;

public class ElementServletTest {

	private ElementServlet createTestSubject() {
		UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
		GroupBusinessLogic groupBL = mock(GroupBusinessLogic.class);
		ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
		ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
		return new ElementServlet(userBusinessLogic, groupBL, componentInstanceBL, componentsUtils);
	}

	
	@Test
	public void testGetComponentCategories() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetAllCategories() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentCategory() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentCategory() throws Exception {
		ElementServlet testSubject;
		String categoryUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentSubCategory() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String categoryId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentSubCategory() throws Exception {
		ElementServlet testSubject;
		String categoryUniqueId = "";
		String subCategoryUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testCreateComponentGrouping() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		String grandParentCategoryId = "";
		String parentSubCategoryId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteComponentGrouping() throws Exception {
		ElementServlet testSubject;
		String grandParentCategoryUniqueId = "";
		String parentSubCategoryUniqueId = "";
		String groupingUniqueId = "";
		String componentType = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetTags() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetPropertyScopes() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetArtifactTypes() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetConfiguration() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetFollowedResourcesServices() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetCatalogComponents() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		List<OriginTypeEnum> excludeTypes = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteMarkedResources() throws Exception {
		ElementServlet testSubject;
		String componentType = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetListOfCsars() throws Exception {
		ElementServlet testSubject;
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}
}

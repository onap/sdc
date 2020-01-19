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

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.CapabilitiesBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.InterfaceOperationBusinessLogic;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.RelationshipTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.mock;

public class TypesFetchServletTest {

	private TypesFetchServlet createTestSubject() {
		UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
		ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
		ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
		ServletUtils servletUtils = mock(ServletUtils.class);
		ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
		PropertyBusinessLogic propertyBusinessLogic = mock(PropertyBusinessLogic.class);
		RelationshipTypeBusinessLogic relationshipTypeBusinessLogic = mock(RelationshipTypeBusinessLogic.class);
		CapabilitiesBusinessLogic capabilitiesBusinessLogic = mock(CapabilitiesBusinessLogic.class);
		InterfaceOperationBusinessLogic interfaceOperationBusinessLogic = mock(InterfaceOperationBusinessLogic.class);
		ResourceBusinessLogic resourceBusinessLogic = mock(ResourceBusinessLogic.class);

		return new TypesFetchServlet(userBusinessLogic, componentInstanceBL, componentsUtils, servletUtils,
				resourceImportManager, propertyBusinessLogic, relationshipTypeBusinessLogic, capabilitiesBusinessLogic,
				interfaceOperationBusinessLogic, resourceBusinessLogic);
	}

	
	@Test
	public void testGetAllDataTypesServlet() throws Exception {
		TypesFetchServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetPropertyBL() throws Exception {
		TypesFetchServlet testSubject;
		ServletContext context = null;
		PropertyBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}
}

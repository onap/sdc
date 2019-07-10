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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.User;

import fj.data.Either;
import org.openecomp.sdc.be.user.UserBusinessLogic;

public class LifecycleServletTest {

	private LifecycleServlet createTestSubject() {
		UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
		LifecycleBusinessLogic lifecycleBusinessLogic = mock(LifecycleBusinessLogic.class);
		ComponentsUtils componentsUtils = mock(ComponentsUtils.class);

		return new LifecycleServlet(userBusinessLogic, componentsUtils, lifecycleBusinessLogic);
	}

	
	@Test
	public void testChangeResourceState() throws Exception {
		LifecycleServlet testSubject;
		String jsonChangeInfo = "";
		String componentCollection = "";
		String lifecycleTransition = "";
		String componentId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateTransitionEnum() throws Exception {
		LifecycleServlet testSubject;
		String lifecycleTransition = "";
		User user = null;
		Either<LifeCycleTransitionEnum, Response> result;

		// default test
		testSubject = createTestSubject();
	}
}

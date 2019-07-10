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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class ConsumerServletTest {

	private ConsumerServlet createTestSubject() {
		UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
		ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
		ConsumerBusinessLogic consumerBusinessLogic = mock(ConsumerBusinessLogic.class);
		return new ConsumerServlet(userBusinessLogic, componentsUtils, consumerBusinessLogic);
	}

	
	@Test
	public void testCreateConsumer() throws Exception {
		ConsumerServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetConsumer() throws Exception {
		ConsumerServlet testSubject;
		String consumerId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteConsumer() throws Exception {
		ConsumerServlet testSubject;
		String consumerId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetConsumerBL() throws Exception {
		ConsumerServlet testSubject;
		ServletContext context = null;
		ConsumerBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testConvertJsonToObject() throws Exception {
		ConsumerServlet testSubject;
		String data = "";
		User user = null;
		AuditingActionEnum actionEnum = null;
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}
}

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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.base.Equivalence.Wrapper;

public class AttributeServletTest {

	private AttributeServlet createTestSubject() {
		return new AttributeServlet();
	}

	
	@Test
	public void testCreateAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String attributeId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteAttribute() throws Exception {
		AttributeServlet testSubject;
		String resourceId = "";
		String attributeId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testBuildAttributeFromString() throws Exception {
	AttributeServlet testSubject;String data = "";
	Wrapper<PropertyDefinition> attributesWrapper = null;
	Wrapper<ResponseFormat> errorWrapper = null;
	
	
	// default test
	}
}

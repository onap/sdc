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

package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;

import java.util.List;
import java.util.Map;


public class ToscaTemplateCapabilityTest {

	private ToscaTemplateCapability createTestSubject() {
		return new ToscaTemplateCapability();
	}

	
	@Test
	public void testGetValid_source_types() throws Exception {
		ToscaTemplateCapability testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	
	@Test
	public void testSetValid_source_types() throws Exception {
		ToscaTemplateCapability testSubject;
		List<String> valid_source_types = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(valid_source_types);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		ToscaTemplateCapability testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		ToscaTemplateCapability testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}

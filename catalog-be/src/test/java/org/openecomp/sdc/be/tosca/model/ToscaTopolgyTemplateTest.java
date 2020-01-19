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

import java.util.Map;


public class ToscaTopolgyTemplateTest {

	private ToscaTopolgyTemplate createTestSubject() {
		return new ToscaTopolgyTemplate();
	}

	
	@Test
	public void testGetNode_templates() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaNodeTemplate> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode_templates();
	}

	
	@Test
	public void testSetNode_templates() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaNodeTemplate> node_templates = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setNode_templates(node_templates);
	}

	
	@Test
	public void testGetGroups() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaGroupTemplate> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroups();
	}



	
	@Test
	public void testGetSubstitution_mappings() throws Exception {
		ToscaTopolgyTemplate testSubject;
		SubstitutionMapping result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubstitution_mappings();
	}

	
	@Test
	public void testSetSubstitution_mappings() throws Exception {
		ToscaTopolgyTemplate testSubject;
		SubstitutionMapping substitution_mapping = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSubstitution_mappings(substitution_mapping);
	}

	
	@Test
	public void testGetInputs() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		ToscaTopolgyTemplate testSubject;
		Map<String, ToscaProperty> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}
}

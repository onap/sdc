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

package org.openecomp.sdc.be.tosca;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.openecomp.sdc.be.model.Component;

import java.util.List;


public class ToscaRepresentationTest {

	private ToscaRepresentation createTestSubject() {
		return new ToscaRepresentation();
	}

	
	@Test
	public void testGetMainYaml() throws Exception {
		ToscaRepresentation testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMainYaml();
	}

	
	@Test
	public void testSetMainYaml() throws Exception {
		ToscaRepresentation testSubject;
		String mainYaml = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMainYaml(mainYaml);
	}

	
	@Test
	public void testGetDependencies() throws Exception {
		ToscaRepresentation testSubject;
		List<Triple<String, String, Component>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDependencies();
	}

	
	@Test
	public void testSetDependencies() throws Exception {
		ToscaRepresentation testSubject;
		List<Triple<String, String, Component>> dependencies = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDependencies(dependencies);
	}
}

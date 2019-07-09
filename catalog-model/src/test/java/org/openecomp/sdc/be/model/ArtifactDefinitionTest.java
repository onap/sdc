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

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class ArtifactDefinitionTest {

	private ArtifactDefinition createTestSubject() {
		return new ArtifactDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new ArtifactDefinition(new ArtifactDefinition());
		new ArtifactDefinition(new ArtifactDataDefinition());
		new ArtifactDefinition(new HashMap<>());
		new ArtifactDefinition(new ArtifactDataDefinition(), "mock");
	}
	
	@Test
	public void testGetPayloadData() throws Exception {
		ArtifactDefinition testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadData();
	}

	
	@Test
	public void testSetPayload() throws Exception {
		ArtifactDefinition testSubject;
		byte[] payloadData = new byte[] { ' ' };

		// default test
		testSubject = createTestSubject();
		testSubject.setPayload(payloadData);
	}

	
	@Test
	public void testSetPayloadData() throws Exception {
		ArtifactDefinition testSubject;
		String payloadData = "";

		// test 1
		testSubject = createTestSubject();
		payloadData = null;
		testSubject.setPayloadData(payloadData);

		// test 2
		testSubject = createTestSubject();
		payloadData = "";
		testSubject.setPayloadData(payloadData);
	}

	
	@Test
	public void testGetListHeatParameters() throws Exception {
		ArtifactDefinition testSubject;
		List<HeatParameterDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListHeatParameters();
		testSubject.setHeatParameters(new LinkedList<>());
		result = testSubject.getListHeatParameters();
	}

	
	@Test
	public void testSetListHeatParameters() throws Exception {
		ArtifactDefinition testSubject;
		List<HeatParameterDefinition> properties = null;

		// test 1
		testSubject = createTestSubject();
		testSubject.setListHeatParameters(properties);
		properties = new LinkedList<>();
		testSubject.setListHeatParameters(properties);
	}

	
	@Test
	public void testCheckEsIdExist() throws Exception {
		ArtifactDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.checkEsIdExist();
		testSubject.setEsId("mock");
		result = testSubject.checkEsIdExist();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ArtifactDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ArtifactDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(null);
		result = testSubject.equals(testSubject);
		result = testSubject.equals(new Object());
		result = testSubject.equals(createTestSubject());
	}
}

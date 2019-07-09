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

package org.openecomp.sdc.be.ui.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;


public class UiServiceMetadataTest {

	private UiServiceMetadata createTestSubject() {
		return new UiServiceMetadata(null, new ServiceMetadataDataDefinition());
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		UiServiceMetadata testSubject;
		String distributionStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatus(distributionStatus);
	}

	
	@Test
	public void testGetEcompGeneratedNaming() throws Exception {
		UiServiceMetadata testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompGeneratedNaming();
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		UiServiceMetadata testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testGetNamingPolicy() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNamingPolicy();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		UiServiceMetadata testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		UiServiceMetadata testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		UiServiceMetadata testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}
}

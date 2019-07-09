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

package org.openecomp.sdc.be.resources.data.auditing.model;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo.Builder;


public class ResourceVersionInfoTest {

	private ResourceVersionInfo createTestSubject() {
		Builder newBuilder = ResourceVersionInfo.newBuilder();
		return newBuilder.build();
	}

	
	@Test
	public void testNewBuilder() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
	}

	@Test
	public void testArtifactUuid() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.artifactUuid("mock");
	}
	
	@Test
	public void testState() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.state("mock");
	}
	
	@Test
	public void testvVersion() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.version("mock");
	}
	
	@Test
	public void testDistributionStatus() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.distributionStatus("mock");
	}
	
	@Test
	public void testGetArtifactUuid() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUuid();
	}

	
	@Test
	public void testGetState() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getState();
	}

	
	@Test
	public void testGetVersion() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}
}

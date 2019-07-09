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

package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;

import java.util.List;

public class NotificationDataImplTest {

	private NotificationDataImpl createTestSubject() {
		return new NotificationDataImpl();
	}

	@Test
	public void testGetDistributionID() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionID();
	}

	@Test
	public void testGetServiceName() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceName();
	}

	@Test
	public void testGetServiceVersion() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceVersion();
	}

	@Test
	public void testGetServiceUUID() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceUUID();
	}

	@Test
	public void testSetDistributionID() throws Exception {
		NotificationDataImpl testSubject;
		String distributionID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionID(distributionID);
	}

	@Test
	public void testSetServiceName() throws Exception {
		NotificationDataImpl testSubject;
		String serviceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceName(serviceName);
	}

	@Test
	public void testSetServiceVersion() throws Exception {
		NotificationDataImpl testSubject;
		String serviceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceVersion(serviceVersion);
	}

	@Test
	public void testSetServiceUUID() throws Exception {
		NotificationDataImpl testSubject;
		String serviceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceUUID(serviceUUID);
	}

	@Test
	public void testGetServiceDescription() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceDescription();
	}

	@Test
	public void testSetServiceDescription() throws Exception {
		NotificationDataImpl testSubject;
		String serviceDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceDescription(serviceDescription);
	}

	@Test
	public void testGetWorkloadContext() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getWorkloadContext();
	}

	@Test
	public void testSetWorkloadContext() throws Exception {
		NotificationDataImpl testSubject;
		String workloadContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setWorkloadContext(workloadContext);
	}

	@Test
	public void testToString() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetResources() throws Exception {
		NotificationDataImpl testSubject;
		List<JsonContainerResourceInstance> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResources();
	}

	@Test
	public void testSetResources() throws Exception {
		NotificationDataImpl testSubject;
		List<JsonContainerResourceInstance> resources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResources(resources);
	}

	@Test
	public void testGetServiceArtifacts() throws Exception {
		NotificationDataImpl testSubject;
		List<ArtifactInfoImpl> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceArtifacts();
	}

	@Test
	public void testSetServiceArtifacts() throws Exception {
		NotificationDataImpl testSubject;
		List<ArtifactInfoImpl> serviceArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceArtifacts(serviceArtifacts);
	}

	@Test
	public void testGetServiceInvariantUUID() throws Exception {
		NotificationDataImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInvariantUUID();
	}

	@Test
	public void testSetServiceInvariantUUID() throws Exception {
		NotificationDataImpl testSubject;
		String serviceInvariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInvariantUUID(serviceInvariantUUID);
	}
}

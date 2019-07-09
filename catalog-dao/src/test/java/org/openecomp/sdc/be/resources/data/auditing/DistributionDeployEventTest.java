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

package org.openecomp.sdc.be.resources.data.auditing;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;

import java.util.Date;
import java.util.UUID;


public class DistributionDeployEventTest {

	private DistributionDeployEvent createTestSubject() {
		return new DistributionDeployEvent();
	}

	@Test
	public void testCtor() throws Exception {
		new DistributionDeployEvent();
		new DistributionDeployEvent("mock", CommonAuditData.newBuilder().build(),new ResourceCommonInfo(), "mock", "mock", "mock");
	}
	
	@Test
	public void testFillFields() throws Exception {
		DistributionDeployEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetResourceName() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	
	@Test
	public void testSetResourceName() throws Exception {
		DistributionDeployEvent testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		DistributionDeployEvent testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetCurrVersion() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrVersion();
	}

	
	@Test
	public void testSetCurrVersion() throws Exception {
		DistributionDeployEvent testSubject;
		String currVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrVersion(currVersion);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		DistributionDeployEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		DistributionDeployEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		DistributionDeployEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		DistributionDeployEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		DistributionDeployEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		DistributionDeployEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		DistributionDeployEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		DistributionDeployEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		DistributionDeployEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		DistributionDeployEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	@Test
	public void testGetDid() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDid();
	}

	
	@Test
	public void testSetDid() throws Exception {
		DistributionDeployEvent testSubject;
		String did = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDid(did);
	}

	
	@Test
	public void testToString() throws Exception {
		DistributionDeployEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

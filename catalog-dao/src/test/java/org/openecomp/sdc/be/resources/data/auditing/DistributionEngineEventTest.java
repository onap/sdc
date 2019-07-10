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
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

import java.util.Date;
import java.util.UUID;


public class DistributionEngineEventTest {

	private DistributionEngineEvent createTestSubject() {
		return new DistributionEngineEvent();
	}

	@Test
	public void testCtor() throws Exception {
		new DistributionEngineEvent();
		new DistributionEngineEvent("mock", CommonAuditData.newBuilder().build(), "mock",  DistributionTopicData.newBuilder().build(),  "mock", "mock", "mock");
	}
	
	@Test
	public void testFillFields() throws Exception {
		DistributionEngineEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetDstatusTopic() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDstatusTopic();
	}

	
	@Test
	public void testSetDstatusTopic() throws Exception {
		DistributionEngineEvent testSubject;
		String dstatusTopic = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDstatusTopic(dstatusTopic);
	}

	
	@Test
	public void testGetDnotifTopic() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDnotifTopic();
	}

	
	@Test
	public void testSetDnotifTopic() throws Exception {
		DistributionEngineEvent testSubject;
		String dnotifTopic = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDnotifTopic(dnotifTopic);
	}

	
	@Test
	public void testGetEnvironmentName() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentName();
	}

	
	@Test
	public void testSetEnvironmentName() throws Exception {
		DistributionEngineEvent testSubject;
		String environmentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironmentName(environmentName);
	}

	
	@Test
	public void testGetRole() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRole();
	}

	
	@Test
	public void testSetRole() throws Exception {
		DistributionEngineEvent testSubject;
		String role = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRole(role);
	}

	
	@Test
	public void testGetApiKey() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiKey();
	}

	
	@Test
	public void testSetApiKey() throws Exception {
		DistributionEngineEvent testSubject;
		String apiKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setApiKey(apiKey);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		DistributionEngineEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		DistributionEngineEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		DistributionEngineEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		DistributionEngineEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		DistributionEngineEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		DistributionEngineEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		DistributionEngineEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		DistributionEngineEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		DistributionEngineEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testSetConsumerId() throws Exception {
		DistributionEngineEvent testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	
	@Test
	public void testToString() throws Exception {
		DistributionEngineEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

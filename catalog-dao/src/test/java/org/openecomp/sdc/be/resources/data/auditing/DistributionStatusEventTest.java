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
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;

import java.util.Date;
import java.util.UUID;


public class DistributionStatusEventTest {

	private DistributionStatusEvent createTestSubject() {
		return new DistributionStatusEvent();
	}

	@Test
	public void testCtor() throws Exception {
		new DistributionStatusEvent();
		new DistributionStatusEvent("mock", CommonAuditData.newBuilder().build(),new DistributionData("",""), "mock", "mock", "mock");
	}
	
	@Test
	public void testFillFields() throws Exception {
		DistributionStatusEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetDid() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDid();
	}

	
	@Test
	public void testSetDid() throws Exception {
		DistributionStatusEvent testSubject;
		String did = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDid(did);
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testSetConsumerId() throws Exception {
		DistributionStatusEvent testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	
	@Test
	public void testGetTopicName() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTopicName();
	}

	
	@Test
	public void testSetTopicName() throws Exception {
		DistributionStatusEvent testSubject;
		String topicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTopicName(topicName);
	}

	
	@Test
	public void testGetResourceURL() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResoureURL();
	}

	
	@Test
	public void testSetResourceURL() throws Exception {
		DistributionStatusEvent testSubject;
		String resourceURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResoureURL(resourceURL);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		DistributionStatusEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		DistributionStatusEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		DistributionStatusEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		DistributionStatusEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		DistributionStatusEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		DistributionStatusEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		DistributionStatusEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		DistributionStatusEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		DistributionStatusEvent testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp);
	}

	
	@Test
	public void testGetStatusTime() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatusTime();
	}

	
	@Test
	public void testSetStatusTime() throws Exception {
		DistributionStatusEvent testSubject;
		String statusTime = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatusTime(statusTime);
	}

	
	@Test
	public void testToString() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}

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
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

public class CommonAuditDataTest {

	private CommonAuditData createTestSubject() {
		return CommonAuditData.newBuilder().build();
	}

	@Test
	public void testGetStatus() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testGetDescription() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testGetRequestId() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	@Test
	public void testGetServiceInstanceId() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	@Test
	public void testSetServiceInstanceId() throws Exception {
		CommonAuditData testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testNewBuilder() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
	}
	
	@Test
	public void testDescription() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
		result.description("mock");
	}
	
	@Test
	public void testStatus() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
		result.status(1);
		result.status("mock");
	}
	
	@Test
	public void testRequestId() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
		result.requestId("mock");
	}
	
	@Test
	public void testServiceInstanceId() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
		result.serviceInstanceId("mock");
	}
}

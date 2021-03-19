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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuditingGenericEventTest {

	private AuditingGenericEvent createTestSubject() {
		return new AuditingGenericEvent();
	}

	@Test
	public void testTimestamp() throws Exception {
		AuditingGenericEvent testSubject = createTestSubject();
		testSubject.setTimestamp("timestamp");
		assertEquals("timestamp", testSubject.getTimestamp());
	}

	@Test
	public void testFields() throws Exception {
		AuditingGenericEvent testSubject = createTestSubject();
		Map<String, Object> map = new HashMap<>();
		testSubject.setFields(map);
		assertNotNull(testSubject.getFields());
	}

	@Test
	public void testGetRequestId() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	@Test
	public void testSetRequestId() throws Exception {
		AuditingGenericEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	@Test
	public void testGetServiceInstanceId() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	@Test
	public void testSetServiceInstanceId() throws Exception {
		AuditingGenericEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetAction() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		AuditingGenericEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	@Test
	public void testGetStatus() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		AuditingGenericEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetDesc() throws Exception {
		AuditingGenericEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	@Test
	public void testSetDesc() throws Exception {
		AuditingGenericEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	@Test
	public void testFillFields() throws Exception {
		AuditingGenericEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}
}

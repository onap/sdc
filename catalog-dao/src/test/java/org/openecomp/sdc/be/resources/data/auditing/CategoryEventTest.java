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
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

import java.util.Date;
import java.util.UUID;

public class CategoryEventTest {

	private CategoryEvent createTestSubject() {
		return new CategoryEvent();
	}

	@Test
	public void testCtor() throws Exception {
		new CategoryEvent();
		Builder newBuilder = CommonAuditData.newBuilder();
		new CategoryEvent("mock", newBuilder.build(), "mock", "mock", "mock", "mock", "mock");
	}
	
	@Test
	public void testFillFields() throws Exception {
		CategoryEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	@Test
	public void testGetTimebaseduuid() throws Exception {
		CategoryEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	@Test
	public void testSetTimebaseduuid() throws Exception {
		CategoryEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	@Test
	public void testGetAction() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	@Test
	public void testSetAction() throws Exception {
		CategoryEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	@Test
	public void testGetStatus() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		CategoryEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testGetDesc() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	@Test
	public void testSetDesc() throws Exception {
		CategoryEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	@Test
	public void testGetCategoryName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategoryName();
	}

	@Test
	public void testSetCategoryName() throws Exception {
		CategoryEvent testSubject;
		String categoryName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategoryName(categoryName);
	}

	@Test
	public void testGetSubCategoryName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategoryName();
	}

	@Test
	public void testSetSubCategoryName() throws Exception {
		CategoryEvent testSubject;
		String subCategoryName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubCategoryName(subCategoryName);
	}

	@Test
	public void testGetGroupingName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupingName();
	}

	@Test
	public void testSetGroupingName() throws Exception {
		CategoryEvent testSubject;
		String groupingName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupingName(groupingName);
	}

	@Test
	public void testGetTimestamp1() throws Exception {
		CategoryEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	@Test
	public void testSetTimestamp1() throws Exception {
		CategoryEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	@Test
	public void testGetModifier() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	@Test
	public void testSetModifier() throws Exception {
		CategoryEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	@Test
	public void testGetServiceInstanceId() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	@Test
	public void testSetServiceInstanceId() throws Exception {
		CategoryEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	@Test
	public void testGetResourceType() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	@Test
	public void testSetResourceType() throws Exception {
		CategoryEvent testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	@Test
	public void testGetRequestId() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	@Test
	public void testSetRequestId() throws Exception {
		CategoryEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}
}

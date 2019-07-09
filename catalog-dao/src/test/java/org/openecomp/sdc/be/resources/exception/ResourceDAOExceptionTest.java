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

package org.openecomp.sdc.be.resources.exception;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;


public class ResourceDAOExceptionTest {

	private ResourceDAOException createTestSubject() {
		return new ResourceDAOException("", null);
	}

	@Test
	public void testCtor() throws Exception {
		new ResourceDAOException("mock");
		new ResourceDAOException("mock", new Throwable());
		new ResourceDAOException(ResourceUploadStatus.ALREADY_EXIST, "mock");
		new ResourceDAOException(ResourceUploadStatus.ALREADY_EXIST, "mock", new Throwable());
	}
	
	@Test
	public void testGetStatus() throws Exception {
		ResourceDAOException testSubject;
		ResourceUploadStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		ResourceDAOException testSubject;
		ResourceUploadStatus status = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}
}

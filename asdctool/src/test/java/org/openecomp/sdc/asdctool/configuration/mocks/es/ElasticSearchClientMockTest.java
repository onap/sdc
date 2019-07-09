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

package org.openecomp.sdc.asdctool.configuration.mocks.es;

import org.junit.Test;

public class ElasticSearchClientMockTest {

	private ElasticSearchClientMock createTestSubject() {
		return new ElasticSearchClientMock();
	}

	@Test
	public void testInitialize() throws Exception {
		ElasticSearchClientMock testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.initialize();
	}

	@Test
	public void testSetClusterName() throws Exception {
		ElasticSearchClientMock testSubject;
		String clusterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setClusterName(clusterName);
	}

	@Test
	public void testSetLocal() throws Exception {
		ElasticSearchClientMock testSubject;
		String strIsLocal = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLocal(strIsLocal);
	}

	@Test
	public void testSetTransportClient() throws Exception {
		ElasticSearchClientMock testSubject;
		String strIsTransportclient = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTransportClient(strIsTransportclient);
	}
}

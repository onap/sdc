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

package org.openecomp.sdc.be.dao.es;

import org.elasticsearch.client.Client;
import org.junit.Test;
import org.openecomp.sdc.be.utils.DAOConfDependentTest;

public class ElasticSearchClientTest extends DAOConfDependentTest{

	private ElasticSearchClient createTestSubject() {
		return new ElasticSearchClient();
	}

	@Test
	public void testInitialize() throws Exception {
		ElasticSearchClient testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setTransportClient("true");
		testSubject.setLocal("true");
		testSubject.initialize();
		testSubject.setTransportClient("false");
		testSubject.setClusterName("false");
		testSubject.initialize();
	}
	
	@Test
	public void testClose() throws Exception {
		ElasticSearchClient testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.close();
	}

	
	@Test
	public void testGetClient() throws Exception {
		ElasticSearchClient testSubject;
		Client result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClient();
	}

	
	@Test
	public void testGetServerHost() throws Exception {
		ElasticSearchClient testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServerHost();
	}

	
	@Test
	public void testGetServerPort() throws Exception {
		ElasticSearchClient testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServerPort();
	}

	
	@Test
	public void testSetClusterName() throws Exception {
		ElasticSearchClient testSubject;
		String clusterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setClusterName(clusterName);
	}

	
	@Test
	public void testSetLocal() throws Exception {
		ElasticSearchClient testSubject;
		String strIsLocal = "";

		// test 1
		testSubject = createTestSubject();
		strIsLocal = null;
		testSubject.setLocal(strIsLocal);

		// test 2
		testSubject = createTestSubject();
		strIsLocal = "";
		testSubject.setLocal(strIsLocal);
		
		strIsLocal = "true";
		testSubject.setLocal(strIsLocal);
	}

	
	@Test
	public void testIsTransportClient() throws Exception {
		ElasticSearchClient testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isTransportClient();
	}

	
	@Test
	public void testSetTransportClient() throws Exception {
		ElasticSearchClient testSubject;
		String strIsTransportclient = "";

		// test 1
		testSubject = createTestSubject();
		strIsTransportclient = null;
		testSubject.setTransportClient(strIsTransportclient);

		// test 2
		testSubject = createTestSubject();
		strIsTransportclient = "true";
		testSubject.setTransportClient(strIsTransportclient);
	}
}

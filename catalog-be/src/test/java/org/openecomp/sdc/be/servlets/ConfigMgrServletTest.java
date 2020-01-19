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

package org.openecomp.sdc.be.servlets;

import com.datastax.driver.core.Configuration;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

public class ConfigMgrServletTest {

	private ConfigMgrServlet createTestSubject() {
		return new ConfigMgrServlet();
	}

	
	@Test
	public void testGetConfig() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		String type = "";
		String result;

		// test 1
		testSubject = createTestSubject();
		type = null;


		// test 2
		testSubject = createTestSubject();
		type = "";

		// test 3
		testSubject = createTestSubject();
		type = "configuration";
	}

	
	@Test
	public void testSetConfig1() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;
		String result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig2() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig3() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;
		String result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testSetConfig4() throws Exception {
		ConfigMgrServlet testSubject;
		HttpServletRequest request = null;
		Configuration configuration = null;

		// default test
		testSubject = createTestSubject();
	}
}

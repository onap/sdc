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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

import ch.qos.logback.classic.Logger;

public class ResourceArtifactDownloadServletTest {

	private ResourceArtifactDownloadServlet createTestSubject() {
		return new ResourceArtifactDownloadServlet();
	}

	
	@Test
	public void testGetResourceArtifactByName() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		String resourceName = "";
		String resourceVersion = "";
		String artifactName = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetResourceArtifactMetadata() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		String resourceName = "";
		String resourceVersion = "";
		String artifactName = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetLogger() throws Exception {
		ResourceArtifactDownloadServlet testSubject;
		Logger result;

		// default test
		testSubject = createTestSubject();
	}
}

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

import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

import ch.qos.logback.classic.Logger;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.DownloadArtifactLogic;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.user.UserBusinessLogic;

public class ResourceArtifactDownloadServletTest {

	private ResourceArtifactDownloadServlet createTestSubject() {
		UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
		GroupBusinessLogic groupBL = mock(GroupBusinessLogic.class);
		ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
		ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
		IResourceUploader resourceUploader = mock(IResourceUploader.class);
		DownloadArtifactLogic downloadArtifactLogic = mock(DownloadArtifactLogic.class);

		return new ResourceArtifactDownloadServlet(userBusinessLogic, groupBL, componentInstanceBL, componentsUtils,
			resourceUploader, downloadArtifactLogic);
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

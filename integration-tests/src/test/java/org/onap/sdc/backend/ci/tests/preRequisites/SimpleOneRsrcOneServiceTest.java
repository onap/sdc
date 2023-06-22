/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.sdc.backend.ci.tests.preRequisites;


import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.utils.ArtifactUtils;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;

import static org.testng.AssertJUnit.assertTrue;

public abstract class SimpleOneRsrcOneServiceTest extends ComponentBaseTest {

	protected ResourceReqDetails resourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected ArtifactReqDetails heatArtifactDetails1;

	private static final String heatExtension = "yaml";
	private static final String yangXmlExtension = "xml";
	private static final String muranoPkgExtension = "zip";
	private static final String extension = null;
	private final String folderName = "heatEnv";

	protected User sdncDesignerDetails;
	protected ArtifactReqDetails defaultArtifactDetails;

	protected ArtifactUtils artifactUtils;
	protected Utils utils;

	private static RestResponse createServiceResponse;

	@BeforeMethod
	public void before() throws Exception {

		initializeMembers();
		createComponents();

	}

	public void initializeMembers() throws IOException, Exception {
		sdncDesignerDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		resourceDetails = ElementFactory.getDefaultResource();
		serviceDetails = ElementFactory.getDefaultService();
		heatArtifactDetails1 = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance("resourceInstanceReqDetails");
	}

	protected void createComponents() throws Exception {

		RestResponse response = new ResourceRestUtils().createResource(resourceDetails, sdncDesignerDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);

		response = new ServiceRestUtils().createService(serviceDetails, sdncDesignerDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);

	}

}

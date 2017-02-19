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

package org.openecomp.sdc.ci.tests.preRequisites;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.apache.log4j.lf5.util.ResourceUtils;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.ArtifactUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.BeforeMethod;

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
	protected ResourceUtils resourceUtils;
	protected ArtifactUtils artifactUtils;
	protected Utils utils;

	private static RestResponse createServiceResponse;

	public SimpleOneRsrcOneServiceTest(TestName testName, String className) {
		super(testName, className);
	}

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

		RestResponse response = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);

		response = ServiceRestUtils.createService(serviceDetails, sdncDesignerDetails);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);

	}

}

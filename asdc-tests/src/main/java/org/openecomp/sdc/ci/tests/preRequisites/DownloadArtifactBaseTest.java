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

import java.io.IOException;

import org.apache.log4j.lf5.util.ResourceUtils;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
import org.openecomp.sdc.ci.tests.utils.ArtifactUtils;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;

public class DownloadArtifactBaseTest extends ComponentBaseTest {

	protected ResourceReqDetails downloadResourceDetails;
	protected ServiceReqDetails serviceDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
	protected User sdncUserDetails;
	protected User sdncDesignerDetails1;
	protected ArtifactReqDetails heatArtifactDetails;

	protected ArtifactReqDetails defaultArtifactDetails;
	protected ResourceUtils resourceUtils;
	protected ArtifactUtils artifactUtils;
	protected Service service;

	public DownloadArtifactBaseTest(TestName testName, String className) {
		super(testName, className);
	}

	@BeforeMethod
	public void before() throws Exception {

		initializeMembers();
		createComponents();

	}

	public void initializeMembers() throws IOException, Exception {
		downloadResourceDetails = ElementFactory.getDefaultResource();
		serviceDetails = ElementFactory.getDefaultService();
		sdncUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance();

	}

	protected void createComponents() throws Exception {

		RestResponse response = ResourceRestUtils.createResource(downloadResourceDetails, sdncUserDetails);
		AssertJUnit.assertTrue("create request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 201);
		AssertJUnit.assertNotNull("resource uniqueId is null:", downloadResourceDetails.getUniqueId());

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncUserDetails,
				downloadResourceDetails.getUniqueId());
		AssertJUnit.assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		// certified resource
		response = LCSbaseTest.certifyResource(downloadResourceDetails, sdncDesignerDetails1);
		AssertJUnit.assertTrue("certify resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = ServiceRestUtils.createService(serviceDetails, sdncUserDetails);
		AssertJUnit.assertTrue("create request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 201);
		AssertJUnit.assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

		// add resource instance with HEAT deployment artifact to the service
		resourceInstanceReqDetails.setComponentUid(downloadResourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, sdncUserDetails,
				serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		AssertJUnit.assertTrue("response code is not 201, returned: " + response.getErrorCode(),
				response.getErrorCode() == 201);

		response = ServiceRestUtils.getService(serviceDetails, sdncUserDetails);
		AssertJUnit.assertTrue("response code is not 200, returned: " + response.getErrorCode(),
				response.getErrorCode() == 200);
		service = ResponseParser.convertServiceResponseToJavaObject(response.getResponse());

		DbUtils.cleanAllAudits();

	}
}

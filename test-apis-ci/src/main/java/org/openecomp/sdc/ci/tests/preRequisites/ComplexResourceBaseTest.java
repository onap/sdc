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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.BeforeMethod;

public class ComplexResourceBaseTest extends ComponentBaseTest {

	protected ServiceReqDetails serviceDetails;
	protected ResourceReqDetails resourceDetailsVFC;
	protected ResourceReqDetails resourceDetailsVL;
	protected ResourceReqDetails resourceDetailsVF;
	protected ResourceReqDetails resourceDetailsCP;
	protected ComponentInstanceReqDetails resourceInstanceReqDetailsVF;
	protected ComponentInstanceReqDetails resourceInstanceReqDetailsVFC;
	protected ComponentInstanceReqDetails resourceInstanceReqDetailsVL;
	protected ComponentInstanceReqDetails resourceInstanceReqDetailsCP;
	protected User sdncDesignerDetails1;
	protected User sdncTesterDeatails1;
	protected User sdncAdminDetails1;
	protected ArtifactReqDetails heatArtifactDetails;

	protected ArtifactReqDetails defaultArtifactDetails;
	protected int maxLength = 50;
	protected Resource resourceVF = null;

	@Rule
	public static TestName name = new TestName();

	public ComplexResourceBaseTest() {
		super(name, ComplexResourceBaseTest.class.getName());
	}

	@BeforeMethod
	public void before() throws Exception {

		initializeMembers();

		createComponents();

	}

	public void initializeMembers() throws IOException, Exception {

		serviceDetails = ElementFactory.getDefaultService();
		resourceDetailsVFC = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VFC, "resourceVFC");
		resourceDetailsVF = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, "resourceVF3");
		resourceDetailsVL = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VL, "resourceVL");
		resourceDetailsCP = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.CP, "resourceCP");
		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncAdminDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());

	}

	protected void createComponents() throws Exception {

		RestResponse response = ServiceRestUtils.createService(serviceDetails, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

		response = ResourceRestUtils.createResource(resourceDetailsVFC, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVFC.getUniqueId());
		response = LifecycleRestUtils.changeResourceState(resourceDetailsVFC, sdncDesignerDetails1,
				resourceDetailsVFC.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LS state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = ResourceRestUtils.createResource(resourceDetailsVF, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVF.getUniqueId());

		response = ResourceRestUtils.createResource(resourceDetailsCP, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsCP.getUniqueId());
		response = LifecycleRestUtils.changeResourceState(resourceDetailsCP, sdncDesignerDetails1,
				resourceDetailsCP.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LS state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = ResourceRestUtils.createResource(resourceDetailsVL, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVL.getUniqueId());
		response = LifecycleRestUtils.changeResourceState(resourceDetailsVL, sdncDesignerDetails1,
				resourceDetailsVL.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LS state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		resourceInstanceReqDetailsVFC = ElementFactory.getDefaultComponentInstance("VFC", resourceDetailsVFC);
		resourceInstanceReqDetailsVF = ElementFactory.getDefaultComponentInstance("VF", resourceDetailsVF);
		resourceInstanceReqDetailsVL = ElementFactory.getDefaultComponentInstance("VL", resourceDetailsVL);
		resourceInstanceReqDetailsCP = ElementFactory.getDefaultComponentInstance("CP", resourceDetailsCP);

	}

	protected void createVFWithCertifiedResourceInstance(ResourceReqDetails resourceDetails,
			ComponentInstanceReqDetails resourceInstanceReqDetails) throws Exception {

		RestResponse response = LifecycleRestUtils.changeResourceState(resourceDetails, sdncDesignerDetails1,
				resourceDetails.getVersion(), LifeCycleStatesEnum.CHECKOUT);
		assertEquals("Check response code after CHECKOUT", 200, response.getErrorCode().intValue());

		// add heat artifact to resource and certify
		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1,
				resourceDetails.getUniqueId());
		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);
		response = LCSbaseTest.certifyResource(resourceDetails, sdncDesignerDetails1);
		assertEquals("Check response code after CERTIFY request", 200, response.getErrorCode().intValue());

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);

		resourceInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(resourceInstanceReqDetails, sdncDesignerDetails1,
				resourceVF);
		assertEquals("Check response code after create RI", 201, response.getErrorCode().intValue());

		resourceVF = convertResourceGetResponseToJavaObject(resourceDetailsVF);
	}

	protected Resource convertResourceGetResponseToJavaObject(ResourceReqDetails resourceDetails) throws IOException {
		RestResponse response = ResourceRestUtils.getResource(resourceDetails, sdncDesignerDetails1);
		assertEquals("Check response code after get resource", 200, response.getErrorCode().intValue());
		return ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
	}

}

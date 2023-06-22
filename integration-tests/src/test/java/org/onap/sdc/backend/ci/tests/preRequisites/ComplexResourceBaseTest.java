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

import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.execute.lifecycle.LCSbaseTest;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.testng.annotations.BeforeMethod;
import java.io.IOException;

import static org.testng.AssertJUnit.*;

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

		RestResponse response = new ServiceRestUtils().createService(serviceDetails, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

		response = new ResourceRestUtils().createResource(resourceDetailsVFC, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVFC.getUniqueId());
		response = new LifecycleRestUtils().changeResourceState(resourceDetailsVFC, sdncDesignerDetails1,
				resourceDetailsVFC.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LS state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = new ResourceRestUtils().createResource(resourceDetailsVF, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVF.getUniqueId());

		response = new ResourceRestUtils().createResource(resourceDetailsCP, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsCP.getUniqueId());
		response = new LifecycleRestUtils().changeResourceState(resourceDetailsCP, sdncDesignerDetails1,
				resourceDetailsCP.getVersion(), LifeCycleStatesEnum.CHECKIN);
		assertTrue("change LS state to CHECKIN, returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		response = new ResourceRestUtils().createResource(resourceDetailsVL, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetailsVL.getUniqueId());
		response = new LifecycleRestUtils().changeResourceState(resourceDetailsVL, sdncDesignerDetails1,
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

		RestResponse response = new LifecycleRestUtils().changeResourceState(resourceDetails, sdncDesignerDetails1,
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
		RestResponse response = new ResourceRestUtils().getResource(resourceDetails, sdncDesignerDetails1);
		assertEquals("Check response code after get resource", 200, response.getErrorCode().intValue());
		return ResponseParser.convertResourceResponseToJavaObject(response.getResponse());
	}

}

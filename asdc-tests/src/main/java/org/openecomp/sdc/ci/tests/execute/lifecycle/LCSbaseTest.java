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

package org.openecomp.sdc.ci.tests.execute.lifecycle;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;

import org.apache.log4j.lf5.util.ResourceUtils;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.ArtifactUtils;
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.BeforeMethod;

/**
 * 
 * @author al714h
 * 
 *         resourceDetails - create, Add Heat, certify resourceDetails1 - create
 *         resource, LCS - CheckOut serviceDetails - create, add RI from
 *         resourceDetails serviceDetails2 - create, add RI from resourceDetails
 *         serviceDetailsEmpty - create, LCS - CheckOut serviceDetailsEmpty2 -
 *         create, LCS - CheckOut
 *
 */
public abstract class LCSbaseTest extends ComponentBaseTest {

	protected ResourceReqDetails resourceDetails;
	protected ResourceReqDetails resourceDetails1;
	protected ServiceReqDetails serviceDetails;
	protected ServiceReqDetails serviceDetails2;
	protected ServiceReqDetails serviceDetailsEmpty;
	protected ServiceReqDetails serviceDetailsEmpty2;
	protected ComponentInstanceReqDetails componentInstanceReqDetails;
	protected ComponentInstanceReqDetails resourceInstanceReqDetails2;
	protected User sdncDesignerDetails1;
	protected User sdncDesignerDetails2;
	protected static User sdncTesterDeatails1;
	protected User sdncAdminDetails1;
	protected ArtifactReqDetails heatArtifactDetails;
	protected ArtifactReqDetails heatVolArtifactDetails;
	protected ArtifactReqDetails heatNetArtifactDetails;

	protected ArtifactReqDetails defaultArtifactDetails;
	protected ResourceUtils resourceUtils;
	protected ArtifactUtils artifactUtils;

	// protected static ServiceUtils serviceUtils = new ServiceUtils();
	public LCSbaseTest(TestName testName, String className) {
		super(testName, className);
	}

	@BeforeMethod
	public void before() throws Exception {

		initializeMembers();

		createComponents();

	}

	public void initializeMembers() throws IOException, Exception {
		resourceDetails = ElementFactory.getDefaultResource();
		// resourceDetails =
		// ElementFactory.getDefaultResource("myNewResource1234567890",
		// NormativeTypesEnum.ROOT, ResourceServiceCategoriesEnum.ROUTERS,
		// UserRoleEnum.DESIGNER.getUserId());
		resourceDetails1 = ElementFactory.getDefaultResource("secondResource", NormativeTypesEnum.ROOT);
		serviceDetails = ElementFactory.getDefaultService();
		serviceDetails2 = ElementFactory.getDefaultService("newTestService2", ServiceCategoriesEnum.MOBILITY, "al1976");
		serviceDetailsEmpty = ElementFactory.getDefaultService("newEmptyService", ServiceCategoriesEnum.MOBILITY,
				"al1976");
		serviceDetailsEmpty2 = ElementFactory.getDefaultService("newEmptyService2", ServiceCategoriesEnum.MOBILITY,
				"al1976");
		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		sdncDesignerDetails2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);
		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		sdncAdminDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		heatNetArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_NET.getType());
		heatVolArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT_VOL.getType());
		componentInstanceReqDetails = ElementFactory.getDefaultComponentInstance();
		resourceInstanceReqDetails2 = ElementFactory.getDefaultComponentInstance();

	}

	protected void createComponents() throws Exception {

		RestResponse response = ResourceRestUtils.createResource(resourceDetails1, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetails1.getUniqueId());

		response = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("resource uniqueId is null:", resourceDetails.getUniqueId());

		response = ServiceRestUtils.createService(serviceDetails, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1,
				resourceDetails.getUniqueId());
		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		// certified resource
		response = LCSbaseTest.certifyResource(resourceDetails, sdncDesignerDetails1);
		assertTrue("certify resource request returned status:" + response.getErrorCode(),
				response.getErrorCode() == 200);

		// add resource instance with HEAT deployment artifact to the service
		componentInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails, sdncDesignerDetails1,
				serviceDetails.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);

		response = ServiceRestUtils.createService(serviceDetails2, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetails2.getUniqueId());

		componentInstanceReqDetails.setComponentUid(resourceDetails.getUniqueId());
		response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails, sdncDesignerDetails1,
				serviceDetails2.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertTrue("response code is not 201, returned: " + response.getErrorCode(), response.getErrorCode() == 201);

		response = ServiceRestUtils.createService(serviceDetailsEmpty, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetailsEmpty.getUniqueId());

		response = ServiceRestUtils.createService(serviceDetailsEmpty2, sdncDesignerDetails1);
		assertTrue("create request returned status:" + response.getErrorCode(), response.getErrorCode() == 201);
		assertNotNull("service uniqueId is null:", serviceDetailsEmpty2.getUniqueId());

		DbUtils.cleanAllAudits();

	}

	public static RestResponse certifyResource(ResourceReqDetails resourceDetails, User user) throws Exception {
		RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user.getUserId(),
				LifeCycleStatesEnum.CHECKIN);
		// if (restResponseResource.getErrorCode() == 200){
		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		// }else
		// return restResponseResource;
		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails,
					sdncTesterDeatails1.getUserId(), LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponseResource;
		if (restResponseResource.getErrorCode() == 200) {
			restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails,
					sdncTesterDeatails1.getUserId(), LifeCycleStatesEnum.CERTIFY);
		}
		return restResponseResource;
	}

	public static RestResponse certifyService(ServiceReqDetails serviceDetails, User user) throws Exception {
		RestResponse restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails, user,
				LifeCycleStatesEnum.CHECKIN);
		// if (restResponseService.getErrorCode() == 200){
		restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails, user,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		// }else
		// return restResponseService;

		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
		if (restResponseService.getErrorCode() == 200) {
			restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDeatails1,
					LifeCycleStatesEnum.STARTCERTIFICATION);
		} else
			return restResponseService;
		if (restResponseService.getErrorCode() == 200) {
			restResponseService = LifecycleRestUtils.changeServiceState(serviceDetails, sdncTesterDeatails1,
					LifeCycleStatesEnum.CERTIFY);
		}
		return restResponseService;
	}

	protected static RestResponse raiseResourceToTargetVersion(ResourceReqDetails resourceDetails, String targetVersion,
			User user) throws Exception {
		return raiseResourceToTargetVersion(resourceDetails, targetVersion, null, user);
	}

	protected static RestResponse raiseResourceToTargetVersion(ResourceReqDetails resourceDetails, String targetVersion,
			RestResponse prevResponse, User user) throws Exception {

		String[] splitParts = targetVersion.split("\\.");

		int version = Integer.parseInt(splitParts[1]);
		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";

		if (prevResponse != null) {
			Resource resourceRespJavaObject = ResponseParser
					.convertResourceResponseToJavaObject(prevResponse.getResponse());
			if (resourceRespJavaObject.getLifecycleState().equals(LifecycleStateEnum.CERTIFIED)) {
				RestResponse restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails,
						user.getUserId(), LifeCycleStatesEnum.CHECKOUT);
			}
		}

		RestResponse restResponseResource = null;
		for (int i = 0; i < (version - 1); i++) {

			restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user, null,
					LifeCycleStatesEnum.CHECKIN, checkinComentJson);
			if (restResponseResource.getErrorCode() == 200) {
				restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user.getUserId(),
						LifeCycleStatesEnum.CHECKOUT);
				if (restResponseResource.getErrorCode() == 200) {

				} else
					break;

			} else
				break;

		}

		restResponseResource = LifecycleRestUtils.changeResourceState(resourceDetails, user, null,
				LifeCycleStatesEnum.CHECKIN, checkinComentJson);
		assertEquals("Check response code ", 200, restResponseResource.getErrorCode().intValue());
		return restResponseResource;
	}

}

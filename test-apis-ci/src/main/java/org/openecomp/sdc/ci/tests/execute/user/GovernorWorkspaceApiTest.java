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

package org.openecomp.sdc.ci.tests.execute.user;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
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
import org.openecomp.sdc.ci.tests.utils.DbUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ServiceValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GovernorWorkspaceApiTest extends ComponentBaseTest {

	private static Logger logger = LoggerFactory.getLogger(GovernorWorkspaceApiTest.class.getName());
	@Rule
	public static TestName name = new TestName();

	public GovernorWorkspaceApiTest() {
		super(name, GovernorWorkspaceApiTest.class.getName());

	}

	protected final User admin1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected final User governor = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
	protected final User sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected ResourceReqDetails resourceDetails1;
	protected ComponentInstanceReqDetails componentInstanceReqDetails;
	protected ArtifactReqDetails heatArtifactDetails;

	protected final String serviceVersion = "0.1";
	protected final String servicesString = "services";
	protected final String userRemarks = "commentTest";

	protected ServiceReqDetails serviceDetails11 = null;
	protected ServiceReqDetails serviceDetails22 = null;
	protected ServiceReqDetails serviceDetails33 = null;

	@BeforeMethod
	public void initBeforeTest() throws Exception {
		DbUtils.deleteFromEsDbByPattern("_all");
		Resource resourceObj = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetails1 = new ResourceReqDetails(resourceObj);
		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		createThreeServices(sdncDesignerDetails1);
	}

	protected void createThreeServices(User user) throws Exception {

		String checkinComment = "good checkin";
		String checkinComentJson = "{\"userRemarks\": \"" + checkinComment + "\"}";

		RestResponse addInformationalArtifactToResource = ArtifactRestUtils.addInformationalArtifactToResource(
				heatArtifactDetails, sdncDesignerDetails1, resourceDetails1.getUniqueId());
		RestResponse certifyResource = LifecycleRestUtils.certifyResource(resourceDetails1);
		componentInstanceReqDetails = ElementFactory.getDefaultComponentInstance("defaultInstance", resourceDetails1);

		serviceDetails11 = ElementFactory.getDefaultService();
		serviceDetails22 = ElementFactory.getDefaultService();
		serviceDetails33 = ElementFactory.getDefaultService();

		serviceDetails11.setName(serviceDetails11.getName() + "1");
		List<String> tags = serviceDetails11.getTags();
		tags.add(serviceDetails11.getName());
		serviceDetails11.setTags(tags);

		serviceDetails22.setName(serviceDetails11.getName() + "2");
		tags = serviceDetails22.getTags();
		tags.add(serviceDetails22.getName());
		serviceDetails22.setTags(tags);

		serviceDetails33.setName(serviceDetails11.getName() + "3");
		tags = serviceDetails33.getTags();
		tags.add(serviceDetails33.getName());
		serviceDetails33.setTags(tags);
		
		RestResponse createServiceResponse1 = createService(user, serviceDetails11);
		RestResponse createServiceResponse2 = createService(user, serviceDetails22);
		RestResponse createServiceResponse3 = createService(user, serviceDetails33);
	}

	protected RestResponse createService(User user, ServiceReqDetails serviceDetails) throws Exception, IOException {
		RestResponse createServiceResponse1 = ServiceRestUtils.createService(serviceDetails, user);
		assertNotNull("check response object is not null after creating service", createServiceResponse1);
		assertNotNull("check if error code exists in response after creating service",
				createServiceResponse1.getErrorCode());
		assertEquals("Check response code after creating service", 201,
				createServiceResponse1.getErrorCode().intValue());
		Service convertServiceResponseToJavaObject = ResponseParser
				.convertServiceResponseToJavaObject(createServiceResponse1.getResponse());
		serviceDetails.setUniqueId(convertServiceResponseToJavaObject.getUniqueId());
		logger.debug("Created service1 ={}", serviceDetails);
		addResourceWithHeatArt(serviceDetails);
		return createServiceResponse1;
	}

	protected void addResourceWithHeatArt(ServiceReqDetails serviceDetails) throws Exception {

		RestResponse createResourceInstance = ComponentInstanceRestUtils.createComponentInstance(
				componentInstanceReqDetails, sdncDesignerDetails1, serviceDetails.getUniqueId(),
				ComponentTypeEnum.SERVICE);
		// System.out.println("serviceUID --->" + serviceDetails.getUniqueId());
		assertEquals("Check response code ", 201, createResourceInstance.getErrorCode().intValue());
	}

	protected void certifyAllServices() throws Exception {
		LifecycleRestUtils.certifyService(serviceDetails11);
		LifecycleRestUtils.certifyService(serviceDetails22);
		LifecycleRestUtils.certifyService(serviceDetails33);
	}

	protected boolean isElementInArray(String elementId, JSONArray jsonArray) throws Exception {
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jobject = (JSONObject) jsonArray.get(i);

			if (jobject.get("uniqueId").toString().equals(elementId)) {
				return true;
			}
		}
		return false;
	}

	protected void approveDistributionStatusOfCertifiedService(ServiceReqDetails serviceDetails, User user)
			throws Exception {
		approveDistributionStatusOfService(serviceDetails, user, "1.0");
	}

	protected void approveDistributionStatusOfService(ServiceReqDetails serviceDetails, User user, String version)
			throws Exception {
		RestResponse res = LifecycleRestUtils.sendApproveDistribution(user, serviceDetails.getUniqueId(), userRemarks);
		assertEquals(200, res.getErrorCode().intValue());
		ServiceValidationUtils.validateDistrubtionStatusValue(res, DistributionStatusEnum.DISTRIBUTION_APPROVED);
	}

	protected void rejectDistributionStatusOfService(ServiceReqDetails serviceDetails, User user) throws Exception {
		rejectDistributionStatusOfService(serviceDetails, user, "1.0");
	}

	protected void rejectDistributionStatusOfService(ServiceReqDetails serviceDetails, User user, String version)
			throws Exception {
		RestResponse res = LifecycleRestUtils.rejectDistribution(serviceDetails, version, user, userRemarks);
		assertEquals(200, res.getErrorCode().intValue());
		ServiceValidationUtils.validateDistrubtionStatusValue(res, DistributionStatusEnum.DISTRIBUTION_REJECTED);
	}

	protected JSONArray getFollowedListAsJsonArray(User user) throws Exception {
		RestResponse getGovernorFollowed = ServiceRestUtils.getFollowed(user);
		assertNotNull(getGovernorFollowed);
		assertNotNull(getGovernorFollowed.getErrorCode());
		assertEquals(200, getGovernorFollowed.getErrorCode().intValue());

		JSONArray listArrayFromRestResponse = ServiceRestUtils.getListArrayFromRestResponse(getGovernorFollowed);

		return listArrayFromRestResponse;
	}

	protected void changeDistributionStatusOfAllService(boolean approved, User user) throws Exception {
		if (approved) {
			approveDistributionStatusOfCertifiedService(serviceDetails11, user);
			approveDistributionStatusOfCertifiedService(serviceDetails22, user);
			approveDistributionStatusOfCertifiedService(serviceDetails33, user);
		} else {
			rejectDistributionStatusOfService(serviceDetails11, user);
			rejectDistributionStatusOfService(serviceDetails22, user);
			rejectDistributionStatusOfService(serviceDetails33, user);
		}

	}

	protected JSONArray checkFollowed(User user) throws Exception {
		JSONArray getFollowedList = getFollowedListAsJsonArray(user);
		assertFalse(getFollowedList.isEmpty());
		assertTrue(isElementInArray(serviceDetails11.getUniqueId(), getFollowedList));
		assertTrue(isElementInArray(serviceDetails22.getUniqueId(), getFollowedList));
		assertTrue(isElementInArray(serviceDetails33.getUniqueId(), getFollowedList));

		return getFollowedList;
	}

	// -------------------------------------T E S T
	// S------------------------------------------------------//

	@Test
	public void governorList_AllCertifiedVersionsOfService() throws Exception {
		certifyAllServices();
		String serviceUniqueIdCertified1 = serviceDetails11.getUniqueId();
		RestResponse res = LifecycleRestUtils.changeServiceState(serviceDetails11, sdncDesignerDetails1, "1.0",
				LifeCycleStatesEnum.CHECKOUT);
		assertEquals(200, res.getErrorCode().intValue());

		JSONArray getFollowedList = getFollowedListAsJsonArray(governor);
		assertFalse(getFollowedList.isEmpty());
		assertFalse(isElementInArray(serviceDetails11.getUniqueId(), getFollowedList));
		assertTrue(isElementInArray(serviceDetails22.getUniqueId(), getFollowedList));
		assertTrue(isElementInArray(serviceDetails33.getUniqueId(), getFollowedList));
		assertTrue(isElementInArray(serviceUniqueIdCertified1, getFollowedList));
		assertEquals(3, getFollowedList.size());

		// certifyService(serviceDetails11, "1.1");
		LifecycleRestUtils.certifyService(serviceDetails11);

		JSONArray governorFollowedList2 = checkFollowed(governor);
		assertEquals(4, governorFollowedList2.size());
		assertTrue(isElementInArray(serviceDetails11.getUniqueId(), governorFollowedList2));
		assertTrue(isElementInArray(serviceUniqueIdCertified1, governorFollowedList2));

	}

	// -------------------------------------T E S T
	// S------------------------------------------------------//

	@Test
	public void governorList_distributionNotApproved() throws Exception {
		certifyAllServices();

		JSONArray checkFollowed = checkFollowed(governor);
		assertEquals(3, checkFollowed.size());
	}

	@Test
	public void governorGetEmptyListTest_notCertifiedServices() throws Exception {
		JSONArray governorFollowedList = getFollowedListAsJsonArray(governor);

		assertTrue(governorFollowedList.isEmpty());
	}

	@Test
	public void governorList_distributionApproved() throws Exception {
		certifyAllServices();
		boolean approved = true;
		changeDistributionStatusOfAllService(approved, governor);

		JSONArray checkFollowed = checkFollowed(governor);
		assertEquals(3, checkFollowed.size());
	}

	@Test(enabled = false)
	public void governorList_distributed() throws Exception {
		certifyAllServices();

		LifecycleRestUtils.changeDistributionStatus(serviceDetails11, "1.0", governor, userRemarks,
				DistributionStatusEnum.DISTRIBUTED);
		LifecycleRestUtils.changeDistributionStatus(serviceDetails22, "1.0", governor, userRemarks,
				DistributionStatusEnum.DISTRIBUTED);
		LifecycleRestUtils.changeDistributionStatus(serviceDetails33, "1.0", governor, userRemarks,
				DistributionStatusEnum.DISTRIBUTED);

		JSONArray governorFollowedList = getFollowedListAsJsonArray(governor);
		assertFalse(governorFollowedList.isEmpty());
		assertTrue(isElementInArray(serviceDetails11.getUniqueId(), governorFollowedList));
		assertTrue(isElementInArray(serviceDetails22.getUniqueId(), governorFollowedList));
		assertTrue(isElementInArray(serviceDetails33.getUniqueId(), governorFollowedList));
	}

	@Test
	public void governorList_distributionRejected() throws Exception {
		certifyAllServices();
		boolean distributionRejected = false;
		changeDistributionStatusOfAllService(distributionRejected, governor);

		JSONArray checkFollowed = checkFollowed(governor);
		assertEquals(3, checkFollowed.size());
	}

}

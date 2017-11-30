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

package org.openecomp.sdc.ci.tests.execute.service;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.CapReqDef;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentInstanceBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.BaseValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ServiceComponentInstanceCRUDTest extends ComponentInstanceBaseTest {
	private static Logger log = LoggerFactory.getLogger(ServiceComponentInstanceCRUDTest.class.getName());
	private static final String SPACE_STRING = " ";
	private static String REQUIREMENT_NAME = "host";
	private static String CAPABILITY_TYPE = "tosca.capabilities.Container";

	private String reqOwnerId;
	private String capOwnerId;

	public ServiceComponentInstanceCRUDTest() {
		super(new TestName(), ServiceComponentInstanceCRUDTest.class.getSimpleName());
	}

	@BeforeMethod(alwaysRun = true)
	public void before() throws Exception {
		init();
		createComponents();
	}

	private void createComponents() throws Exception {
		createAtomicResource(resourceDetailsVFC_01);
		createAtomicResource(resourceDetailsVFC_02);
		createAtomicResource(resourceDetailsCP_01);
		createAtomicResource(resourceDetailsVL_01);
		createAtomicResource(resourceDetailsVL_02);
		createVF(resourceDetailsVF_01);
		createVF(resourceDetailsVF_02);
		createService(serviceDetails_01);
		certifyResource(resourceDetailsVFC_01);
		certifyResource(resourceDetailsVFC_02);
		RestResponse createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resourceDetailsVF_01,
				resourceDetailsVFC_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		reqOwnerId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);
		createAtomicResourceInstance = createAtomicInstanceForVFDuringSetup(resourceDetailsVF_02, resourceDetailsVFC_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		capOwnerId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);// should
																							// be
																							// updated
																							// to
																							// getUniqueIdOfFirstInstance
																							// in
																							// service
																							// context
	}

	private void certifyResource(ResourceReqDetails resource) throws Exception {
		changeResourceLifecycleState(resource, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		changeResourceLifecycleState(resource, sdncTesterDetails.getUserId(), LifeCycleStatesEnum.STARTCERTIFICATION);
		changeResourceLifecycleState(resource, sdncTesterDetails.getUserId(), LifeCycleStatesEnum.CERTIFY);
	}

	private void changeResourceLifecycleState(ResourceReqDetails resourceDetails, String userUserId,
			LifeCycleStatesEnum lifeCycleStates) throws Exception {
		RestResponse response = LifecycleRestUtils.changeResourceState(resourceDetails, userUserId, lifeCycleStates);
		LifecycleRestUtils.checkLCS_Response(response);
	}

	private void changeServiceLifecycleState(ServiceReqDetails serviceDetails, User user,
			LifeCycleStatesEnum lifeCycleStates) throws Exception {
		RestResponse response = LifecycleRestUtils.changeServiceState(serviceDetails, user, lifeCycleStates);
		LifecycleRestUtils.checkLCS_Response(response);
	}

	private void createVFInstanceFailWithoutChangeState(ActionStatus actionStatus, List<String> variables,
			ResourceReqDetails vfResource, User user, int errorCode) throws Exception {
		RestResponse createVFInstanceSuccessfullyWithoutChangeStateResp = createVFInstance(serviceDetails_01,
				vfResource, user);
		checkErrorMessage(actionStatus, variables, errorCode, createVFInstanceSuccessfullyWithoutChangeStateResp);
	}

	private void createVFInstanceFail(ActionStatus actionStatus, List<String> variables, ResourceReqDetails vfResource,
			User user, int errorCode) throws Exception, FileNotFoundException, JSONException {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, vfResource, user);
		checkErrorMessage(actionStatus, variables, errorCode, createVFInstResp);
	}

	private void deleteVFInstanceFail(ActionStatus actionStatus, List<String> variables, ResourceReqDetails vfResource,
			User user, int errorCode) throws Exception, FileNotFoundException, JSONException {
		RestResponse deleteVFInstResp = deleteVFInstance(vfResource.getUniqueId(), serviceDetails_01, user);
		checkErrorMessage(actionStatus, variables, errorCode, deleteVFInstResp);
	}

	private void createAtomicResourceInstanceFailWithoutChangeState(ActionStatus actionStatus, List<String> variables,
			ResourceReqDetails atomicResource, User user, int errorCode)
			throws Exception, FileNotFoundException, JSONException {
		RestResponse createAtomicInstResp = createAtomicInstanceForService(serviceDetails_01, atomicResource, user);
		checkErrorMessage(actionStatus, variables, errorCode, createAtomicInstResp);
	}

	private void createAtomicResourceInstanceFail(ActionStatus actionStatus, List<String> variables,
			ResourceReqDetails atomicResource, User user, int errorCode)
			throws Exception, FileNotFoundException, JSONException {
		RestResponse createAtomicInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, atomicResource,
				user);
		checkErrorMessage(actionStatus, variables, errorCode, createAtomicInstResp);
	}

	private void deleteAtomicResourceInstanceFail(ActionStatus actionStatus, List<String> variables,
			ResourceReqDetails atomicResource, User user, int errorCode)
			throws Exception, FileNotFoundException, JSONException {
		RestResponse deleteAtomicInstResp = deleteAtomicInstanceForService(atomicResource.getUniqueId(),
				serviceDetails_01, user);
		checkErrorMessage(actionStatus, variables, errorCode, deleteAtomicInstResp);
	}

	private void checkErrorMessage(ActionStatus actionStatus, List<String> variables, int errorCode,
			RestResponse response) throws Exception {

		log.debug(response.getResponse());
		AssertJUnit.assertEquals(errorCode, response.getErrorCode().intValue());
		ErrorValidationUtils.checkBodyResponseOnError(actionStatus.name(), variables, response.getResponse());
	}

	private RestResponse createCheckedinVFInstance(ServiceReqDetails containerDetails,
			ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		changeResourceLifecycleState(compInstOriginDetails, compInstOriginDetails.getCreatorUserId(),
				LifeCycleStatesEnum.CHECKIN);
		return createVFInstance(containerDetails, compInstOriginDetails, modifier);
	}

	private RestResponse createCheckedinAtomicInstanceForService(ServiceReqDetails containerDetails,
			ResourceReqDetails compInstOriginDetails, User modifier) throws Exception {
		changeResourceLifecycleState(compInstOriginDetails, compInstOriginDetails.getCreatorUserId(),
				LifeCycleStatesEnum.CHECKIN);
		return createAtomicInstanceForService(containerDetails, compInstOriginDetails, modifier);
	}

	private void createVFInstanceAndAtomicResourceInstanceWithoutCheckin(ResourceReqDetails vf,
			ResourceReqDetails atomicResource, User user) throws Exception {
		RestResponse createVFInstance = createVFInstance(serviceDetails_01, vf, user);
		ResourceRestUtils.checkCreateResponse(createVFInstance);
		RestResponse atomicInstanceForService = createAtomicInstanceForService(serviceDetails_01, atomicResource, user);
		ResourceRestUtils.checkCreateResponse(atomicInstanceForService);
	}

	private void createVFInstanceAndAtomicResourceInstanceSuccessully(ResourceReqDetails vf,
			ResourceReqDetails atomicResource) throws Exception, IOException {
		createVFInstanceAndAtomicResourceInstanceSuccessully(vf, atomicResource, sdncDesignerDetails);
	}

	private void createVFInstanceAndAtomicResourceInstanceSuccessully(ResourceReqDetails vf,
			ResourceReqDetails atomicResource, User user) throws Exception, IOException {
		changeResourceLifecycleState(vf, vf.getCreatorUserId(), LifeCycleStatesEnum.CHECKIN);
		changeResourceLifecycleState(atomicResource, atomicResource.getCreatorUserId(), LifeCycleStatesEnum.CHECKIN);
		createVFInstanceAndAtomicResourceInstanceWithoutCheckin(vf, atomicResource, user);
	}

	@Test
	public void createVFInstanceSuccessfullyTest() throws Exception {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void createVFAndAtomicInstanceTest() throws Exception {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsVL_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsVL_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		getComponentAndValidateRIs(serviceDetails_01, 4, 0);
	}

	@Test
	public void deleteAtomicInstanceTest() throws Exception {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		// 1 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		// 2 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsVL_01,
				sdncDesignerDetails);
		// 3 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsVL_02,
				sdncDesignerDetails);
		// 4 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		// To delete
		String compInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// 3 rel
		createVFInstResp = deleteAtomicInstanceForService(compInstId, serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(createVFInstResp);
		getComponentAndValidateRIs(serviceDetails_01, 3, 0);
	}

	@Test
	public void deleteVFInstanceTest() throws Exception {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		// 1 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		String compInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		// 2 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		createVFInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		// 3 rel
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		// 2 rel
		createVFInstResp = deleteVFInstance(compInstId, serviceDetails_01, sdncDesignerDetails);
		ResourceRestUtils.checkDeleteResponse(createVFInstResp);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void associateDissociateTwoVFs() throws Exception {

		RestResponse createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String fromCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String toCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);

		String capType = CAPABILITY_TYPE;
		String reqName = REQUIREMENT_NAME;

		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(fromCompInstId, toCompInstId, capType, reqName,
				capList, reqList);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<RequirementDefinition> list = capReqDef.getRequirements().get(capType);
		AssertJUnit.assertEquals("Check requirement", null, list);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		dissociateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		list = capReqDef.getRequirements().get(capType);
		AssertJUnit.assertEquals("Check requirement", 1, list.size());
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	private RequirementCapabilityRelDef getReqCapRelation(String reqCompInstId, String capCompInstId, String capType,
			String reqName, List<CapabilityDefinition> capList, List<RequirementDefinition> reqList) {
		return ElementFactory.getReqCapRelation(reqCompInstId, capCompInstId, reqOwnerId, capOwnerId, capType, reqName,
				capList, reqList);
	}

	@Test
	public void createResourceInstanceByDifferentDesignerTest() throws Exception {
		createVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), 409);
		createAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), 409);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void createResourceInstanceByDifferentDesignerTest_ServiceIsCheckedin() throws Exception {
		User designer2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, designer2, LifeCycleStatesEnum.CHECKOUT);

		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01, designer2);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

	}

	@Test
	public void createResourceInstanceByTester() throws Exception {
		createVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 409);
		createAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 409);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void createResourceInstanceWithNotASDCUserTest() throws Exception {
		sdncDesignerDetails.setUserId("ab0001");
		createVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				sdncDesignerDetails, 409);
		createAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, sdncDesignerDetails, 409);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void createResourceInstanceWithEmptyUserIdTest() throws Exception {
		sdncDesignerDetails.setUserId("");
		createVFInstanceFail(ActionStatus.MISSING_INFORMATION, new ArrayList<String>(), resourceDetailsVF_01,
				sdncDesignerDetails, 403);
		createAtomicResourceInstanceFail(ActionStatus.MISSING_INFORMATION, new ArrayList<String>(),
				resourceDetailsCP_01, sdncDesignerDetails, 403);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void createResourceInstanceWithEmptyServiceUidTest() throws Exception {
		serviceDetails_01.setUniqueId("");
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		assertEquals(404, createVFInstResp.getErrorCode().intValue());
		RestResponse createAtomicInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01,
				resourceDetailsCP_01, sdncDesignerDetails);
		assertEquals(404, createAtomicInstResp.getErrorCode().intValue());
	}

	@Test
	public void createResourceInstanceWhileResourceNotExistTest() throws Exception {
		String vfResourceUniqueId = "1234";
		String atomicResourceUniqueId = "5678";

		resourceDetailsVF_01.setUniqueId(vfResourceUniqueId);
		resourceDetailsCP_01.setUniqueId(atomicResourceUniqueId);

		createVFInstanceFailWithoutChangeState(ActionStatus.RESOURCE_NOT_FOUND,
				new ArrayList<String>(Arrays.asList("")), resourceDetailsVF_01, sdncDesignerDetails, 404);
		createAtomicResourceInstanceFailWithoutChangeState(ActionStatus.RESOURCE_NOT_FOUND,
				new ArrayList<String>(Arrays.asList("")), resourceDetailsCP_01, sdncDesignerDetails, 404);
	}

	@Test
	public void createResourceInstanceInServiceNotExistsTest() throws Exception {
		serviceDetails_01.setUniqueId("1234");
		createVFInstanceFail(ActionStatus.SERVICE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsVF_01, sdncDesignerDetails, 404);
		createAtomicResourceInstanceFail(ActionStatus.SERVICE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsCP_01, sdncDesignerDetails, 404);
	}

	@Test
	public void createResourceInstanceInCheckedinServiceTest() throws Exception {
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);

		createVFInstanceFailWithoutChangeState(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsVF_01, sdncDesignerDetails, 409);
		createAtomicResourceInstanceFailWithoutChangeState(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, sdncDesignerDetails, 409);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test(enabled = false)
	public void createResourceInstance_ResourceInCheckoutStateTest() throws Exception {
		LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		RestResponse createVFInstanceWithoutChangeStateResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ComponentInstanceRestUtils.checkCreateResponse(createVFInstanceWithoutChangeStateResp);
		RestResponse createAtomicInstWithoutCheangeStateResp = createAtomicInstanceForService(serviceDetails_01,
				resourceDetailsCP_01, sdncDesignerDetails);
		ComponentInstanceRestUtils.checkCreateResponse(createAtomicInstWithoutCheangeStateResp);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void createResourceInstance_ResourceInCertificationRequestStateTest() throws Exception {
		changeResourceLifecycleState(resourceDetailsVF_01, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		changeResourceLifecycleState(resourceDetailsCP_01, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void createResourceInstance_startCertificationStateTest() throws Exception {
		changeResourceLifecycleState(resourceDetailsVF_01, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		changeResourceLifecycleState(resourceDetailsCP_01, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);

		changeResourceLifecycleState(resourceDetailsVF_01, sdncTesterDetails.getUserId(),
				LifeCycleStatesEnum.STARTCERTIFICATION);
		changeResourceLifecycleState(resourceDetailsCP_01, sdncTesterDetails.getUserId(),
				LifeCycleStatesEnum.STARTCERTIFICATION);

		createVFInstanceAndAtomicResourceInstanceWithoutCheckin(resourceDetailsVF_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

	}

	@Test
	public void createResourceInstance_certifiedStateTest() throws Exception {
		certifyResource(resourceDetailsVF_01);
		certifyResource(resourceDetailsCP_01);

		createVFInstanceAndAtomicResourceInstanceWithoutCheckin(resourceDetailsVF_01, resourceDetailsCP_01,
				sdncDesignerDetails);
	}

	@Test
	public void createResourceInstance_OneHasDifferentOwner() throws Exception {
		User designer2 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2);

		ResourceReqDetails vfResource = new ResourceReqDetails(resourceDetailsVF_01, "0.1");
		vfResource.setUniqueId(null);
		vfResource.setName("newVF");
		vfResource.setTags(new ArrayList<String>(Arrays.asList(vfResource.getName())));
		createVF(vfResource, designer2);

		RestResponse atomicInstanceForService = createCheckedinAtomicInstanceForService(serviceDetails_01,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(atomicInstanceForService);
		createVFInstanceFailWithoutChangeState(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), vfResource,
				designer2, 409);

		getComponentAndValidateRIs(serviceDetails_01, 1, 0);
	}

	@Test
	public void indexesOfVFInstancesTest() throws Exception {
		String firstInstanceName = resourceDetailsVF_01.getName() + SPACE_STRING + "1";
		String secondInstanceName = resourceDetailsVF_01.getName() + SPACE_STRING + "2";
		String thirdInstanceName = resourceDetailsVF_01.getName() + SPACE_STRING + "3";

		LifecycleRestUtils.changeResourceState(resourceDetailsVF_01, sdncDesignerDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);

		RestResponse createFirstVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createSecondVFInstResp);
		RestResponse createThirdVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createThirdVFInstResp);

		Component service = getComponentAndValidateRIs(serviceDetails_01, 3, 0);
		List<ComponentInstance> componentInstancesList = service.getComponentInstances();
		for (ComponentInstance instance : componentInstancesList) {
			String instanceName = instance.getName();
			boolean isEqualToFirstInstanceName = instanceName.equals(firstInstanceName);
			boolean isEqualToSecondInstanceName = instanceName.equals(secondInstanceName);
			boolean isEqualToThirdInstanceName = instanceName.equals(thirdInstanceName);
			assertTrue(isEqualToFirstInstanceName || isEqualToSecondInstanceName || isEqualToThirdInstanceName);
		}
	}

	@Test
	public void vfInstancesAmountInTwoServiceVersionsTest() throws Exception {
		String oldServiceUniqueId = serviceDetails_01.getUniqueId();

		createTwoCheckedinVFInstances();

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);

		String newSerivceUniqueIdAfterChangeLifecycleState = serviceDetails_01.getUniqueId();
		getComponentAndValidateRIsAfterChangeLifecycleState(oldServiceUniqueId, serviceDetails_01, 2, 0);

		// Check old version
		checkServiceOldVersionRIs(oldServiceUniqueId, newSerivceUniqueIdAfterChangeLifecycleState, 2, 0);

		// Add one more resource instance to second version of service
		LifecycleRestUtils.changeResourceState(resourceDetailsVL_01, sdncDesignerDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		RestResponse createAtomicResourceInstResp = createAtomicResourceInstanceToSecondServiceVersion(
				newSerivceUniqueIdAfterChangeLifecycleState, resourceDetailsVL_01);
		String atomicResourceUniqueId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstResp);
		getComponentAndValidateRIsAfterAddingAtomicResourceInstance(oldServiceUniqueId, serviceDetails_01, 3, 0);

		// Check that RIs are same as in the beginning - like in old version of
		// service
		deleteCompInstReqCapFromExpected(atomicResourceUniqueId);
		checkServiceOldVersionRIs(oldServiceUniqueId, newSerivceUniqueIdAfterChangeLifecycleState, 2, 0);

	}

	private void createTwoCheckedinVFInstances() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createSecondVFInstResp);
	}

	private void getComponentAndValidateRIsAfterAddingAtomicResourceInstance(String oldComponentUniqueId,
			ComponentReqDetails componentDetails, int numOfRIs, int numOfRelations) throws Exception {
		getComponentAndValidateRIsAfterChangeLifecycleState(oldComponentUniqueId, componentDetails, numOfRIs,
				numOfRelations);

	}

	private void checkServiceOldVersionRIs(String oldUniqueId, String newUniqueId, int numOfRIs, int numOfRelations)
			throws IOException, Exception {
		serviceDetails_01.setUniqueId(oldUniqueId);
		getComponentAndValidateRIsAfterChangeLifecycleState(newUniqueId, serviceDetails_01, numOfRIs, numOfRelations);
	}

	private RestResponse createAtomicResourceInstanceToSecondServiceVersion(String secondServiceUniqueId,
			ResourceReqDetails resourceToAdd) throws Exception {
		serviceDetails_01.setUniqueId(secondServiceUniqueId);
		RestResponse createAtomicResourceInstResp = createAtomicInstanceForService(serviceDetails_01, resourceToAdd,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstResp);
		return createAtomicResourceInstResp;
	}

	@Test
	public void createResourceInstanceToUnsupportedComponentTest() throws Exception {
		String unsupportedType = "unsupported";
		ComponentInstanceReqDetails resourceInstanceReqDetails = ElementFactory
				.getComponentResourceInstance(resourceDetailsCP_01);
		RestResponse createResourceInstanceResponse = ComponentInstanceRestUtils.createComponentInstance(
				resourceInstanceReqDetails, sdncDesignerDetails, serviceDetails_01.getUniqueId(), unsupportedType);
		checkErrorMessage(ActionStatus.UNSUPPORTED_ERROR, new ArrayList<String>(Arrays.asList(unsupportedType)), 400,
				createResourceInstanceResponse);
	}

	@Test
	public void deleteResourceInstanceByDifferentDesignerTest() throws Exception {

		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01);

		deleteVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), 409);
		deleteAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2), 409);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void deleteResourceInstanceByDifferentDesignerTest_ServiceIsCheckedin() throws Exception {

		String oldServiceUniqueId = serviceDetails_01.getUniqueId();

		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		RestResponse createAtomicResourceInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01,
				resourceDetailsCP_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstResp);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2),
				LifeCycleStatesEnum.CHECKOUT);
		String newServiceUniqueId = serviceDetails_01.getUniqueId();

		String oldVFInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		String newVFInstanceUniqueId = oldVFInstanceUniqueId.replaceAll(oldServiceUniqueId,
				serviceDetails_01.getUniqueId());
		String oldAtomicResourceInstanceUniqueId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstResp);
		String newAtomicResourceInstanceUniqueId = oldAtomicResourceInstanceUniqueId.replaceAll(oldServiceUniqueId,
				serviceDetails_01.getUniqueId());

		deleteVFInstanceAndAtomicResourceInstanceSuccessfully(newVFInstanceUniqueId, newAtomicResourceInstanceUniqueId,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2));

		serviceDetails_01.setUniqueId(oldServiceUniqueId);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

		serviceDetails_01.setUniqueId(newServiceUniqueId);
		updateExpectedReqCapAfterChangeLifecycleState(oldServiceUniqueId, serviceDetails_01.getUniqueId());
		deleteCompInstReqCapFromExpected(newVFInstanceUniqueId);
		deleteCompInstReqCapFromExpected(newAtomicResourceInstanceUniqueId);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	private void deleteVFInstanceAndAtomicResourceInstanceSuccessfully(String vfInstanceUniqueId,
			String atomicResourceInstanceUniqueId) throws IOException, Exception {
		deleteVFInstanceAndAtomicResourceInstanceSuccessfully(vfInstanceUniqueId, atomicResourceInstanceUniqueId,
				sdncDesignerDetails);
	}

	private void deleteVFInstanceAndAtomicResourceInstanceSuccessfully(String vfInstanceUniqueId,
			String atomicResourceInstanceUniqueId, User user) throws IOException, Exception {
		RestResponse deleteVFInstResp = deleteVFInstance(vfInstanceUniqueId, serviceDetails_01, user);
		ResourceRestUtils.checkDeleteResponse(deleteVFInstResp);
		RestResponse deleteAtomicResourceInsResp = deleteAtomicInstanceForService(atomicResourceInstanceUniqueId,
				serviceDetails_01, user);
		ResourceRestUtils.checkDeleteResponse(deleteAtomicResourceInsResp);
	}

	@Test
	public void deleteResourceInstanceByTesterUserTest() throws Exception {
		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01);
		deleteVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 409);
		deleteAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, ElementFactory.getDefaultUser(UserRoleEnum.TESTER), 409);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void deleteResourceInstanceByNotASDCUserTest() throws Exception {
		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01);
		User notASDCUser = new User();
		notASDCUser.setUserId("ab0001");
		deleteVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				notASDCUser, 409);
		deleteAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, notASDCUser, 409);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void deleteResourceInstanceFromCheckedinServiceTest() throws Exception {
		createVFInstanceAndAtomicResourceInstanceSuccessully(resourceDetailsVF_01, resourceDetailsCP_01);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		deleteVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				sdncDesignerDetails, 409);
		deleteAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, sdncDesignerDetails, 409);
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void deleteResourceInstanceWhileResourceCertifiedStateTest() throws Exception {
		certifyResource(resourceDetailsVF_01);
		certifyResource(resourceDetailsCP_01);

		RestResponse createVFInstance = createVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstance);
		String vfInstUniqueId = ResponseParser.getUniqueIdFromResponse(createVFInstance);
		RestResponse atomicInstanceForService = createAtomicInstanceForService(serviceDetails_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(atomicInstanceForService);
		String atomicInstUniqueId = ResponseParser.getUniqueIdFromResponse(atomicInstanceForService);

		deleteVFInstanceAndAtomicResourceInstanceSuccessfully(vfInstUniqueId, atomicInstUniqueId);

		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	// fail - bug DE191849
	@Test
	public void deleteNotFoundResourceInstanceTest() throws Exception, Throwable {

		resourceDetailsVF_01.setUniqueId("1234");
		resourceDetailsCP_01.setUniqueId("5678");

		deleteVFInstanceFail(ActionStatus.RESOURCE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsVF_01, sdncDesignerDetails, 404);
		deleteAtomicResourceInstanceFail(ActionStatus.RESOURCE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsCP_01, sdncDesignerDetails, 404);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);

		// {"requestError":{"serviceException":{"messageId":"SVC4503","text":"Error:
		// Requested '%1' service was not found.","variables":["1234"]}}}>
	}

	@Test
	public void deleteResourceInstanceFromServiceNotFoundTest() throws Exception, Throwable {
		serviceDetails_01.setUniqueId("1234");
		deleteVFInstanceFail(ActionStatus.SERVICE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsVF_01, sdncDesignerDetails, 404);
		deleteAtomicResourceInstanceFail(ActionStatus.SERVICE_NOT_FOUND, new ArrayList<String>(Arrays.asList("")),
				resourceDetailsCP_01, sdncDesignerDetails, 404);
	}

	@Test
	public void deleteResourceInstanceFromUnsupportedTypeTest() throws Exception {
		String unsupportedType = "unsupportedType";
		RestResponse deleteVFInstanceResponse = ComponentInstanceRestUtils.deleteComponentInstance(sdncDesignerDetails,
				serviceDetails_01.getUniqueId(), resourceDetailsVF_01.getUniqueId(), unsupportedType);
		checkErrorMessage(ActionStatus.UNSUPPORTED_ERROR, new ArrayList<String>(Arrays.asList(unsupportedType)), 400,
				deleteVFInstanceResponse);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void deleteResourceInstanceWithEmptyServiceUidTest() throws Exception, Throwable {
		serviceDetails_01.setUniqueId("");
		RestResponse deleteVFInstResp = deleteVFInstance(resourceDetailsVF_01.getUniqueId(), serviceDetails_01,
				sdncDesignerDetails);
		assertEquals(404, deleteVFInstResp.getErrorCode().intValue());
	}

	@Test
	public void deleteResourceInstanceWithEmptyResourceInstanceUidTest() throws Exception, Throwable {
		RestResponse deleteVFInstResp = deleteVFInstance("", serviceDetails_01, sdncDesignerDetails);
		assertEquals(405, deleteVFInstResp.getErrorCode().intValue());
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	@Test
	public void deleteResourceInstanceWithEmptyUserIdTest() throws Exception {
		sdncDesignerDetails.setUserId("");
		deleteVFInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(), resourceDetailsVF_01,
				sdncDesignerDetails, 409);
		deleteAtomicResourceInstanceFail(ActionStatus.RESTRICTED_OPERATION, new ArrayList<String>(),
				resourceDetailsCP_01, sdncDesignerDetails, 409);
		getComponentAndValidateRIs(serviceDetails_01, 0, 0);
	}

	// fail - bug DE188994
	@Test
	public void associateResourceInstanceToResourceInstanceNotFoundTest() throws Exception, Throwable {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		String capCompInstId = "1234";

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		List<CapabilityDefinition> capListBeforeAssociate = new ArrayList<CapabilityDefinition>();
		CapabilityDefinition cap = new CapabilityDefinition();
		cap.setUniqueId(capCompInstId);
		capListBeforeAssociate.add(cap);
		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		assocaiteInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, 404,
				new ArrayList<String>(Arrays.asList(capCompInstId)));

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capabilitiesAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> requirementsAfterAssoicate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate,
		// requirementsAfterAssoicate);
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capabilitiesAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 1, 0);

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "VF100 1",
		// "9ae76786-2a9c-4409-95cb-db32885ed07f.eece8aaf-eb9f-4aff-b9a5-a11ca11de9e5.vf1001",
		// "host"
		// ]
	}

	// this case is not relevant any more, it is tested as part of occurrences
	// story
	@Test(enabled = false)
	public void associateOnceAgainExistingRelationTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		//////////////////////////////////////////////
		// NO ERROR - RELATION ALREADY EXIST
		// assocaiteInstancesFail(requirementDef, sdncDesignerDetails,
		// ActionStatus.RESOURCE_INSTANCE_NOT_FOUND, 404, new
		// ArrayList<String>(Arrays.asList(capCompInstId)));
		//////////////////////////////////////////////

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListAfterAssociate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		// AssertJUnit.assertEquals("Check requirement", null,
		// reqListAfterAssociate);
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capListAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		// "messageId": "SVC4119",
		// "text": "Error: No relation found between resource instances
		// \u0027%1\u0027 and \u0027%2\u0027 for requirement \u0027%3\u0027.",
		// "variables": [
		// "VF100 1",
		// "VF_admin 2",
		// "host"

	}

	@Test
	public void associateInstancesInMissingServiceTest() throws Exception {
		serviceDetails_01.setUniqueId("1234");
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		assocaiteInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.SERVICE_NOT_FOUND, 404,
				new ArrayList<String>(Arrays.asList("")));
	}

	@Test
	public void associateAfterDeletingResourceTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		ResourceRestUtils.deleteResource(resourceDetailsVF_01.getUniqueId(), sdncDesignerDetails.getUserId());

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);

		// for (CapabilityDefinition capabilityDefinition :
		// capListBeforeAssociate) {
		// if (capabilityDefinition.getType().equals(CAPABILITY_TYPE)){
		// capabilityDefinition.setMinOccurrences("0");
		// }
		// }
		//
		// List<RequirementDefinition> reqListAfterAssociate =
		// capReqDefAfterAssociate.getRequirements().get(CAPABILITY_TYPE);
		//
		// AssertJUnit.assertEquals("Check requirement", null,
		// reqListAfterAssociate);
		//
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capListAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 1);
	}

	@Test
	public void associateInstancesInCheckedinServiceTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);

		assocaiteInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESTRICTED_OPERATION, 409,
				new ArrayList<String>());

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capabilitiesAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> requirementsAfterAssoicate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate, requirementsAfterAssoicate);
		AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate, capabilitiesAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	// fail - bug DE188994
	@Test
	public void associateAfterCheckoutAllInstancesTest() throws Exception {
		String firstVFUniqueId = resourceDetailsVF_01.getUniqueId();
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		String secondVFUniqueId = resourceDetailsVF_02.getUniqueId();
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		changeResourceLifecycleState(resourceDetailsVF_01, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CHECKOUT);
		changeResourceLifecycleState(resourceDetailsVF_02, sdncDesignerDetails.getUserId(),
				LifeCycleStatesEnum.CHECKOUT);

		requirementDef.setFromNode(
				requirementDef.getFromNode().replaceAll(firstVFUniqueId, resourceDetailsVF_01.getUniqueId()));
		requirementDef
				.setToNode(requirementDef.getToNode().replaceAll(secondVFUniqueId, resourceDetailsVF_02.getUniqueId()));

		assocaiteInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESTRICTED_OPERATION, 409,
				new ArrayList<String>());

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capabilitiesAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> requirementsAfterAssoicate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate,
		// requirementsAfterAssoicate);
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capabilitiesAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

		// "messageId": "SVC4116",
		// "text": "Error: Invalid Content.",
		// "variables": [
		// "e9dcea15-ce27-4381-a554-4278973cefb1.d0b3affd-cf92-4626-adfe-961b44103924.vf1001",
		// "e9dcea15-ce27-4381-a554-4278973cefb1.d0b3affd-cf92-4626-adfe-961b44103924.vf1001",
		// "host"
		// ]

	}

	@Test
	public void associateInstancesByDifferentUsersTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		assocaiteInstancesFail(requirementDef, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER2),
				ActionStatus.RESTRICTED_OPERATION, 409, new ArrayList<String>());
		assocaiteInstancesFail(requirementDef, ElementFactory.getDefaultUser(UserRoleEnum.TESTER),
				ActionStatus.RESTRICTED_OPERATION, 409, new ArrayList<String>());
		assocaiteInstancesFail(requirementDef, ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR),
				ActionStatus.RESTRICTED_OPERATION, 409, new ArrayList<String>());
		assocaiteInstancesFail(requirementDef, ElementFactory.getDefaultUser(UserRoleEnum.OPS),
				ActionStatus.RESTRICTED_OPERATION, 409, new ArrayList<String>());
		assocaiteInstancesFail(requirementDef, ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1),
				ActionStatus.RESTRICTED_OPERATION, 409, new ArrayList<String>());

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capabilitiesAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> requirementsAfterAssoicate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate, requirementsAfterAssoicate);
		AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate, capabilitiesAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	private void assocaiteInstancesFail(RequirementCapabilityRelDef requirementDef, User user,
			ActionStatus actionStatus, int errorCode, List<String> variables) throws IOException, Exception {
		RestResponse associateInstancesResp = ComponentInstanceRestUtils.associateInstances(requirementDef, user,
				serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		checkErrorMessage(actionStatus, variables, errorCode, associateInstancesResp);
	}

	private void dissoicateInstancesFail(RequirementCapabilityRelDef requirementDef, User user,
			ActionStatus actionStatus, int errorCode, List<String> variables) throws IOException, Exception {
		RestResponse dissoicateInstancesResp = ComponentInstanceRestUtils.dissociateInstances(requirementDef, user,
				serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		checkErrorMessage(actionStatus, variables, errorCode, dissoicateInstancesResp);
	}

	@Test
	public void associateWithMissingServiceUidTest() throws Exception {
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		serviceDetails_01.setUniqueId("");
		RestResponse associateInstancesResp = ComponentInstanceRestUtils.associateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals(404, associateInstancesResp.getErrorCode().intValue());
	}

	// fail - bug DE191824
	@Test
	public void associateNotCompitableReqCapTest() throws Exception {
		RestResponse createFirstAtomicResourceInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01,
				resourceDetailsCP_01, sdncDesignerDetails);
		String reqCompInstName = ResponseParser.getNameFromResponse(createFirstAtomicResourceInstResp);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstAtomicResourceInstResp);
		RestResponse createSecondAtomicResourceInstResp = createCheckedinAtomicInstanceForService(serviceDetails_01,
				resourceDetailsVL_02, sdncDesignerDetails);
		String capCompInstName = ResponseParser.getNameFromResponse(createSecondAtomicResourceInstResp);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondAtomicResourceInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		List<String> variables = new ArrayList<String>();
		variables.add(reqCompInstName);
		variables.add(capCompInstName);
		variables.add(REQUIREMENT_NAME);

		assocaiteInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESOURCE_INSTANCE_MATCH_NOT_FOUND, 404,
				variables);

		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capabilitiesAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> requirementsAfterAssoicate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate,
		// requirementsAfterAssoicate);
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capabilitiesAfterAssociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

		// {"requestError":{"serviceException":{"messageId":"SVC4119","text":"Error:
		// No relation found between resource instances '%1' and '%2' for
		// requirement '%3'.","variables":["CP100 1","VL200 2","host"]}}}>
	}

	@Test
	public void associateInstancesInTwoServiceVersionsTest() throws Exception {
		String oldServiceUniqueId = serviceDetails_01.getUniqueId();
		RestResponse createFirstVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);
		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);
		String secondServiceUniqueId = serviceDetails_01.getUniqueId();

		serviceDetails_01.setUniqueId(oldServiceUniqueId);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		updateCapabilitiesOwnerId(oldServiceUniqueId, capListBeforeAssociate, secondServiceUniqueId);
		updateExpectedReqCapAfterChangeLifecycleState(oldServiceUniqueId, secondServiceUniqueId);
		CapReqDef capReqDefAfterAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterAssociate = capReqDefAfterAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListAfterAssociate = capReqDefAfterAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", null,
		// reqListAfterAssociate);
		// AssertJUnit.assertEquals("Check capabilities",
		// capListBeforeAssociate, capListAfterAssociate);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		RestResponse createThirdVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqSecondCompInstId = ResponseParser.getUniqueIdFromResponse(createThirdVFInstResp);

		CapReqDef capReqDefBeforeSeconderyAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeSeconderyAssociate = capReqDefBeforeSeconderyAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeSeconderyAssociate = capReqDefBeforeSeconderyAssociate
				.getRequirements().get(CAPABILITY_TYPE);

		capCompInstId = capCompInstId.replaceAll(oldServiceUniqueId, secondServiceUniqueId);
		RequirementCapabilityRelDef secondRequirementDef = getReqCapRelation(reqSecondCompInstId, capCompInstId,
				CAPABILITY_TYPE, REQUIREMENT_NAME, capListBeforeSeconderyAssociate, reqListBeforeSeconderyAssociate);
		associateComponentInstancesForService(secondRequirementDef, serviceDetails_01, sdncDesignerDetails);

		CapReqDef capReqDefAfterSeconderyAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterSeconderyAssociate = capReqDefAfterSeconderyAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListAfterSeconderyAssociate = capReqDefAfterSeconderyAssociate.getRequirements()
				.get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", null,
		// reqListAfterSeconderyAssociate);
		// AssertJUnit.assertEquals("Check capabilities",
		// capListBeforeAssociate, capListAfterSeconderyAssociate);
		getComponentAndValidateRIs(serviceDetails_01, 3, 2);
	}

	private void updateCapabilitiesOwnerId(String oldUniqueId, List<CapabilityDefinition> capList, String newUniqueId) {
		serviceDetails_01.setUniqueId(newUniqueId);
		for (CapabilityDefinition cap : capList) {
			String oldOwnerId = cap.getOwnerId();
			String newOwnerId = oldOwnerId.replaceAll(oldUniqueId, newUniqueId);
			cap.setOwnerId(newOwnerId);
		}
	}

	@Test
	public void dissociateRelationNotFoundTest() throws Exception {
		createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
		String reqCompInstId = "1234";
		createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		String capCompInstId = "4567";

		CapReqDef capReqDef = ComponentRestUtils.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capList, reqList);

		List<String> variables = new ArrayList<String>();
		variables.add(reqCompInstId);
		variables.add(capCompInstId);
		variables.add(REQUIREMENT_NAME);
		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND,
				404, variables);

		CapReqDef capReqDefAfterDissociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterDissociate = capReqDefAfterDissociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListAfterDissociate = capReqDefAfterDissociate.getRequirements()
				.get(CAPABILITY_TYPE);

		AssertJUnit.assertEquals("Check requirement", 1, reqListAfterDissociate.size());
		AssertJUnit.assertEquals("Check requirement", reqList, reqListAfterDissociate);
		AssertJUnit.assertEquals("Check capabilities", capList, capListAfterDissociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);

	}

	@Test
	public void dissociateRelationInServiceNotFoundTest() throws Exception {
		String uniqueId = "1234";
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		serviceDetails_01.setUniqueId(uniqueId);
		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.SERVICE_NOT_FOUND, 404,
				new ArrayList<String>(Arrays.asList("")));

	}

	@Test
	public void dissoicateRelationWhileInstanceNotFound() throws Exception {
		String capUniqueId = "1234";

		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		String capCompInstId = capUniqueId;

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		List<String> variables = new ArrayList<String>();
		variables.add(reqCompInstId);
		variables.add(capCompInstId);
		variables.add(REQUIREMENT_NAME);
		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND,
				404, variables);

		CapReqDef capReqDefAfterDissociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListAfterDissociate = capReqDefAfterDissociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListAfterDissociate = capReqDefAfterDissociate.getRequirements()
				.get(CAPABILITY_TYPE);
		AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate, reqListAfterDissociate);
		AssertJUnit.assertEquals("Check capabilities", capListBeforeAssociate, capListAfterDissociate);

		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void dissociateWhileServiceCheckedinTest() throws Exception {
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESTRICTED_OPERATION, 409,
				new ArrayList<String>());
	}

	@Test
	public void dissoicateWithEmptyUserIdHeaderTest() throws Exception {
		sdncDesignerDetails.setUserId("");
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESTRICTED_OPERATION, 409,
				new ArrayList<String>());
	}

	@Test
	public void dissociateWithMissingUidOfServiceTest() throws Exception {
		serviceDetails_01.setUniqueId("");
		RequirementCapabilityRelDef requirementDef = new RequirementCapabilityRelDef();
		RestResponse dissociateResp = ComponentInstanceRestUtils.dissociateInstances(requirementDef,
				sdncDesignerDetails, serviceDetails_01.getUniqueId(), ComponentTypeEnum.SERVICE);
		assertEquals(404, dissociateResp.getErrorCode().intValue());
	}

	@Test
	public void relationDeletedAfterDeletingResourceInstanceTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);

		RestResponse deleteVFInstance = deleteVFInstance(reqCompInstId, serviceDetails_01, sdncDesignerDetails);
		ComponentInstanceRestUtils.checkDeleteResponse(deleteVFInstance);
		getComponentAndValidateRIs(serviceDetails_01, 1, 0);
	}

	@Test
	public void relationNotFoundInSecondVersionAfterDissociateTest() throws Exception {
		String oldContainerUniqueIdToReplace = serviceDetails_01.getUniqueId();
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		dissociateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);

		updateExpectedReqCapAfterChangeLifecycleState(oldContainerUniqueIdToReplace, serviceDetails_01.getUniqueId());
		getComponentAndValidateRIs(serviceDetails_01, 2, 0);
	}

	@Test
	public void dissociateOnceAgainTest() throws Exception {
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		String reqCompInsName = ResponseParser
				.convertComponentInstanceResponseToJavaObject(createFirstVFInstResp.getResponse()).getName();
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);
		String capCompInstName = ResponseParser
				.convertComponentInstanceResponseToJavaObject(createSecondVFInstResp.getResponse()).getName();

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, CAPABILITY_TYPE,
				REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		dissociateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);

		List<String> variables = new ArrayList<String>();
		variables.add(reqCompInsName);
		variables.add(capCompInstName);
		variables.add(REQUIREMENT_NAME);

		dissoicateInstancesFail(requirementDef, sdncDesignerDetails, ActionStatus.RESOURCE_INSTANCE_RELATION_NOT_FOUND,
				404, variables);
	}

	// fail - bug : DE191707
	@Test
	public void associateTwoRelations_CheckinCheckout_DissoicateOneRelationInSecondVersion() throws Exception {
		String oldContainerUniqueIdToReplace = serviceDetails_01.getUniqueId();
		RestResponse createFirstVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createFirstVFInstResp);
		RestResponse createSecondVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createSecondVFInstResp);
		RestResponse createThirdVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		String secondReqCompInstId = ResponseParser.getUniqueIdFromResponse(createThirdVFInstResp);

		CapReqDef capReqDefBeforeAssociate = ComponentRestUtils
				.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails, serviceDetails_01);
		List<CapabilityDefinition> capListBeforeAssociate = capReqDefBeforeAssociate.getCapabilities()
				.get(CAPABILITY_TYPE);
		List<RequirementDefinition> reqListBeforeAssociate = capReqDefBeforeAssociate.getRequirements()
				.get(CAPABILITY_TYPE);

		RequirementCapabilityRelDef requirementDefFirstRelation = getReqCapRelation(reqCompInstId, capCompInstId,
				CAPABILITY_TYPE, REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);
		RequirementCapabilityRelDef requirementDefSecondRelation = getReqCapRelation(secondReqCompInstId, capCompInstId,
				CAPABILITY_TYPE, REQUIREMENT_NAME, capListBeforeAssociate, reqListBeforeAssociate);

		associateComponentInstancesForService(requirementDefFirstRelation, serviceDetails_01, sdncDesignerDetails);
		associateComponentInstancesForService(requirementDefSecondRelation, serviceDetails_01, sdncDesignerDetails);
		getComponentAndValidateRIs(serviceDetails_01, 3, 2);

		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKOUT);
		String newContainerUniqueId = serviceDetails_01.getUniqueId();

		// check if dissoicate of old relation is possibile
		// dissoicateInstancesFail(requirementDefFirstRelation,
		// sdncDesignerDetails, actionStatus, errorCode, variables);
		getComponentAndValidateRIs(serviceDetails_01, 3, 2);

		requirementDefFirstRelation
				.setFromNode(reqCompInstId.replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId));
		requirementDefFirstRelation
				.setToNode(reqCompInstId.replaceAll(oldContainerUniqueIdToReplace, newContainerUniqueId));

		dissociateComponentInstancesForService(requirementDefFirstRelation, serviceDetails_01, sdncDesignerDetails);

		// updateCapabilitiesOwnerId(oldContainerUniqueIdToReplace,
		// capListBeforeAssociate, newContainerUniqueId);
		// CapReqDef capReqDefAfterAssociate =
		// ComponentRestUtils.getAndParseComponentRequirmentsCapabilities(sdncDesignerDetails,
		// serviceDetails_01);
		// List<CapabilityDefinition> capListAfterAssociate =
		// capReqDefAfterAssociate.getCapabilities().get(CAPABILITY_TYPE);
		// List<RequirementDefinition> reqListAfterAssociate =
		// capReqDefAfterAssociate.getRequirements().get(CAPABILITY_TYPE);
		// AssertJUnit.assertEquals("Check requirement", reqListBeforeAssociate,
		// reqListAfterAssociate);
		// AssertJUnit.assertEquals("Check requirement", capListBeforeAssociate,
		// capListAfterAssociate);
		updateExpectedReqCapAfterChangeLifecycleState(oldContainerUniqueIdToReplace, serviceDetails_01.getUniqueId());
		getComponentAndValidateRIs(serviceDetails_01, 3, 1);
	}

	@Test
	public void createResourceInstancesAndUpdatedServiceMetadataTest() throws Exception, Exception {
		serviceDetails_02.setUniqueId(serviceDetails_01.getUniqueId());
		createTwoCheckedinVFInstances();
		LifecycleRestUtils.changeResourceState(resourceDetailsCP_01, sdncDesignerDetails, "0.1",
				LifeCycleStatesEnum.CHECKIN);
		createVFInstanceAndAtomicResourceInstanceWithoutCheckin(resourceDetailsVF_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		RestResponse updateServiceResp = ServiceRestUtils.updateService(serviceDetails_02, sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(updateServiceResp);
		getComponentAndValidateRIs(serviceDetails_01, 4, 0);
	}

	@Test(enabled = false)
	public void forAcceptanceUserStory() throws Exception {
		RestResponse createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String reqCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);
		createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_02, sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		String capCompInstId = ResponseParser.getUniqueIdFromResponse(createVFInstResp);

		String capType = CAPABILITY_TYPE;
		String reqName = REQUIREMENT_NAME;

		RestResponse getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		ResourceRestUtils.checkSuccess(getResourceResponse);
		CapReqDef capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<CapabilityDefinition> capList = capReqDef.getCapabilities().get(capType);
		List<RequirementDefinition> reqList = capReqDef.getRequirements().get(capType);

		RequirementCapabilityRelDef requirementDef = getReqCapRelation(reqCompInstId, capCompInstId, capType, reqName,
				capList, reqList);

		associateComponentInstancesForService(requirementDef, serviceDetails_01, sdncDesignerDetails);
		getResourceResponse = ComponentRestUtils.getComponentRequirmentsCapabilities(sdncDesignerDetails,
				serviceDetails_01);
		capReqDef = ResponseParser.parseToObject(getResourceResponse.getResponse(), CapReqDef.class);
		List<RequirementDefinition> list = capReqDef.getRequirements().get(capType);
		AssertJUnit.assertEquals("Check requirement", null, list);

		serviceDetails_02.setUniqueId(serviceDetails_01.getUniqueId());
		RestResponse updateServiceResp = ServiceRestUtils.updateService(serviceDetails_02, sdncDesignerDetails);
		ServiceRestUtils.checkSuccess(updateServiceResp);
		changeServiceLifecycleState(serviceDetails_01, sdncDesignerDetails, LifeCycleStatesEnum.CHECKIN);
		getComponentAndValidateRIs(serviceDetails_01, 2, 1);
	}

	@Test
	public void testUnsatisfiedCpReqInService() throws Exception {

		// Certify all the needed atomic resources
		RestResponse response = LifecycleRestUtils.certifyResource(resourceDetailsCP_01);
		ResourceRestUtils.checkSuccess(response);

		ArtifactReqDetails heatArtifactDetails = ElementFactory
				.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails,
				resourceDetailsVF_02.getUniqueId());
		ResourceRestUtils.checkSuccess(response);
		response = LifecycleRestUtils.certifyResource(resourceDetailsVF_02);
		ResourceRestUtils.checkSuccess(response);
		capOwnerId = getUniqueIdOfFirstInstanceFromResponse(response);

		RestResponse createAtomicResourceInstance = createVFInstance(serviceDetails_01, resourceDetailsVF_02,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String vfCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		createAtomicResourceInstance = createAtomicInstanceForService(serviceDetails_01, resourceDetailsCP_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createAtomicResourceInstance);
		String compInstName = ResponseParser.getNameFromResponse(createAtomicResourceInstance);
		String cpCompInstId = ResponseParser.getUniqueIdFromResponse(createAtomicResourceInstance);

		RestResponse submitForTesting = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		String[] variables = new String[] { serviceDetails_01.getName(), "service", "CP (Connection Point)",
				compInstName, "requirement", "tosca.capabilities.network.Bindable", "fulfilled" };
		BaseValidationUtils.checkErrorResponse(submitForTesting,
				ActionStatus.REQ_CAP_NOT_SATISFIED_BEFORE_CERTIFICATION, variables);

		fulfillCpRequirement(serviceDetails_01, cpCompInstId, vfCompInstId, capOwnerId, sdncDesignerDetails,
				ComponentTypeEnum.SERVICE);

		submitForTesting = LifecycleRestUtils.changeServiceState(serviceDetails_01, sdncDesignerDetails,
				LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		BaseValidationUtils.checkSuccess(submitForTesting);
	}

	@Test
	public void getVFInstanceSuccessfullyTest() throws Exception {
		RestResponse createVFInstResp = createCheckedinVFInstance(serviceDetails_01, resourceDetailsVF_01,
				sdncDesignerDetails);
		ResourceRestUtils.checkCreateResponse(createVFInstResp);
		System.out.println("instance successfuly created");
		RestResponse getInstancesResponce = ComponentInstanceRestUtils.getComponentInstances(ComponentTypeEnum.SERVICE,
				serviceDetails_01.getUniqueId(), sdncDesignerDetails);

		for (int i = 0; i < 1500; i++) {
			createVFInstResp = createVFInstance(serviceDetails_01, resourceDetailsVF_01, sdncDesignerDetails);
			ResourceRestUtils.checkCreateResponse(createVFInstResp);
			System.out.println("instance " + i + "successfuly created");
		}

		getInstancesResponce = ComponentInstanceRestUtils.getComponentInstances(ComponentTypeEnum.SERVICE,
				serviceDetails_01.getUniqueId(), sdncDesignerDetails);

		BaseValidationUtils.checkSuccess(getInstancesResponce);

	}

	private String getUniqueIdOfFirstInstanceFromResponse(RestResponse response) {
		try {
			JSONArray value = ResponseParser.getListFromJson(response, "componentInstances");
			return ResponseParser.getValueFromJsonResponse(value.get(0).toString(), "uniqueId");
		} catch (Exception e) {
			return null;
		}
	}
}
